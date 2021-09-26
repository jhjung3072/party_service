package com.party.modules.party;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.party.modules.account.CurrentAccount;
import com.party.modules.account.Account;
import com.party.modules.party.form.PartyDescriptionForm;
import com.party.modules.platform.Platform;
import com.party.modules.platform.PlatformForm;
import com.party.modules.platform.PlatformRepository;
import com.party.modules.tag.Tag;
import com.party.modules.tag.TagForm;
import com.party.modules.tag.TagRepository;
import com.party.modules.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/party/{path}/settings")
@RequiredArgsConstructor
public class PartySettingsController {

    private final PartyRepository partyRepository;
    private final PartyService partyService;
    private final ModelMapper modelMapper;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final PlatformRepository platformRepository;
    private final ObjectMapper objectMapper;

    //파티 소개 수정 Get
    @GetMapping("/description")
    public String viewPartySetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Party party = partyService.getPartyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(party);
        model.addAttribute(modelMapper.map(party, PartyDescriptionForm.class));
        return "party/settings/description";
    }

    //파티 소개 수정 Post
    @PostMapping("/description")
    public String updatePartyInfo(@CurrentAccount Account account, @PathVariable String path,
                                  @Valid PartyDescriptionForm partyDescriptionForm, Errors errors,
                                  Model model, RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(party);
            return "party/settings/description";
        }

        partyService.updatePartyDescription(party, partyDescriptionForm);
        attributes.addFlashAttribute("message", "파티 소개를 수정했습니다.");
        return "redirect:/party/" + party.getEncodedPath() + "/settings/description";
    }

    // 배너 이미지 수정 Get
    @GetMapping("/banner")
    public String partyImageForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Party party = partyService.getPartyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(party);
        return "party/settings/banner";
    }

    // 배너 이미지 수정 Post
    @PostMapping("/banner")
    public String partyImageSubmit(@CurrentAccount Account account, @PathVariable String path,
                                   String image, RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdate(account, path);
        partyService.updatePartyImage(party, image);
        attributes.addFlashAttribute("message", "파티 이미지를 수정했습니다.");
        return "redirect:/party/" + party.getEncodedPath() + "/settings/banner";
    }

    // 배너 이미지 enable
    @PostMapping("/banner/enable")
    public String enablePartyBanner(@CurrentAccount Account account, @PathVariable String path) {
        Party party = partyService.getPartyToUpdate(account, path);
        partyService.enablePartyBanner(party);
        return "redirect:/party/" + party.getEncodedPath() + "/settings/banner";
    }

    // 배너 이미지 disable
    @PostMapping("/banner/disable")
    public String disablePartyBanner(@CurrentAccount Account account, @PathVariable String path) {
        Party party = partyService.getPartyToUpdate(account, path);
        partyService.disablePartyBanner(party);
        return "redirect:/party/" + party.getEncodedPath() + "/settings/banner";
    }

    // 태그 수정 Get
    @GetMapping("/tags")
    public String partyTagsForm(@CurrentAccount Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Party party = partyService.getPartyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(party);

        // 현재 파티에 등록된 태그 리스트
        model.addAttribute("tags", party.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));
        // tagRepository 에 저장된 모든 태그 리스트
        List<String> allTagTitles = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        // allTagTitles 를 Json 형식의 String 으로 변환
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
        return "party/settings/tags";
    }

    //태그 추가
    //Ajax 요청
    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable String path,
                                 @RequestBody TagForm tagForm) {
        Party party = partyService.getPartyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        partyService.addTag(party, tag);
        return ResponseEntity.ok().build();
    }

    //태그 삭제
    //Ajax 요청
    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String path,
                                    @RequestBody TagForm tagForm) {
        Party party = partyService.getPartyToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        partyService.removeTag(party, tag);
        return ResponseEntity.ok().build();
    }

    // 플랫폼 설정 Get
    @GetMapping("/platforms")
    public String partyPlatformsForm(@CurrentAccount Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Party party = partyService.getPartyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(party);
        // 현재 파티에 등록된 플랫폼 리스트
        model.addAttribute("platforms", party.getPlatforms().stream().map(Platform::toString).collect(Collectors.toList()));
        // platformRepository 에 저장된 모든 플랫폼 리스트
        List<String> allPlatforms = platformRepository.findAll().stream().map(Platform::toString).collect(Collectors.toList());
        // allPlatforms 를 Json 형식의 String 으로 변환
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allPlatforms));
        return "party/settings/platforms";
    }

    //플랫폼 추가
    //Ajax 요청
    @PostMapping("/platforms/add")
    @ResponseBody
    public ResponseEntity addPlatform(@CurrentAccount Account account, @PathVariable String path,
                                  @RequestBody PlatformForm platformForm) {
        Party party = partyService.getPartyToUpdatePlatform(account, path);
        Platform platform = platformRepository.findByKoreanNameOfPlatformAndEnglishNameOfPlatform(platformForm.getKoreanNameOfPlatform(), platformForm.getEnglishNameOfPlatform());
        if (platform == null) {
            return ResponseEntity.badRequest().build();
        }


        partyService.addPlatform(party, platform);
        return ResponseEntity.ok().build();
    }

    //플랫폼 삭제
    //Ajax 요청
    @PostMapping("/platforms/remove")
    @ResponseBody
    public ResponseEntity removePlatform(@CurrentAccount Account account, @PathVariable String path,
                                     @RequestBody PlatformForm platformForm) {
        Party party = partyService.getPartyToUpdatePlatform(account, path);
        Platform platform = platformRepository.findByKoreanNameOfPlatformAndEnglishNameOfPlatform(platformForm.getKoreanNameOfPlatform(), platformForm.getEnglishNameOfPlatform());
        if (platform == null) {
            return ResponseEntity.badRequest().build();
        }

        partyService.removePlatform(party, platform);
        return ResponseEntity.ok().build();
    }

    // 파티 설정 Get
    @GetMapping("/party")
    public String partySettingForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Party party = partyService.getPartyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(party);
        return "party/settings/party";
    }

    // 파티 공개 Post
    @PostMapping("/party/publish")
    public String publishParty(@CurrentAccount Account account, @PathVariable String path,
                               RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, path);
        partyService.publish(party);
        attributes.addFlashAttribute("message", "파티를 공개했습니다.");
        return "redirect:/party/" + party.getEncodedPath() + "/settings/party";
    }

    // 파티 종료 Post
    @PostMapping("/party/close")
    public String closeParty(@CurrentAccount Account account, @PathVariable String path,
                             RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, path);
        partyService.close(party);
        attributes.addFlashAttribute("message", "파티를 종료했습니다.");
        return "redirect:/party/" + party.getEncodedPath() + "/settings/party";
    }

    // 멤버 모집 시작 Post
    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String path,
                               RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, path);
        if (!party.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/party/" + party.getEncodedPath() + "/settings/party";
        }

        partyService.startRecruit(party);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/party/" + party.getEncodedPath() + "/settings/party";
    }

    // 멤버 모집 종료 Post
    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String path,
                              RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdate(account, path);
        if (!party.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/party/" + party.getEncodedPath() + "/settings/party";
        }

        partyService.stopRecruit(party);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/party/" + party.getEncodedPath() + "/settings/party";
    }

    // 파티 경로 수정 Post
    @PostMapping("/party/path")
    public String updatePartyPath(@CurrentAccount Account account, @PathVariable String path, String newPath,
                                  Model model, RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, path);
        if (!partyService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(party);
            model.addAttribute("partyPathError", "해당 파티 경로는 사용할 수 없습니다. 다른 값을 입력하세요.");
            return "party/settings/party";
        }

        partyService.updatePartyPath(party, newPath);
        attributes.addFlashAttribute("message", "파티 경로를 수정했습니다.");
        return "redirect:/party/" + party.getEncodedPath() + "/settings/party";
    }

    // 파티 제목 수정 Post
    @PostMapping("/party/title")
    public String updatePartyTitle(@CurrentAccount Account account, @PathVariable String path, String newTitle,
                                   Model model, RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, path);
        if (!partyService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(party);
            model.addAttribute("partyTitleError", "파티 이름을 다시 입력하세요.");
            return "party/settings/party";
        }

        partyService.updatePartyTitle(party, newTitle);
        attributes.addFlashAttribute("message", "파티 이름을 수정했습니다.");
        return "redirect:/party/" + party.getEncodedPath() + "/settings/party";
    }

    // 파티 삭제 Post
    @PostMapping("/party/remove")
    public String removeParty(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Party party = partyService.getPartyToUpdateStatus(account, path);
        partyService.remove(party);
        return "redirect:/";
    }

}
