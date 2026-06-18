package com.jobradar.application.service.gmail.gmail_email_processing_service;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
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
    private static final int MIN_MATCH_SCORE = 30;

    public ApplicationMatcher(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        return safe(value)
                .toLowerCase()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int positionWordScore(String position, String text) {
        if (position.isBlank()) {
            return 0;
        }

        return Arrays.stream(position.split("\\s+"))
                .filter(word -> word.length() > 3)
                .filter(word -> !isGenericPositionWord(word))
                .filter(text::contains)
                .mapToInt(word -> 2)
                .sum();
    }

    private int calculateScore(Application application, String text, String subject,
                               String detectedCompany, String detectedPosition) {
        int score = 0;

        String company = normalize(application.getCompany());
        String position = normalize(application.getPosition());

        String aiCompany = normalize(detectedCompany);
        String aiPosition = normalize(detectedPosition);

        if (!position.isBlank() && containsPhrase(subject, position)) { score += 20; }

        if (!company.isBlank() && !aiCompany.isBlank()) {

            if (company.equals(aiCompany)) { score += 30; }

            else if (containsPhrase(aiCompany, company)  || containsPhrase(company, aiCompany)) { score += 20; }
            else if (companyWordScore(company, aiCompany) >= 6) { score += 15; }
        }

        if (!position.isBlank() && !aiPosition.isBlank()) {

            if (position.equals(aiPosition)) { score += 15; }
            else if (positionWordScore(position, aiPosition) >= 2) { score += 8; }
        }

        boolean companyMatched =
                (!company.isBlank() && text.contains(company))
                        || companyWordScore(company, text) >= 6;

        boolean positionMatched = positionWordScore(position, text) >= 2;

        boolean platformEmail = isPlatformEmail(text);

        if (companyMatched) { score += 10; }
        if (positionMatched) { score += 8; }
        if (companyMatched && positionMatched) { score += 10; }
        if (platformEmail && positionMatched) { score += 6; }

        return score;
    }

    public Optional<Application> findMatchingApplication(User user, GmailMessageDto email) {
        return findMatchingApplication(user, email, null);
    }

    public Optional<Application> findMatchingApplication( User user,  GmailMessageDto email,  EmailAnalysisResult analysisResult) {
        String detectedCompany = analysisResult == null
                ? ""
                : safe(analysisResult.detectedCompany());

        String detectedPosition = analysisResult == null
                ? ""
                : safe(analysisResult.detectedPosition());

        log.info(
                "Application matching started: subject={}, sender={}, detectedCompany={}, detectedPosition={}",
                email.subject(),
                email.from(),
                detectedCompany,
                detectedPosition
        );

        String normalizedDetectedCompany = normalize(detectedCompany);
        String normalizedDetectedPosition = normalize(detectedPosition);

        if (normalizedDetectedCompany.isBlank() && normalizedDetectedPosition.isBlank()) {
            log.warn(
                    "Application matching skipped: no detected company and no detected position. subject={}, sender={}",
                    email.subject(),
                    email.from()
            );
            return Optional.empty();
        }

        String text = normalize(String.join(" ",
                safe(email.subject()),
                safe(email.from()),
                safe(email.snippet()),
                safe(email.bodyText()),
                detectedCompany,
                detectedPosition
        ));

        String subject = normalize(email.subject());

        List<Application> applications = applicationRepository.findByUser(user);

        List<MatchCandidate> candidates = applications.stream()
                .map(application -> new MatchCandidate(
                        application,
                        calculateScore(
                                application,
                                text,
                                subject,
                                detectedCompany,
                                detectedPosition
                        )
                ))
                .filter(candidate -> candidate.score() >= MIN_MATCH_SCORE)
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
                && candidates.get(0).score() - candidates.get(1).score() < 10) {

            log.warn(
                    "Ambiguous application match: bestScore={}, secondScore={}, company1={}, company2={}",
                    candidates.get(0).score(),
                    candidates.get(1).score(),
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

    private int companyWordScore(String company, String text) {
        if (company.isBlank()) {
            return 0;
        }

        return Arrays.stream(company.split("\\s+"))
                .filter(word -> word.length() > 3)
                .filter(word -> !isGenericCompanyWord(word))
                .filter(text::contains)
                .mapToInt(word -> 3)
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

    private boolean isGenericPositionWord(String word) {
        return Set.of(
                "m",
                "w",
                "d",
                "mwd",
                "vollzeit",
                "teilzeit",
                "werkstudent",
                "praktikum",
                "ausbildung",
                "quereinsteiger",
                "mitarbeiter",
                "student",
                "junior",
                "senior"
        ).contains(word);
    }

    private boolean isPlatformEmail(String text) {
        return text.contains("indeed")
                || text.contains("stepstone")
                || text.contains("linkedin")
                || text.contains("xing");
    }

    private boolean containsPhrase(String text, String phrase) {
        return (" " + text + " ").contains(" " + phrase + " ");
    }

    private record MatchCandidate(Application application, int score) {
    }
}