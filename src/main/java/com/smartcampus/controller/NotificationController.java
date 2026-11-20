package com.smartcampus.controller;

import com.smartcampus.model.CampusUser;
import com.smartcampus.service.PortalNotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final PortalNotificationService notifications;

    public NotificationController(PortalNotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public String inbox(@AuthenticationPrincipal CampusUser user, Model model) {
        model.addAttribute("notifications", notifications.inboxFor(user));
        model.addAttribute("unreadCount", notifications.unreadCountFor(user));
        return "notifications";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable long id, @AuthenticationPrincipal CampusUser user, Model model) {
        model.addAttribute("notification", notifications.findFor(user, id));
        model.addAttribute("unreadCount", notifications.unreadCountFor(user));
        return "notification-detail";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable long id, @AuthenticationPrincipal CampusUser user) {
        notifications.markRead(user, id);
        return "redirect:/notifications/" + id + "?readUpdated";
    }

    @PostMapping("/read-all")
    public String markAllRead(@AuthenticationPrincipal CampusUser user) {
        notifications.markAllRead(user);
        return "redirect:/notifications?readAllUpdated";
    }
}
