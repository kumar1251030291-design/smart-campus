package com.smartcampus.model;

import java.util.List;

/** Safe response for a member's pre-submit availability check; it exposes no other requester's identity. */
public record BookingAvailabilityFeedback(boolean available, List<String> messages,
                                          List<SlotSuggestion> commonAlternatives,
                                          List<SlotSuggestion> predictiveSuggestions) { }
