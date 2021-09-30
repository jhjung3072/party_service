package com.party.modules.account;

import com.party.modules.account.form.SignUpForm;
import com.party.infra.config.AppProperties;
import com.party.modules.tag.Tag;
import com.party.modules.platform.Platform;
import com.party.infra.mail.EmailMessage;
import com.party.infra.mail.EmailService;
import com.party.modules.account.form.Notifications;
import com.party.modules.account.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    // 회원가입된 account에 이메일 전송
    public Account processNewAccount(SignUpForm signUpForm) {
        //EntityManager가 이 객체를 관리하고 있기 때문에 가급적이면 반환값의 객체를 사용하는 것이 좋다
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    // 회원가입 폼 -> 저장
    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        /* account.setNickname(signUpForm.getNickname());
        account.setEmail(signUpForm.getEmail());
        account.setPassword(signUpForm.getPassword());*/
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    // 이메일 인증 전송
    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "파티올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("파티올래, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    // 1. username과 password를 이용하여 UsernamePasswordAuthenticationToken 인스턴스 생성
    // 2. SecurityContextHolder -> SecurityContext -> Authentication 에 token 을 set
    // 참고 : https://flyburi.com/584
    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account), //principal 객체
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    //login 요청이 오면 자동으로 UserDetailsService 타입으로 IoC 되어 있는 loadUserByUsername 함수가 실행
    //입력된 값을 DB에 들어있는 nickname이나 email로 매치되는지 살펴보고 그에 해당하는 유저 데이터를 읽어옴
    // 그 다음 그렇게 읽어온 유저 데이터에는 패스워드가 인코딩 된 값으로 들어있는데, 그 값을 스프링 시큐리티 설정에 연결해 놓은 PasswordEncoder를 사용해서 체크
    // 참고 : https://dncjf64.tistory.com/330
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname); //이메일로 먼저 찾아보고
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname); // 닉네임으로 찾음
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    //회원가입 완료
    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    // 프로필 수정
    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account);
        /* account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setLocation(profile.getLocation());
        account.setBio(profile.getBio());
        account.setProfileImage(profile.getProfileImage());*/
        accountRepository.save(account);
    }

    // 패스워드 수정
    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    // 알림 수정
    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
         /*  account.setPartyCreatedByWeb(notifications.isPartyCreatedByWeb());
        account.setPartyCreatedByEmail(notifications.isPartyCreatedByEmail());
        account.setPartyUpdatedByWeb(notifications.isPartyUpdatedByWeb());
        account.setPartyUpdatedByEmail(notifications.isPartyUpdatedByEmail());
        account.setPartyEnrollmentResultByWeb(notifications.isPartyEnrollmentResultByWeb());
        account.setPartyEnrollmentResultByEmail(notifications.isPartyEnrollmentResultByEmail());*/
        accountRepository.save(account);
    }

    // 닉네임 수정
    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account); // 프로필에 수정된 닉네임으로 바로 바꾸기
    }

    // 로그인 링크 메일로 보내기
    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "파티올래 로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("파티올래, 로그인 링크")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    // 태그 추가
    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    // 태그목록 get
    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags(); //저장된 값이 존재하면 그 값을 반환하고, 값이 존재하지 않으면 파라미터의 예외를 발생시킴
    }

    // 태그 삭제
    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    // 플랫폼 목록 get
    public Set<Platform> getPlatforms(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getPlatforms();
    }

    // 플랫폼 추가
    public void addPlatform(Account account, Platform platform) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getPlatforms().add(platform));
    }

    // 플랫폼 삭제
    public void removePlatform(Account account, Platform platform) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getPlatforms().remove(platform));
    }

    // 닉네임으로 account get
    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if (account == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        return account;
    }
}
