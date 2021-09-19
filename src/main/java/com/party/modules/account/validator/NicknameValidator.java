package com.party.modules.account.validator;

import com.party.modules.account.AccountRepository;
import com.party.modules.account.Account;
import com.party.modules.account.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor // private final 타입의 멤버변수의 생성자를 만들어준다.
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {  //인스턴스가 검증 대상 타입인지 확인
        return NicknameForm.class.isAssignableFrom(aClass);  // instanceof 는 특정 object, isAssignableFrom은 특정 class, 차이점은 검사 대상이 Instance화 되었는지
    }

    @Override
    public void validate(Object target, Errors errors) { //검증작업
        NicknameForm nicknameForm = (NicknameForm) target;
        Account byNickname = accountRepository.findByNickname(nicknameForm.getNickname()); //이미 사용중인 닉네임이라면
        if (byNickname != null) {
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임을 사용할 수 없습니다.");
        }
    }
}
