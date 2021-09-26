package com.party.modules.party;

import com.party.modules.account.AccountRepository;
import com.party.modules.account.Account;
import lombok.RequiredArgsConstructor;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class PartySettingsControllerTest extends PartyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    PartyService partyService;
    @Autowired AccountRepository accountRepository;
    @Autowired
    PartyRepository partyRepository;

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 소개 수정 폼 조회 - 실패 (권한 없는 유저)")
    void updateDescriptionForm_fail() throws Exception {
        Account jaeho = createAccount("jaeho");
        Party party = createParty("test-party", jaeho);

        mockMvc.perform(get("/party/" + party.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 소개 수정 폼 조회 - 성공")
    void updateDescriptionForm_success() throws Exception {
        Account jaeho = accountRepository.findByNickname("jaeho");
        Party party = createParty("test-party", jaeho);

        mockMvc.perform(get("/party/" + party.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("party/settings/description"))
                .andExpect(model().attributeExists("partyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("party"));
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 소개 수정 - 성공")
    void updateDescription_success() throws Exception {
        Account jaeho = accountRepository.findByNickname("jaeho");
        Party party = createParty("test-party", jaeho);

        String settingsDescriptionUrl = "/party/" + party.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionUrl)
                        .param("shortDescription", "short description")
                        .param("fullDescription", "full description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsDescriptionUrl))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithUserDetails(value = "jaeho", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("파티 소개 수정 - 실패")
    void updateDescription_fail() throws Exception {
        Account jaeho = accountRepository.findByNickname("jaeho");
        Party party = createParty("test-party", jaeho);

        String settingsDescriptionUrl = "/party/" + party.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionUrl)
                        .param("shortDescription", "")
                        .param("fullDescription", "full description")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("partyDescriptionForm"))
                .andExpect(model().attributeExists("party"))
                .andExpect(model().attributeExists("account"));
    }

}