package com.party.account;

import com.party.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model){
        model.addAttribute("signUpForm", new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm,Errors errors){
        if (errors.hasErrors()){
            return "account/sign-up";
        }
        //signUpFormValidator.validate(signUpForm,errors);
        Account account=Account.builder()
                    .email(signUpForm.getEmail())
                    .nickname(signUpForm.getNickname())
                    .password(signUpForm.getPassword())
                    .partyCreatedByWeb(true)
                    .partyEnrollmentResultByWeb(true)
                    .partyUpdatedByWeb(true)
                    .build();

        Account newAccount = accountRepository.save(account);

        newAccount.generateEmailCheckToken();
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("회원가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken()+
                        "&email=" + newAccount.getEmail());
        javaMailSender.send(mailMessage);

        return "redirect:/";
    }
}
