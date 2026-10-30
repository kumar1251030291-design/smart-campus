package com.smartcampus.controller;

import com.smartcampus.model.CampusUser;
import com.smartcampus.service.MemberPortalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class MemberDashboardController {
    private final MemberPortalService memberPortalService;
    public MemberDashboardController(MemberPortalService memberPortalService) {
        this.memberPortalService = memberPortalService;
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(@AuthenticationPrincipal CampusUser user, Model model) {
        model.addAttribute("dashboardHeading", "Your student dashboard");
        model.addAttribute("dashboardDescription", "Welcome to your Smart Campus student space. Explore campus resources and find the spaces and equipment available to you.");
        model.addAttribute("dashboardRole", "STUDENT");
        model.addAttribute("memberPath", "student");
        model.addAttribute("campusUser", user);
        model.addAttribute("dashboardData", memberPortalService.dashboard(user));
        model.addAttribute("dashboardAnnouncements", java.util.List.of());
        model.addAttribute("today", LocalDate.now());
        return "member-dashboard";
    }

    @GetMapping("/club/dashboard")
    public String clubDashboard(@AuthenticationPrincipal CampusUser user, Model model) {
        model.addAttribute("dashboardHeading", "Your club dashboard");
        model.addAttribute("dashboardDescription", "Welcome to your Smart Campus club space. Explore the campus resources available to your club.");
        model.addAttribute("dashboardRole", "CLUB REPRESENTATIVE");
        model.addAttribute("memberPath", "club");
        model.addAttribute("campusUser", user);
        model.addAttribute("dashboardData", memberPortalService.dashboard(user));
        model.addAttribute("dashboardAnnouncements", java.util.List.of());
        model.addAttribute("today", LocalDate.now());
        return "member-dashboard";
    }
}
