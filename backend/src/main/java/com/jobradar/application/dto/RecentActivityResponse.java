package com.jobradar.application.dto;

import com.jobradar.application.model.ApplicationStatus;

import java.time.LocalDateTime;

public class RecentActivityResponse {
    private String type;
    private String company;
    private String position;
    private ApplicationStatus oldStatus;
    private ApplicationStatus newStatus;
    private LocalDateTime timestamp;


    public RecentActivityResponse(String type,
                                  String company,
                                  String position,
                                  ApplicationStatus oldStatus,
                                  ApplicationStatus newStatus,
                                  LocalDateTime timestamp) {
        this.type = type;
        this.company = company;
        this.position = position;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = timestamp;
    }

    public String getType() { return type; }
    public String getCompany() { return company; }
    public String getPosition() { return position; }
    public ApplicationStatus getOldStatus() { return oldStatus; }
    public ApplicationStatus getNewStatus() { return newStatus; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
