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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:smartcampus-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class MemberPortalTests {
    private static final AtomicInteger DATE_COUNTER = new AtomicInteger();
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository users;
    @Autowired private ResourceRepository resources;
    @Autowired private BookingService bookings;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void dashboardsUseRealOwnerScopedBookingDataForStudentsAndClubs() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        var room = resource(ResourceCategory.CLASSROOM);
        var request = bookings.submit(form(List.of(room.getId()), "Private student event"), student);

        mockMvc.perform(get("/student/dashboard").with(user(student)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Welcome, " + student.getFullName())))
                .andExpect(content().string(containsString("Private student event")))
                .andExpect(content().string(containsString("Year <span>2</span>")))
                .andExpect(content().string(containsString("CS-")));
        mockMvc.perform(get("/club/dashboard").with(user(club)))
                .andExpect(status().isOk()).andExpect(content().string(containsString(club.getClubName())))
                .andExpect(content().string(not(containsString("Private student event"))))
                .andExpect(content().string(containsString("Total requests")));
        assertThat(bookings.forRequester(club.getUserId())).isEmpty();
        assertThat(bookings.forRequester(student.getUserId())).extracting(item -> item.bookingId()).contains(request.bookingId());
    }

    @Test
    void bookingPagesAndEquipmentAreOwnerScopedAndProfilesUseAuthenticatedAccount() throws Exception {
        CampusUser first = createUser(UserRole.STUDENT);
        CampusUser second = createUser(UserRole.STUDENT);
        var room = resource(ResourceCategory.CLASSROOM);
        var request = bookings.submit(form(List.of(room.getId()), "Only my request"), first);

        mockMvc.perform(get("/student/bookings").with(user(first)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Only my request")));
        mockMvc.perform(get("/student/bookings").with(user(second)))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("Only my request"))));
        mockMvc.perform(get("/student/bookings/" + request.bookingId()).with(user(second)))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/student/equipment").with(user(second)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("No equipment is currently reserved or issued")));
        mockMvc.perform(get("/student/profile").with(user(first)))
                .andExpect(status().isOk()).andExpect(content().string(containsString(first.getEmail())))
                .andExpect(content().string(not(containsString(second.getEmail()))));
    }

    @Test
    void resourceSearchFiltersByDescriptionGroupCapacityAndShowsDetailsAndPrefilledRequest() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        var classroom = resource(ResourceCategory.CLASSROOM);
        mockMvc.perform(get("/resources").param("search", classroom.getDescription().substring(0, 6))
                        .param("group", "CLASSROOMS").param("minimumCapacity", "1"))
                .andExpect(status().isOk()).andExpect(content().string(containsString(classroom.getName())))
                .andExpect(content().string(containsString("Campus collection")));
        mockMvc.perform(get("/resources/" + classroom.getId()))
                .andExpect(status().isOk()).andExpect(content().string(containsString(classroom.getDescription())))
                .andExpect(content().string(containsString("Sign in to send a request")));
        mockMvc.perform(get("/student/bookings/new").param("resourceId", classroom.getId().toString()).with(user(student)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Requester information")))
                .andExpect(content().string(containsString(student.getEmail())))
                .andExpect(content().string(containsString("value=\"" + classroom.getId() + "\"")))
                .andExpect(content().string(containsString("selected=\"selected\"")));
        mockMvc.perform(get("/resources").param("search", "wireless").param("category", "EQUIPMENT"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Microphones")));
    }

    @Test
    void membersCanSubmitMultipleResourcesAndCheckAvailabilityServerSide() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        var room = resource(ResourceCategory.CLASSROOM);
        var equipment = resource(ResourceCategory.EQUIPMENT);
        BookingRequestForm form = form(List.of(room.getId(), equipment.getId()), "Multi resource request");

        mockMvc.perform(get("/student/bookings/availability").with(user(student))
                        .param("eventDate", form.getEventDate().toString())
                        .param("startTime", form.getStartTime().toString()).param("endTime", form.getEndTime().toString())
                        .param("expectedParticipants", "5").param("resourceIds", room.getId().toString(), equipment.getId().toString())
                        .param("requestedQuantities", "1", "2"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("\"available\":true")));

        mockMvc.perform(post("/student/bookings").with(user(student)).with(csrf())
                        .param("eventTitle", form.getEventTitle()).param("eventType", EventType.ACADEMIC.name())
                        .param("purpose", form.getPurpose()).param("eventDate", form.getEventDate().toString())
                        .param("startTime", form.getStartTime().toString()).param("endTime", form.getEndTime().toString())
                        .param("expectedParticipants", "5").param("resourceIds", room.getId().toString(), equipment.getId().toString())
                        .param("requestedQuantities", "1", "2"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("/student/bookings/*?submitted"));
        var saved = bookings.forRequester(student.getUserId()).getFirst();
        assertThat(bookings.items(saved.bookingId())).hasSize(2);

        mockMvc.perform(post("/student/bookings").with(user(student)).with(csrf())
                        .param("eventTitle", "Invalid time").param("eventType", EventType.ACADEMIC.name())
                        .param("purpose", "Invalid time check").param("eventDate", form.getEventDate().toString())
                        .param("startTime", "14:00").param("endTime", "13:00").param("expectedParticipants", "5")
                        .param("resourceIds", room.getId().toString()).param("requestedQuantities", "1"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("End time must be later than start time.")));
    }

    private CampusUser createUser(UserRole role) {
        String email = role.name().toLowerCase() + "+portal-" + UUID.randomUUID() + "@college.edu";
        return users.save("Portal " + role, email, passwordEncoder.encode("portal-test"), role,
                "Computing", role == UserRole.STUDENT ? 2 : null,
                role == UserRole.STUDENT ? "CS-" + UUID.randomUUID().toString().substring(0, 6) : null,
                role == UserRole.CLUB_REPRESENTATIVE ? "Engineering Club" : null);
    }

    private com.smartcampus.model.Resource resource(ResourceCategory category) {
        return resources.findAll().stream().filter(item -> item.getCategory() == category).findFirst().orElseThrow();
    }

    private BookingRequestForm form(List<Long> resourceIds, String title) {
        int unique = DATE_COUNTER.getAndIncrement();
        BookingRequestForm form = new BookingRequestForm();
        form.setEventTitle(title); form.setEventType(EventType.ACADEMIC.name());
        form.setPurpose("A real test request to verify account-scoped portal data.");
        form.setEventDate(LocalDate.now().plusDays(30 + unique));
        form.setStartTime(LocalTime.of(10, unique % 50)); form.setEndTime(form.getStartTime().plusHours(1));
        form.setExpectedParticipants(5); form.setResourceIds(resourceIds);
        form.setRequestedQuantities(resourceIds.stream().map(id -> 1).toList());
        return form;
    }
}
