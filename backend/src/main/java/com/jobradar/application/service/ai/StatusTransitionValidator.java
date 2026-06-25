package com.jobradar.application.service.ai;

import com.jobradar.application.model.ApplicationStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class StatusTransitionValidator {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    ApplicationStatus.SAVED, Set.of(
                            ApplicationStatus.APPLIED,
                            ApplicationStatus.WAITING,
                            ApplicationStatus.INTERVIEW,
                            ApplicationStatus.REJECTED
                    ),

                    ApplicationStatus.APPLIED, Set.of(
                            ApplicationStatus.WAITING,
                            ApplicationStatus.INTERVIEW,
                            ApplicationStatus.REJECTED,
                            ApplicationStatus.OFFER
                    ),

                    ApplicationStatus.WAITING, Set.of(
                            ApplicationStatus.INTERVIEW,
                            ApplicationStatus.REJECTED,
                            ApplicationStatus.OFFER
                    ),

                    ApplicationStatus.INTERVIEW, Set.of(
                            ApplicationStatus.OFFER,
                            ApplicationStatus.REJECTED
                    ),

                    ApplicationStatus.OFFER, Set.of(),

                    ApplicationStatus.REJECTED, Set.of()
            );


    public boolean isValid(ApplicationStatus currentStatus, ApplicationStatus suggestedStatus){
        if(currentStatus==null || suggestedStatus==null){ return false; }

        if(currentStatus == suggestedStatus) { return false; }

        return ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(suggestedStatus);
    }
}
