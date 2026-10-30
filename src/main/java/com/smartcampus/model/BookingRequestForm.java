package com.smartcampus.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingRequestForm {
    @NotBlank(message = "Enter an event title.") @Size(max = 150)
    private String eventTitle;
    @NotBlank(message = "Choose an event type.")
    @Pattern(regexp = "ACADEMIC|TECHNICAL|WORKSHOP|CLUB_EVENT|OTHER", message = "Choose a valid event type.")
    private String eventType;
    @NotBlank(message = "Describe the purpose of the event.") @Size(max = 1200)
    private String purpose;
    @NotNull(message = "Choose a date.") @FutureOrPresent(message = "The event date must be today or later.")
    private LocalDate eventDate;
    @NotNull(message = "Choose a start time.")
    private LocalTime startTime;
    @NotNull(message = "Choose an end time.")
    private LocalTime endTime;
    @Min(value = 1, message = "Expected participants must be at least one.") @Max(value = 10000)
    private int expectedParticipants;
    @Size(max = 1200) private String specialRequirements;
    @Size(max = 2000) private String supportingInformation;
    @NotEmpty(message = "Choose at least one resource.") @Size(max = 10, message = "Choose up to 10 resources.")
    private List<Long> resourceIds = new ArrayList<>();
    @NotEmpty(message = "Enter a quantity for each resource.") @Size(max = 10)
    private List<Integer> requestedQuantities = new ArrayList<>();

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public int getExpectedParticipants() { return expectedParticipants; }
    public void setExpectedParticipants(int expectedParticipants) { this.expectedParticipants = expectedParticipants; }
    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }
    public String getSupportingInformation() { return supportingInformation; }
    public void setSupportingInformation(String supportingInformation) { this.supportingInformation = supportingInformation; }
    public List<Long> getResourceIds() { return resourceIds; }
    public void setResourceIds(List<Long> resourceIds) { this.resourceIds = resourceIds; }
    public List<Integer> getRequestedQuantities() { return requestedQuantities; }
    public void setRequestedQuantities(List<Integer> requestedQuantities) { this.requestedQuantities = requestedQuantities; }
}
