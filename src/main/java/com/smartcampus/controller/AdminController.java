package com.smartcampus.controller;

import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.Resource;
import com.smartcampus.model.ResourceForm;
import com.smartcampus.service.AdminDashboardService;
import com.smartcampus.service.ResourceService;
import com.smartcampus.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class AdminController {
    private final AdminDashboardService dashboardService;
    private final ResourceService resourceService;
    private final UserManagementService userManagementService;
    private final com.smartcampus.service.BookingService bookingService;
    private final com.smartcampus.repository.AnnouncementRepository announcements;

    public AdminController(AdminDashboardService dashboardService, ResourceService resourceService,
                           UserManagementService userManagementService,
                           com.smartcampus.service.BookingService bookingService,
                           com.smartcampus.repository.AnnouncementRepository announcements) {
        this.bookingService = bookingService;
        this.announcements = announcements;
        this.dashboardService = dashboardService;
        this.resourceService = resourceService;
        this.userManagementService = userManagementService;
    }

    @GetMapping("/admin")
    public String adminHome() { return "redirect:/admin/dashboard"; }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.loadStatistics());
        var allBookings = bookingService.searchAll(null, null, null, null);
        java.util.Map<String, Long> byStatus = new java.util.LinkedHashMap<>();
        for (var status : com.smartcampus.enums.BookingStatus.values()) {
            byStatus.put(status.getDisplayName(), allBookings.stream().filter(b -> b.status() == status).count());
        }
        model.addAttribute("bookingTotal", allBookings.size());
        model.addAttribute("bookingsByStatus", byStatus);
        model.addAttribute("awaitingReview", allBookings.stream().filter(b -> b.status() == com.smartcampus.enums.BookingStatus.PENDING
                || b.status() == com.smartcampus.enums.BookingStatus.UNDER_REVIEW).count());
        model.addAttribute("recentBookings", allBookings.stream()
                .sorted(java.util.Comparator.comparing(com.smartcampus.model.BookingRequest::createdAt).reversed()).limit(6).toList());
        model.addAttribute("dashboardAnnouncements", announcements.latest(3));
        return "admin/dashboard";
    }

    @GetMapping("/admin/resources")
    public String resources(@RequestParam(required = false) String search,
                            @RequestParam(required = false) ResourceCategory category,
                            @RequestParam(required = false) AvailabilityStatus availability,
                            Model model) {
        model.addAttribute("resources", resourceService.searchResources(search, category, availability));
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("selectedCategory", category == null ? "" : category.name());
        model.addAttribute("selectedAvailability", availability == null ? "" : availability.name());
        model.addAttribute("categories", ResourceCategory.values());
        model.addAttribute("statuses", AvailabilityStatus.values());
        return "admin/resources";
    }

    @GetMapping("/admin/resources/new")
    public String newResource(Model model) {
        prepareResourceForm(model, new ResourceForm(), false, null);
        return "admin/resource-form";
    }

    @PostMapping("/admin/resources")
    public String createResource(@Valid @ModelAttribute("resourceForm") ResourceForm resourceForm,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            prepareResourceForm(model, resourceForm, false, null);
            return "admin/resource-form";
        }
        resourceService.createResource(resourceForm);
        return "redirect:/admin/resources?created";
    }

    @GetMapping("/admin/resources/{id}/edit")
    public String editResource(@PathVariable long id, Model model) {
        return resourceService.findResource(id)
                .map(resource -> {
                    prepareResourceForm(model, new ResourceForm(resource), true, id);
                    return "admin/resource-form";
                })
                .orElse("redirect:/admin/resources?error=not-found");
    }

    @PostMapping("/admin/resources/{id}")
    public String updateResource(@PathVariable long id, @Valid @ModelAttribute("resourceForm") ResourceForm resourceForm,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            prepareResourceForm(model, resourceForm, true, id);
            return "admin/resource-form";
        }
        if (!resourceService.updateResource(id, resourceForm)) return "redirect:/admin/resources?error=not-found";
        return "redirect:/admin/resources?updated";
    }

    @PostMapping("/admin/resources/{id}/delete")
    public String deleteResource(@PathVariable long id) {
        return resourceService.deleteResource(id)
                ? "redirect:/admin/resources?deleted"
                : "redirect:/admin/resources?error=not-found";
    }

    @PostMapping("/admin/resources/{id}/availability")
    public String updateAvailability(@PathVariable long id, @RequestParam AvailabilityStatus availabilityStatus) {
        return resourceService.updateAvailability(id, availabilityStatus)
                ? "redirect:/admin/resources?statusUpdated"
                : "redirect:/admin/resources?error=not-found";
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(required = false) String search,
                        @RequestParam(required = false) UserRole role,
                        @RequestParam(required = false) AccountStatus status,
                        @AuthenticationPrincipal CampusUser currentAdmin,
                        Model model) {
        model.addAttribute("users", userManagementService.searchUsers(search, role, status));
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("selectedRole", role == null ? "" : role.name());
        model.addAttribute("selectedStatus", status == null ? "" : status.name());
        model.addAttribute("roles", new UserRole[]{UserRole.STUDENT, UserRole.CLUB_REPRESENTATIVE, UserRole.ADMIN});
        model.addAttribute("accountStatuses", AccountStatus.values());
        model.addAttribute("currentAdminId", currentAdmin.getUserId());
        return "admin/users";
    }

    @GetMapping("/admin/users/{id}")
    public String userDetails(@PathVariable long id, @AuthenticationPrincipal CampusUser currentAdmin, Model model) {
        return userManagementService.findUser(id)
                .map(user -> {
                    model.addAttribute("campusUser", user);
                    model.addAttribute("currentAdminId", currentAdmin.getUserId());
                    return "admin/user-details";
                })
                .orElse("redirect:/admin/users?error=not-found");
    }

    @PostMapping("/admin/users/{id}/status")
    public String changeUserStatus(@PathVariable long id, @RequestParam AccountStatus status,
                                   @AuthenticationPrincipal CampusUser currentAdmin) {
        try {
            if (!userManagementService.changeStatus(id, status, currentAdmin.getUserId())) {
                return "redirect:/admin/users?error=not-found";
            }
            return "redirect:/admin/users?updated";
        } catch (UserManagementService.SelfDeactivationException exception) {
            return "redirect:/admin/users?error=self";
        }
    }

    private void prepareResourceForm(Model model, ResourceForm form, boolean editing, Long resourceId) {
        model.addAttribute("resourceForm", form);
        model.addAttribute("editing", editing);
        model.addAttribute("resourceId", resourceId);
        model.addAttribute("categories", ResourceCategory.values());
        model.addAttribute("statuses", AvailabilityStatus.values());
    }
}
