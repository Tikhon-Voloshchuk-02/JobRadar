package com.jobradar.application.dto.statistics;

import java.util.List;

public record DailyActivityEntry(String date, int count, List<AppBrief> entries) {}
