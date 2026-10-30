package com.smartcampus;

import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.DashboardStats;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.AdminDashboardService;
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
class AdminModuleTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ResourceRepository resourceRepository;
    @Autowired private AdminDashboardService dashboardService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void adminDashboardUsesLiveDatabaseStatistics() throws Exception {
        CampusUser admin = createUser("Dashboard Admin", UserRole.ADMIN);
        CampusUser student = createUser("Dashboard Student", UserRole.STUDENT);
        CampusUser club = createUser("Dashboard Club", UserRole.CLUB_REPRESENTATIVE);
        userRepository.updateAccountStatus(club.getUserId(), AccountStatus.DISABLED);
        DashboardStats expected = dashboardService.loadStatistics();

        mockMvc.perform(get("/admin/dashboard").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Resource distribution")))
                .andExpect(content().string(containsString("Community accounts")))
                .andExpect(content().string(containsString(String.valueOf(expected.totalResources()))))
                .andExpect(content().string(containsString(String.valueOf(expected.classrooms()))))
                .andExpect(content().string(containsString(String.valueOf(expected.students()))))
                .andExpect(content().string(containsString(String.valueOf(expected.clubRepresentatives()))))
                .andExpect(content().string(containsString(String.valueOf(expected.activeAccounts()))))
                .andExpect(content().string(containsString(String.valueOf(expected.inactiveAccounts()))))
                .andExpect(content().string(containsString("Resource Management")))
                .andExpect(content().string(containsString("User Management")));
        assertThat(userRepository.findByEmail(student.getEmail())).isPresent();
    }

    @Test
    void adminNamespaceRejectsAnonymousAndNonAdminUsers() throws Exception {
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/admin/dashboard").with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/resources").with(user("club@example.edu").roles("CLUB_REPRESENTATIVE")))
                .andExpect(status().isForbidden());
        CampusUser admin = createUser("Security Admin", UserRole.ADMIN);
        mockMvc.perform(get("/admin/dashboard").with(user(admin))).andExpect(status().isOk());
    }

    @Test
    void eachRoleCanAccessOnlyItsOwnDashboard() throws Exception {
        CampusUser admin = createUser("Role Test Admin", UserRole.ADMIN);
        CampusUser student = createUser("Role Test Student", UserRole.STUDENT);
        CampusUser club = createUser("Role Test Club", UserRole.CLUB_REPRESENTATIVE);

        mockMvc.perform(get("/admin/dashboard").with(user(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/student/dashboard").with(user(student))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Your student dashboard")));
        mockMvc.perform(get("/club/dashboard").with(user(club))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Your club dashboard")));

        mockMvc.perform(get("/student/dashboard").with(user(club))).andExpect(status().isForbidden());
        mockMvc.perform(get("/club/dashboard").with(user(student))).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/dashboard").with(user(student))).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/dashboard").with(user(club))).andExpect(status().isForbidden());
        mockMvc.perform(get("/student/dashboard").with(user(admin))).andExpect(status().isForbidden());
        mockMvc.perform(get("/club/dashboard").with(user(admin))).andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateEditSearchFilterUpdateAvailabilityAndDeleteResources() throws Exception {
        CampusUser admin = createUser("Resource Admin", UserRole.ADMIN);
        long startingCount = resourceRepository.countAll();

        mockMvc.perform(post("/admin/resources").with(user(admin)).with(csrf())
                        .param("name", "Admin Test Resource").param("description", "Created during an admin test")
                        .param("location", "Engineering Block").param("capacityOrQuantity", "12")
                        .param("category", "EQUIPMENT").param("availabilityStatus", "AVAILABLE"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/resources?created"));
        long resourceId = resourceRepository.search("Admin Test Resource", null, null).getFirst().getId();

        mockMvc.perform(get("/admin/resources").param("search", "Admin Test")
                        .param("category", "EQUIPMENT").param("availability", "AVAILABLE").with(user(admin)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Admin Test Resource")));
        mockMvc.perform(post("/admin/resources/{id}", resourceId).with(user(admin)).with(csrf())
                        .param("name", "Edited Test Lab").param("description", "Updated description")
                        .param("location", "Research Wing").param("capacityOrQuantity", "24")
                        .param("category", "LABORATORY").param("availabilityStatus", "AVAILABLE"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/resources?updated"));
        assertThat(resourceRepository.findById(resourceId).orElseThrow().getCategory().name()).isEqualTo("LABORATORY");

        mockMvc.perform(post("/admin/resources/{id}/availability", resourceId).with(user(admin)).with(csrf())
                        .param("availabilityStatus", "MAINTENANCE"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/resources?statusUpdated"));
        mockMvc.perform(get("/admin/resources").param("search", "Edited Test Lab")
                        .param("category", "LABORATORY").param("availability", "MAINTENANCE").with(user(admin)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Edited Test Lab")));

        mockMvc.perform(post("/admin/resources/{id}/delete", resourceId).with(user(admin)).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/resources?deleted"));
        assertThat(resourceRepository.countAll()).isEqualTo(startingCount);
    }

    @Test
    void resourceFormsValidateAndNonAdminsCannotModifyCatalogue() throws Exception {
        CampusUser admin = createUser("Validation Admin", UserRole.ADMIN);
        mockMvc.perform(get("/admin/resources/new").with(user(admin)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Create catalogue entry")));
        mockMvc.perform(post("/admin/resources").with(user(admin)).with(csrf())
                        .param("name", " ").param("description", "").param("location", "")
                        .param("capacityOrQuantity", "0").param("category", "ADMIN")
                        .param("availabilityStatus", "UNKNOWN"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Enter a resource name.")))
                .andExpect(content().string(containsString("Choose a valid category.")));
        mockMvc.perform(post("/admin/resources").with(user("student@example.edu").roles("STUDENT")).with(csrf())
                        .param("name", "Unauthorized").param("description", "No").param("location", "Nowhere")
                        .param("capacityOrQuantity", "2").param("category", "EQUIPMENT")
                        .param("availabilityStatus", "AVAILABLE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersCanBeSearchedFilteredViewedAndActivatedOrDeactivatedSafely() throws Exception {
        CampusUser admin = createUser("User Manager", UserRole.ADMIN);
        CampusUser student = createUser("Searchable Student", UserRole.STUDENT);
        CampusUser club = createUser("Searchable Club", UserRole.CLUB_REPRESENTATIVE);

        mockMvc.perform(get("/admin/users").with(user(admin)).param("search", student.getEmail())
                        .param("role", "STUDENT").param("status", "ACTIVE"))
                .andExpect(status().isOk()).andExpect(content().string(containsString(student.getEmail())))
                .andExpect(content().string(not(containsString(club.getEmail()))))
                .andExpect(content().string(not(containsString(student.getPassword()))));
        mockMvc.perform(get("/admin/users/{id}", student.getUserId()).with(user(admin)))
                .andExpect(status().isOk()).andExpect(content().string(containsString(student.getFullName())))
                .andExpect(content().string(containsString("Roll number")))
                .andExpect(content().string(not(containsString(student.getPassword()))));

        mockMvc.perform(post("/admin/users/{id}/status", admin.getUserId()).with(user(admin)).with(csrf())
                        .param("status", "DISABLED"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/users?error=self"));
        assertThat(userRepository.findByEmail(admin.getEmail()).orElseThrow().getAccountStatus())
                .isEqualTo(AccountStatus.ACTIVE);

        mockMvc.perform(post("/admin/users/{id}/status", student.getUserId()).with(user(admin)).with(csrf())
                        .param("status", "DISABLED"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/users?updated"));
        CampusUser disabledStudent = userRepository.findByEmail(student.getEmail()).orElseThrow();
        assertThat(disabledStudent.getAccountStatus()).isEqualTo(AccountStatus.DISABLED);
        mockMvc.perform(get("/admin/users").with(user(admin)).param("search", student.getFullName())
                        .param("role", "STUDENT").param("status", "DISABLED"))
                .andExpect(status().isOk()).andExpect(content().string(containsString(student.getEmail())));
        mockMvc.perform(get("/resources").with(user(disabledStudent)))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?disabled"));

        mockMvc.perform(post("/admin/users/{id}/status", student.getUserId()).with(user(admin)).with(csrf())
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection());
        assertThat(userRepository.findByEmail(student.getEmail()).orElseThrow().getAccountStatus())
                .isEqualTo(AccountStatus.ACTIVE);
    }

    private CampusUser createUser(String name, UserRole role) {
        String email = role.name().toLowerCase() + "+" + UUID.randomUUID() + "@college.edu";
        return userRepository.save(name, email, passwordEncoder.encode("test-password-123"), role,
                "Computing", role == UserRole.STUDENT ? 2 : null,
                role == UserRole.STUDENT ? "CS-" + UUID.randomUUID().toString().substring(0, 6) : null,
                role == UserRole.CLUB_REPRESENTATIVE ? "Test Club" : null);
    }
}
