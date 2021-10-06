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
@RequestMapping("/party/{id}/settings")
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
    public String viewPartySetting(@CurrentAccount Account account, @PathVariable Long id, Model model) {
        Party party = partyService.getPartyToUpdate(account, id);
        model.addAttribute(account);
        model.addAttribute(party);
        model.addAttribute(modelMapper.map(party, PartyDescriptionForm.class));
        return "party/settings/description";
    }

    //파티 소개 수정 Post
    @PostMapping("/description")
    public String updatePartyInfo(@CurrentAccount Account account, @PathVariable Long id,
                                  @Valid PartyDescriptionForm partyDescriptionForm, Errors errors,
                                  Model model, RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdate(account, id);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(party);
            return "party/settings/description";
        }

        partyService.updatePartyDescription(party, partyDescriptionForm);
        attributes.addFlashAttribute("message", "파티 소개를 수정했습니다.");
        return "redirect:/party/" + party.getId() + "/settings/description";
    }

    // 배너 이미지 수정 Get
    @GetMapping("/banner")
    public String partyImageForm(@CurrentAccount Account account, @PathVariable Long id, Model model) {
        Party party = partyService.getPartyToUpdate(account, id);
        model.addAttribute(account);
        model.addAttribute(party);
        return "party/settings/banner";
    }

    // 배너 이미지 수정 Post
    @PostMapping("/banner")
    public String partyImageSubmit(@CurrentAccount Account account, @PathVariable Long id,
                                   String image, RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdate(account, id);
        partyService.updatePartyImage(party, image);
        attributes.addFlashAttribute("message", "파티 이미지를 수정했습니다.");
        return "redirect:/party/" + party.getId() + "/settings/banner";
    }

    // 배너 이미지 enable
    @PostMapping("/banner/enable")
    public String enablePartyBanner(@CurrentAccount Account account, @PathVariable Long id) {
        Party party = partyService.getPartyToUpdate(account, id);
        partyService.enablePartyBanner(party);
        return "redirect:/party/" + party.getId() + "/settings/banner";
    }

    // 배너 이미지 disable
    @PostMapping("/banner/disable")
    public String disablePartyBanner(@CurrentAccount Account account, @PathVariable Long id) {
        Party party = partyService.getPartyToUpdate(account, id);
        partyService.disablePartyBanner(party);
        return "redirect:/party/" + party.getId() + "/settings/banner";
    }

    // 태그 수정 Get
    @GetMapping("/tags")
    public String partyTagsForm(@CurrentAccount Account account, @PathVariable Long id, Model model)
            throws JsonProcessingException {
        Party party = partyService.getPartyToUpdate(account, id);
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
    public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable Long id,
                                 @RequestBody TagForm tagForm) {
        Party party = partyService.getPartyToUpdateTag(account, id);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        partyService.addTag(party, tag);
        return ResponseEntity.ok().build();
    }

    //태그 삭제
    //Ajax 요청
    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable Long id,
                                    @RequestBody TagForm tagForm) {
        Party party = partyService.getPartyToUpdateTag(account, id);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        partyService.removeTag(party, tag);
        return ResponseEntity.ok().build();
    }

    // 플랫폼 설정 Get
    @GetMapping("/platforms")
    public String partyPlatformsForm(@CurrentAccount Account account, @PathVariable Long id, Model model)
            throws JsonProcessingException {
        Party party = partyService.getPartyToUpdate(account, id);
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
    public ResponseEntity addPlatform(@CurrentAccount Account account, @PathVariable Long id,
                                  @RequestBody PlatformForm platformForm) {
        Party party = partyService.getPartyToUpdatePlatform(account, id);
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
    public ResponseEntity removePlatform(@CurrentAccount Account account, @PathVariable Long id,
                                     @RequestBody PlatformForm platformForm) {
        Party party = partyService.getPartyToUpdatePlatform(account, id);
        Platform platform = platformRepository.findByKoreanNameOfPlatformAndEnglishNameOfPlatform(platformForm.getKoreanNameOfPlatform(),
                                 platformForm.getEnglishNameOfPlatform());
        if (platform == null) {
            return ResponseEntity.badRequest().build();
        }

        partyService.removePlatform(party, platform);
        return ResponseEntity.ok().build();
    }

    // 파티 설정 Get
    @GetMapping("/party")
    public String partySettingForm(@CurrentAccount Account account, @PathVariable Long id, Model model) {
        Party party = partyService.getPartyToUpdate(account, id);
        model.addAttribute(account);
        model.addAttribute(party);
        return "party/settings/party";
    }

    // 파티 공개 Post
    @PostMapping("/party/publish")
    public String publishParty(@CurrentAccount Account account, @PathVariable Long id,
                               RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, id);
        partyService.publish(party);
        attributes.addFlashAttribute("message", "파티를 공개했습니다.");
        return "redirect:/party/" + party.getId() + "/settings/party";
    }

    // 파티 종료 Post
    @PostMapping("/party/close")
    public String closeParty(@CurrentAccount Account account, @PathVariable Long id,
                             RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, id);
        partyService.close(party);
        attributes.addFlashAttribute("message", "파티를 종료했습니다.");
        return "redirect:/party/" + party.getId() + "/settings/party";
    }

    // 멤버 모집 시작 Post
    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable Long id,
                               RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, id);
        if (!party.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/party/" + party.getId() + "/settings/party";
        }

        partyService.startRecruit(party);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/party/" + party.getId() + "/settings/party";
    }

    // 멤버 모집 종료 Post
    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable Long id,
                              RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdate(account, id);
        if (!party.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/party/" + party.getId() + "/settings/party";
        }

        partyService.stopRecruit(party);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/party/" + party.getId() + "/settings/party";
    }

    @PostMapping("/party/kakaoLink")
    public String updatePartyKakaoLink(@CurrentAccount Account account, @PathVariable Long id, String newLink,
                                  Model model, RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, id);
        if (!partyService.isValidLink(newLink)) {
            model.addAttribute(account);
            model.addAttribute(party);
            model.addAttribute("partyKakaoLinkError", "해당 스터디 경로는 사용할 수 없습니다. 다른 값을 입력하세요.");
            return "party/settings/party";
        }

        partyService.updatePartyKakaoLink(party, newLink);
        attributes.addFlashAttribute("message", "카카오 오픈채팅방 링크를 수정했습니다.");
        return "redirect:/party/" + party.getId() + "/settings/party";
    }


    // 파티 제목 수정 Post
    @PostMapping("/party/title")
    public String updatePartyTitle(@CurrentAccount Account account, @PathVariable Long id, String newTitle,
                                   Model model, RedirectAttributes attributes) {
        Party party = partyService.getPartyToUpdateStatus(account, id);
        if (!partyService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(party);
            model.addAttribute("partyTitleError", "파티 이름을 다시 입력하세요.");
            return "party/settings/party";
        }

        partyService.updatePartyTitle(party, newTitle);
        attributes.addFlashAttribute("message", "파티 이름을 수정했습니다.");
        return "redirect:/party/" + party.getId() + "/settings/party";
    }

    // 파티 삭제 Post
    @PostMapping("/party/remove")
    public String removeParty(@CurrentAccount Account account, @PathVariable Long id, Model model) {
        Party party = partyService.getPartyToUpdateStatus(account, id);
        partyService.remove(party);
        return "redirect:/";
    }

}
