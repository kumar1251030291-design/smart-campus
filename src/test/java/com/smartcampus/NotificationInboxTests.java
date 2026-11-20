package com.smartcampus;

import com.smartcampus.enums.NotificationType;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.enums.ReturnCondition;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.BookingRequestForm;
import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.EquipmentRepository;
import com.smartcampus.repository.PortalNotificationRepository;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.BookingService;
import com.smartcampus.service.EquipmentAllocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:smartcampus-notification-inbox-tests;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class NotificationInboxTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private PortalNotificationRepository notifications;
    @Autowired private UserRepository users;
    @Autowired private ResourceRepository resources;
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private BookingService bookings;
    @Autowired private EquipmentAllocationService equipment;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void studentAndClubCanOpenTheirOwnInboxAndSeeNewestFirstWithUnreadCount() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        CampusUser admin = createUser(UserRole.ADMIN);
        createNotification(student, "Older update", "older-event");
        createNotification(student, "Newest update", "newest-event");
        createNotification(club, "Club only update", "club-event");
        createNotification(admin, "Admin only update", "admin-event");

        mockMvc.perform(get("/notifications").with(user(student)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Newest update")))
                .andExpect(content().string(containsString("Older update")))
                .andExpect(content().string(not(containsString("Club only update"))))
                .andExpect(content().string(not(containsString("Admin only update"))))
                .andExpect(content().string(containsString("2</strong> unread")))
                .andExpect(content().string(containsString("Booking approved")));
        mockMvc.perform(get("/notifications").with(user(club)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Club only update")))
                .andExpect(content().string(not(containsString("Newest update"))));

        var ordered = notifications.allForUser(student.getUserId());
        assertThat(ordered).extracting(item -> item.title()).containsExactly("Newest update", "Older update");
    }

    @Test
    void emptyInboxAndRoleRestrictionsAreHandled() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        CampusUser admin = createUser(UserRole.ADMIN);

        mockMvc.perform(get("/notifications").with(user(student)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("There are no notifications yet")))
                .andExpect(content().string(containsString("0</strong> unread")));
        mockMvc.perform(get("/notifications").with(user(club))).andExpect(status().isOk());
        mockMvc.perform(get("/notifications")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/notifications").with(user(admin))).andExpect(status().isForbidden());
    }

    @Test
    void notificationDetailsAreRecipientScopedAndOpeningDoesNotSilentlyMarkRead() throws Exception {
        CampusUser recipient = createUser(UserRole.STUDENT);
        CampusUser other = createUser(UserRole.STUDENT);
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        long notificationId = createNotification(recipient, "Private notice", "private-event");

        mockMvc.perform(get("/notifications/{id}", notificationId).with(user(recipient)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Private notice")))
                .andExpect(content().string(containsString("Open related update")));
        mockMvc.perform(get("/notifications/{id}", notificationId).with(user(other))).andExpect(status().isNotFound());
        mockMvc.perform(get("/notifications/{id}", notificationId).with(user(club))).andExpect(status().isNotFound());
        assertThat(notifications.findForUser(notificationId, recipient.getUserId()).orElseThrow().unread()).isTrue();
    }

    @Test
    void individualReadActionRequiresCsrfAndCannotUpdateAnotherRecipientsNotification() throws Exception {
        CampusUser recipient = createUser(UserRole.STUDENT);
        CampusUser other = createUser(UserRole.STUDENT);
        long notificationId = createNotification(recipient, "Read me", "read-event");

        mockMvc.perform(post("/notifications/{id}/read", notificationId).with(user(recipient)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/notifications/{id}/read", notificationId).with(user(other)).with(csrf()))
                .andExpect(status().isNotFound());
        assertThat(notifications.unreadCountForUser(recipient.getUserId())).isEqualTo(1);

        mockMvc.perform(post("/notifications/{id}/read", notificationId).with(user(recipient)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications/" + notificationId + "?readUpdated"));
        assertThat(notifications.findForUser(notificationId, recipient.getUserId()).orElseThrow().unread()).isFalse();
        assertThat(notifications.unreadCountForUser(recipient.getUserId())).isZero();

        mockMvc.perform(post("/notifications/{id}/read", notificationId).with(user(recipient)).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void markAllReadOnlyChangesTheAuthenticatedUsersUnreadRows() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        createNotification(student, "Student notice one", "student-all-1");
        createNotification(student, "Student notice two", "student-all-2");
        createNotification(club, "Club notice remains unread", "club-unread");

        mockMvc.perform(post("/notifications/read-all").with(user(student)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/notifications/read-all").with(user(student)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications?readAllUpdated"));

        assertThat(notifications.unreadCountForUser(student.getUserId())).isZero();
        assertThat(notifications.unreadCountForUser(club.getUserId())).isEqualTo(1);
        assertThat(notifications.allForUser(student.getUserId())).allMatch(item -> !item.unread());
    }

    @Test
    void bookingSubmissionStillCreatesRecipientAndAdministratorNotifications() {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser admin = createUser(UserRole.ADMIN);
        var classroom = resources.findAll().stream()
                .filter(resource -> resource.getCategory() == ResourceCategory.CLASSROOM)
                .findFirst().orElseThrow();
        BookingRequestForm form = new BookingRequestForm();
        form.setEventTitle("Notification workflow check");
        form.setEventType("ACADEMIC");
        form.setPurpose("Verify the existing booking submission notification trigger.");
        form.setEventDate(LocalDate.now().plusDays(40));
        form.setStartTime(LocalTime.of(10, 0));
        form.setEndTime(LocalTime.of(11, 0));
        form.setExpectedParticipants(10);
        form.setResourceIds(java.util.List.of(classroom.getId()));
        form.setRequestedQuantities(java.util.List.of(1));

        var booking = bookings.submit(form, student);

        assertThat(notifications.allForUser(student.getUserId()))
                .anyMatch(item -> item.bookingId() != null && item.bookingId() == booking.bookingId()
                        && item.title().equals("Booking request submitted"));
        assertThat(notifications.allForUser(admin.getUserId()))
                .anyMatch(item -> item.bookingId() != null && item.bookingId() == booking.bookingId()
                        && item.title().equals("New booking request"));
    }

    @Test
    void equipmentIssueAndReturnStillCreateMemberNotifications() {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser admin = createUser(UserRole.ADMIN);
        var asset = equipmentRepository.findAllAssets().stream()
                .filter(item -> item.resourceId() == 38).findFirst().orElseThrow();
        BookingRequestForm form = new BookingRequestForm();
        form.setEventTitle("Equipment notification workflow check");
        form.setEventType("ACADEMIC");
        form.setPurpose("Verify existing equipment issue and return notifications.");
        form.setEventDate(LocalDate.now().plusDays(41));
        form.setStartTime(LocalTime.of(12, 0));
        form.setEndTime(LocalTime.of(13, 0));
        form.setExpectedParticipants(5);
        form.setResourceIds(java.util.List.of(asset.resourceId()));
        form.setRequestedQuantities(java.util.List.of(1));
        var booking = bookings.submit(form, student);
        bookings.transition(booking.bookingId(), BookingStatus.APPROVED, admin, "Approved", null, null, null);
        var allocation = equipment.forBooking(booking.bookingId()).getFirst();

        assertThat(equipment.issue(allocation.allocationId(), admin, LocalDateTime.now().plusHours(12))).isTrue();
        assertThat(equipment.receive(allocation.allocationId(), admin, ReturnCondition.GOOD, "Returned in good condition")).isTrue();

        assertThat(notifications.allForUser(student.getUserId()))
                .extracting(item -> item.type())
                .contains(NotificationType.EQUIPMENT_ISSUED, NotificationType.EQUIPMENT_RETURNED);
    }

    private CampusUser createUser(UserRole role) {
        String email = role.name().toLowerCase() + "+notification-" + UUID.randomUUID() + "@college.edu";
        return users.save("Notification " + role, email, passwordEncoder.encode("notification-test-password"), role,
                "Computing", role == UserRole.STUDENT ? 2 : null,
                role == UserRole.STUDENT ? "NT-" + UUID.randomUUID().toString().substring(0, 7) : null,
                role == UserRole.CLUB_REPRESENTATIVE ? "Robotics Club" : null);
    }

    private long createNotification(CampusUser recipient, String title, String eventKey) {
        notifications.create(recipient.getUserId(), title, "Message for " + title,
                NotificationType.BOOKING_APPROVED, null, "/resources", eventKey);
        return notifications.allForUser(recipient.getUserId()).stream()
                .filter(item -> item.title().equals(title)).findFirst().orElseThrow().notificationId();
    }
}
