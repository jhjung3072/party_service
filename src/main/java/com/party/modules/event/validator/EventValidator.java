package com.party.modules.event.validator;

import com.party.modules.event.Event;
import com.party.modules.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return EventForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm)target;

        if (isNotValidStartDateTime(eventForm)) {
            errors.rejectValue("startDateTime", "wrong.datetime", "모임 시작 일시를 정확히 입력하세요.");
        }

        if (isNotValidEndEnrollmentDateTime(eventForm)) {
            errors.rejectValue("endEnrollmentDateTime", "wrong.datetime", "모임 접수 종료 일시를 정확히 입력하세요.");
        }

        if (isNotValidEndDateTime(eventForm)) {
            errors.rejectValue("endDateTime", "wrong.datetime", "모임 종료 일시를 정확히 입력하세요.");
        }

    }

    // 모임 시작 날짜
    private boolean isNotValidStartDateTime(EventForm eventForm) {
        // 모임 시작 날짜가 모임 접수 종료 날짜보다 이전일 경우 Not valid
        return eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    // 모임 접수 종료 날짜
    private boolean isNotValidEndEnrollmentDateTime(EventForm eventForm) {
        // 모임 접수 종료 날짜가 현재 날짜보다 이전일 경우 Not valid
        return eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    // 모임 종료 날짜
    private boolean isNotValidEndDateTime(EventForm eventForm) {
        LocalDateTime endDateTime = eventForm.getEndDateTime();
        // 모임 종료 날짜가 모임 시작 날짜보다 이전이거나 모임 모집 종료날짜보다 이전일 경우 Not valid
        return endDateTime.isBefore(eventForm.getStartDateTime()) || endDateTime.isBefore(eventForm.getEndEnrollmentDateTime());
    }


    // 모임 수정 폼에서 모집인원 수정 검사
    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        // 모집 인원보다 승인된 참가신청이 많다면 not valid
        if (eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()) {
            errors.rejectValue("limitOfEnrollments", "wrong.value", "승인된 참가 신청보다 모집 인원 수가 커야 합니다.");
        }
    }
}
