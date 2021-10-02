package com.party.modules.party.validator;

import com.party.modules.party.PartyRepository;
import com.party.modules.party.form.PartyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PartyFormValidator implements Validator {

    private final PartyRepository partyRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return PartyForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PartyForm partyForm = (PartyForm)target;
        //파티경로가 이미 존재한다면 reject
        if (partyRepository.existsByPath(partyForm.getPath())) {
            errors.rejectValue("path", "wrong.path", "해당 파티 경로값을 사용할 수 없습니다.");
        }

        if (isNotValidStartDateTime(partyForm)) {
            errors.rejectValue("startDateTime", "wrong.datetime", "파티 시작 일시를 정확히 입력하세요.");
        }

        if (isNotValidEndDateTime(partyForm)) {
            errors.rejectValue("endDateTime", "wrong.datetime", "파티 종료 일시를 정확히 입력하세요.");
        }
    }

    // 모임 시작 날짜
    private boolean isNotValidStartDateTime(PartyForm partyForm) {
        // 모임 시작 날짜가 모임 접수 종료 날짜보다 이전일 경우 Not valid
        return partyForm.getStartDateTime().isBefore(LocalDateTime.now());
    }


    // 모임 종료 날짜
    private boolean isNotValidEndDateTime(PartyForm partyForm) {
        LocalDateTime endDateTime = partyForm.getEndDateTime();
        // 모임 종료 날짜가 모임 시작 날짜보다 이전이거나 모임 모집 종료날짜보다 이전일 경우 Not valid
        return endDateTime.isBefore(partyForm.getStartDateTime());
    }

}
