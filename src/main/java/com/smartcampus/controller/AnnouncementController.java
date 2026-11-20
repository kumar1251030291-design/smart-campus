package com.smartcampus.controller;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.AnnouncementForm;
import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.AnnouncementRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AnnouncementController {
    private final AnnouncementRepository announcements;
    public AnnouncementController(AnnouncementRepository announcements) { this.announcements = announcements; }

    @GetMapping("/announcements")
    public String list(Model model) {
        model.addAttribute("announcements", announcements.all());
        return "announcements";
    }

    @GetMapping("/announcements/{id:[0-9]+}")
    public String details(@PathVariable long id, Model model) {
        return announcements.find(id).map(announcement -> {
            model.addAttribute("announcement", announcement);
            model.addAttribute("otherAnnouncements", announcements.latest(4).stream()
                    .filter(other -> other.announcementId() != id).limit(3).toList());
            return "announcement-detail";
        }).orElse("redirect:/announcements");
    }

    /** Club representatives and administrators only (enforced in SecurityConfig and checked again here). */
    @GetMapping("/announcements/new")
    public String newAnnouncement(@AuthenticationPrincipal CampusUser user, Model model) {
        if (!canPost(user)) return "redirect:/announcements";
        prepare(model, new AnnouncementForm());
        return "announcement-form";
    }

    @PostMapping("/announcements")
    public String create(@Valid @ModelAttribute("announcementForm") AnnouncementForm form, BindingResult errors,
                         @AuthenticationPrincipal CampusUser user, Model model) {
        if (!canPost(user)) return "redirect:/announcements";
        if (form.getImagePath() != null && !AnnouncementForm.IMAGES.containsKey(form.getImagePath())) {
            errors.rejectValue("imagePath", "announcement.image", "Choose one of the listed pictures.");
        }
        if (errors.hasErrors()) { prepare(model, form); return "announcement-form"; }
        String body = form.getBody() == null || form.getBody().isBlank() ? form.getSummary().trim() : form.getBody().trim();
        String postedBy = user.getRole() == UserRole.CLUB_REPRESENTATIVE && user.getClubName() != null
                ? user.getFullName() + " (" + user.getClubName() + ")" : user.getFullName() + " (Administrator)";
        long id = announcements.create(form.getTitle().trim(), form.getSummary().trim(), body, form.getCategory(),
                form.getImagePath(), postedBy);
        return "redirect:/announcements/" + id + "?posted";
    }

    private void prepare(Model model, AnnouncementForm form) {
        model.addAttribute("announcementForm", form);
        model.addAttribute("categories", AnnouncementForm.CATEGORIES);
        model.addAttribute("images", AnnouncementForm.IMAGES);
    }

    private boolean canPost(CampusUser user) {
        return user != null && (user.getRole() == UserRole.CLUB_REPRESENTATIVE || user.getRole() == UserRole.ADMIN);
    }
}
