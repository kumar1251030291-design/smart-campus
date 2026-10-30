-- Fixed IDs and H2 MERGE make this sample data safe to load on every startup.
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (1, 'Classroom A101', 'Bright lecture room with a wall display and whiteboard.', 'Block A, Floor 1', 60, 'CLASSROOM', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (2, 'Classroom A102', 'Flexible classroom with movable tables for group work.', 'Block A, Floor 1', 42, 'CLASSROOM', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (3, 'Classroom B201', 'Lecture room equipped for presentations and hybrid teaching.', 'Block B, Floor 2', 75, 'CLASSROOM', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (4, 'Classroom B202', 'Seminar-style classroom with tiered seating.', 'Block B, Floor 2', 48, 'CLASSROOM', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (5, 'Classroom C301', 'Quiet project classroom currently receiving equipment upgrades.', 'Block C, Floor 3', 36, 'CLASSROOM', 'MAINTENANCE');

MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (6, 'Computer Lab 1', 'Desktop workstations with development tools for coursework.', 'Technology Block, Floor 1', 32, 'LABORATORY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (7, 'Electronics Lab', 'Benches for circuit design, testing and electronics practice.', 'Engineering Block, Floor 2', 24, 'LABORATORY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (8, 'Robotics Lab', 'Student robotics workspace with assembly benches and test area.', 'Engineering Block, Floor 3', 20, 'LABORATORY', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (9, 'NVIDIA GPU Lab', 'GPU workstations for supervised graphics and machine learning coursework.', 'Technology Block, Floor 3', 18, 'LABORATORY', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (10, 'Networking Lab', 'Routers, switches and practice stations for networking classes.', 'Technology Block, Floor 2', 26, 'LABORATORY', 'AVAILABLE');

MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (11, 'Arduino Kits', 'Microcontroller starter kits with sensors and jumper wires.', 'Central Equipment Desk', 30, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (12, 'Raspberry Pi Kits', 'Single-board computer kits with power supplies and cases.', 'Central Equipment Desk', 15, 'EQUIPMENT', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (13, 'Projectors', 'Portable HD projectors for classroom and club presentations.', 'Library Equipment Desk', 8, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (14, 'Wireless Microphones', 'Handheld wireless microphones for talks and campus events.', 'Activities Office', 12, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (15, '3D Printer', 'FDM printer for supervised prototypes and design projects.', 'Makerspace, Engineering Block', 2, 'EQUIPMENT', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (16, 'DSLR Camera Kits', 'Camera kits with lenses, batteries and carrying cases.', 'Media Centre', 4, 'EQUIPMENT', 'UNAVAILABLE');

MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (17, 'Auditorium', 'Main event hall with stage, projection screen and fixed seating.', 'Main Building, Ground Floor', 300, 'FACILITY', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (18, 'Seminar Hall', 'Medium-sized hall for talks, workshops and student events.', 'Academic Block, Ground Floor', 110, 'FACILITY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (19, 'Conference Room', 'Meeting room with a display and video-conference equipment.', 'Administration Block, Floor 2', 20, 'FACILITY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (20, 'Student Activity Hall', 'Open multipurpose hall for club gatherings and campus activities.', 'Student Centre, Ground Floor', 140, 'FACILITY', 'AVAILABLE');

-- Additional classroom spaces (IDs 21-27).
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (21, 'Standard Classroom', 'General-purpose teaching room with a whiteboard and flexible seating.', 'Academic Block, Floor 1', 40, 'CLASSROOM', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (22, 'Smart Classroom', 'Teaching room with an interactive display, lecture capture and hybrid meeting equipment.', 'Academic Block, Floor 2', 48, 'CLASSROOM', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (23, 'Digital Classroom', 'Connected learning room with student charging points and a large presentation display.', 'Digital Learning Block, Floor 1', 36, 'CLASSROOM', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (24, 'Computer Classroom', 'Classroom with desktop computers for guided software and computing lessons.', 'Technology Block, Floor 1', 30, 'CLASSROOM', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (25, 'Tutorial Room', 'Small-group room suited to tutorials, mentoring and project discussions.', 'Library Annex, Floor 2', 18, 'CLASSROOM', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (26, 'Lecture Hall', 'Tiered teaching hall with a projection system and lecture capture.', 'Main Academic Block, Floor 3', 120, 'CLASSROOM', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (27, 'Interactive Learning Room', 'Collaborative room with writable walls and movable group tables.', 'Innovation Block, Floor 1', 32, 'CLASSROOM', 'AVAILABLE');

-- Specialized laboratories and learning facilities (IDs 28-37).
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (28, 'NVIDIA AI/GPU Lab', 'GPU-equipped lab for supervised artificial intelligence, graphics and parallel computing work.', 'Technology Block, Floor 4', 24, 'LABORATORY', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (29, 'Liquid Galaxy Lab', 'Immersive multi-screen visualization lab for geospatial and data exploration projects.', 'Innovation Block, Floor 2', 12, 'LABORATORY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (30, 'Robotics and Automation Lab', 'Lab for robot assembly, motion control and industrial automation exercises.', 'Engineering Block, Floor 3', 22, 'LABORATORY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (31, 'Internet of Things (IoT) Lab', 'Connected-device lab with development stations for sensor and network projects.', 'Engineering Block, Floor 2', 24, 'LABORATORY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (32, 'Embedded Systems Lab', 'Workstations for microcontroller programming and embedded hardware testing.', 'Engineering Block, Floor 2', 20, 'LABORATORY', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (33, 'Artificial Intelligence and Machine Learning Lab', 'Computing lab for supervised AI, machine learning and data science coursework.', 'Technology Block, Floor 4', 28, 'LABORATORY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (34, 'Electronics and Communication Lab', 'Lab benches for communication circuits, signal testing and electronics experiments.', 'Engineering Block, Floor 2', 26, 'LABORATORY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (35, 'Computer Programming Lab', 'Computer lab for programming classes, software practice and supervised coding sessions.', 'Technology Block, Floor 2', 34, 'LABORATORY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (36, 'Innovation and Research Lab', 'Shared research space for supervised student prototypes and interdisciplinary projects.', 'Innovation Block, Floor 3', 16, 'LABORATORY', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (37, '3D Printing and Fabrication Lab', 'Fabrication lab with supervised additive manufacturing and prototyping work areas.', 'Makerspace, Engineering Block', 14, 'LABORATORY', 'AVAILABLE');

-- Engineering and presentation equipment (IDs 38-61).
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (38, 'Arduino Uno', 'Uno development boards with USB cables for introductory microcontroller projects.', 'Electronics Equipment Desk', 24, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (39, 'Arduino Mega', 'Mega development boards for projects that need additional digital and analog pins.', 'Electronics Equipment Desk', 16, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (40, 'Raspberry Pi 4', 'Raspberry Pi 4 single-board computers with power supplies and cases.', 'Embedded Systems Equipment Desk', 18, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (41, 'Raspberry Pi 5', 'Raspberry Pi 5 computers for higher-performance embedded and computing projects.', 'Embedded Systems Equipment Desk', 12, 'EQUIPMENT', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (42, 'ESP32 Development Boards', 'Wi-Fi and Bluetooth-enabled boards for connected-device coursework.', 'IoT Equipment Desk', 32, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (43, 'Robotics Kits', 'Programmable robotics kits with motors, controllers and assembly parts.', 'Robotics Lab Store', 12, 'EQUIPMENT', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (44, 'Robotic Arms', 'Desktop robotic arms for supervised motion control and automation exercises.', 'Robotics Lab Store', 4, 'EQUIPMENT', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (45, 'IoT Development Kits', 'Connected-device kits with controller boards, sensors and wireless modules.', 'IoT Equipment Desk', 18, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (46, 'Sensors and Sensor Kits', 'Assorted temperature, distance, motion and environmental sensor kits.', 'IoT Equipment Desk', 28, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (47, 'FPGA Development Boards', 'FPGA boards for digital logic design and hardware description language practice.', 'Digital Systems Equipment Desk', 14, 'EQUIPMENT', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (48, 'PLC Training Kits', 'Programmable logic controller training kits for supervised automation lessons.', 'Automation Lab Store', 8, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (49, 'Oscilloscopes', 'Bench oscilloscopes for observing and measuring electronic signals.', 'Electronics Lab Store', 10, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (50, 'Digital Multimeters', 'Digital meters for supervised voltage, current and resistance measurements.', 'Electronics Lab Store', 24, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (51, 'Function Generators', 'Signal generators for electronics experiments and circuit testing.', 'Electronics Lab Store', 8, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (52, 'DC Power Supplies', 'Adjustable bench power supplies for electronics and embedded systems work.', 'Electronics Lab Store', 12, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (53, 'Soldering Stations', 'Temperature-controlled soldering stations for supervised circuit assembly.', 'Electronics Lab Store', 14, 'EQUIPMENT', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (54, 'Logic Analyzers', 'Digital logic analyzers for observing and debugging communication signals.', 'Digital Systems Equipment Desk', 6, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (55, 'FDM 3D Printers', 'FDM printers for supervised prototypes and student design projects.', 'Makerspace, Engineering Block', 4, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (56, 'CNC Machines', 'Computer-controlled milling machines for supervised fabrication projects.', 'Fabrication Workshop', 2, 'EQUIPMENT', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (57, 'Laser Cutting Machines', 'Enclosed laser cutters for supervised sheet material prototyping.', 'Fabrication Workshop', 1, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (58, 'GPU Workstations', 'High-performance workstations for supervised graphics and compute workloads.', 'Technology Equipment Desk', 10, 'EQUIPMENT', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (59, 'VR Headsets', 'Virtual reality headsets for supervised visualization and interaction projects.', 'Media Centre Equipment Desk', 10, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (60, 'Portable Projectors', 'Portable HD projectors for classrooms, seminars and student presentations.', 'Library Equipment Desk', 6, 'EQUIPMENT', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (61, 'Microphones and Speakers', 'Portable microphone and speaker sets for talks and small campus events.', 'Activities Office', 8, 'EQUIPMENT', 'AVAILABLE');

-- Additional event and collaboration spaces (IDs 62-69).
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (62, 'Main Auditorium', 'Large event venue with stage, control booth and fixed audience seating.', 'Main Building, Ground Floor', 500, 'FACILITY', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (63, 'Mini Auditorium', 'Compact auditorium for student talks, screenings and department events.', 'Academic Block, Ground Floor', 150, 'FACILITY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (64, 'Small Seminar Hall', 'Smaller seminar venue with flexible seating, projection and presentation equipment.', 'Academic Block, Floor 1', 90, 'FACILITY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (65, 'Conference Hall', 'Formal conference space for department meetings and campus workshops.', 'Administration Block, Floor 1', 80, 'FACILITY', 'LIMITED');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (66, 'Smart Conference Room', 'Conference room with interactive display and hybrid meeting equipment.', 'Administration Block, Floor 2', 24, 'FACILITY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (67, 'Meeting Room', 'Small meeting room for student groups and project discussions.', 'Student Centre, Floor 1', 16, 'FACILITY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (68, 'Innovation Centre', 'Collaborative centre with flexible project and presentation areas.', 'Innovation Block, Ground Floor', 100, 'FACILITY', 'AVAILABLE');
MERGE INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status) KEY(id)
VALUES (69, 'Project Presentation Room', 'Presentation room with display equipment and audience seating for project reviews.', 'Technology Block, Floor 1', 40, 'FACILITY', 'AVAILABLE');

-- Small identifiable demo inventory for equipment assignment; no booking or allocation records are seeded.
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('ARD-001', 38, 'AVAILABLE', 'Arduino Uno board');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('ARD-002', 38, 'AVAILABLE', 'Arduino Uno board');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('ARD-003', 38, 'AVAILABLE', 'Arduino Uno board');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('ARD-004', 38, 'AVAILABLE', 'Arduino Uno board');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('ARD-005', 38, 'AVAILABLE', 'Arduino Uno board');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('RPI4-001', 40, 'AVAILABLE', 'Raspberry Pi 4 kit');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('RPI4-002', 40, 'AVAILABLE', 'Raspberry Pi 4 kit');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('RPI4-003', 40, 'AVAILABLE', 'Raspberry Pi 4 kit');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('RPI4-004', 40, 'AVAILABLE', 'Raspberry Pi 4 kit');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('PROJ-001', 13, 'AVAILABLE', 'Portable projector');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('PROJ-002', 13, 'AVAILABLE', 'Portable projector');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('PROJ-003', 13, 'AVAILABLE', 'Portable projector');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('MIC-001', 14, 'AVAILABLE', 'Wireless microphone');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('MIC-002', 14, 'AVAILABLE', 'Wireless microphone');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('MIC-003', 14, 'AVAILABLE', 'Wireless microphone');
MERGE INTO equipment_assets (asset_tag, resource_id, asset_status, notes) KEY(asset_tag) VALUES ('MIC-004', 14, 'AVAILABLE', 'Wireless microphone');
