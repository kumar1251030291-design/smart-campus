-- Stable sample IDs are inserted only when absent; existing records are never updated.
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 1, 'Classroom A101', 'Bright lecture room with a wall display and whiteboard.', 'Block A, Floor 1', 60, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 1);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 2, 'Classroom A102', 'Flexible classroom with movable tables for group work.', 'Block A, Floor 1', 42, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 2);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 3, 'Classroom B201', 'Lecture room equipped for presentations and hybrid teaching.', 'Block B, Floor 2', 75, 'CLASSROOM', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 3);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 4, 'Classroom B202', 'Seminar-style classroom with tiered seating.', 'Block B, Floor 2', 48, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 4);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 5, 'Classroom C301', 'Quiet project classroom currently receiving equipment upgrades.', 'Block C, Floor 3', 36, 'CLASSROOM', 'MAINTENANCE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 5);

INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 6, 'Computer Lab 1', 'Desktop workstations with development tools for coursework.', 'Technology Block, Floor 1', 32, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 6);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 7, 'Electronics Lab', 'Benches for circuit design, testing and electronics practice.', 'Engineering Block, Floor 2', 24, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 7);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 8, 'Robotics Lab', 'Student robotics workspace with assembly benches and test area.', 'Engineering Block, Floor 3', 20, 'LABORATORY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 8);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 9, 'NVIDIA GPU Lab', 'GPU workstations for supervised graphics and machine learning coursework.', 'Technology Block, Floor 3', 18, 'LABORATORY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 9);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 10, 'Networking Lab', 'Routers, switches and practice stations for networking classes.', 'Technology Block, Floor 2', 26, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 10);

INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 11, 'Arduino Kits', 'Microcontroller starter kits with sensors and jumper wires.', 'Central Equipment Desk', 30, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 11);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 12, 'Raspberry Pi Kits', 'Single-board computer kits with power supplies and cases.', 'Central Equipment Desk', 15, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 12);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 13, 'Projectors', 'Portable HD projectors for classroom and club presentations.', 'Library Equipment Desk', 8, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 13);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 14, 'Wireless Microphones', 'Handheld wireless microphones for talks and campus events.', 'Activities Office', 12, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 14);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 15, '3D Printer', 'FDM printer for supervised prototypes and design projects.', 'Makerspace, Engineering Block', 2, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 15);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 16, 'DSLR Camera Kits', 'Camera kits with lenses, batteries and carrying cases.', 'Media Centre', 4, 'EQUIPMENT', 'UNAVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 16);

INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 17, 'Auditorium', 'Main event hall with stage, projection screen and fixed seating.', 'Main Building, Ground Floor', 300, 'FACILITY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 17);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 18, 'Seminar Hall', 'Medium-sized hall for talks, workshops and student events.', 'Academic Block, Ground Floor', 110, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 18);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 19, 'Conference Room', 'Meeting room with a display and video-conference equipment.', 'Administration Block, Floor 2', 20, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 19);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 20, 'Student Activity Hall', 'Open multipurpose hall for club gatherings and campus activities.', 'Student Centre, Ground Floor', 140, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 20);

-- Additional classroom spaces (IDs 21-27).
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 21, 'Standard Classroom', 'General-purpose teaching room with a whiteboard and flexible seating.', 'Academic Block, Floor 1', 40, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 21);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 22, 'Smart Classroom', 'Teaching room with an interactive display, lecture capture and hybrid meeting equipment.', 'Academic Block, Floor 2', 48, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 22);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 23, 'Digital Classroom', 'Connected learning room with student charging points and a large presentation display.', 'Digital Learning Block, Floor 1', 36, 'CLASSROOM', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 23);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 24, 'Computer Classroom', 'Classroom with desktop computers for guided software and computing lessons.', 'Technology Block, Floor 1', 30, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 24);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 25, 'Tutorial Room', 'Small-group room suited to tutorials, mentoring and project discussions.', 'Library Annex, Floor 2', 18, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 25);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 26, 'Lecture Hall', 'Tiered teaching hall with a projection system and lecture capture.', 'Main Academic Block, Floor 3', 120, 'CLASSROOM', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 26);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 27, 'Interactive Learning Room', 'Collaborative room with writable walls and movable group tables.', 'Innovation Block, Floor 1', 32, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 27);

