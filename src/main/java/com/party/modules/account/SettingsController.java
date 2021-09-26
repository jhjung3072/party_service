package com.party.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.party.modules.account.form.NicknameForm;
import com.party.modules.account.form.Notifications;
import com.party.modules.account.form.PasswordForm;
import com.party.modules.account.form.Profile;
import com.party.modules.tag.Tag;
import com.party.modules.platform.Platform;
import com.party.modules.account.validator.NicknameValidator;
import com.party.modules.account.validator.PasswordFormValidator;
import com.party.modules.tag.TagForm;
import com.party.modules.tag.TagRepository;
import com.party.modules.tag.TagService;
import com.party.modules.platform.PlatformForm;
import com.party.modules.platform.PlatformRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(SettingsController.ROOT + SettingsController.SETTINGS)
@RequiredArgsConstructor
public class SettingsController {

    static final String ROOT = "/";
    static final String SETTINGS = "settings";
    static final String PROFILE = "/profile";
    static final String PASSWORD = "/password";
    static final String NOTIFICATIONS = "/notifications";
    static final String ACCOUNT = "/account";
    static final String TAGS = "/tags";
    static final String PLATFORMS = "/platforms";

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final PasswordFormValidator passwordFormValidator;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final PlatformRepository platformRepository;
    private final ObjectMapper objectMapper;

    // passwordForm 을 받을때 검증
    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    // nicknameForm 을 받을때 검증
    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    // 프로필 수정 Get
    @GetMapping(PROFILE)
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        return SETTINGS + PROFILE;
    }

     // 프로필 수정 Post
    @PostMapping(PROFILE)
    public String updateProfile(@CurrentAccount Account account, @Valid Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PROFILE;
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/" + SETTINGS + PROFILE;
    }

    // 패스워드 수정 폼 Get
    @GetMapping(PASSWORD)
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS + PASSWORD;
    }

    // 패스워드 수정 Post
    @PostMapping(PASSWORD)
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PASSWORD;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:/" + SETTINGS + PASSWORD;
    }

    // 알림 수정 Get
    @GetMapping(NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS + NOTIFICATIONS;
    }

    // 알림 수정 Post
    @PostMapping(NOTIFICATIONS)
    public String updateNotifications(@CurrentAccount Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + NOTIFICATIONS;
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/" + SETTINGS + NOTIFICATIONS;
    }

    // 태그 수정 Get
    @GetMapping(TAGS)
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        //해당 account가 갖고 있는 tag.getTitle 리스트
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
        //tagRepository의 모든 tags를 문자열로
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        // allTags 를 Json 형식의 String 으로 변환
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS + TAGS;
    }

    // 태그 추가 Post
    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    // 태그 삭제 Post
    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }

    // 플랫폼 수정 Get
    @GetMapping(PLATFORMS)
    public String updatePlatformsForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        //해당 account가 갖고 있는 platform 을 문자열로
        Set<Platform> platforms = accountService.getPlatforms(account);
        model.addAttribute("platforms", platforms.stream().map(Platform::toString).collect(Collectors.toList()));
        //platformRepository의 모든 platforms를 문자열로
        List<String> allPlatforms = platformRepository.findAll().stream().map(Platform::toString).collect(Collectors.toList());
        // allPlatforms 를 Json 형식의 String 으로 변환
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allPlatforms));

        return SETTINGS + PLATFORMS;
    }

    // 플랫폼 추가 Post
    @PostMapping(PLATFORMS + "/add")
    @ResponseBody
    public ResponseEntity addPlatform(@CurrentAccount Account account, @RequestBody PlatformForm platformForm) {
        Platform platform = platformRepository.findByKoreanNameOfPlatformAndEnglishNameOfPlatform(platformForm.getKoreanNameOfPlatform(), platformForm.getEnglishNameOfPlatform());
        if (platform == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addPlatform(account, platform);
        return ResponseEntity.ok().build();
    }

    // 플랫폼 삭제 Post
    @PostMapping(PLATFORMS + "/remove")
    @ResponseBody
    public ResponseEntity removePlatform(@CurrentAccount Account account, @RequestBody PlatformForm platformForm) {
        Platform platform = platformRepository.findByKoreanNameOfPlatformAndEnglishNameOfPlatform(platformForm.getKoreanNameOfPlatform(), platformForm.getEnglishNameOfPlatform());
        if (platform == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removePlatform(account, platform);
        return ResponseEntity.ok().build();
    }

    // 닉네임 수정 Get
    @GetMapping(ACCOUNT)
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS + ACCOUNT;
    }

    // 닉네임 수정 Post
    @PostMapping(ACCOUNT)
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + ACCOUNT;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:/" + SETTINGS + ACCOUNT;
    }

}
