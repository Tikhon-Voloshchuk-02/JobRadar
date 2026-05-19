package com.jobradar.application.repository.gmail;

import com.jobradar.application.model.gmail.GmailOAuthState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GmailOAuthStateRepository extends JpaRepository<GmailOAuthState, Long> {

    Optional<GmailOAuthState> findByStateAndUsedFalse(String state);

}
