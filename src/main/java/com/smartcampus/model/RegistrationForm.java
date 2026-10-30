package com.smartcampus.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {
    @NotBlank(message = "Enter your full name.")
    @Size(max = 100, message = "Full name must be 100 characters or fewer.")
    private String fullName;
    @NotBlank(message = "Enter your college email.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 254, message = "Email must be 254 characters or fewer.")
    private String collegeEmail;
    @NotBlank(message = "Create a password.")
    @Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters.")
    private String password;
    @NotBlank(message = "Confirm your password.")
    private String confirmPassword;
    @NotBlank(message = "Enter your department.")
    @Size(max = 100, message = "Department must be 100 characters or fewer.")
    private String department;
    @Min(value = 1, message = "Academic year must be between 1 and 8.")
    @Max(value = 8, message = "Academic year must be between 1 and 8.")
    private Integer yearOfStudy;
    @Size(max = 40, message = "Roll number must be 40 characters or fewer.")
    private String rollNumber;
    @Size(max = 100, message = "Club name must be 100 characters or fewer.")
    private String clubName;
    @NotBlank(message = "Choose an account type.")
    @Pattern(regexp = "STUDENT|CLUB_REPRESENTATIVE", message = "Choose Student or Club Representative.")
    private String accountType;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getCollegeEmail() { return collegeEmail; }
    public void setCollegeEmail(String collegeEmail) { this.collegeEmail = collegeEmail; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}
