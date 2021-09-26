package com.party.modules.party;

import com.party.modules.account.AccountRepository;
import com.party.modules.account.AccountService;
import com.party.modules.account.form.SignUpForm;
import com.party.modules.account.Account;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class PartyControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected PartyService partyService;
    @Autowired protected PartyRepository partyRepository;
    @Autowired protected AccountRepository accountRepository;
    @Autowired protected AccountService accountService;

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

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 개설 폼 조회")
    @Test
    void createPartyForm() throws Exception{
        mockMvc.perform(get("/new-party"))
                .andExpect(status().isOk())
                .andExpect(view().name("party/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("partyForm"));
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 개설 - 완료")
    void createParty_success() throws Exception {
        mockMvc.perform(post("/new-party")
                        .param("path", "test-path")
                        .param("title", "party title")
                        .param("shortDescription", "short description of a party")
                        .param("fullDescription", "full description of a party")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/test-path"));

        Party party = partyRepository.findByPath("test-path");
        assertNotNull(party);
        Account account = accountRepository.findByNickname("jaeho");
        assertTrue(party.getManagers().contains(account));
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 개설 - 실패")
    void createParty_fail() throws Exception {
        mockMvc.perform(post("/new-party")
                        .param("path", "wrong path")
                        .param("title", "party title")
                        .param("shortDescription", "short description of a party")
                        .param("fullDescription", "full description of a party")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("party/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("partyForm"))
                .andExpect(model().attributeExists("account"));

        Party party = partyRepository.findByPath("test-path");
        assertNull(party);
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 조회")
    void viewParty() throws Exception {
        Party party = new Party();
        party.setPath("test-path");
        party.setTitle("test party");
        party.setShortDescription("short description");
        party.setFullDescription("<p>full description</p>");

        Account jaeho = accountRepository.findByNickname("jaeho");
        partyService.createNewParty(party, jaeho);

        mockMvc.perform(get("/party/test-path"))
                .andExpect(view().name("party/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("party"));
    }
    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 가입")
    void joinParty() throws Exception {
        Account nana = createAccount("nana");
        Party party = createParty("test-party", nana);

        mockMvc.perform(get("/party/" + party.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getPath() + "/members"));

        Account jaeho = accountRepository.findByNickname("jaeho");
        assertTrue(party.getMembers().contains(jaeho));
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 탈퇴")
    void leaveParty() throws Exception {
        Account nana = createAccount("nana");
        Party party = createParty("test-party", nana);
        Account jaeho = accountRepository.findByNickname("jaeho");
        partyService.addMember(party, jaeho);

        mockMvc.perform(get("/party/" + party.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/party/" + party.getPath() + "/members"));

        assertFalse(party.getMembers().contains(jaeho));
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
