package com.party.modules.study;

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
public class StudyControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected StudyService studyService;
    @Autowired protected StudyRepository studyRepository;
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
    @DisplayName("스터디 개설 폼 조회")
    @Test
    void createStudyForm() throws Exception{
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 - 완료")
    void createStudy_success() throws Exception {
        mockMvc.perform(post("/new-study")
                        .param("path", "test-path")
                        .param("title", "study title")
                        .param("shortDescription", "short description of a study")
                        .param("fullDescription", "full description of a study")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath("test-path");
        assertNotNull(study);
        Account account = accountRepository.findByNickname("jaeho");
        assertTrue(study.getManagers().contains(account));
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 - 실패")
    void createStudy_fail() throws Exception {
        mockMvc.perform(post("/new-study")
                        .param("path", "wrong path")
                        .param("title", "study title")
                        .param("shortDescription", "short description of a study")
                        .param("fullDescription", "full description of a study")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attributeExists("account"));

        Study study = studyRepository.findByPath("test-path");
        assertNull(study);
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description</p>");

        Account jaeho = accountRepository.findByNickname("jaeho");
        studyService.createNewStudy(study, jaeho);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }
    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 가입")
    void joinStudy() throws Exception {
        Account nana = createAccount("nana");
        Study study = createStudy("test-study", nana);

        mockMvc.perform(get("/study/" + study.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        Account jaeho = accountRepository.findByNickname("jaeho");
        assertTrue(study.getMembers().contains(jaeho));
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 탈퇴")
    void leaveStudy() throws Exception {
        Account nana = createAccount("nana");
        Study study = createStudy("test-study", nana);
        Account jaeho = accountRepository.findByNickname("jaeho");
        studyService.addMember(study, jaeho);

        mockMvc.perform(get("/study/" + study.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        assertFalse(study.getMembers().contains(jaeho));
    }

    protected Study createStudy(String path, Account manager){
        Study study= new Study();
        study.setPath(path);
        studyService.createNewStudy(study,manager);
        return study;
    }
    protected Account createAccount(String nickname){
        Account account=new Account();
        account.setNickname(nickname);
        account.setEmail(nickname+"@email.com");
        accountRepository.save(account);
        return account;
    }
}
