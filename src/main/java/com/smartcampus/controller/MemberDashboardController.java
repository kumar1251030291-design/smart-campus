package com.smartcampus.controller;

import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.AnnouncementRepository;
import com.smartcampus.repository.ResourceViewRepository;
import com.smartcampus.service.MemberPortalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class MemberDashboardController {
    private final MemberPortalService memberPortalService;
    private final AnnouncementRepository announcements;
    private final ResourceViewRepository resourceViews;

    public MemberDashboardController(MemberPortalService memberPortalService, AnnouncementRepository announcements,
                                     ResourceViewRepository resourceViews) {
        this.memberPortalService = memberPortalService;
        this.announcements = announcements;
        this.resourceViews = resourceViews;
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(@AuthenticationPrincipal CampusUser user, Model model) {
        return dashboard(user, model, "Your student dashboard", "STUDENT", "student");
    }

    @GetMapping("/club/dashboard")
    public String clubDashboard(@AuthenticationPrincipal CampusUser user, Model model) {
        return dashboard(user, model, "Your club dashboard", "CLUB REPRESENTATIVE", "club");
    }

    private String dashboard(CampusUser user, Model model, String heading, String role, String memberPath) {
        model.addAttribute("dashboardHeading", heading);
        model.addAttribute("dashboardRole", role);
        model.addAttribute("memberPath", memberPath);
        model.addAttribute("campusUser", user);
        model.addAttribute("dashboardData", memberPortalService.dashboard(user));
        model.addAttribute("dashboardAnnouncements", announcements.latest(3));
        model.addAttribute("recentlyExplored", resourceViews.recentForUser(user.getUserId(), 5));
        model.addAttribute("today", LocalDate.now());
        return "member-dashboard";
    }
}
