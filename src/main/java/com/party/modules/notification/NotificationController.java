package com.party.modules.notification;

import com.party.modules.account.Account;
import com.party.modules.account.CurrentAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;

    private final NotificationService service;

    //읽지 않은 알림
    @GetMapping("/notifications")
    public String getNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = repository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = repository.countByAccountAndChecked(account, true);
        putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
        model.addAttribute("isNew", true);
        service.markAsRead(notifications);
        return "notification/list";
    }

    //읽은 알림
    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = repository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfNotChecked = repository.countByAccountAndChecked(account, false);
        putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    //읽은 알림 삭제
    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentAccount Account account) {
        repository.deleteByAccountAndChecked(account, true);
        return "redirect:/notifications";
    }

    //알림 종류를 구분 후 List에 담아줌
    private void putCategorizedNotifications(Model model, List<Notification> notifications,
                                             long numberOfChecked, long numberOfNotChecked) {
        List<Notification> newStudyNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingStudyNotifications = new ArrayList<>();
        for (var notification : notifications) {
            switch (notification.getNotificationType()) {
                case STUDY_CREATED: newStudyNotifications.add(notification); break;
                case EVENT_ENROLLMENT: eventEnrollmentNotifications.add(notification); break;
                case STUDY_UPDATED: watchingStudyNotifications.add(notification); break;
            }
        }

        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
    }

}
