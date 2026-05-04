package com.jobradar.application.controller;

import com.jobradar.application.dto.AiSuggestionResponse;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.service.AiSuggestionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-suggestions")
public class AiSuggestionController {

        private final AiSuggestionService aiSuggestionService;
        private final UserRepository userRepository;

    public AiSuggestionController(AiSuggestionService aiSuggestionService,
                                  UserRepository userRepository) {
        this.aiSuggestionService = aiSuggestionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/pending")
    public List<AiSuggestionResponse> getPendingSuggestions(Authentication authentication){
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("USer not found"));

        return aiSuggestionService.getPendingSuggestionResponses(user);
    }
}