-- Specialized laboratories and learning facilities (IDs 28-37).
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 28, 'NVIDIA AI/GPU Lab', 'GPU-equipped lab for supervised artificial intelligence, graphics and parallel computing work.', 'Technology Block, Floor 4', 24, 'LABORATORY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 28);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 29, 'Liquid Galaxy Lab', 'Immersive multi-screen visualization lab for geospatial and data exploration projects.', 'Innovation Block, Floor 2', 12, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 29);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 30, 'Robotics and Automation Lab', 'Lab for robot assembly, motion control and industrial automation exercises.', 'Engineering Block, Floor 3', 22, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 30);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 31, 'Internet of Things (IoT) Lab', 'Connected-device lab with development stations for sensor and network projects.', 'Engineering Block, Floor 2', 24, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 31);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 32, 'Embedded Systems Lab', 'Workstations for microcontroller programming and embedded hardware testing.', 'Engineering Block, Floor 2', 20, 'LABORATORY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 32);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 33, 'Artificial Intelligence and Machine Learning Lab', 'Computing lab for supervised AI, machine learning and data science coursework.', 'Technology Block, Floor 4', 28, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 33);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 34, 'Electronics and Communication Lab', 'Lab benches for communication circuits, signal testing and electronics experiments.', 'Engineering Block, Floor 2', 26, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 34);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 35, 'Computer Programming Lab', 'Computer lab for programming classes, software practice and supervised coding sessions.', 'Technology Block, Floor 2', 34, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 35);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 36, 'Innovation and Research Lab', 'Shared research space for supervised student prototypes and interdisciplinary projects.', 'Innovation Block, Floor 3', 16, 'LABORATORY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 36);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 37, '3D Printing and Fabrication Lab', 'Fabrication lab with supervised additive manufacturing and prototyping work areas.', 'Makerspace, Engineering Block', 14, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 37);

