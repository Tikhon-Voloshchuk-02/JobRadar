package com.jobradar.application.service;

import com.jobradar.application.dto.DashboardSummaryResponse;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final ApplicationRepository applicationRepository;

    public DashboardService(ApplicationRepository applicationRepository){
        this.applicationRepository=applicationRepository;
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
        return new DashboardSummaryResponse(total, active, interviews, offers);
    }
}
