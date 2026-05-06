package com.jobradar.application.repository;

import com.jobradar.application.model.Application;
import com.jobradar.application.model.StatusHistory;
import com.jobradar.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByApplicationIdOrderByChangedAtDesc (Long applicationId);

    List<StatusHistory> findTop5ByApplicationUserOrderByChangedAtDesc(User user);

    void deleteByApplicationId(Long applicationId);
}
