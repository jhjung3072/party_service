package com.party.modules.study.validator;

import com.party.modules.study.StudyRepository;
import com.party.modules.study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return StudyForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm)target;
        //스터디경로가 이미 존재한다면 reject
        if (studyRepository.existsByPath(studyForm.getPath())) {
            errors.rejectValue("path", "wrong.path", "해당 스터디 경로값을 사용할 수 없습니다.");
        }
    }
}
