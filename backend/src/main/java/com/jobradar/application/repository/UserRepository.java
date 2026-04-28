package com.jobradar.application.repository;

import com.jobradar.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByEmailVerificationToken(String token);

}
