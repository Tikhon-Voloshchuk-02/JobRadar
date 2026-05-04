package com.jobradar.application.controller;

import com.jobradar.application.dto.AiSuggestionResponse;
import com.jobradar.application.dto.FakeEmailAnalysisRequest;

import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.AiSuggestionRepository;
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

//ACCEPT
    @PostMapping("/{id}/accept")
    public AiSuggestionResponse acceptSuggestion(@PathVariable Long id, Authentication auth){
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return aiSuggestionService.acceptSuggestion(id, user);
    }

//REJECT
    @PostMapping("/{id}/reject")
    public AiSuggestionResponse rejectSuggestion(@PathVariable Long id,
                                                 Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return aiSuggestionService.rejectSuggestion(id, user);
    }

    @PostMapping("/fake-analyze")
    public AiSuggestionResponse analyzeFakeEmail(@RequestBody FakeEmailAnalysisRequest request,
                                                 Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return aiSuggestionService.analyzeFakeEmail(request, user);
    }
/*
    @PostMapping("/test")
    public String createTestSuggestion(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));


        Application application = user.getApplications().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No applications"));

        AiSuggestion suggestion = new AiSuggestion();
        suggestion.setApplication(application);
        suggestion.setCurrentStatus(application.getStatus());
        suggestion.setSuggestedStatus(ApplicationStatus.INTERVIEW);
        suggestion.setConfidence(ConfidenceLevel.HIGH);
        suggestion.setReason("Test suggestion: interview invitation detected");
        suggestion.setEmailSnippet("We invite you to an interview...");

        aiSuggestionRepository.save(suggestion);

        return "Test suggestion created";
    }
 */

}
