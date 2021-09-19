package com.party.modules.main;

import com.party.modules.account.AccountRepository;
import com.party.modules.account.CurrentAccount;
import com.party.modules.account.Account;
import com.party.modules.event.EnrollmentRepository;
import com.party.modules.study.Study;
import com.party.modules.study.StudyRepository;
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

    private final StudyRepository studyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AccountRepository accountRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model) {
        if (account != null) {
            Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(accountLoaded);
            // 모임 리스트
            model.addAttribute("enrollmentList", enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded, true));
            //account가 갖고 있는 tags 와 zones 에 대한 study 리스트
            model.addAttribute("studyList", studyRepository.findByAccount(
                    accountLoaded.getTags(),
                    accountLoaded.getZones()));
            //관리중인 스터디 리스트
            model.addAttribute("studyManagerOf",
                    studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            //참여중인 스터디 리스트
            model.addAttribute("studyMemberOf",
                    studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            return "index-after-login";
        }
        // 공개된 스터디 리스트
        model.addAttribute("studyList", studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 검색 페이징
    @GetMapping("/search/study")
    public String searchStudy(String keyword, Model model,
                              @PageableDefault(size = 9, sort = "publishedDateTime", direction = Sort.Direction.DESC)
                                      Pageable pageable) {
        Page<Study> studyPage = studyRepository.findByKeyword(keyword, pageable);
        model.addAttribute("studyPage", studyPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }

}
