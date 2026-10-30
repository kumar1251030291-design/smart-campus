package com.smartcampus.model;

import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** A campus account and the identity Spring Security uses for a session. */
public class CampusUser implements UserDetails {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final String department;
    private final Integer yearOfStudy;
    private final String rollNumber;
    private final String clubName;
    private final LocalDateTime registrationDate;
    private final AccountStatus accountStatus;

    public CampusUser(Long userId, String fullName, String email, String passwordHash, UserRole role,
                      String department, Integer yearOfStudy, String rollNumber, String clubName,
                      LocalDateTime registrationDate, AccountStatus accountStatus) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.rollNumber = rollNumber;
        this.clubName = clubName;
        this.registrationDate = registrationDate;
        this.accountStatus = accountStatus;
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public String getDepartment() { return department; }
    public Integer getYearOfStudy() { return yearOfStudy; }
    public String getRollNumber() { return rollNumber; }
    public String getClubName() { return clubName; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public AccountStatus getAccountStatus() { return accountStatus; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return accountStatus == AccountStatus.ACTIVE; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return accountStatus == AccountStatus.ACTIVE; }
}
