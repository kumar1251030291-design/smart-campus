# Smart Campus

A beginner-friendly Java 21 and Spring Boot foundation for the Smart Campus college project.

## Requirements

- Java Development Kit (JDK) 21
- Visual Studio Code with the recommended Java extensions

Maven is provided by the Maven Wrapper, so a separate Maven installation is not required.

## Run the application

First check that `java -version` reports Java 21. If Windows is selecting another Java version, set `JAVA_HOME` to your JDK 21 installation and put `%JAVA_HOME%\bin` at the start of `PATH` for that terminal.

In the project folder, run:

```powershell
./mvnw.cmd spring-boot:run
```

Then open <http://localhost:8080> in a browser.

To run the complete Maven test suite:

```powershell
./mvnw.cmd test
```

The local H2 database is stored in `data/smartcampus.mv.db`. The H2 console is available at <http://localhost:8080/h2-console> while the application is running. Use JDBC URL `jdbc:h2:file:./data/smartcampus`, username `sa`, and leave the password blank.

## Resource catalogue

Open <http://localhost:8080/resources> to browse the campus catalogue. Search by resource name and filter by category or availability. The app initializes `campus_resources` from `schema.sql` and `data.sql`; the fixed sample IDs make the 85 sample rows (15 classrooms, 19 laboratories, 35 equipment resources and 16 facilities) safe to reload on every startup.

## Accounts and roles

- `/register` creates a Student or Club Representative account. Student accounts require an academic year and roll number; club accounts require a club name.
- `/login` is for students and club representatives. `/login/admin` is the separate administrator login route.
- Successful login sends administrators to `/admin/dashboard`, students to `/student/dashboard`, and club representatives to `/club/dashboard` based on their authenticated role.
- Authentication uses Spring Security form login, BCrypt password hashes, an HTTP session, and CSRF protection. The `campus_users` table stores role and account status. Public registration rejects administrator roles. Passwords must be 6–72 characters and fit BCrypt's 72-byte input limit.
- Student and club representatives can view and edit permitted fields from `/student/profile` and `/club/profile`. Email and roll number are read-only; role, account status, and permissions cannot be changed there.
- Member password changes are available from the profile page. The current password is verified and the new password is BCrypt-hashed; new passwords must be 6 to 72 characters and fit BCrypt's 72-byte input limit.
- `/admin/**` requires `ADMIN`, `/student/**` requires `STUDENT`, and `/club/**` requires `CLUB_REPRESENTATIVE`. The resource catalogue stays public.
- The shared navigation shows the signed-in name and a POST logout button when a session is active.
- The administrator area is at `/admin/dashboard`. Its resource distribution, student/club comparison, and account status charts use current H2 records.
- `/admin/resources` supports resource search, category/availability filters, create, edit, availability changes, and confirmed deletion. The catalogue uses the existing `Resource`, `Classroom`, `Laboratory`, `Equipment`, and `Facility` classes.
- `/admin/users` supports name/email search, role/status filters, and password-free user detail views. Administrators can activate or deactivate accounts; an admin cannot deactivate their own account. Deactivation is checked on subsequent requests and ends an existing session.
- The admin sidebar links to Dashboard, Resource Management, User Management, Booking Requests, Equipment Tracking, Analytics, and Logout. Analytics is part of the dashboard.

## Booking requests and equipment

- Students and club representatives submit requests from their dashboard or at `/student/bookings/new` and `/club/bookings/new`. Each request can include up to 10 resources, an event purpose, event type, time, participant estimate, and supporting information.
- Requesters can see their own request history, cancel eligible requests, answer an information request, and accept a proposed alternative time. Request detail routes check the owning account.
- Administrators review requests at `/admin/bookings`, then move requests through Pending, Under Review, Additional Information Required, Alternative Proposed, Approved, Rejected, Cancelled, and Completed. A reason is required for rejection and a note is required when asking for more information. The database records every transition and actor.
- Availability is calculated from the stored resources and overlapping request records. Classrooms and event facilities are exclusive for a time slot. Equipment and lab workstations use quantity capacity; room and lab participant limits are checked. Availability is rechecked at approval while the involved resource rows are locked in a transaction.
- Approving a request reserves equipment. `/admin/equipment` and each booking detail page support issuing and receiving equipment, including condition notes. Seeded tag examples (Arduino boards, Raspberry Pi kits, projectors and microphones) are in `equipment_assets`; resources without individual tags use quantity allocations.
- Four review aids are provided: multi-resource time-window suggestions, historical weekday/time suggestions when at least five completed/approved examples exist, a transparent configurable priority score, and 30-day department/club usage shares. They are advisory: approved requests are never shifted or cancelled automatically.
- Tables are additive and initialized with `CREATE TABLE IF NOT EXISTS`; no existing user/resource rows are deleted or reset. The demo asset rows are idempotent `MERGE` seeds. No sample booking requests are inserted.

