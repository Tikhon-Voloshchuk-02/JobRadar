package com.jobradar.application.dto.statistics;

public record AvgTimingsResponse(
        double appliedToInterviewDays,
        double interviewToDecisionDays,
        double fullCycleAvgDays
) {}
