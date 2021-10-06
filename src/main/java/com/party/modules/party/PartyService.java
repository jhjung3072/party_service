package com.party.modules.party;

import com.party.modules.account.Account;
import com.party.modules.party.event.PartyCreatedEvent;
import com.party.modules.party.event.PartyUpdateEvent;
import com.party.modules.party.form.PartyDescriptionForm;
import com.party.modules.tag.Tag;
import com.party.modules.tag.TagRepository;
import com.party.modules.platform.Platform;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static com.party.modules.party.form.PartyForm.VALID_PATH_PATTERN;

@Service
@Transactional
@RequiredArgsConstructor
public class PartyService {

    private final PartyRepository repository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;

    //파티 생성
    public Party createNewParty(Party party, Account account) {
        Party newParty = repository.save(party);
        newParty.addManager(account);
        return newParty;
    }

    //  파티 수정을 위한 getParty
    public Party getPartyToUpdate(Account account, Long id) {
        Party party = this.getParty(id);
        checkIfManager(account, party);
        return party;
    }

    //  파티 목록을 위한 getParty
    public Party getParty(Long id) {
        Party party = this.repository.findPartyById(id);
        checkIfExistingParty(id, party);
        return party;
    }

    // 파티 소개 수정 및 알림
    public void updatePartyDescription(Party party, PartyDescriptionForm partyDescriptionForm) {
        modelMapper.map(partyDescriptionForm, party);
        eventPublisher.publishEvent(new PartyUpdateEvent(party, "파티 소개를 수정했습니다."));
    }

    // 파티 이미지 수정
    public void updatePartyImage(Party party, String image) {
        party.setImage(image);
    }

    // 파티 배너 이미지 사용
    public void enablePartyBanner(Party party) {
        party.setUseBanner(true);
    }

    // 파티 배너 이미지 미사용
    public void disablePartyBanner(Party party) {
        party.setUseBanner(false);
    }

    // 태그 추가
    public void addTag(Party party, Tag tag) {
        party.getTags().add(tag);
    }

    // 태그 삭제
    public void removeTag(Party party, Tag tag) {
        party.getTags().remove(tag);
    }

    // 플랫폼 추가
    public void addPlatform(Party party, Platform platform) {
        party.getPlatforms().add(platform);
    }

    // 플랫폼 삭제
    public void removePlatform(Party party, Platform platform) {
        party.getPlatforms().remove(platform);
    }

    //태그 추가,삭제를 위한 getParty
    public Party getPartyToUpdateTag(Account account, Long id) {
        Party party = repository.findPartyWithTagsById(id);
        checkIfExistingParty(id, party);
        checkIfManager(account, party);
        return party;
    }

    //플랫폼 추가,삭제를 위한 Party get
    public Party getPartyToUpdatePlatform(Account account, Long id) {
        Party party = repository.findPartyWithPlatformsById(id);
        checkIfExistingParty(id, party);
        checkIfManager(account, party);
        return party;
    }

    //파티 및 모임 수정을 Party get
    public Party getPartyToUpdateStatus(Account account, Long id) {
        Party party = repository.findPartyWithManagersById(id);
        checkIfExistingParty(id, party);
        checkIfManager(account, party);
        return party;
    }

    private void checkIfManager(Account account, Party party) {
        if (!party.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingParty(Long id, Party party) {
        if (party == null) {
            throw new IllegalArgumentException(id + "에 해당하는 파티가 없습니다.");
        }
    }

    // 파티 공개 및 알림
    public void publish(Party party) {
        party.publish();

    }

    // 파티 종료 및 알림
    public void close(Party party) {
        party.close();
        eventPublisher.publishEvent(new PartyUpdateEvent(party, "파티를 종료했습니다."));
    }

    // 파티 멤버 모집 시작 및 알림
    public void startRecruit(Party party) {
        party.startRecruit();
        this.eventPublisher.publishEvent(new PartyCreatedEvent(party));
        eventPublisher.publishEvent(new PartyUpdateEvent(party, "팀원 모집을 시작합니다."));
    }

    // 파티 멤버 모집 종료 및 알림
    public void stopRecruit(Party party) {
        party.stopRecruit();
        eventPublisher.publishEvent(new PartyUpdateEvent(party, "팀원 모집을 중단했습니다."));
    }


    // 파티 제목 타당성 검사
    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    // 파티 제목 수정
    public void updatePartyTitle(Party party, String newTitle) {
        party.setTitle(newTitle);
    }

    // 파티 삭제
    public void remove(Party party) {
        if (party.isRemovable()) {
            repository.delete(party);
        } else {
            throw new IllegalArgumentException("파티를 삭제할 수 없습니다.");
        }
    }

    // 멤버 추가
    public void addMember(Party party, Account account) {
        party.addMember(account);
    }

    // 멤버 삭제
    public void removeMember(Party party, Account account) {
        party.removeMember(account);
    }

    public boolean isValidLink(String newLink) {
        if (!newLink.matches(VALID_PATH_PATTERN)) {
            return false;
        }

        return !repository.existsByKakaoLink(newLink);
    }
    public void updatePartyKakaoLink(Party party, String newLink) {
        party.setKakaoLink(newLink);
    }

    public void generateTestStudies(Account account) {
        for (int i=0; i<30; i++){
            String randomvalue= RandomString.make(5);
            Party party = Party.builder()
                    .title("테스트 파티" + randomvalue)
                    .shortDescription("테스트용 파티")
                    .fullDescription("test")
                    .tags(new HashSet<>())
                    .managers(new HashSet<>())
                    .build();
            party.publish();
            Party newParty = this.createNewParty(party, account);
            Tag netflix = tagRepository.findByTitle("넷플릭스");
            newParty.getTags().add(netflix);
        }
    }


}
