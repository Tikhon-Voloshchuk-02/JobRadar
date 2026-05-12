package com.jobradar.application.gmail;

import com.jobradar.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GmailConnectionRepository extends JpaRepository<GmailConnection, Long> {

    Optional<GmailConnection> findByUser(User user);

    Optional<GmailConnection> findByUserId(Long userId);

    Optional<GmailConnection> findByUserIdAndConnectedTrue(Long userId);

    List<GmailConnection> findByConnectedTrue();
}
