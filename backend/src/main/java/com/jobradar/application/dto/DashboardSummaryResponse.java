package com.jobradar.application.dto;

public class DashboardSummaryResponse {
    private long totalApplications;
    private long activeApplications;
    private long interviews;
    private long offers;

    public DashboardSummaryResponse(long totalApplications, long activeApplications,
                                    long interviews, long offers) {
        this.totalApplications = totalApplications;
        this.activeApplications = activeApplications;
        this.interviews = interviews;
        this.offers = offers;
    }

    public long getTotalApplications() { return totalApplications;}
    public long getActiveApplications() { return activeApplications; }
    public long getInterviews() { return interviews; }
    public long getOffers() { return offers; }
}
