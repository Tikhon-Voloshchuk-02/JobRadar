package com.jobradar.application.service;

import com.jobradar.application.dto.DashboardSummaryResponse;
import com.jobradar.application.dto.RecentActivityResponse;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.ApplicationRepository;
import com.jobradar.application.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class DashboardService {

    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public DashboardService(ApplicationRepository applicationRepository,
                            StatusHistoryRepository statusHistoryRepository) {
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    public DashboardSummaryResponse getSummary (User user){
        long total = applicationRepository.countByUser(user);

        long active = applicationRepository.countByUserAndStatusIn(user,
                                                                    List.of(
                                                                            ApplicationStatus.SAVED,
                                                                            ApplicationStatus.APPLIED,
                                                                            ApplicationStatus.WAITING,
                                                                            ApplicationStatus.INTERVIEW
                                                                    ));
        long interviews = applicationRepository.countByUserAndStatus(
                user,
                ApplicationStatus.INTERVIEW
        );
        long offers = applicationRepository.countByUserAndStatus(
                user,
                ApplicationStatus.OFFER
        );
        Map<ApplicationStatus, Long> statusDistribution = getStatusDistribution(user);

        return new DashboardSummaryResponse(total, active, interviews, offers, statusDistribution);
    }

    private Map<ApplicationStatus, Long> getStatusDistribution(User user) {
        Map<ApplicationStatus, Long> distribution = new EnumMap<>(ApplicationStatus.class);

        for (ApplicationStatus status : ApplicationStatus.values()) {
            long count = applicationRepository.countByUserAndStatus(user, status);
            distribution.put(status, count);
        }

        return distribution;
    }

    public List<RecentActivityResponse> getRecentActivity(User user){
        List<RecentActivityResponse> createdActivities = applicationRepository.findTop5ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(application -> new RecentActivityResponse(
                        "CREARED",
                        application.getCompany(),
                        application.getPosition(),
                        null,
                        application.getStatus(),
                        application.getCreatedAt()
                ))
                .toList();
        List<RecentActivityResponse> statusActivities =
                statusHistoryRepository.findTop5ByApplicationUserOrderByChangedAtDesc(user)
                        .stream()
                        .map(history -> new RecentActivityResponse(
                                "STATUS-CHANGED",
                                history.getApplication().getCompany(),
                                history.getApplication().getPosition(),
                                history.getOldStatus(),
                                history.getNewStatus(),
                                history.getChangedAt()
                        ))
                        .toList();

        return Stream.concat(createdActivities.stream(), statusActivities.stream())
                .sorted(Comparator.comparing(RecentActivityResponse::getTimestamp).reversed())
                .limit(10)
                .toList();
    }


}
