package com.party.modules.notification;

import com.party.modules.account.Account;
import com.party.modules.account.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
//알림색을 모든 페이지 요청에 적용
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationRepository notificationRepository;

    @Override // 핸들러 처리 이후, 뷰 랜더링 전
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //인증정보가 있는 요청에 대한 응답만 처리
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //모델뷰를 쓰는 경우 and 리다이렉트가 아닌 경우 and  authentication 이 있어야 하고, authentication 타입이 UserAccount인 경우
        if (modelAndView != null && !isRedirectView(modelAndView) && authentication != null && authentication.getPrincipal() instanceof UserAccount) {
            Account account = ((UserAccount)authentication.getPrincipal()).getAccount();
            long count = notificationRepository.countByAccountAndChecked(account, false);
            modelAndView.addObject("hasNotification", count > 0); // true or false인지
        }
    }

    //redirect 확인
    private boolean isRedirectView(ModelAndView modelAndView) {
        return modelAndView.getViewName().startsWith("redirect:") || modelAndView.getView() instanceof RedirectView;
    }
}
