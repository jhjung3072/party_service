package com.party.modules.event.event;

import com.party.infra.config.AppProperties;
import com.party.infra.mail.EmailMessage;
import com.party.infra.mail.EmailService;
import com.party.modules.account.Account;
import com.party.modules.event.Enrollment;
import com.party.modules.event.Event;
import com.party.modules.notification.Notification;
import com.party.modules.notification.NotificationRepository;
import com.party.modules.notification.NotificationType;
import com.party.modules.party.Party;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final NotificationRepository notificationRepository;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Party party = event.getParty();

        if (account.isPartyEnrollmentResultByEmail()) {
            sendEmail(enrollmentEvent, account, event, party);
        }

        if (account.isPartyEnrollmentResultByWeb()) {
            createNotification(enrollmentEvent, account, event, party);
        }
    }

    private void sendEmail(EnrollmentEvent enrollmentEvent, Account account, Event event, Party party) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/party/" + party.getEncodedPath() + "/events/" + event.getId());
        context.setVariable("linkName", party.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("파티올래, " + event.getTitle() + " 모임 참가 신청 결과입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Event event, Party party) {
        Notification notification = new Notification();
        notification.setTitle(party.getTitle() + " / " + event.getTitle());
        notification.setLink("/party/" + party.getEncodedPath() + "/events/" + event.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }

}