Priority and fairness weights can be adjusted in `application.properties` using `smartcampus.priority.*` and `smartcampus.fairness.threshold` (default 0.40). Review these values with campus policy before using the demo data to make real allocation decisions.

## Student and club portals

- `/student/dashboard` and `/club/dashboard` show statistics derived from the signed-in account's own booking requests and equipment allocations. Students and club representatives see separate dashboards based on their role.
- `/resources` supports search across resource names and descriptions, resource collection/category filtering, availability filtering, and capacity range filtering. `/resources/{id}` shows details for a catalogue item. Signed-in students and club representatives can start a prefilled request from a resource page.
- `/student/bookings` and `/club/bookings` show only the current account's requests, with status and upcoming/completed filters. Request detail pages include the status history and any equipment issue/return records.
- `/student/equipment` and `/club/equipment` show the current account's allocated equipment, due/overdue state, and returned-item condition history.
- `/student/profile` and `/club/profile` show only the authenticated member's account details and allow updates to name, department, academic year, or club name as appropriate. Members can change their password after confirming their current password.
- Availability feedback on the request form is calculated by the server from the current catalogue and request records; the request is checked again when submitted. A requester cannot supply another user's identity through the form.
- The portals do not send email/SMS or collect a user-facing resource exploration history. Those capabilities are not enabled.
- `/admin/**`, `/student/**`, and `/club/**` remain protected by their respective roles. Booking detail pages and dashboard summaries are scoped to the signed-in user; catalogue browsing remains public.

### Initialize a development administrator

There is no default admin password. Set both environment variables before the first application startup; the initializer creates one administrator row and stores only its BCrypt hash. It does not replace an existing administrator password, and it will not elevate a student or club account.

In PowerShell:

```powershell
$env:SMART_CAMPUS_ADMIN_EMAIL = "admin@college.edu"
$env:SMART_CAMPUS_ADMIN_PASSWORD = "choose-a-private-password-of-6-or-more-characters"
./mvnw.cmd spring-boot:run
```

Use those values at `/login/admin`. Choose a private password for your local environment and do not add it to source control. Clear the environment variables after the initial setup if you prefer; the database account remains. To create another demo admin, configure a different email and restart. If a configured email already belongs to a non-admin account, startup stops with an error rather than changing its role.

New accounts are written to the same persistent local H2 database at `data/smartcampus.mv.db`. The `campus_users` schema is created idempotently alongside the existing resource table.

## Project layout

Java code lives under `src/main/java/com/smartcampus` and is grouped into `config`, `controller`, `model`, `service`, `repository`, and `enums`. Page templates are in `src/main/resources/templates`; CSS and other static files are in `src/main/resources/static`.

## Announcements, recently explored and decision letters

- `/announcements` lists campus notices (seeded once into `campus_announcements` with images under `static/images/announcements`). The latest three appear on the student, club and admin dashboards.
- Opening a resource page while signed in as a student or club representative records it in `resource_views`; the dashboard's "Recently explored" panel shows the last five distinct resources.
- Every booking has a PDF decision letter (status, administrator reason, any suggested alternative time and the full status history): `/student/bookings/{id}/decision.pdf`, `/club/bookings/{id}/decision.pdf` (own bookings only) and `/admin/bookings/{id}/decision.pdf`. Links appear on the dashboard, My bookings, booking details, notification details and the admin booking pages. Generated with OpenPDF.
- Club representatives and administrators can post announcements with the "+" button on the announcement panels (`/announcements/new`); students and visitors can only read them.
- `PostgresCompatibilityTests` runs the schema, demo data and main pages on a real (embedded) PostgreSQL, the database used by the hosted site.
