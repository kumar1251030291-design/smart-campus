package com.smartcampus;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:smartcampus-announcements;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class AnnouncementPostingTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void clubRepresentativesAndAdminsCanPostAnnouncementsButStudentsAndVisitorsCannot() throws Exception {
        CampusUser club = create(UserRole.CLUB_REPRESENTATIVE);
        CampusUser admin = create(UserRole.ADMIN);
        CampusUser student = create(UserRole.STUDENT);
        int before = count();

        mockMvc.perform(get("/club/dashboard").with(user(club))).andExpect(content().string(containsString("/announcements/new")));
        mockMvc.perform(get("/student/dashboard").with(user(student))).andExpect(content().string(org.hamcrest.Matchers.not(containsString("/announcements/new"))));
        mockMvc.perform(get("/admin/dashboard").with(user(admin))).andExpect(content().string(containsString("/announcements/new")));
        mockMvc.perform(get("/announcements/new").with(user(club))).andExpect(status().isOk());

        mockMvc.perform(post("/announcements").with(csrf()).with(user(club))
                        .param("title", "Robotics club open house").param("category", "EVENT")
                        .param("imagePath", "/images/announcements/plain-tech-fest.svg")
                        .param("summary", "Visit the robotics lab on Friday to see our competition robots."))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("/announcements/*?posted"));
        mockMvc.perform(get("/announcements")).andExpect(content().string(containsString("Robotics club open house")))
                .andExpect(content().string(containsString("Posted by Test CLUB_REPRESENTATIVE (Test Club)")));

        mockMvc.perform(post("/announcements").with(csrf()).with(user(admin))
                        .param("title", "Library closed on Monday").param("category", "FACILITY")
                        .param("imagePath", "/images/announcements/plain-guidelines.svg")
                        .param("summary", "The central library is closed for stock-taking on Monday."))
                .andExpect(status().is3xxRedirection());
        assertThat(count()).isEqualTo(before + 2);

        // Invalid picture path and short title are rejected.
        mockMvc.perform(post("/announcements").with(csrf()).with(user(club))
                        .param("title", "Hi").param("category", "EVENT").param("imagePath", "https://evil.example/x.png")
                        .param("summary", "Too short title and an outside picture."))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Choose one of the listed pictures.")));

        // Students and visitors cannot post.
        mockMvc.perform(get("/announcements/new").with(user(student))).andExpect(status().isForbidden());
        mockMvc.perform(post("/announcements").with(csrf()).with(user(student)).param("title", "Student post attempt")
                .param("category", "EVENT").param("imagePath", "/images/announcements/plain-tech-fest.svg")
                .param("summary", "Students should not be able to post this.")).andExpect(status().isForbidden());
        mockMvc.perform(get("/announcements/new")).andExpect(status().is3xxRedirection());
        assertThat(count()).isEqualTo(before + 2);
    }

    private int count() { return jdbc.queryForObject("SELECT COUNT(*) FROM campus_announcements", Integer.class); }

    private CampusUser create(UserRole role) {
        return users.save("Test " + role.name(), "announce-" + UUID.randomUUID() + "@college.edu", encoder.encode("unused-test-password"),
                role, "Engineering", role == UserRole.STUDENT ? 1 : null, role == UserRole.STUDENT ? "R-" + UUID.randomUUID() : null,
                role == UserRole.CLUB_REPRESENTATIVE ? "Test Club" : null);
    }
}
