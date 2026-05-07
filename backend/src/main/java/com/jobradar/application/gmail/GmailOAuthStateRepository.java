package com.jobradar.application.gmail;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GmailOAuthStateRepository extends JpaRepository<GmailOAuthState, Long> {

    Optional<GmailOAuthState> findByStateAndUsedFalse(String state);

}
