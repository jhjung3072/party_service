package com.party.modules.event;

import com.party.modules.account.Account;
import com.party.modules.event.event.EnrollmentAcceptedEvent;
import com.party.modules.event.event.EnrollmentRejectedEvent;
import com.party.modules.study.Study;
import com.party.modules.event.form.EventForm;
import com.party.modules.study.event.StudyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 모임 생성
    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'" + event.getTitle() + "' 모임을 만들었습니다."));
        return eventRepository.save(event);
    }

    // 모임 수정
    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'" + event.getTitle() + "' 모임 정보를 수정했으니 확인하세요."));
    }

    // 모임 취소
    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'" + event.getTitle() + "' 모임을 취소했습니다."));
    }

    // 참가 신청
    public void newEnrollment(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    // 참가 신청 취소
    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (!enrollment.isAttended()) { // 이미 출석 시 취소 불가능
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            event.acceptNextWaitingEnrollment();
        }
    }

    // 참가 신청 승인
    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    // 참가 신청 거부
    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    }

    // 출석체크
    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    // 출석체크 취소
    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
