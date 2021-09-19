package com.party.modules.study;

import com.party.modules.account.Account;
import com.party.modules.study.event.StudyCreatedEvent;
import com.party.modules.study.event.StudyUpdateEvent;
import com.party.modules.tag.Tag;
import com.party.modules.tag.TagRepository;
import com.party.modules.zone.Zone;
import com.party.modules.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static com.party.modules.study.form.StudyForm.VALID_PATH_PATTERN;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository repository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;

    //스터디 생성
    public Study createNewStudy(Study study, Account account) {
        Study newStudy = repository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    //  수정을 위한 getStudy, manager check
    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudy(String path) {
        Study study = this.repository.findByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }

    // 스터디 소개 수정 및 알림
    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디 소개를 수정했습니다."));
    }

    // 스터디 이미지 수정
    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    // 스터디 배너 이미지 사용
    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    // 스터디 배너 이미지 미사용
    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    // 태그 추가
    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    // 태그 삭제
    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    // 지역 추가
    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    // 지역 삭제
    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    //태그 추가,삭제를 위한 Study get
    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = repository.findStudyWithTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    //지역 추가,삭제를 위한 Study get
    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = repository.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    //스터디 및 모임 수정을 Study get
    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = repository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    private void checkIfManager(Account account, Study study) {
        if (!study.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    // 스터디 공개 및 알림
    public void publish(Study study) {
        study.publish();
        this.eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }

    // 스터디 종료 및 알림
    public void close(Study study) {
        study.close();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디를 종료했습니다."));
    }

    // 스터디 멤버 모집 시작 및 알림
    public void startRecruit(Study study) {
        study.startRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "팀원 모집을 시작합니다."));
    }

    // 스터디 멤버 모집 종료 및 알림
    public void stopRecruit(Study study) {
        study.stopRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "팀원 모집을 중단했습니다."));
    }

    // 스터디 경로 타당성 검사
    public boolean isValidPath(String newPath) {
        if (!newPath.matches(VALID_PATH_PATTERN)) {
            return false;
        }
        return !repository.existsByPath(newPath);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }

    // 스터디 삭제
    public void remove(Study study) {
        if (study.isRemovable()) {
            repository.delete(study);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    // 멤버 추가
    public void addMember(Study study, Account account) {
        study.addMember(account);
    }

    // 멤버 삭제
    public void removeMember(Study study, Account account) {
        study.removeMember(account);
    }

    //모집참가 신청 및 취소 요청을 위한 study get
    public Study getStudyToEnroll(String path) {
        Study study = repository.findStudyOnlyByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }

    public void generateTestStudies(Account account) {
        for (int i=0; i<30; i++){
            String randomvalue= RandomString.make(5);
            Study study = Study.builder()
                    .title("테스트 파티" + randomvalue)
                    .path("test-" + randomvalue)
                    .shortDescription("테스트용 파티")
                    .fullDescription("test")
                    .tags(new HashSet<>())
                    .managers(new HashSet<>())
                    .build();
            study.publish();
            Study newStudy = this.createNewStudy(study, account);
            Tag netflix = tagRepository.findByTitle("넷플릭스");
            newStudy.getTags().add(netflix);
        }
    }
}
