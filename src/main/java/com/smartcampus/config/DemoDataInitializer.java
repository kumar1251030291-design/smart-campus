package com.smartcampus.config;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.BookingRequest;
import com.smartcampus.model.BookingRequestForm;
import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.ResourceViewRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.BookingService;
import com.smartcampus.service.EquipmentAllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Optional demo data for a hosted showcase (SMART_CAMPUS_DEMO_DATA=true).
 * Creates two student and two club demo accounts, then - only when no bookings exist yet - submits
 * sample requests and records administrator decisions through the normal services, so notifications,
 * equipment reservations and history are produced exactly as they would be in real use.
 * Dates are relative to the start-up day so the bookings are always in the future.
 */
@Component
@Order(2)
public class DemoDataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final boolean enabled;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final BookingService bookings;
    private final EquipmentAllocationService equipment;
    private final ResourceViewRepository views;
    private final JdbcTemplate jdbc;

    public DemoDataInitializer(@Value("${smartcampus.demo.enabled:false}") boolean enabled, UserRepository users,
                               PasswordEncoder encoder, BookingService bookings, EquipmentAllocationService equipment,
                               ResourceViewRepository views, JdbcTemplate jdbc) {
        this.enabled = enabled; this.users = users; this.encoder = encoder; this.bookings = bookings;
        this.equipment = equipment; this.views = views; this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        if (!enabled) return;
        CampusUser s1 = member("Student One", "student1@gmail.com", "student1", UserRole.STUDENT, 1, "STU001", null);
        CampusUser s2 = member("Student Two", "student2@gmail.com", "student2", UserRole.STUDENT, 2, "STU002", null);
        CampusUser c1 = member("Club One", "club1@gmail.com", "club001", UserRole.CLUB_REPRESENTATIVE, null, null, "Club One");
        CampusUser c2 = member("Club Two", "club2@gmail.com", "club002", UserRole.CLUB_REPRESENTATIVE, null, null, "Club Two");

        Integer existing = jdbc.queryForObject("SELECT COUNT(*) FROM booking_requests", Integer.class);
        if (existing != null && existing > 0) return;
        List<Long> adminIds = users.findActiveAdminIds();
        if (adminIds.isEmpty()) {
            log.warn("Demo bookings skipped: set SMART_CAMPUS_ADMIN_EMAIL and SMART_CAMPUS_ADMIN_PASSWORD first.");
            return;
        }
        CampusUser admin = users.findById(adminIds.getFirst()).orElseThrow();
        try {
            seedBookings(admin, s1, s2, c1, c2);
            log.info("Demo bookings created.");
        } catch (RuntimeException exception) {
            log.warn("Demo bookings could not be fully created: {}", exception.getMessage());
        }
    }

    private void seedBookings(CampusUser admin, CampusUser s1, CampusUser s2, CampusUser c1, CampusUser c2) {
        LocalDate d = LocalDate.now();
        // Student One
        long s1a = book(s1, "DSA study group", "ACADEMIC", d.plusDays(4), 10, 12, 15, "Peer study group to revise data structures and algorithms before exams.", null, 25, 1);
        long s1b = book(s1, "Python workshop practice", "WORKSHOP", d.plusDays(6), 14, 16, 25, "Hands-on Python practice session for first-year students.", null, 35, 1);
        long s1c = book(s1, "IoT mini project testing", "TECHNICAL", d.plusDays(11), 11, 13, 6, "Testing a smart-parking sensor prototype for the IoT course project.", null, 31, 1, 42, 4);
        long s1d = book(s1, "Robotics demo preparation", "TECHNICAL", d.plusDays(13), 15, 17, 4, "Assembling and calibrating robots for the tech fest demo.", null, 43, 2);
        book(s1, "Exam revision session", "ACADEMIC", d.plusDays(15), 9, 11, 30, "Class revision session led by the class representative.", null, 21, 1);
        // Student Two
        long s2a = book(s2, "ML model training session", "TECHNICAL", d.plusDays(5), 13, 16, 20, "Training image classification models for the machine learning lab assignment.", null, 33, 1);
        long s2b = book(s2, "Seminar presentation rehearsal", "ACADEMIC", d.plusDays(7), 10, 12, 40, "Final-year seminar rehearsal with faculty feedback.", null, 70, 1, 60, 1);
        long s2c = book(s2, "Drone flying practice", "OTHER", d.plusDays(9), 7, 9, 6, "Practising drone manoeuvres for an aerial photography hobby group.", null, 77, 3);
        long s2d = book(s2, "Data visualisation hackathon prep", "TECHNICAL", d.plusDays(12), 14, 17, 30, "Preparing dashboards and datasets for the Innovate 2026 hackathon.", null, 75, 1);
        long s2e = book(s2, "Spoken English practice", "ACADEMIC", d.plusDays(14), 10, 11, 20, "Weekly communication skills practice with headsets.", null, 73, 1);
        // Club One
        long c1a = book(c1, "Innovate 2026 hackathon", "TECHNICAL", d.plusDays(22), 9, 18, 90, "Day-long inter-department hackathon organised by Club One.", "Event plan: 9 AM opening, team hacking until 4 PM, judging 4-6 PM. 22 teams registered.", 68, 1, 78, 10);
        long c1b = book(c1, "Club orientation", "CLUB_EVENT", d.plusDays(10), 16, 18, 120, "Orientation for new club members with talks from seniors.", null, 63, 1);
        long c1c = book(c1, "Inter-club cultural night", "CLUB_EVENT", d.plusDays(8), 18, 21, 400, "Cultural performances by all campus clubs.", "Event plan: 12 performances, sound check from 5 PM.", 62, 1);
        long c1d = book(c1, "Guest lecture: startup funding", "ACADEMIC", d.plusDays(18), 11, 13, 70, "Guest lecture by an alumni founder on early-stage funding.", null, 65, 1, 80, 1);
        book(c1, "Weekly club meeting", "CLUB_EVENT", d.plusDays(5), 17, 18, 12, "Weekly planning meeting of the core committee.", null, 67, 1);
        // Club Two
        long c2a = book(c2, "Inter-college sports meet", "CLUB_EVENT", d.plusDays(20), 8, 14, 200, "Badminton and table tennis tournament with four visiting colleges.", "Event plan: registration 8 AM, matches 8:30 AM-1 PM, prizes 1:30 PM.", 83, 1, 79, 1);
        long c2b = book(c2, "Art and project expo", "OTHER", d.plusDays(23), 10, 17, 100, "Exhibition of student art and engineering projects.", null, 84, 1);
        long c2c = book(c2, "Open mic evening", "CLUB_EVENT", d.plusDays(15), 17, 20, 300, "Music, poetry and stand-up open mic for all students.", null, 82, 1, 61, 2);
        long c2d = book(c2, "Startup pitch practice", "WORKSHOP", d.plusDays(7), 15, 16, 8, "Mock pitch session before the incubator demo day.", null, 85, 1);
        book(c2, "Volunteer briefing", "CLUB_EVENT", d.plusDays(11), 16, 17, 50, "Briefing for volunteers of the sports meet.", null, 64, 1);

        decide(admin, s1a, BookingStatus.APPROVED, "Approved: the Tutorial Room is free for this slot and peer study groups are encouraged before exams.");
        decide(admin, s1b, BookingStatus.REJECTED, "Rejected: the Computer Programming Lab is reserved for scheduled semester practicals that afternoon. Please use the Computer Classroom or book a weekend slot.");
        alternative(admin, s1c, "The IoT Lab has a faculty demonstration that day. The same lab and ESP32 boards are free the next day at the same time.", d.plusDays(12), 11, 13);
        decide(admin, s1d, BookingStatus.ADDITIONAL_INFORMATION_REQUIRED, "Please share the name of the supervising faculty member and confirm whether the robots will be powered from mains or batteries.");
        decide(admin, s2a, BookingStatus.APPROVED, "Approved: GPU workstations are available and the session supports an assessed lab assignment.");
        decide(admin, s2b, BookingStatus.APPROVED, "Approved: Seminar Room S1 and one portable projector are reserved. Collect the projector from the library equipment desk 15 minutes early.");
        decide(admin, s2c, BookingStatus.REJECTED, "Rejected: drone flights on campus need written safety clearance from the Robotics Lab in-charge. Re-apply with the clearance letter attached.");
        decide(admin, s2d, BookingStatus.UNDER_REVIEW, "Checking Data Science Lab availability with the department timetable.");
        alternative(admin, s2e, "The Language Lab is booked for a placement test that morning. The lab is free the same day from 2 PM.", d.plusDays(14), 14, 15);
        decide(admin, c1a, BookingStatus.APPROVED, "Approved: Innovation Centre and 10 loaner laptops reserved. The event plan meets the large-event guidelines.");
        decide(admin, c1b, BookingStatus.APPROVED, "Approved: Mini Auditorium reserved. Please keep the aisles clear and end on time.");
        alternative(admin, c1c, "The Main Auditorium sound system is under maintenance that day (see announcements). The auditorium is free eight days later, 6-9 PM.", d.plusDays(16), 18, 21);
        decide(admin, c1d, BookingStatus.REJECTED, "Rejected: the Conference Hall is reserved for the accreditation committee visit that day. Consider the Smart Conference Room or Seminar Hall.");
        decide(admin, c2a, BookingStatus.APPROVED, "Approved: Sports Complex Hall and a portable PA system are reserved. Share the visiting team list with security two days before.");
        decide(admin, c2b, BookingStatus.ADDITIONAL_INFORMATION_REQUIRED, "Events for 100 or more visitors need an event plan. Please add the layout of exhibits and the volunteer schedule.");
        decide(admin, c2c, BookingStatus.REJECTED, "Rejected: the Open Air Theatre is closed that evening for the convocation rehearsal. Please choose another date.");
        decide(admin, c2d, BookingStatus.APPROVED, "Approved: Startup Incubation Pod reserved for your pitch practice.");

        bookings.acceptAlternative(s2e, s2);
        decide(admin, s2e, BookingStatus.APPROVED, "Approved for the accepted alternative slot (2 PM - 3 PM).");
        bookings.provideAdditionalInformation(c2b, c2, "Exhibit layout: 30 student stalls along the gallery walls, 10 project tables in the centre. Six volunteers will manage entry and visitor flow in two shifts.");
        bookings.acceptAlternative(c1c, c1);
        equipment.forBooking(s2b).stream().findFirst()
                .ifPresent(allocation -> equipment.issue(allocation.allocationId(), admin, d.plusDays(7).atTime(12, 30)));

        for (long id : List.of(74L, 28L, 75L, 33L)) views.record(s1.getUserId(), id);
        for (long id : List.of(35L, 73L, 60L, 70L)) views.record(s2.getUserId(), id);
        for (long id : List.of(63L, 82L, 62L, 78L, 68L)) views.record(c1.getUserId(), id);
        for (long id : List.of(85L, 84L, 79L, 83L)) views.record(c2.getUserId(), id);
    }

    private CampusUser member(String name, String email, String password, UserRole role, Integer year, String roll, String club) {
        return users.findByEmail(email).orElseGet(() -> users.save(name, email, encoder.encode(password), role,
                "Computer Engineering", year, roll, club));
    }

    private long book(CampusUser requester, String title, String type, LocalDate date, int startHour, int endHour,
                      int participants, String purpose, String supporting, long... resourceAndQuantity) {
        BookingRequestForm form = new BookingRequestForm();
        form.setEventTitle(title);
        form.setEventType(type);
        form.setPurpose(purpose);
        form.setSupportingInformation(supporting);
        form.setEventDate(date);
        form.setStartTime(LocalTime.of(startHour, 0));
        form.setEndTime(LocalTime.of(endHour, 0));
        form.setExpectedParticipants(participants);
        List<Long> ids = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        for (int i = 0; i < resourceAndQuantity.length; i += 2) {
            ids.add(resourceAndQuantity[i]);
            quantities.add((int) resourceAndQuantity[i + 1]);
        }
        form.setResourceIds(ids);
        form.setRequestedQuantities(quantities);
        BookingRequest request = bookings.submit(form, requester);
        return request.bookingId();
    }

    private void decide(CampusUser admin, long id, BookingStatus status, String note) {
        bookings.transition(id, status, admin, note, null, null, null);
    }

    private void alternative(CampusUser admin, long id, String note, LocalDate date, int startHour, int endHour) {
        bookings.transition(id, BookingStatus.ALTERNATIVE_PROPOSED, admin, note, date,
                LocalTime.of(startHour, 0), LocalTime.of(endHour, 0));
    }
}