-- Engineering and presentation equipment (IDs 38-61).
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 38, 'Arduino Uno', 'Uno development boards with USB cables for introductory microcontroller projects.', 'Electronics Equipment Desk', 24, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 38);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 39, 'Arduino Mega', 'Mega development boards for projects that need additional digital and analog pins.', 'Electronics Equipment Desk', 16, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 39);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 40, 'Raspberry Pi 4', 'Raspberry Pi 4 single-board computers with power supplies and cases.', 'Embedded Systems Equipment Desk', 18, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 40);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 41, 'Raspberry Pi 5', 'Raspberry Pi 5 computers for higher-performance embedded and computing projects.', 'Embedded Systems Equipment Desk', 12, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 41);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 42, 'ESP32 Development Boards', 'Wi-Fi and Bluetooth-enabled boards for connected-device coursework.', 'IoT Equipment Desk', 32, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 42);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 43, 'Robotics Kits', 'Programmable robotics kits with motors, controllers and assembly parts.', 'Robotics Lab Store', 12, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 43);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 44, 'Robotic Arms', 'Desktop robotic arms for supervised motion control and automation exercises.', 'Robotics Lab Store', 4, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 44);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 45, 'IoT Development Kits', 'Connected-device kits with controller boards, sensors and wireless modules.', 'IoT Equipment Desk', 18, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 45);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 46, 'Sensors and Sensor Kits', 'Assorted temperature, distance, motion and environmental sensor kits.', 'IoT Equipment Desk', 28, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 46);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 47, 'FPGA Development Boards', 'FPGA boards for digital logic design and hardware description language practice.', 'Digital Systems Equipment Desk', 14, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 47);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 48, 'PLC Training Kits', 'Programmable logic controller training kits for supervised automation lessons.', 'Automation Lab Store', 8, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 48);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 49, 'Oscilloscopes', 'Bench oscilloscopes for observing and measuring electronic signals.', 'Electronics Lab Store', 10, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 49);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 50, 'Digital Multimeters', 'Digital meters for supervised voltage, current and resistance measurements.', 'Electronics Lab Store', 24, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 50);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 51, 'Function Generators', 'Signal generators for electronics experiments and circuit testing.', 'Electronics Lab Store', 8, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 51);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 52, 'DC Power Supplies', 'Adjustable bench power supplies for electronics and embedded systems work.', 'Electronics Lab Store', 12, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 52);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 53, 'Soldering Stations', 'Temperature-controlled soldering stations for supervised circuit assembly.', 'Electronics Lab Store', 14, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 53);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 54, 'Logic Analyzers', 'Digital logic analyzers for observing and debugging communication signals.', 'Digital Systems Equipment Desk', 6, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 54);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 55, 'FDM 3D Printers', 'FDM printers for supervised prototypes and student design projects.', 'Makerspace, Engineering Block', 4, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 55);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 56, 'CNC Machines', 'Computer-controlled milling machines for supervised fabrication projects.', 'Fabrication Workshop', 2, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 56);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 57, 'Laser Cutting Machines', 'Enclosed laser cutters for supervised sheet material prototyping.', 'Fabrication Workshop', 1, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 57);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 58, 'GPU Workstations', 'High-performance workstations for supervised graphics and compute workloads.', 'Technology Equipment Desk', 10, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 58);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 59, 'VR Headsets', 'Virtual reality headsets for supervised visualization and interaction projects.', 'Media Centre Equipment Desk', 10, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 59);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 60, 'Portable Projectors', 'Portable HD projectors for classrooms, seminars and student presentations.', 'Library Equipment Desk', 6, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 60);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 61, 'Microphones and Speakers', 'Portable microphone and speaker sets for talks and small campus events.', 'Activities Office', 8, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 61);

-- Additional event and collaboration spaces (IDs 62-69).
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 62, 'Main Auditorium', 'Large event venue with stage, control booth and fixed audience seating.', 'Main Building, Ground Floor', 500, 'FACILITY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 62);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 63, 'Mini Auditorium', 'Compact auditorium for student talks, screenings and department events.', 'Academic Block, Ground Floor', 150, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 63);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 64, 'Small Seminar Hall', 'Smaller seminar venue with flexible seating, projection and presentation equipment.', 'Academic Block, Floor 1', 90, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 64);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 65, 'Conference Hall', 'Formal conference space for department meetings and campus workshops.', 'Administration Block, Floor 1', 80, 'FACILITY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 65);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 66, 'Smart Conference Room', 'Conference room with interactive display and hybrid meeting equipment.', 'Administration Block, Floor 2', 24, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 66);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 67, 'Meeting Room', 'Small meeting room for student groups and project discussions.', 'Student Centre, Floor 1', 16, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 67);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 68, 'Innovation Centre', 'Collaborative centre with flexible project and presentation areas.', 'Innovation Block, Ground Floor', 100, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 68);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 69, 'Project Presentation Room', 'Presentation room with display equipment and audience seating for project reviews.', 'Technology Block, Floor 1', 40, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 69);

