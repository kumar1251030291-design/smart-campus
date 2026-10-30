package com.smartcampus.model;

public record FairnessSummary(double departmentShare, double clubShare, double threshold,
                              double priorityPenalty, String explanation) { }
