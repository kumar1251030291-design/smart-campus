package com.smartcampus.model;

import java.util.List;

public record AvailabilityResult(boolean available, List<String> issues, List<BookingRequest> conflictingRequests) { }
