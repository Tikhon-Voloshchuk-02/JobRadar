package com.jobradar.application.controller;
import com.jobradar.application.dto.StatusChangeRequest;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.StatusHistory;
import com.jobradar.application.repository.ApplicationRepository;
import com.jobradar.application.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController (ApplicationService applicationService) {
        this.applicationService=applicationService;
    }

    /**
     * Creates a new application.
     *
     * @param application application data from request body
     * @return saved application
     */
    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        Application savedApplication = applicationService.createApplication(application);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApplication);
    }

    /**
     * Returns all applications.
     *
     * @return list of all applications
     */
    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    /**
     * Returns one application by ID.
     *
     * @param id application ID
     * @return found application
     */
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        Application application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(application);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<StatusHistory>> getApplicationHistory(@PathVariable Long id) {
        List<StatusHistory> history = applicationService.getApplicationHistory(id);
        return ResponseEntity.ok(history);
    }


    /**
     * Updates an existing application without changing its status.
     *
     * @param id application ID
     * @param updatedApplication updated application data
     * @return updated application
     */
    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(
            @PathVariable Long id,
            @RequestBody Application updatedApplication
    ) {
        Application application = applicationService.updateApplication(id, updatedApplication);
        return ResponseEntity.ok(application);
    }

    /**
     * Deletes an application by ID.
     *
     * @param id application ID
     * @return empty response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the status of a specific application
     *
     * @param id the ID of the application
     * @param request contains the new status to be applied
     * @return the updated application with the new_status or 404 if not found
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Application> changeStatus(
            @PathVariable Long id,
            @RequestBody StatusChangeRequest request
    ) {
        return ResponseEntity.ok(
                applicationService.changeStatus(id, request.getNewStatus())
        );
    }


}
