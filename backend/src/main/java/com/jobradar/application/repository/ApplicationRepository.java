package com.jobradar.application.repository;

import com.jobradar.application.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository <Application, Long> {

}
