package com.jobradar.application.gmail;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEmailRepository
        extends JpaRepository<ProcessedEmail, Long> {

    boolean existsByGmailMessageId(String gmailMessageId);
}
