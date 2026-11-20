package com.smartcampus;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:smartcampus-member-profile-tests;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class MemberProfileTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void studentAndClubProfilesShowTheirOwnStoredInformationWithoutPasswordHashes() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);

        mockMvc.perform(get("/student/profile").with(user(student)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(student.getEmail())))
                .andExpect(content().string(containsString(student.getRollNumber())))
                .andExpect(content().string(containsString("Academic year")))
                .andExpect(content().string(containsString("href=\"/student/profile\"")))
                .andExpect(content().string(not(containsString(student.getPassword()))));
        mockMvc.perform(get("/club/profile").with(user(club)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(club.getEmail())))
                .andExpect(content().string(containsString(club.getClubName())))
                .andExpect(content().string(containsString("Club representative")))
                .andExpect(content().string(not(containsString(club.getPassword()))));
    }

    @Test
    void unauthenticatedAndWrongRoleRequestsAreRejected() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        mockMvc.perform(get("/student/profile")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/club/profile")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/student/profile").with(user(club))).andExpect(status().isForbidden());
        mockMvc.perform(get("/club/profile").with(user(student))).andExpect(status().isForbidden());
    }

    @Test
    void profileEditCannotTargetAnotherAccountOrChangeProtectedFields() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser anotherStudent = createUser(UserRole.STUDENT);

        mockMvc.perform(post("/student/profile").with(user(student)).with(csrf())
                        .param("userId", String.valueOf(anotherStudent.getUserId()))
                        .param("role", "ADMIN").param("email", anotherStudent.getEmail())
                        .param("rollNumber", "CHANGED-ROLL")
                        .param("fullName", "Updated Student").param("department", "Design")
                        .param("yearOfStudy", "3"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/student/profile?updated"));

        CampusUser updated = users.findById(student.getUserId()).orElseThrow();
        assertThat(updated.getFullName()).isEqualTo("Updated Student");
        assertThat(updated.getDepartment()).isEqualTo("Design");
        assertThat(updated.getYearOfStudy()).isEqualTo(3);
        assertThat(updated.getEmail()).isEqualTo(student.getEmail());
        assertThat(updated.getRollNumber()).isEqualTo(student.getRollNumber());
        assertThat(updated.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(users.findById(anotherStudent.getUserId()).orElseThrow().getFullName())
                .isEqualTo(anotherStudent.getFullName());
        mockMvc.perform(get("/student/profile").with(user(student)).param("userId", String.valueOf(anotherStudent.getUserId())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(student.getEmail())))
                .andExpect(content().string(not(containsString(anotherStudent.getEmail()))));
    }

    @Test
    void clubProfileEditKeepsRoleAndEmailAndInvalidProfileDataIsRejected() throws Exception {
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        mockMvc.perform(post("/club/profile").with(user(club)).with(csrf())
                        .param("fullName", "Updated Club Rep").param("department", "Robotics")
                        .param("clubName", "Autonomy Club").param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection());
        CampusUser updated = users.findById(club.getUserId()).orElseThrow();
        assertThat(updated.getFullName()).isEqualTo("Updated Club Rep");
        assertThat(updated.getDepartment()).isEqualTo("Robotics");
        assertThat(updated.getClubName()).isEqualTo("Autonomy Club");
        assertThat(updated.getEmail()).isEqualTo(club.getEmail());
        assertThat(updated.getRole()).isEqualTo(UserRole.CLUB_REPRESENTATIVE);

        mockMvc.perform(post("/club/profile").with(user(club)).with(csrf())
                        .param("fullName", "").param("department", "").param("clubName", " "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enter your name.")))
                .andExpect(content().string(containsString("Enter your department.")))
                .andExpect(content().string(containsString("Enter your club name.")));
    }

    @Test
    void passwordChangeChecksCurrentPasswordAndPersistsOnlyBcryptHash() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        String previousHash = users.findById(student.getUserId()).orElseThrow().getPassword();

        mockMvc.perform(post("/student/profile/password").with(user(student)).with(csrf())
                        .param("currentPassword", "incorrect-current")
                        .param("newPassword", "new-campus-password")
                        .param("confirmPassword", "new-campus-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Current password was not accepted.")));
        assertThat(users.findById(student.getUserId()).orElseThrow().getPassword()).isEqualTo(previousHash);

        mockMvc.perform(post("/student/profile/password").with(user(student)).with(csrf())
                        .param("currentPassword", "initial-campus-password")
                        .param("newPassword", "new-campus-password")
                        .param("confirmPassword", "new-campus-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/profile/password?passwordUpdated"));
        String newHash = users.findById(student.getUserId()).orElseThrow().getPassword();
        assertThat(newHash).isNotEqualTo(previousHash);
        assertThat(passwordEncoder.matches("new-campus-password", newHash)).isTrue();
        assertThat(passwordEncoder.matches("initial-campus-password", newHash)).isFalse();

        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        mockMvc.perform(post("/club/profile/password").with(user(club)).with(csrf())
                        .param("currentPassword", "initial-campus-password")
                        .param("newPassword", "club-campus-password")
                        .param("confirmPassword", "club-campus-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/club/profile/password?passwordUpdated"));
        CampusUser updatedClub = users.findById(club.getUserId()).orElseThrow();
        assertThat(updatedClub.getRole()).isEqualTo(UserRole.CLUB_REPRESENTATIVE);
        assertThat(passwordEncoder.matches("club-campus-password", updatedClub.getPassword())).isTrue();

        mockMvc.perform(post("/student/profile/password").with(user(student)).with(csrf())
                        .param("currentPassword", "new-campus-password")
                        .param("newPassword", "x".repeat(73)).param("confirmPassword", "x".repeat(73)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("New password must be between 6 and 72 characters.")));

        String overBcryptByteLimit = "😀".repeat(24);
        mockMvc.perform(post("/student/profile/password").with(user(student)).with(csrf())
                        .param("currentPassword", "new-campus-password")
                        .param("newPassword", overBcryptByteLimit).param("confirmPassword", overBcryptByteLimit))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("New password must fit BCrypt")));
    }

    private CampusUser createUser(UserRole role) {
        String email = "profile-" + role.name().toLowerCase() + "+" + UUID.randomUUID() + "@college.edu";
        return users.save("Profile " + role, email, passwordEncoder.encode("initial-campus-password"), role,
                "Computing", role == UserRole.STUDENT ? 2 : null,
                role == UserRole.STUDENT ? "PR-" + UUID.randomUUID().toString().substring(0, 7) : null,
                role == UserRole.CLUB_REPRESENTATIVE ? "Robotics Club" : null);
    }
}
