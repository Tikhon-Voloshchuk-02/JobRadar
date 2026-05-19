package com.jobradar.application.repository.gmail;

import com.jobradar.application.model.gmail.ProcessedEmail;
import com.jobradar.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedEmailRepository
        extends JpaRepository<ProcessedEmail, Long> {

    boolean existsByUserAndGmailMessageId(User user, String gmailMessageId);

    Optional<ProcessedEmail> findByUserAndGmailMessageId(User user, String gmailMessageId);
}
