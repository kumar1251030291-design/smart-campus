package com.smartcampus.model;

import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.UserRole;

import java.time.LocalDateTime;

/** Safe user fields for administrator pages; this view deliberately has no password field. */
public record AdminUserView(Long userId, String fullName, String email, UserRole role, String department,
                            Integer yearOfStudy, String rollNumber, String clubName,
                            LocalDateTime registrationDate, AccountStatus accountStatus) { }
