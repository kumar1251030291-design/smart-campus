package com.smartcampus.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeForm {
    @NotBlank(message = "Enter your current password.")
    private String currentPassword;

    @NotBlank(message = "Enter a new password.")
    @Size(min = 6, max = 72, message = "New password must be between 6 and 72 characters.")
    private String newPassword;

    @NotBlank(message = "Confirm your new password.")
    private String confirmPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