-- Small identifiable demo inventory for equipment assignment; no booking or allocation records are seeded.
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'ARD-001', 38, 'AVAILABLE', 'Arduino Uno board' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'ARD-001');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'ARD-002', 38, 'AVAILABLE', 'Arduino Uno board' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'ARD-002');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'ARD-003', 38, 'AVAILABLE', 'Arduino Uno board' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'ARD-003');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'ARD-004', 38, 'AVAILABLE', 'Arduino Uno board' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'ARD-004');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'ARD-005', 38, 'AVAILABLE', 'Arduino Uno board' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'ARD-005');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'RPI4-001', 40, 'AVAILABLE', 'Raspberry Pi 4 kit' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'RPI4-001');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'RPI4-002', 40, 'AVAILABLE', 'Raspberry Pi 4 kit' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'RPI4-002');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'RPI4-003', 40, 'AVAILABLE', 'Raspberry Pi 4 kit' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'RPI4-003');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'RPI4-004', 40, 'AVAILABLE', 'Raspberry Pi 4 kit' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'RPI4-004');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'PROJ-001', 13, 'AVAILABLE', 'Portable projector' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'PROJ-001');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'PROJ-002', 13, 'AVAILABLE', 'Portable projector' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'PROJ-002');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'PROJ-003', 13, 'AVAILABLE', 'Portable projector' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'PROJ-003');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'MIC-001', 14, 'AVAILABLE', 'Wireless microphone' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'MIC-001');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'MIC-002', 14, 'AVAILABLE', 'Wireless microphone' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'MIC-002');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'MIC-003', 14, 'AVAILABLE', 'Wireless microphone' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'MIC-003');
INSERT INTO equipment_assets (asset_tag, resource_id, asset_status, notes)
SELECT 'MIC-004', 14, 'AVAILABLE', 'Wireless microphone' WHERE NOT EXISTS (SELECT 1 FROM equipment_assets WHERE asset_tag = 'MIC-004');

-- Additional resources for wider booking choice (IDs 70-85).
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 70, 'Seminar Room S1', 'Mid-sized seminar room with a projector, podium microphone and movable chairs.', 'Academic Block, Floor 2', 60, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 70);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 71, 'Case Study Room', 'Discussion room with U-shaped seating for management and case study sessions.', 'Management Block, Floor 1', 35, 'CLASSROOM', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 71);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 72, 'Design Studio', 'Bright studio with drawing tables, pin-up walls and a large display for design reviews.', 'Architecture Block, Floor 2', 30, 'CLASSROOM', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 72);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 73, 'Language Lab', 'Audio-equipped lab with headsets for language and communication skills practice.', 'Humanities Block, Floor 1', 40, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 73);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 74, 'Cyber Security Lab', 'Isolated network lab for supervised security exercises and capture-the-flag practice.', 'Technology Block, Floor 3', 30, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 74);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 75, 'Data Science Lab', 'High-memory workstations with notebooks and visualization tools for analytics coursework.', 'Technology Block, Floor 4', 36, 'LABORATORY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 75);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 76, 'Mobile App Development Lab', 'Workstations with Android and iOS test devices for app development projects.', 'Innovation Block, Floor 2', 28, 'LABORATORY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 76);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 77, 'Drone Kits', 'Programmable quadcopter kits with spare propellers and batteries for supervised flights.', 'Robotics Lab Store', 6, 'EQUIPMENT', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 77);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 78, 'Loaner Laptops', 'Loaner laptops preloaded with office and development software for events and workshops.', 'Library Equipment Desk', 20, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 78);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 79, 'Portable PA System', 'Speaker and mixer set for outdoor events, rallies and cultural programmes.', 'Activities Office', 3, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 79);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 80, 'Video Conferencing Kit', 'PTZ camera, speakerphone and mini PC for hybrid meetings and guest lectures.', 'Media Centre Equipment Desk', 4, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 80);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 81, 'Mobile Interactive Panel', 'Wheeled 75-inch touch panel for collaborative sessions in any room.', 'Digital Learning Block, Floor 1', 2, 'EQUIPMENT', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 81);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 82, 'Open Air Theatre', 'Outdoor amphitheatre with stage lighting for cultural evenings and large gatherings.', 'Central Lawn', 800, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 82);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 83, 'Sports Complex Hall', 'Indoor multi-sport hall suitable for tournaments, fitness events and fests.', 'Sports Complex', 250, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 83);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 84, 'Exhibition Gallery', 'Gallery space with display boards and spot lighting for project expos and art shows.', 'Library Building, Ground Floor', 120, 'FACILITY', 'AVAILABLE' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 84);
INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
SELECT 85, 'Startup Incubation Pod', 'Small co-working pod with whiteboard and screen for startup team meetings.', 'Innovation Centre, Floor 1', 10, 'FACILITY', 'LIMITED' WHERE NOT EXISTS (SELECT 1 FROM campus_resources WHERE id = 85);

