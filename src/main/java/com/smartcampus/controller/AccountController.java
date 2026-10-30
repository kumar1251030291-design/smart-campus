package com.smartcampus.controller;

import com.smartcampus.model.RegistrationForm;
import com.smartcampus.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountController {
    private final RegistrationService registrationService;

    public AccountController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("administratorLogin", false);
        return "login";
    }

    @GetMapping("/login/admin")
    public String showAdministratorLogin(Model model) {
        model.addAttribute("administratorLogin", true);
        return "login";
    }

    @GetMapping("/register")
    public String showRegistration(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String submitRegistration(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                                     BindingResult result, Model model) {
        for (RegistrationService.RegistrationIssue issue : registrationService.validate(form)) {
            result.rejectValue(issue.field(), "registration.invalid", issue.message());
        }

        if (!result.hasErrors()) {
            try {
                registrationService.register(form);
                return "redirect:/login?registered";
            } catch (RegistrationService.DuplicateEmailException exception) {
                result.rejectValue("collegeEmail", "email.duplicate", exception.getMessage());
            }
        }

        // Never send password values back into the page after a failed attempt.
        form.setPassword(null);
        form.setConfirmPassword(null);
        return "register";
    }
}
