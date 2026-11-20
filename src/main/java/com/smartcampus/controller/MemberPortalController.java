package com.smartcampus.controller;

import com.smartcampus.model.CampusUser;
import com.smartcampus.model.PasswordChangeForm;
import com.smartcampus.model.ProfileForm;
import com.smartcampus.enums.UserRole;
import com.smartcampus.service.MemberAccountSettingsService;
import com.smartcampus.service.MemberPortalService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberPortalController {
    private final MemberPortalService portalService;
    private final MemberAccountSettingsService accountSettings;

    public MemberPortalController(MemberPortalService portalService, MemberAccountSettingsService accountSettings) {
        this.portalService = portalService;
        this.accountSettings = accountSettings;
    }

    @GetMapping("/student/profile")
    public String studentProfile(@AuthenticationPrincipal CampusUser user, Model model) {
        return showProfile(user, UserRole.STUDENT, model);
    }

    @GetMapping("/club/profile")
    public String clubProfile(@AuthenticationPrincipal CampusUser user, Model model) {
        return showProfile(user, UserRole.CLUB_REPRESENTATIVE, model);
    }

    @PostMapping("/student/profile")
    public String updateStudentProfile(@Valid @ModelAttribute("profileForm") ProfileForm form,
                                       BindingResult result, @AuthenticationPrincipal CampusUser user, Model model) {
        return updateProfile(form, result, user, UserRole.STUDENT, model);
    }

    @PostMapping("/club/profile")
    public String updateClubProfile(@Valid @ModelAttribute("profileForm") ProfileForm form,
                                    BindingResult result, @AuthenticationPrincipal CampusUser user, Model model) {
        return updateProfile(form, result, user, UserRole.CLUB_REPRESENTATIVE, model);
    }

    @GetMapping("/student/profile/password")
    public String studentPasswordPage(@AuthenticationPrincipal CampusUser user, Model model) {
        return showPasswordPage(user, UserRole.STUDENT, model);
    }

    @GetMapping("/club/profile/password")
    public String clubPasswordPage(@AuthenticationPrincipal CampusUser user, Model model) {
        return showPasswordPage(user, UserRole.CLUB_REPRESENTATIVE, model);
    }

    @PostMapping("/student/profile/password")
    public String changeStudentPassword(@Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm form,
                                        BindingResult result, @AuthenticationPrincipal CampusUser user, Model model) {
        return changePassword(form, result, user, UserRole.STUDENT, model);
    }

    @PostMapping("/club/profile/password")
    public String changeClubPassword(@Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm form,
                                     BindingResult result, @AuthenticationPrincipal CampusUser user, Model model) {
        return changePassword(form, result, user, UserRole.CLUB_REPRESENTATIVE, model);
    }

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

    private String showProfile(CampusUser user, UserRole expectedRole, Model model) {
        CampusUser profile = accountSettings.findOwnProfile(user, expectedRole);
        model.addAttribute("profile", profile);
        model.addAttribute("profileForm", accountSettings.toForm(profile));
        model.addAttribute("memberPath", memberPath(user));
        return "member-profile";
    }

    private String updateProfile(ProfileForm form, BindingResult result, CampusUser user,
                                 UserRole expectedRole, Model model) {
        for (MemberAccountSettingsService.ProfileIssue issue : accountSettings.validateProfile(expectedRole, form)) {
            result.rejectValue(issue.field(), "profile.invalid", issue.message());
        }
        if (!result.hasErrors()) {
            try {
                accountSettings.updateProfile(user, expectedRole, form);
                return "redirect:/" + memberPath(user) + "/profile?updated";
            } catch (MemberAccountSettingsService.InvalidProfileException exception) {
                result.rejectValue(exception.getIssue().field(), "profile.invalid", exception.getMessage());
            }
        }
        CampusUser profile = accountSettings.findOwnProfile(user, expectedRole);
        addProfilePageModel(model, profile, form, user);
        return "member-profile";
    }

    private String showPasswordPage(CampusUser user, UserRole expectedRole, Model model) {
        accountSettings.findOwnProfile(user, expectedRole);
        model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        model.addAttribute("memberPath", memberPath(user));
        return "member-password";
    }

    private String changePassword(PasswordChangeForm form, BindingResult result, CampusUser user,
                                 UserRole expectedRole, Model model) {
        if (!result.hasErrors()) {
            try {
                accountSettings.changePassword(user, expectedRole, form);
                return "redirect:/" + memberPath(user) + "/profile/password?passwordUpdated";
            } catch (MemberAccountSettingsService.InvalidPasswordChangeException exception) {
                result.rejectValue(exception.getField(), "password.invalid", exception.getMessage());
            }
        }
        form.setCurrentPassword(null);
        form.setNewPassword(null);
        form.setConfirmPassword(null);
        model.addAttribute("memberPath", memberPath(user));
        return "member-password";
    }

    private void addProfilePageModel(Model model, CampusUser profile, ProfileForm form, CampusUser user) {
        model.addAttribute("profile", profile);
        model.addAttribute("profileForm", form);
        model.addAttribute("memberPath", memberPath(user));
    }

    private static String memberPath(CampusUser user) {
        return user.getRole().name().equals("STUDENT") ? "student" : "club";
    }
}
