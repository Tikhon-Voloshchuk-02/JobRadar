package com.jobradar.application.dto.application;

import com.jobradar.application.model.ApplicationStatus;

public class StatusChangeRequest {

    private ApplicationStatus newStatus;

    public StatusChangeRequest() {}
    public ApplicationStatus getNewStatus(){
        return newStatus;
    }

    public void setNewStatus(ApplicationStatus newStatus){
        this.newStatus=newStatus;
    }
}
