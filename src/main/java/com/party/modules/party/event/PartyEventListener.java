package com.party.modules.party.event;

import com.party.infra.config.AppProperties;
import com.party.infra.mail.EmailMessage;
import com.party.infra.mail.EmailService;
import com.party.modules.account.Account;
import com.party.modules.account.AccountPredicates;
import com.party.modules.account.AccountRepository;
import com.party.modules.notification.Notification;
import com.party.modules.notification.NotificationRepository;
import com.party.modules.notification.NotificationType;
import com.party.modules.party.Party;
import com.party.modules.party.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class PartyEventListener {

    private final PartyRepository partyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handlePartyCreatedEvent(PartyCreatedEvent partyCreatedEvent) {
        Party party = partyRepository.findPartyWithTagsAndPlatformsById(partyCreatedEvent.getParty().getId());
        //만들어진 Party의 Tags 와 Platforms를 갖고 있는 account 리스트
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndPlatforms(party.getTags(), party.getPlatforms()));
        accounts.forEach(account -> {
            if (account.isPartyCreatedByEmail()) {
                sendPartyCreatedEmail(party, account, "새로운 파티가 생겼습니다",
                        "넷플프렌드, '" + party.getTitle() + "' 파티가 생겼습니다.");
            }

            if (account.isPartyCreatedByWeb()) {
                createNotification(party, account, party.getShortDescription(), NotificationType.PARTY_CREATED);
            }
        });
    }

    @EventListener
    public void handlePartyUpdateEvent(PartyUpdateEvent partyUpdateEvent) {
        //수정된 Party에 소속된 Manager 와 Member
        Party party = partyRepository.findPartyWithManagersAndMembersById(partyUpdateEvent.getParty().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(party.getManagers());
        accounts.addAll(party.getMembers());

        accounts.forEach(account -> {
            if (account.isPartyUpdatedByEmail()) {
                sendPartyCreatedEmail(party, account, partyUpdateEvent.getMessage(),
                        "넷플프렌드, '" + party.getTitle() + "' 파티에 새소식이 있습니다.");
            }

            if (account.isPartyUpdatedByWeb()) {
                createNotification(party, account, partyUpdateEvent.getMessage(), NotificationType.PARTY_UPDATED);
            }
        });
    }

    private void createNotification(Party party, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(party.getTitle());
        notification.setLink("/party/" + party.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendPartyCreatedEmail(Party party, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/party/" + party.getEncodedPath());
        context.setVariable("linkName", party.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

}
