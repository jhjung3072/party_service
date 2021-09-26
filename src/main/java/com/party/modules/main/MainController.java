package com.party.modules.main;

import com.party.modules.account.AccountRepository;
import com.party.modules.account.CurrentAccount;
import com.party.modules.account.Account;
import com.party.modules.event.EnrollmentRepository;
import com.party.modules.party.Party;
import com.party.modules.party.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PartyRepository partyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AccountRepository accountRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model) {
        if (account != null) {
            Account accountLoaded = accountRepository.findAccountWithTagsAndPlatformsById(account.getId());
            model.addAttribute(accountLoaded);
            // 모임 리스트
            model.addAttribute("enrollmentList", enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded, true));
            //account가 갖고 있는 tags 와 platforms 에 대한 party 리스트
            model.addAttribute("partyList", partyRepository.findByAccount(
                    accountLoaded.getTags(),
                    accountLoaded.getPlatforms()));
            //관리중인 파티 리스트
            model.addAttribute("partyManagerOf",
                    partyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            //참여중인 파티 리스트
            model.addAttribute("partyMemberOf",
                    partyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            return "index-after-login";
        }
        // 공개된 파티 리스트
        model.addAttribute("partyList", partyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 검색 페이징
    @GetMapping("/search/party")
    public String searchParty(String keyword, Model model,
                              @PageableDefault(size = 9, sort = "publishedDateTime", direction = Sort.Direction.DESC)
                                      Pageable pageable) {
        Page<Party> partyPage = partyRepository.findByKeyword(keyword, pageable);
        model.addAttribute("partyPage", partyPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }

}