-- Sample campus announcements (inserted once, never overwritten).
INSERT INTO campus_announcements (announcement_id, title, summary, body, category, image_path, published_on)
SELECT 1, 'Innovate 2026 tech fest registrations are open', 'Register your teams for hackathons, robotics challenges and project expos by 5 December.', 'Innovate 2026 runs from 12 to 14 December across the Main Auditorium, Innovation Centre and Exhibition Gallery. Clubs planning stalls or competitions should submit resource booking requests early so venues and equipment can be allocated fairly. Hackathon teams of 2 to 4 members can register through their club representative.', 'EVENT', '/images/announcements/tech-fest.svg', DATE '2026-11-18' WHERE NOT EXISTS (SELECT 1 FROM campus_announcements WHERE announcement_id = 1);
INSERT INTO campus_announcements (announcement_id, title, summary, body, category, image_path, published_on)
SELECT 2, 'NVIDIA AI/GPU Lab extended hours', 'The AI/GPU Lab now stays open until 9 PM on weekdays for supervised project work.', 'From 23 November the NVIDIA AI/GPU Lab will remain open until 9:00 PM, Monday to Friday. Book workstation slots through Smart Campus and carry your college ID. A lab assistant will be present during extended hours.', 'FACILITY', '/images/announcements/gpu-lab.svg', DATE '2026-11-16' WHERE NOT EXISTS (SELECT 1 FROM campus_announcements WHERE announcement_id = 2);
INSERT INTO campus_announcements (announcement_id, title, summary, body, category, image_path, published_on)
SELECT 3, 'New loaner laptops at the library equipment desk', 'Twenty loaner laptops are now available for workshops and club events.', 'Students and clubs can request loaner laptops through the booking form under Equipment. Laptops must be returned within the booking window and are checked for condition on return.', 'EQUIPMENT', '/images/announcements/laptops.svg', DATE '2026-11-14' WHERE NOT EXISTS (SELECT 1 FROM campus_announcements WHERE announcement_id = 3);
INSERT INTO campus_announcements (announcement_id, title, summary, body, category, image_path, published_on)
SELECT 4, 'Main Auditorium maintenance on 28 November', 'The Main Auditorium sound system will be serviced, so bookings that day are paused.', 'Routine maintenance of the auditorium sound and lighting systems is scheduled for 28 November. Requests for that date will be offered alternative venues such as the Mini Auditorium or the Open Air Theatre.', 'MAINTENANCE', '/images/announcements/maintenance.svg', DATE '2026-11-12' WHERE NOT EXISTS (SELECT 1 FROM campus_announcements WHERE announcement_id = 4);
INSERT INTO campus_announcements (announcement_id, title, summary, body, category, image_path, published_on)
SELECT 5, 'Booking guidelines updated', 'Submit venue requests at least 3 days in advance and attach an event plan for large events.', 'To keep resource allocation fair, requests for venues over 100 people now need a short event plan in the supporting information field. Administrators may propose alternative times when slots overlap. Every decision comes with a notification and a downloadable PDF decision letter.', 'POLICY', '/images/announcements/guidelines.svg', DATE '2026-11-10' WHERE NOT EXISTS (SELECT 1 FROM campus_announcements WHERE announcement_id = 5);
INSERT INTO campus_announcements (announcement_id, title, summary, body, category, image_path, published_on)
SELECT 6, 'Inter-club cultural night: volunteers wanted', 'Clubs can nominate volunteers for stage, hospitality and logistics teams.', 'The inter-club cultural night will be held at the Open Air Theatre in December. Club representatives can share volunteer names with the Student Activities Office by 30 November.', 'EVENT', '/images/announcements/cultural-night.svg', DATE '2026-11-08' WHERE NOT EXISTS (SELECT 1 FROM campus_announcements WHERE announcement_id = 6);
