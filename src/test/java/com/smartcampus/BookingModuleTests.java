package com.smartcampus;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.EquipmentStatus;
import com.smartcampus.enums.EventType;
import com.smartcampus.enums.ReturnCondition;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.BookingRequestForm;
import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.EquipmentRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:smartcampus-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class BookingModuleTests {
    private static final java.util.concurrent.atomic.AtomicInteger SLOT_COUNTER = new java.util.concurrent.atomic.AtomicInteger();
    @Autowired private BookingService bookingService;
    @Autowired private EquipmentAllocationService equipmentService;
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private ResourceRepository resourceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MockMvc mockMvc;

    @Test
    void studentAndClubCanSubmitRequestsAndSeeOnlyTheirOwnHistory() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser club = createUser(UserRole.CLUB_REPRESENTATIVE);
        var classroom = resourceRepository.findAll().stream().filter(r -> r.getCategory() == ResourceCategory.CLASSROOM).findFirst().orElseThrow();
        var studentRequest = bookingService.submit(form(classroom.getId(), "Student demo event"), student);
        var clubRequest = bookingService.submit(form(classroom.getId(), "Club demo event"), club);

        assertThat(studentRequest.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(bookingService.history(studentRequest.bookingId())).hasSize(1);
        assertThat(bookingService.forRequester(student.getUserId())).extracting(r -> r.bookingId()).contains(studentRequest.bookingId());
        assertThat(bookingService.forRequester(student.getUserId())).extracting(r -> r.bookingId()).doesNotContain(clubRequest.bookingId());
        mockMvc.perform(get("/student/bookings").with(user(student)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Student demo event")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Club demo event"))));
        mockMvc.perform(get("/student/bookings/new").with(user(student)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Tell us what your event needs")));
        mockMvc.perform(get("/student/bookings/" + studentRequest.bookingId()).with(user(student)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Status history")));
        mockMvc.perform(get("/student/bookings/" + clubRequest.bookingId()).with(user(student)))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/club/bookings/new").with(user(club)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("/club/bookings")));
        mockMvc.perform(get("/admin/bookings").with(user(student))).andExpect(status().isForbidden());
    }

    @Test
    void overlappingVenueRequestsAreRejectedAndAdminsCanReviewRequests() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser secondStudent = createUser(UserRole.STUDENT);
        CampusUser admin = createUser(UserRole.ADMIN);
        var room = resourceRepository.findAll().stream().filter(r -> r.getCategory() == ResourceCategory.CLASSROOM).findFirst().orElseThrow();
        BookingRequestForm form = form(room.getId(), "Overlap test");
        var submitted = bookingService.submit(form, student);
        assertThatThrownBy(() -> bookingService.submit(form, secondStudent))
                .isInstanceOf(BookingService.BookingUnavailableException.class);
        assertThatThrownBy(() -> bookingService.transition(submitted.bookingId(), BookingStatus.REJECTED,
                admin, " ", null, null, null)).isInstanceOf(IllegalArgumentException.class);
        bookingService.transition(submitted.bookingId(), BookingStatus.UNDER_REVIEW, admin, "Checking event details", null, null, null);
        bookingService.transition(submitted.bookingId(), BookingStatus.APPROVED, admin, "Room and event details confirmed", null, null, null);
        assertThat(bookingService.find(submitted.bookingId()).orElseThrow().status()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookingService.history(submitted.bookingId())).hasSize(3);
        mockMvc.perform(get("/admin/bookings").with(user(admin)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Overlap test")));
        mockMvc.perform(get("/admin/bookings/" + submitted.bookingId()).with(user(admin)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Review and decision")))
                .andExpect(content().string(containsString("Booking status history")));
    }

    @Test
    void approvedEquipmentCanBeIssuedAndReturnedWithConditionRecorded() throws Exception {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser admin = createUser(UserRole.ADMIN);
        var asset = equipmentRepository.findAllAssets().stream().filter(item -> item.resourceId() == 38).findFirst().orElseThrow();
        var booking = bookingService.submit(form(asset.resourceId(), "Equipment lifecycle"), student);
        bookingService.transition(booking.bookingId(), BookingStatus.APPROVED, admin, "Approved", null, null, null);
        var allocation = equipmentService.forBooking(booking.bookingId()).getFirst();
        assertThat(allocation.assetTag()).isNotBlank();
        assertThat(allocation.equipmentStatus()).isEqualTo(EquipmentStatus.RESERVED);
        assertThat(equipmentService.issue(allocation.allocationId(), admin, LocalDateTime.now().plusHours(12))).isTrue();
        assertThat(equipmentService.receive(allocation.allocationId(), admin, ReturnCondition.GOOD, "Returned in good condition")).isTrue();
        assertThat(equipmentService.forBooking(booking.bookingId()).getFirst().equipmentStatus()).isEqualTo(EquipmentStatus.RETURNED);
        mockMvc.perform(get("/admin/equipment").with(user(admin)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Individual equipment inventory")))
                .andExpect(content().string(containsString("ARD-")));
    }

    @Test
    void additionalInformationAndAlternativeSlotRequireRequesterResponse() {
        CampusUser student = createUser(UserRole.STUDENT);
        CampusUser admin = createUser(UserRole.ADMIN);
        var room = resourceRepository.findAll().stream().filter(r -> r.getCategory() == ResourceCategory.CLASSROOM).findFirst().orElseThrow();
        var request = bookingService.submit(form(room.getId(), "Alternative slot workflow"), student);
        bookingService.transition(request.bookingId(), BookingStatus.ADDITIONAL_INFORMATION_REQUIRED, admin,
                "Please confirm the event equipment setup.", null, null, null);
        assertThat(bookingService.provideAdditionalInformation(request.bookingId(), student, "We will bring our own laptop.").status())
                .isEqualTo(BookingStatus.UNDER_REVIEW);

        LocalDate alternativeDate = request.eventDate().plusDays(2);
        LocalTime alternativeStart = request.startTime().plusHours(2);
        LocalTime alternativeEnd = request.endTime().plusHours(2);
        bookingService.transition(request.bookingId(), BookingStatus.ALTERNATIVE_PROPOSED, admin,
                "The original time is unavailable.", alternativeDate, alternativeStart, alternativeEnd);
        assertThat(bookingService.find(request.bookingId()).orElseThrow().status()).isEqualTo(BookingStatus.ALTERNATIVE_PROPOSED);
        var accepted = bookingService.acceptAlternative(request.bookingId(), student);
        assertThat(accepted.status()).isEqualTo(BookingStatus.UNDER_REVIEW);
        assertThat(accepted.eventDate()).isEqualTo(alternativeDate);
        assertThat(accepted.startTime()).isEqualTo(alternativeStart);
        assertThat(bookingService.allowedNextStatuses(accepted.status())).contains(BookingStatus.APPROVED);
    }

    @Test
    void concurrentSubmissionsCannotReserveTheSameVenueWindowTwice() throws Exception {
        CampusUser first = createUser(UserRole.STUDENT);
        CampusUser second = createUser(UserRole.CLUB_REPRESENTATIVE);
        var room = resourceRepository.findAll().stream().filter(r -> r.getCategory() == ResourceCategory.CLASSROOM).findFirst().orElseThrow();
        BookingRequestForm slot = form(room.getId(), "Concurrent window");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var resultOne = pool.submit(() -> submitAtOnce(slot, first, ready, start));
            var resultTwo = pool.submit(() -> submitAtOnce(slot, second, ready, start));
            ready.await(); start.countDown();
            assertThat(List.of(resultOne.get(), resultTwo.get())).containsExactlyInAnyOrder(true, false);
        } finally {
            pool.shutdownNow();
        }
    }

    private boolean submitAtOnce(BookingRequestForm form, CampusUser user, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown(); start.await();
        try { bookingService.submit(form, user); return true; }
        catch (BookingService.BookingUnavailableException exception) { return false; }
    }

    private CampusUser createUser(UserRole role) {
        String email = role.name().toLowerCase() + "+" + UUID.randomUUID() + "@college.edu";
        return userRepository.save("Booking Test " + role, email, passwordEncoder.encode("testing"), role,
                "Engineering", role == UserRole.STUDENT ? 2 : null, role == UserRole.STUDENT ? "CS-" + UUID.randomUUID() : null,
                role == UserRole.CLUB_REPRESENTATIVE ? "Robotics Club" : null);
    }

    private BookingRequestForm form(long resourceId, String title) {
        BookingRequestForm form = new BookingRequestForm();
        form.setEventTitle(title);
        form.setEventType(EventType.ACADEMIC.name());
        form.setPurpose("A scheduled campus event for booking module verification.");
        int unique = SLOT_COUNTER.getAndIncrement();
        form.setEventDate(LocalDate.now().plusDays(5 + unique));
        int minute = unique % 50;
        form.setStartTime(LocalTime.of(9, minute));
        form.setEndTime(form.getStartTime().plusHours(1));
        form.setExpectedParticipants(5);
        form.setResourceIds(List.of(resourceId));
        form.setRequestedQuantities(List.of(1));
        return form;
    }
}
