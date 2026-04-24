package com.jobradar.application.dto;

import com.jobradar.application.model.ApplicationStatus;

import java.util.Map;

public class DashboardSummaryResponse {
    private long totalApplications;
    private long activeApplications;
    private long interviews;
    private long offers;

    private Map<ApplicationStatus, Long> statusDistribution;

    public DashboardSummaryResponse(long totalApplications, long activeApplications,
                                    long interviews, long offers,
                                    Map<ApplicationStatus, Long> statusDistribution ) {
        this.totalApplications = totalApplications;
        this.activeApplications = activeApplications;
        this.interviews = interviews;
        this.offers = offers;
        this.statusDistribution = statusDistribution;
    }

    public long getTotalApplications() { return totalApplications;}
    public long getActiveApplications() { return activeApplications; }
    public long getInterviews() { return interviews; }
    public long getOffers() { return offers; }

    public Map<ApplicationStatus, Long> getStatusDistribution() {
        return statusDistribution;
    }
    public void setStatusDistribution(Map<ApplicationStatus, Long> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }
}
