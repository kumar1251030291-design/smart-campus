package com.smartcampus;

import com.smartcampus.enums.EventType;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.BookingRequestForm;
import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:smartcampus-visual-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class VisualConsistencyTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ResourceRepository resourceRepository;
    @Autowired private BookingService bookingService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void portalAndAdminPagesRenderUtf8WithTheirSharedStylesheets() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        CampusUser admin = createUser(UserRole.ADMIN);
        var classroom = resourceRepository.findAll().stream()
                .filter(resource -> resource.getCategory() == ResourceCategory.CLASSROOM).findFirst().orElseThrow();
        BookingRequestForm form = new BookingRequestForm();
        form.setEventTitle("Visual review event");
        form.setEventType(EventType.ACADEMIC.name());
        form.setPurpose("Visual formatting check for an admin booking review.");
        form.setEventDate(LocalDate.of(2030, 11, 1));
        form.setStartTime(LocalTime.of(1, 30));
        form.setEndTime(LocalTime.of(4, 30));
        form.setExpectedParticipants(20);
        form.setResourceIds(List.of(classroom.getId()));
        form.setRequestedQuantities(List.of(1));
        var booking = bookingService.submit(form, student);

        assertPage("/", null, "/css/styles.css");
        assertPage("/student/dashboard", student, "/css/portal.css");
        assertPage("/club/dashboard", club, "/css/portal.css");
        MvcResult catalogue = assertPage("/resources", null, "/css/styles.css");
        assertThat(catalogue.getResponse().getContentAsString()).contains("Try “Computer Lab” or “Projector”");
        assertPage("/resources/" + classroom.getId(), null, "/css/portal.css");
        assertPage("/student/bookings/new", student, "/css/portal.css");
        assertPage("/student/bookings", student, "/css/portal.css");
        assertPage("/student/bookings/" + booking.bookingId(), student, "/css/portal.css");
        assertPage("/admin/dashboard", admin, "/css/admin.css");
        assertPage("/admin/resources", admin, "/css/admin.css");
        assertPage("/admin/resources/new", admin, "/css/admin.css");
        assertPage("/admin/users", admin, "/css/admin.css");
        assertPage("/admin/users/" + student.getUserId(), admin, "/css/admin.css");
        assertPage("/admin/bookings", admin, "/css/admin.css");
        MvcResult details = assertPage("/admin/bookings/" + booking.bookingId(), admin, "/css/admin.css");
        assertThat(details.getResponse().getContentAsString())
                .contains("01 Nov 2030", "01:30 AM – 04:30 AM", "3 hours", "Booking status history",
                        "academic · Submitted", "1 × requested");
        assertPage("/admin/equipment", admin, "/css/admin.css");
        assertStaticAsset("/css/styles.css", "--sage: #659287");
        assertStaticAsset("/css/portal.css", ".resource-detail-panel");
        assertStaticAsset("/css/admin.css", ".admin-shell");
    }

    private void assertStaticAsset(String path, String expectedText) throws Exception {
        MvcResult result = mockMvc.perform(get(path)).andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).contains(expectedText);
    }

    private MvcResult assertPage(String path, CampusUser principal, String stylesheet) throws Exception {
        var request = get(path);
        if (principal != null) request.with(user(principal));
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentType()).containsIgnoringCase("charset=UTF-8");
        assertThat(result.getResponse().getContentAsString()).contains(stylesheet);
        return result;
    }

    private CampusUser createUser(UserRole role) {
        String email = "visual-" + role.name().toLowerCase() + "+" + UUID.randomUUID() + "@college.edu";
        return userRepository.save("Visual QA " + role, email, passwordEncoder.encode("unused-test-password"), role,
                "Engineering", role == UserRole.STUDENT ? 2 : null,
                role == UserRole.STUDENT ? "VIS-" + UUID.randomUUID() : null,
                role == UserRole.CLUB_REPRESENTATIVE ? "Visual Club" : null);
    }
}
