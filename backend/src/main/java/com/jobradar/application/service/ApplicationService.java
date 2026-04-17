package com.jobradar.application.service;

import com.jobradar.application.exception.ApplicationNotFoundException;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.StatusHistory;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.ApplicationRepository;
import com.jobradar.application.repository.StatusHistoryRepository;
import com.jobradar.application.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                                 StatusHistoryRepository statusHistoryRepository,
                                 UserRepository userRepository){
        this.applicationRepository=applicationRepository;
        this.statusHistoryRepository=statusHistoryRepository;
        this.userRepository=userRepository;
    }


    public Application createApplication(Application application){
        User currentUser = getCurrentUser();
        application.setUser(currentUser);
        return applicationRepository.save(application);
    }

    public List<Application> getApplications(ApplicationStatus status, String search) {
        User currentUser = getCurrentUser();
        String searchPattern = normalizeSearchPattern(search);

        return applicationRepository.findByUserAndFilters(currentUser, status, searchPattern);
    }

    public Application getApplicationById(Long id){
//        return applicationRepository.findById(id)
//                .orElseThrow(() -> new ApplicationNotFoundException(id));

        User currentUser = getCurrentUser();

        Application application = applicationRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));

        if(!application.getUser().getId().equals(currentUser.getId())){
            throw new AccessDeniedException("You do not have access to this application");
        }
        return application;
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

/*
    Retrieves the currently authenticated user from the SecurityContext.
    The method extracts the principal (email) from the current Authentication
    object and loads the corresponding User entity from the database.

    This ensures that all data access operations are scoped to the authenticated user.

    @return the authenticated User entity
    @throws UsernameNotFoundException if the user cannot be found in the database
*/

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                                            .getAuthentication()
                                            .getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User nor found " +email));
    }


    public List<StatusHistory> getApplicationHistory(Long applicationId) {
        Application application = getApplicationById(applicationId);
        return statusHistoryRepository.findByApplicationIdOrderByChangedAtDesc(application.getId());
    }

    private String normalizeSearchPattern(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return "%" + search.trim().toLowerCase() + "%";
    }
}
