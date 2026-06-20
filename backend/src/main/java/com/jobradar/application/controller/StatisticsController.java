package com.jobradar.application.controller;

import com.jobradar.application.dto.statistics.StatisticsResponse;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.service.application.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserRepository userRepository;

    public StatisticsController(StatisticsService statisticsService,
                                UserRepository userRepository) {
        this.statisticsService = statisticsService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<StatisticsResponse> getStatistics(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(statisticsService.getStatistics(user));
    }
}
