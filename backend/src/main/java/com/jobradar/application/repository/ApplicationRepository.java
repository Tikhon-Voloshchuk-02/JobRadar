package com.jobradar.application.repository;

import com.jobradar.application.model.Application;
import com.jobradar.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository <Application, Long> {

    List<Application> findByUser(User user);
}
