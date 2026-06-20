package com.jobradar.application.dto.statistics;

import java.util.List;
import java.util.Map;

public record StatisticsResponse(
        long totalApplications,
        Map<String, Long> statusCounts,
        List<DailyActivityEntry> applicationsByDate,
        List<WeeklyTrendEntry> weeklyTrend,
        AvgTimingsResponse avgTimings
) {}
