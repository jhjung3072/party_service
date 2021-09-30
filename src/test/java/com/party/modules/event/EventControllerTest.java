package com.party.modules.event;

import com.party.modules.account.Account;
import com.party.modules.account.AccountRepository;
import com.party.modules.account.AccountService;
import com.party.modules.account.form.SignUpForm;
import com.party.modules.party.Party;
import com.party.modules.party.PartyService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class EventControllerTest  {

    @Autowired MockMvc mockMvc;
    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired
    PartyService partyService;
    @Autowired AccountService accountService;

    @BeforeEach
    void addAccount(){
        SignUpForm signUpForm=new SignUpForm();
        signUpForm.setNickname("jaeho");
        signUpForm.setEmail("jaeho@google.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account nana = createAccount("nana");
        Party party = createParty("test-party", nana);
        Event event = createEvent("test-event", 2, party, nana);

        mockMvc.perform(post("/party/" + party.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getPath() + "/events/" + event.getId()));

        Account jaeho = accountRepository.findByNickname("jaeho");
        isAccepted(jaeho, event);
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 인원이 꽉차서)")
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account nana = createAccount("nana");
        Party party = createParty("test-party", nana);
        Event event = createEvent("test-event", 2, party, nana);

        Account may = createAccount("may");
        Account june = createAccount("june");

        mockMvc.perform(post("/party/" + party.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getPath() + "/events/" + event.getId()));

        Account jaeho = accountRepository.findByNickname("jaeho");
        isNotAccepted(jaeho, event);
    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account jaeho = accountRepository.findByNickname("jaeho");
        Account nana = createAccount("nana");
        Account may = createAccount("may");
        Party party = createParty("test-party", nana);
        Event event = createEvent("test-event", 2, party, nana);


        isAccepted(may, event);
        isAccepted(jaeho, event);
        isNotAccepted(nana, event);

        mockMvc.perform(post("/party/" + party.getPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(nana, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, jaeho));
    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void not_accepterd_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account jaeho = accountRepository.findByNickname("jaeho");
        Account nana = createAccount("nana");
        Account may = createAccount("may");
        Party party = createParty("test-party", nana);
        Event event = createEvent("test-event", 2, party, nana);



        isAccepted(may, event);
        isAccepted(nana, event);
        isNotAccepted(jaeho, event);

        mockMvc.perform(post("/party/" + party.getPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(nana, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, jaeho));
    }

    private void isNotAccepted(Account nana, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, nana).isAccepted());
    }

    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
        Account nana = createAccount("nana");
        Party party = createParty("test-party", nana);
        Event event = createEvent("test-event", 2, party, nana);

        mockMvc.perform(post("/party/" + party.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getPath() + "/events/" + event.getId()));

        Account jaeho = accountRepository.findByNickname("jaeho");
        isNotAccepted(jaeho, event);
    }

    private Event createEvent(String eventTitle, int limit, Party party, Account account) {
        Event event = new Event();
        event.setTitle(eventTitle);

        return eventService.createEvent(event, party, account);
    }
    protected Party createParty(String path, Account manager){
        Party party = new Party();
        party.setPath(path);
        partyService.createNewParty(party,manager);
        return party;
    }
    protected Account createAccount(String nickname){
        Account account=new Account();
        account.setNickname(nickname);
        account.setEmail(nickname+"@email.com");
        accountRepository.save(account);
        return account;
    }

}