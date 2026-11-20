package com.smartcampus.service;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.PasswordChangeForm;
import com.smartcampus.model.ProfileForm;
import com.smartcampus.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class MemberAccountSettingsService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public MemberAccountSettingsService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public CampusUser findOwnProfile(CampusUser authenticatedUser, UserRole expectedRole) {
        requireMemberRole(authenticatedUser, expectedRole);
        return users.findById(authenticatedUser.getUserId())
                .filter(user -> user.getRole() == expectedRole)
                .orElseThrow(() -> new AccessDeniedException("The requested member profile is not available."));
    }

    public ProfileForm toForm(CampusUser user) {
        ProfileForm form = new ProfileForm();
        form.setFullName(user.getFullName());
        form.setDepartment(user.getDepartment());
        form.setYearOfStudy(user.getYearOfStudy());
        form.setClubName(user.getClubName());
        return form;
    }

    public List<ProfileIssue> validateProfile(UserRole role, ProfileForm form) {
        List<ProfileIssue> issues = new ArrayList<>();
        if (role == UserRole.STUDENT) {
            if (form.getYearOfStudy() == null || form.getYearOfStudy() < 1 || form.getYearOfStudy() > 8) {
                issues.add(new ProfileIssue("yearOfStudy", "Choose an academic year from 1 to 8."));
            }
        } else if (role == UserRole.CLUB_REPRESENTATIVE) {
            if (form.getClubName() == null || form.getClubName().isBlank()) {
                issues.add(new ProfileIssue("clubName", "Enter your club name."));
            }
        } else {
            throw new AccessDeniedException("Only student and club representative profiles can be edited here.");
        }
        return issues;
    }

    @Transactional
    public CampusUser updateProfile(CampusUser authenticatedUser, UserRole expectedRole, ProfileForm form) {
        requireMemberRole(authenticatedUser, expectedRole);
        List<ProfileIssue> issues = validateProfile(expectedRole, form);
        if (!issues.isEmpty()) {
            throw new InvalidProfileException(issues.getFirst());
        }

        boolean updated = expectedRole == UserRole.STUDENT
                ? users.updateStudentProfile(authenticatedUser.getUserId(), form.getFullName(), form.getDepartment(),
                        form.getYearOfStudy())
                : users.updateClubRepresentativeProfile(authenticatedUser.getUserId(), form.getFullName(),
                        form.getDepartment(), form.getClubName());
        if (!updated) throw new AccessDeniedException("The requested member profile is not available.");
        return findOwnProfile(authenticatedUser, expectedRole);
    }

    @Transactional
    public void changePassword(CampusUser authenticatedUser, UserRole expectedRole, PasswordChangeForm form) {
        requireMemberRole(authenticatedUser, expectedRole);
        CampusUser persistedUser = findOwnProfile(authenticatedUser, expectedRole);
        if (form.getNewPassword() == null || form.getNewPassword().length() < 6) {
            throw new InvalidPasswordChangeException("newPassword", "New password must contain at least 6 characters.");
        }
        if (form.getNewPassword().length() > 72
                || form.getNewPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidPasswordChangeException("newPassword", "New password must fit BCrypt's 72-byte limit.");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new InvalidPasswordChangeException("confirmPassword", "New passwords do not match.");
        }
        if (!passwordEncoder.matches(form.getCurrentPassword(), persistedUser.getPassword())) {
            throw new InvalidPasswordChangeException("currentPassword", "Current password was not accepted.");
        }

        String encoded = passwordEncoder.encode(form.getNewPassword());
        if (!users.updateMemberPasswordHash(authenticatedUser.getUserId(), expectedRole, encoded)) {
            throw new AccessDeniedException("The requested member account is not available.");
        }
    }

    private static void requireMemberRole(CampusUser user, UserRole expectedRole) {
        if (user == null || user.getRole() != expectedRole
                || (expectedRole != UserRole.STUDENT && expectedRole != UserRole.CLUB_REPRESENTATIVE)) {
            throw new AccessDeniedException("You may only manage your own member account.");
        }
    }

    public record ProfileIssue(String field, String message) { }

    public static class InvalidProfileException extends RuntimeException {
        private final ProfileIssue issue;
        public InvalidProfileException(ProfileIssue issue) {
            super(issue.message());
            this.issue = issue;
        }
        public ProfileIssue getIssue() { return issue; }
    }

    public static class InvalidPasswordChangeException extends RuntimeException {
        private final String field;
        public InvalidPasswordChangeException(String field, String message) {
            super(message);
            this.field = field;
        }
        public String getField() { return field; }
    }
}
