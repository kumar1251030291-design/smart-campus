package com.smartcampus.config;

import com.smartcampus.model.CampusUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class CurrentUserAdvice {
    @ModelAttribute("loggedInUserName")
    public String loggedInUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CampusUser user) {
            return user.getFullName();
        }
        return null;
    }

    @ModelAttribute("loggedInUserRole")
    public String loggedInUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CampusUser user) {
            return user.getRole().name();
        }
        return null;
    }

    @ModelAttribute("activePortalPage")
    public String activePortalPage(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.contains("/dashboard")) return "dashboard";
        if (path.contains("/bookings")) return "bookings";
        if (path.contains("/equipment")) return "equipment";
        if (path.contains("/profile")) return "profile";
        if (path.startsWith("/notifications")) return "notifications";
        if (path.startsWith("/resources")) return "resources";
        return "";
    }
}
