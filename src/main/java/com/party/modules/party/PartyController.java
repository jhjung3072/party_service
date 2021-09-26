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
        //party 경로가 한글이 쓰일 수 있으므로 URLEncoder 사용
        return "redirect:/party/" + URLEncoder.encode(newParty.getPath(), StandardCharsets.UTF_8);
    }

    // 파티 상세 뷰
    @GetMapping("/party/{path}")
    public String viewParty(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Party party = partyService.getParty(path);
        model.addAttribute(account);
        model.addAttribute(party);
        return "party/view";
    }

    // 파티 멤버 뷰
    @GetMapping("/party/{path}/members")
    public String viewPartyMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Party party = partyService.getParty(path);
        model.addAttribute(account);
        model.addAttribute(party);
        return "party/members";
    }

    // 파티 참가 Post
    @GetMapping("/party/{path}/join")
    public String joinParty(@CurrentAccount Account account, @PathVariable String path) {
        Party party = partyRepository.findPartyWithMembersByPath(path);
        partyService.addMember(party, account);
        return "redirect:/party/" + party.getEncodedPath() + "/members";
    }

    // 파티 탈퇴 Post
    @GetMapping("/party/{path}/leave")
    public String leaveParty(@CurrentAccount Account account, @PathVariable String path) {
        Party party = partyRepository.findPartyWithMembersByPath(path);
        partyService.removeMember(party, account);
        return "redirect:/party/" + party.getEncodedPath() + "/members";
    }

    @GetMapping("/party/data")
    public String generateTestData(@CurrentAccount Account account){
        partyService.generateTestStudies(account);
        return "redirect:/";
    }

}
