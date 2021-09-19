package com.party.modules.account;

import com.party.modules.tag.TagForm;
import com.party.modules.tag.TagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.party.modules.account.form.SignUpForm;
import com.party.modules.tag.Tag;
import com.party.modules.tag.TagRepository;
import com.party.modules.zone.Zone;
import com.party.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.party.modules.account.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    void addAccount(){
        SignUpForm signUpForm=new SignUpForm();
        signUpForm.setNickname("jaeho");
        signUpForm.setEmail("jaeho@google.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 수정폼")
    @Test
    void updateTagsForm() throws Exception{
        mockMvc.perform(get(SettingsController.TAGS))
                .andExpect(view().name(SETTINGS+TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whiteList"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 추가")
    @Test
    void addTags() throws Exception{
        TagForm tagForm=new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post(TAGS+"/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());
        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("jaeho").getTags().contains(newTag));
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 삭제")
    @Test
    void removeTags() throws Exception{
        Account jaeho = accountRepository.findByNickname("jaeho");
        Tag newTag=tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(jaeho,newTag);

        assertTrue(jaeho.getTags().contains(newTag));

        TagForm tagForm=new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post(SettingsController.TAGS+"/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                        .andExpect(status().isOk());

        assertFalse(jaeho.getTags().contains(newTag));
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정폼")
    @Test
    void updateProfileForm() throws Exception{
        String bio="짧은 소개를 수정하는 경우";
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception{
        String bio="짧은 소개를 수정하는 경우";
        mockMvc.perform(post("/settings/profile")
                .param("bio",bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Account jaeho = accountRepository.findByNickname("jaeho");
        assertEquals(bio,jaeho.getBio());
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception{
        String bio="길게 소개를 수정하는 경우,길게 소개를 수정하는 경우길게 소개를 수정하는 경우 길게 소개를 수정하는 경우길게 소개를 수정하는 경우";
        mockMvc.perform(post("/settings/profile")
                        .param("bio",bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account jaeho = accountRepository.findByNickname("jaeho");
        assertNull(jaeho.getBio());
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(SettingsController.PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(SettingsController.PASSWORD)
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account jaeho = accountRepository.findByNickname("jaeho");
        assertTrue(passwordEncoder.matches("12345678", jaeho.getPassword()));
    }

    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드 불일치")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(SettingsController.PASSWORD)
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "11111111")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }
}