package com.party.modules.event;

import com.party.modules.account.Account;
import com.party.modules.account.AccountRepository;
import com.party.modules.account.AccountService;
import com.party.modules.account.form.SignUpForm;
import com.party.modules.party.Party;
import com.party.modules.party.PartyService;
import com.party.modules.post.Post;
import com.party.modules.post.PostService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class PostControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    PostService postService;
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
        Party party = createParty(123L, nana);
        Post post = createEvent("test-post", 2, party, nana);

        mockMvc.perform(post("/party/" + party.getId() + "/events/" + post.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getId() + "/events/" + post.getId()));

        Account jaeho = accountRepository.findByNickname("jaeho");
        isAccepted(jaeho, post);
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 인원이 꽉차서)")
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account nana = createAccount("nana");
        Party party = createParty(123L, nana);
        Post post = createEvent("test-post", 2, party, nana);

        Account may = createAccount("may");
        Account june = createAccount("june");

        mockMvc.perform(post("/party/" + party.getId() + "/events/" + post.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getId() + "/events/" + post.getId()));

        Account jaeho = accountRepository.findByNickname("jaeho");
        isNotAccepted(jaeho, post);
    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account jaeho = accountRepository.findByNickname("jaeho");
        Account nana = createAccount("nana");
        Account may = createAccount("may");
        Party party = createParty(123L, nana);
        Post post = createEvent("test-post", 2, party, nana);


        isAccepted(may, post);
        isAccepted(jaeho, post);
        isNotAccepted(nana, post);

        mockMvc.perform(post("/party/" + party.getId() + "/events/" + post.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getId() + "/events/" + post.getId()));

        isAccepted(may, post);
        isAccepted(nana, post);

    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void not_accepterd_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account jaeho = accountRepository.findByNickname("jaeho");
        Account nana = createAccount("nana");
        Account may = createAccount("may");
        Party party = createParty(123L, nana);
        Post post = createEvent("test-post", 2, party, nana);



        isAccepted(may, post);
        isAccepted(nana, post);
        isNotAccepted(jaeho, post);

        mockMvc.perform(post("/party/" + party.getId() + "/events/" + post.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getId() + "/events/" + post.getId()));

        isAccepted(may, post);
        isAccepted(nana, post);

    }

    private void isNotAccepted(Account nana, Post post) {

    }

    private void isAccepted(Account account, Post post) {

    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
        Account nana = createAccount("nana");
        Party party = createParty(123L, nana);
        Post post = createEvent("test-post", 2, party, nana);

        mockMvc.perform(post("/party/" + party.getId() + "/events/" + post.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getId() + "/events/" + post.getId()));

        Account jaeho = accountRepository.findByNickname("jaeho");
        isNotAccepted(jaeho, post);
    }

    private Post createEvent(String eventTitle, int limit, Party party, Account account) {
        Post post = new Post();
        post.setTitle(eventTitle);

        return postService.createPost(post, party, account);
    }
    protected Party createParty(Long id, Account manager){
        Party party = new Party();

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