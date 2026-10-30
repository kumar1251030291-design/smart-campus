package com.smartcampus.controller;

import com.smartcampus.model.CampusUser;
import com.smartcampus.service.MemberPortalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPortalController {
    private final MemberPortalService portalService;
    public MemberPortalController(MemberPortalService portalService) { this.portalService = portalService; }

    @GetMapping({"/student/equipment", "/club/equipment"})
    public String equipment(@AuthenticationPrincipal CampusUser user, Model model) {
        var now = java.time.LocalDateTime.now();
        model.addAttribute("allocations", portalService.activeEquipment(user.getUserId()).stream()
                .map(allocation -> new com.smartcampus.model.MemberEquipmentItem(allocation,
                        allocation.equipmentStatus().name().equals("ISSUED") && allocation.expectedReturnTime() != null
                                && allocation.expectedReturnTime().isBefore(now))).toList());
        model.addAttribute("recentReturns", portalService.equipmentHistory(user.getUserId()).stream()
                .filter(allocation -> allocation.equipmentStatus().name().equals("RETURNED")).limit(5).toList());
        model.addAttribute("memberPath", memberPath(user));
        return "member-equipment";
    }

    private static String memberPath(CampusUser user) {
        return user.getRole().name().equals("STUDENT") ? "student" : "club";
    }
}
