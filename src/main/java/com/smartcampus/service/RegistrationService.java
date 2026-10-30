package com.smartcampus.service;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.RegistrationForm;
import com.smartcampus.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<RegistrationIssue> validate(RegistrationForm form) {
        List<RegistrationIssue> issues = new ArrayList<>();
        if (form.getPassword() != null
                && form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            issues.add(new RegistrationIssue("password", "Password must not exceed 72 UTF-8 bytes for BCrypt."));
        }
        if (form.getPassword() != null && form.getConfirmPassword() != null
                && !form.getPassword().equals(form.getConfirmPassword())) {
            issues.add(new RegistrationIssue("confirmPassword", "Passwords do not match."));
        }
        if (UserRole.STUDENT.name().equals(form.getAccountType())) {
            if (form.getYearOfStudy() == null || form.getYearOfStudy() < 1 || form.getYearOfStudy() > 8) {
                issues.add(new RegistrationIssue("yearOfStudy", "Choose your academic year (1 to 8)."));
            }
            if (form.getRollNumber() == null || form.getRollNumber().isBlank()) {
                issues.add(new RegistrationIssue("rollNumber", "Enter your roll number."));
            }
        } else if (UserRole.CLUB_REPRESENTATIVE.name().equals(form.getAccountType())
                && (form.getClubName() == null || form.getClubName().isBlank())) {
            issues.add(new RegistrationIssue("clubName", "Enter the name of your club."));
        }
        return issues;
    }

    public CampusUser register(RegistrationForm form) {
        String email = form.getCollegeEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("An account with this email already exists.");
        }

        UserRole role = UserRole.valueOf(form.getAccountType());
        Integer year = role == UserRole.STUDENT ? form.getYearOfStudy() : null;
        String rollNumber = role == UserRole.STUDENT ? form.getRollNumber() : null;
        String clubName = role == UserRole.CLUB_REPRESENTATIVE ? form.getClubName() : null;
        try {
            return userRepository.save(form.getFullName(), email, passwordEncoder.encode(form.getPassword()), role,
                    form.getDepartment(), year, rollNumber, clubName);
        } catch (DuplicateKeyException exception) {
            // The database unique key also protects against two simultaneous registrations.
            throw new DuplicateEmailException("An account with this email already exists.");
        }
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) { super(message); }
    }

    public record RegistrationIssue(String field, String message) { }
}
