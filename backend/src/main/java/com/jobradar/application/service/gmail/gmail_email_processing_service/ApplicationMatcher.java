package com.jobradar.application.service.gmail.gmail_email_processing_service;

import com.jobradar.application.dto.gmail.GmailMessageDto;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ApplicationMatcher {


    private static final Logger log = LoggerFactory.getLogger(ApplicationMatcher.class);
    private final ApplicationRepository applicationRepository;

    public ApplicationMatcher(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public Optional<Application> findMatchingApplication(User user, GmailMessageDto email) {
        String text = String.join(" ",
                safe(email.subject()),
                safe(email.from()),
                safe(email.snippet()),
                safe(email.bodyText())
        ).toLowerCase();

        List<Application> applications = applicationRepository.findByUser(user);

        List<MatchCandidate> candidates = applications.stream()
                .map(application -> new MatchCandidate(
                        application,
                        calculateScore(application, text)
                ))
                .filter(candidate -> candidate.score() >= 5)
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .toList();

        candidates.forEach(candidate ->
                log.info(
                        "Application match candidate: company={}, position={}, score={}",
                        candidate.application().getCompany(),
                        candidate.application().getPosition(),
                        candidate.score()
                )
        );

        if (candidates.isEmpty()) {
            log.info(
                    "No matching application found: subject={}, sender={}",
                    email.subject(),
                    email.from()
            );
            return Optional.empty();
        }

        if (candidates.size() > 1
                && candidates.get(0).score() == candidates.get(1).score()) {
            log.warn(
                    "Ambiguous application match: score={}, company1={}, company2={}",
                    candidates.get(0).score(),
                    candidates.get(0).application().getCompany(),
                    candidates.get(1).application().getCompany()
            );
            return Optional.empty();
        }

        Application matchedApplication = candidates.get(0).application();

        log.info(
                "Application matched: company={}, position={}, score={}",
                matchedApplication.getCompany(),
                matchedApplication.getPosition(),
                candidates.get(0).score()
        );

        return Optional.of(matchedApplication);
    }

    private int calculateScore(Application application, String text) {
        int score = 0;

        String company = safe(application.getCompany()).toLowerCase();
        String position = safe(application.getPosition()).toLowerCase();

        if (!company.isBlank() && text.contains(company)) {
            score += 10;
        }

        if (!position.isBlank() && text.contains(position)) {
            score += 6;
        }

        score += companyWordScore(company, text);

        return score;
    }

    private int companyWordScore(String company, String text) {
        if (company.isBlank()) {
            return 0;
        }

        return Arrays.stream(company.split("\\s+"))
                .filter(word -> word.length() > 4)
                .filter(word -> !isGenericCompanyWord(word))
                .filter(text::contains)
                .mapToInt(word -> 2)
                .sum();
    }

    private boolean isGenericCompanyWord(String word) {
        return Set.of(
                "gmbh",
                "mbh",
                "kg",
                "ag",
                "co",
                "inc",
                "ltd",
                "llc",
                "group",
                "holding",
                "company",
                "unternehmen"
        ).contains(word);
    }

    private record MatchCandidate(Application application, int score) {
    }
}