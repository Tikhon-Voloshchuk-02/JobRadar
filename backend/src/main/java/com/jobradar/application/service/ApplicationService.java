package com.jobradar.application.service;

import com.jobradar.application.exception.ApplicationNotFoundException;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.StatusHistory;
import com.jobradar.application.repository.ApplicationRepository;
import com.jobradar.application.repository.StatusHistoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                                 StatusHistoryRepository statusHistoryRepository){
        this.applicationRepository=applicationRepository;
        this.statusHistoryRepository=statusHistoryRepository;
    }

    public Application createApplication(Application application){
        return applicationRepository.save(application);
    }

    public List<Application> getAllApplications(){
        return applicationRepository.findAll();
    }

    public Application getApplicationById(Long id){
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    /*
     * @param id the ID of the application
     * @param updatedApplication object containing updated fields
     * @return the updated and saved application
     */

    public Application updateApplication(Long id, Application updatedApplication){
        Application existingApplication = getApplicationById(id);

        existingApplication.setCompany(updatedApplication.getCompany());
        existingApplication.setPosition(updatedApplication.getPosition());
        existingApplication.setLink(updatedApplication.getLink());
        existingApplication.setNotes(updatedApplication.getNotes());
        existingApplication.setAppliedAt(updatedApplication.getAppliedAt());

        return applicationRepository.save(existingApplication);
    }

    public void deleteApplication(Long id) {
        Application application = getApplicationById(id);
        applicationRepository.delete(application);
    }

    /*
      Changes the status of an application and records the change in history.

      @param id the ID of the application
      @param newStatus the new status to set
      @return updated application
     */
    @Transactional
    public Application changeStatus(Long id, ApplicationStatus newStatus) {
        Application application = getApplicationById(id);

        ApplicationStatus oldStatus = application.getStatus();

        if (oldStatus == newStatus) {
            return application;
        }

        application.setStatus(newStatus);

        Application savedApplication = applicationRepository.save(application);

        StatusHistory historyEntry = new StatusHistory(oldStatus, newStatus, savedApplication);
        statusHistoryRepository.save(historyEntry);

        return savedApplication;
    }

    public List<StatusHistory> getApplicationHistory(Long applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new ApplicationNotFoundException(applicationId);
        }

        return statusHistoryRepository.findByApplicationIdOrderByChangedAtDesc(applicationId);
    }
}
