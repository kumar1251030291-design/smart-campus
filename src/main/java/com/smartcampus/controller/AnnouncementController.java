package com.smartcampus.controller;

import com.smartcampus.repository.AnnouncementRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AnnouncementController {
    private final AnnouncementRepository announcements;
    public AnnouncementController(AnnouncementRepository announcements) { this.announcements = announcements; }

    @GetMapping("/announcements")
    public String list(Model model) {
        model.addAttribute("announcements", announcements.all());
        return "announcements";
    }

    @GetMapping("/announcements/{id}")
    public String details(@PathVariable long id, Model model) {
        return announcements.find(id).map(announcement -> {
            model.addAttribute("announcement", announcement);
            model.addAttribute("otherAnnouncements", announcements.latest(4).stream()
                    .filter(other -> other.announcementId() != id).limit(3).toList());
            return "announcement-detail";
        }).orElse("redirect:/announcements");
    }
}
