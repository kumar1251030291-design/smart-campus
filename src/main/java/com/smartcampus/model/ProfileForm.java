package com.smartcampus.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Only fields a member is allowed to edit in their own profile. */
public class ProfileForm {
    @NotBlank(message = "Enter your name.")
    @Size(max = 100, message = "Name must be 100 characters or fewer.")
    private String fullName;

    @NotBlank(message = "Enter your department.")
    @Size(max = 100, message = "Department must be 100 characters or fewer.")
    private String department;

    private Integer yearOfStudy;

    @Size(max = 100, message = "Club name must be 100 characters or fewer.")
    private String clubName;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }
}
