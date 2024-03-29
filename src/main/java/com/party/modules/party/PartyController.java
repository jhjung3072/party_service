package com.party.modules.party;

import com.party.modules.account.CurrentAccount;
import com.party.modules.account.Account;
import com.party.modules.party.form.PartyForm;
import com.party.modules.party.validator.PartyFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class PartyController {

    private final PartyRepository partyRepository;
    private final PartyService partyService;
    private final ModelMapper modelMapper;
    private final PartyFormValidator partyFormValidator;

    //partyForm 을 받을때 검증
    @InitBinder("partyForm")
    public void partyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(partyFormValidator);
    }

    // 파티 만들기 Get
    @GetMapping("/new-party")
    public String newPartyForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PartyForm());
        return "party/form";
    }

    // 파티 만들기 Post
    @PostMapping("/new-party")
    public String newPartySubmit(@CurrentAccount Account account, @Valid PartyForm partyForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "party/form";
        }

        Party newParty = partyService.createNewParty(modelMapper.map(partyForm, Party.class), account);
        return "redirect:/party/" + newParty.getId();
    }

    // 파티 상세 뷰
    @GetMapping("/party/{id}")
    public String viewParty(@CurrentAccount Account account, @PathVariable Long id, Model model) {
        Party party = partyService.getParty(id);
        model.addAttribute(account);
        model.addAttribute(party);
        return "party/view";
    }

    // 파티 멤버 뷰
    @GetMapping("/party/{id}/members")
    public String viewPartyMembers(@CurrentAccount Account account, @PathVariable Long id, Model model) {
        Party party = partyService.getParty(id);
        model.addAttribute(account);
        model.addAttribute(party);
        return "party/members";
    }

    // 카카오 오픈채팅방 링크
    @GetMapping("/party/{id}/kakaoLink")
    public String viewKakaoLink(@CurrentAccount Account account, @PathVariable Long id, Model model) {
        Party party = partyService.getParty(id);
        model.addAttribute(account);
        model.addAttribute(party);
        return "party/kakao-link";
    }

    // 파티 참가 Post
    @GetMapping("/party/{id}/join")
    public String joinParty(@CurrentAccount Account account, @PathVariable Long id) {
        Party party = partyRepository.findPartyWithMembersById(id);
        partyService.addMember(party, account);
        return "redirect:/party/" + party.getId() + "/members";
    }

    // 파티 탈퇴 Post
    @GetMapping("/party/{id}/leave")
    public String leaveParty(@CurrentAccount Account account, @PathVariable Long id) {
        Party party = partyRepository.findPartyWithMembersById(id);
        partyService.removeMember(party, account);
        return "redirect:/party/" + party.getId()+ "/members";
    }

    @GetMapping("/party/data")
    public String generateTestData(@CurrentAccount Account account){
        partyService.generateTestStudies(account);
        return "redirect:/";
    }

}
