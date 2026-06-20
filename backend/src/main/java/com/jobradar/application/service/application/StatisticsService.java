package com.jobradar.application.service.application;

import com.jobradar.application.dto.statistics.*;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.StatusHistory;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.ApplicationRepository;
import com.jobradar.application.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public StatisticsService(ApplicationRepository applicationRepository,
                             StatusHistoryRepository statusHistoryRepository) {
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    public StatisticsResponse getStatistics(User user) {
        List<Application> apps = applicationRepository.findByUser(user);
        List<StatusHistory> history = statusHistoryRepository.findAllByApplicationUserOrderByChangedAtAsc(user);
        Map<Long, String> gmailIdByAppId = buildCurrentStatusGmailIdMap(apps, history);

        return new StatisticsResponse(
                apps.size(),
                buildStatusCounts(apps),
                buildDailyActivity(apps, gmailIdByAppId),
                buildWeeklyTrend(apps, history),
                computeAvgTimings(apps, history)
        );
    }

    /**
     * For each application, finds the most recent StatusHistory entry where
     * newStatus == app.currentStatus and gmailMessageId is non-null.
     * Used to link a status badge in the UI directly to the Gmail email that caused it.
     */
    private Map<Long, String> buildCurrentStatusGmailIdMap(List<Application> apps,
                                                            List<StatusHistory> history) {
        Map<Long, ApplicationStatus> currentStatus = apps.stream()
                .collect(Collectors.toMap(Application::getId, Application::getStatus));

        Map<Long, String> result = new HashMap<>();
        // history is sorted ASC → iterate in reverse to find the most recent match first
        for (int i = history.size() - 1; i >= 0; i--) {
            StatusHistory h = history.get(i);
            Long appId = h.getApplication().getId();
            if (result.containsKey(appId) || h.getGmailMessageId() == null) continue;
            if (h.getNewStatus() == currentStatus.get(appId)) {
                result.put(appId, h.getGmailMessageId());
            }
        }
        return result;
    }

    private Map<String, Long> buildStatusCounts(List<Application> apps) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status.name(), apps.stream().filter(a -> a.getStatus() == status).count());
        }
        return counts;
    }

    private List<DailyActivityEntry> buildDailyActivity(List<Application> apps,
                                                         Map<Long, String> gmailIdByAppId) {
        Map<LocalDate, List<AppBrief>> byDate = new TreeMap<>();
        for (Application app : apps) {
            LocalDate date = app.getCreatedAt().toLocalDate();
            byDate.computeIfAbsent(date, k -> new ArrayList<>())
                    .add(new AppBrief(app.getCompany(), app.getStatus().name(),
                            gmailIdByAppId.get(app.getId())));
        }
        return byDate.entrySet().stream()
                .map(e -> new DailyActivityEntry(e.getKey().toString(), e.getValue().size(), e.getValue()))
                .toList();
    }

    private List<WeeklyTrendEntry> buildWeeklyTrend(List<Application> apps, List<StatusHistory> history) {
        LocalDate today = LocalDate.now();
        List<WeeklyTrendEntry> result = new ArrayList<>();

        for (int offset = 11; offset >= 0; offset--) {
            LocalDate weekStart = today.minusWeeks(offset).with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            long appCount = apps.stream()
                    .filter(a -> inRange(a.getCreatedAt().toLocalDate(), weekStart, weekEnd))
                    .count();

            long interviews = countTransitions(history, ApplicationStatus.INTERVIEW, weekStart, weekEnd);
            long offers     = countTransitions(history, ApplicationStatus.OFFER,      weekStart, weekEnd);
            long rejections = countTransitions(history, ApplicationStatus.REJECTED,   weekStart, weekEnd);

            result.add(new WeeklyTrendEntry("W" + (12 - offset),
                    (int) appCount, (int) interviews, (int) offers, (int) rejections));
        }
        return result;
    }

    private long countTransitions(List<StatusHistory> history, ApplicationStatus target,
                                  LocalDate from, LocalDate to) {
        return history.stream()
                .filter(h -> h.getNewStatus() == target)
                .filter(h -> inRange(h.getChangedAt().toLocalDate(), from, to))
                .count();
    }

    private boolean inRange(LocalDate d, LocalDate from, LocalDate to) {
        return !d.isBefore(from) && !d.isAfter(to);
    }

    private AvgTimingsResponse computeAvgTimings(List<Application> apps, List<StatusHistory> history) {
        Map<Long, Application> appById = apps.stream()
                .collect(Collectors.toMap(Application::getId, a -> a));

        Map<Long, List<StatusHistory>> historyByApp = history.stream()
                .collect(Collectors.groupingBy(h -> h.getApplication().getId()));

        List<Double> appliedToInterview = new ArrayList<>();
        List<Double> interviewToDecision = new ArrayList<>();

        for (Map.Entry<Long, List<StatusHistory>> entry : historyByApp.entrySet()) {
            Application app = appById.get(entry.getKey());
            if (app == null) continue;

            List<StatusHistory> appHistory = entry.getValue();

            Optional<StatusHistory> firstInterview = appHistory.stream()
                    .filter(h -> h.getNewStatus() == ApplicationStatus.INTERVIEW)
                    .findFirst();

            if (firstInterview.isEmpty()) continue;

            LocalDate interviewDate = firstInterview.get().getChangedAt().toLocalDate();
            long days = ChronoUnit.DAYS.between(app.getCreatedAt().toLocalDate(), interviewDate);
            appliedToInterview.add((double) days);

            appHistory.stream()
                    .filter(h -> h.getNewStatus() == ApplicationStatus.REJECTED
                              || h.getNewStatus() == ApplicationStatus.OFFER)
                    .filter(h -> h.getChangedAt().toLocalDate().isAfter(interviewDate))
                    .findFirst()
                    .ifPresent(decision -> interviewToDecision.add((double)
                            ChronoUnit.DAYS.between(interviewDate, decision.getChangedAt().toLocalDate())));
        }

        double a2i = avg(appliedToInterview);
        double i2d = avg(interviewToDecision);
        return new AvgTimingsResponse(round1(a2i), round1(i2d), round1(a2i + i2d));
    }

    private double avg(List<Double> values) {
        return values.isEmpty() ? 0.0 :
                values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
