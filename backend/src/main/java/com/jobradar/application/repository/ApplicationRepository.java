package com.jobradar.application.repository;

import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ApplicationRepository extends JpaRepository <Application, Long> {

    List<Application> findByUser(User user);

    long countByUser(User user);
    long countByUserAndStatus(User user, ApplicationStatus status);
    long countByUserAndStatusIn(User user, Collection<ApplicationStatus> statuses);

    @Query("""
    SELECT a
    FROM Application a
    WHERE a.user = :user
      AND (:status IS NULL OR a.status = :status)
      AND (
            :searchPattern IS NULL
            OR LOWER(a.company) LIKE :searchPattern
            OR LOWER(a.position) LIKE :searchPattern
            OR LOWER(COALESCE(a.notes, '')) LIKE :searchPattern
      )
""")
    List<Application> findByUserAndFilters(
            @Param("user") User user,
            @Param("status") ApplicationStatus status,
            @Param("searchPattern") String searchPattern
    );



}
