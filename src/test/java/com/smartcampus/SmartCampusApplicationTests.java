package com.smartcampus;

import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.Classroom;
import com.smartcampus.model.Equipment;
import com.smartcampus.model.Resource;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.ResourceService;
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

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:smartcampus-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class SmartCampusApplicationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ResourceRepository resourceRepository;
    @Autowired private ResourceService resourceService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test void applicationContextStarts() { }

    @Test
    void accountPagesRenderAndKeepPublicNavigation() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Explore Resources")))
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("href=\"/register\"")));
        mockMvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Student / Club")))
                .andExpect(content().string(containsString("Administrator")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mockMvc.perform(get("/login/admin")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Administrator access")));
        mockMvc.perform(get("/register")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Club Representative")))
                .andExpect(content().string(not(containsString("value=\"ADMIN\""))));
    }

    @Test
    void successfulStudentRegistrationStoresHashedPasswordAndStudentDetails() throws Exception {
        String email = email("student");
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Alex Student").param("collegeEmail", email)
                        .param("password", "campus-pass-123").param("confirmPassword", "campus-pass-123")
                        .param("department", "Computing").param("yearOfStudy", "2")
                        .param("rollNumber", "CS-204").param("accountType", "STUDENT"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registered"));
        CampusUser saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(saved.getDepartment()).isEqualTo("Computing");
        assertThat(saved.getYearOfStudy()).isEqualTo(2);
        assertThat(saved.getRollNumber()).isEqualTo("CS-204");
        assertThat(saved.getClubName()).isNull();
        assertThat(saved.getPassword()).isNotEqualTo("campus-pass-123");
        assertThat(passwordEncoder.matches("campus-pass-123", saved.getPassword())).isTrue();
        assertThat(saved.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void successfulClubRegistrationStoresClubAndOmitsStudentDetails() throws Exception {
        String email = email("club");
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Morgan Representative").param("collegeEmail", email)
                        .param("password", "campus-pass-456").param("confirmPassword", "campus-pass-456")
                        .param("department", "Engineering").param("clubName", "Robotics Club")
                        .param("accountType", "CLUB_REPRESENTATIVE"))
                .andExpect(status().is3xxRedirection());
        CampusUser saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getRole()).isEqualTo(UserRole.CLUB_REPRESENTATIVE);
        assertThat(saved.getClubName()).isEqualTo("Robotics Club");
        assertThat(saved.getYearOfStudy()).isNull();
        assertThat(saved.getRollNumber()).isNull();
        mockMvc.perform(post("/login/authenticate").with(csrf()).param("email", email)
                        .param("password", "campus-pass-456").param("loginMode", "USER"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/club/dashboard"));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        String email = email("duplicate");
        submitStudent(email, "campus-pass-123");
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Second Person").param("collegeEmail", email)
                        .param("password", "campus-pass-123").param("confirmPassword", "campus-pass-123")
                        .param("department", "Computing").param("yearOfStudy", "1")
                        .param("rollNumber", "CS-1").param("accountType", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("An account with this email already exists.")));
    }

    @Test
    void registrationAcceptsSixCharacterPasswordsAndRejectsPasswordsOverBcryptLimit() throws Exception {
        String email = email("minimum-password");
        submitStudent(email, "sixsix");
        CampusUser saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(passwordEncoder.matches("sixsix", saved.getPassword())).isTrue();

        String tooLong = "x".repeat(73);
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "Long Password").param("collegeEmail", email("long-password"))
                        .param("password", tooLong).param("confirmPassword", tooLong)
                        .param("department", "Computing").param("yearOfStudy", "1")
                        .param("rollNumber", "CS-LONG").param("accountType", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Password must be between 6 and 72 characters.")));
    }

    @Test
    void invalidRegistrationDataAndPublicAdminRoleAreRejected() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", " ").param("collegeEmail", "not-email")
                        .param("password", "short").param("confirmPassword", "different")
                        .param("department", "").param("accountType", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enter your full name.")))
                .andExpect(content().string(containsString("Enter a valid email address.")))
                .andExpect(content().string(containsString("Passwords do not match.")))
                .andExpect(content().string(containsString("Choose Student or Club Representative.")));
        mockMvc.perform(post("/register").with(csrf())
                        .param("fullName", "No Year").param("collegeEmail", email("no-year"))
                        .param("password", "campus-pass-123").param("confirmPassword", "campus-pass-123")
                        .param("department", "Computing").param("accountType", "STUDENT"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Choose your academic year")));
    }

    @Test
    void successfulLoginAndIncorrectPasswordAreHandled() throws Exception {
        String email = email("login");
        submitStudent(email, "campus-pass-789");
        mockMvc.perform(post("/login/authenticate").with(csrf()).param("email", email)
                        .param("password", "campus-pass-789").param("loginMode", "USER"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/student/dashboard"));
        mockMvc.perform(post("/login/authenticate").with(csrf()).param("email", email)
                        .param("password", "wrong-password").param("loginMode", "USER"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error"));
        mockMvc.perform(post("/login/authenticate").with(csrf()).param("email", email)
                        .param("password", "campus-pass-789").param("loginMode", "ADMIN"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login/admin?roleError"));

        String adminEmail = email("admin");
        userRepository.save("Demo Admin", adminEmail, passwordEncoder.encode("private-admin-pass-123"),
                UserRole.ADMIN, null, null, null, null);
        mockMvc.perform(post("/login/authenticate").with(csrf()).param("email", adminEmail)
                        .param("password", "private-admin-pass-123").param("loginMode", "USER"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?roleError"));
        mockMvc.perform(post("/login/authenticate").with(csrf()).param("email", adminEmail)
                        .param("password", "private-admin-pass-123").param("loginMode", "ADMIN"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void logoutInvalidatesSessionAndRoleBoundariesAreEnforced() throws Exception {
        mockMvc.perform(get("/admin/overview")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/admin/overview").with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/student/profile").with(user("club@example.edu").roles("CLUB_REPRESENTATIVE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/overview").with(user("admin@example.edu").roles("ADMIN")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/logout").with(csrf()).with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void resourceCatalogueAndOopModelsRemainAvailable() throws Exception {
        Resource classroom = new Classroom(101L, "Classroom A101", "Lecture room", "Block A", 60,
                AvailabilityStatus.AVAILABLE);
        Resource equipment = new Equipment(102L, "Arduino Kits", "Starter kits", "Equipment Desk", 30,
                AvailabilityStatus.LIMITED);
        assertThat(classroom.getCategory()).isEqualTo(ResourceCategory.CLASSROOM);
        assertThat(equipment.getCapacityOrQuantityLabel()).isEqualTo("30 items");

        assertThat(resourceRepository.findAll()).hasSize(85).extracting(Resource::getName)
                .contains("Classroom A101", "NVIDIA AI/GPU Lab", "Arduino Uno", "Innovation Centre");
        assertThat(resourceService.searchResources("computer lab", null, null))
                .extracting(Resource::getName)
                .containsExactlyInAnyOrder("Computer Lab 1", "Computer Programming Lab");
        assertThat(resourceService.searchResources("", ResourceCategory.CLASSROOM, null)).hasSize(15);
        assertThat(resourceService.searchResources("arduino", ResourceCategory.EQUIPMENT,
                AvailabilityStatus.AVAILABLE)).extracting(Resource::getName)
                .containsExactlyInAnyOrder("Arduino Kits", "Arduino Mega", "Arduino Uno");
        mockMvc.perform(get("/resources")).andExpect(status().isOk())
                .andExpect(content().string(containsString("NVIDIA AI/GPU Lab")))
                .andExpect(content().string(containsString("85 results")));
    }

    private void submitStudent(String email, String password) throws Exception {
        mockMvc.perform(post("/register").with(csrf()).param("fullName", "Test Student")
                .param("collegeEmail", email).param("password", password).param("confirmPassword", password)
                .param("department", "Computing").param("yearOfStudy", "1")
                .param("rollNumber", "CS-" + UUID.randomUUID().toString().substring(0, 6))
                .param("accountType", "STUDENT")).andExpect(status().is3xxRedirection());
    }

    private String email(String prefix) { return prefix + "+" + UUID.randomUUID() + "@college.edu"; }
}
