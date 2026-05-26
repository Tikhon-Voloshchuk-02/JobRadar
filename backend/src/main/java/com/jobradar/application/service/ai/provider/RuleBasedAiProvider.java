package com.jobradar.application.service.ai.provider;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailMessageDto;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.ai.ConfidenceLevel;
import org.springframework.stereotype.Service;


@Service
public class RuleBasedAiProvider implements AiProvider {
    private String buildText(GmailMessageDto email) {
        return String.join(" ",
                nullToEmpty(email.subject()),
                nullToEmpty(email.from()),
                nullToEmpty(email.snippet()),
                nullToEmpty(email.bodyText())
        );
    }


    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }



    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AiProviderType getType(){
        return AiProviderType.RULE_BASED;
    }

    @Override
    public EmailAnalysisResult analyze(GmailMessageDto email){
        String text = buildText(email).toLowerCase();

        if (containsAny(text,
                "stepstone",
                "indeed",
                "linkedin jobs",
                "recommended for you",
                "our recommendation",
                "popular job",
                "hot job",
                "top match",
                "great candidate",
                "great fit",
                "your skills are needed",
                "start your application",
                "apply early",
                "jobs that match your search",
                "new jobs that match",
                "this job looks like a great fit",
                "this popular role",
                "don't miss your chance",
                "is this the one you're looking for")) {
            return new EmailAnalysisResult(false, null,
                    ConfidenceLevel.LOW, "Ignored job board recommendation email");
        }

        if (containsAny(text,
                "unfortunately",
                "leider",
                "not move forward",
                "nicht weiter",
                "absage")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.REJECTED,
                    ConfidenceLevel.HIGH,
                    "Email contains rejection-related keywords"
            );
        }

        if (containsAny(text,
                "interview",
                "gespräch",
                "vorstellungsgespräch",
                "termin",
                "meeting")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.INTERVIEW,
                    ConfidenceLevel.HIGH,
                    "Email contains interview-related keywords"
            );
        }

        if (containsAny(text,
                "job offer",
                "offer of employment",
                "employment offer",
                "we are pleased to offer",
                "we would like to offer you",
                "contract offer",
                "arbeitsvertrag",
                "vertragsangebot")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.OFFER,
                    ConfidenceLevel.HIGH,
                    "Email contains offer-related keywords"
            );
        }

        if (containsAny(text,
                "assessment",
                "coding challenge",
                "testaufgabe",
                "aufgabe",
                "case study")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.WAITING,
                    ConfidenceLevel.MEDIUM,
                    "Email contains test-assignment-related keywords"
            );
        }

        if (containsAny(text,
                "application",
                "bewerbung",
                "thank you for applying",
                "eingegangen",
                "received")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.WAITING,
                    ConfidenceLevel.MEDIUM,
                    "Email appears to be related to a job application"
            );
        }

        if (containsAny(text,
                "werkstudent",
                "praktikum",
                "praktikant",
                "bewerbung")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.WAITING,
                    ConfidenceLevel.MEDIUM,
                    "Email appears related to job search"
            );
        }

        return new EmailAnalysisResult(
                false,
                null,
                ConfidenceLevel.LOW,
                "Email does not look job-related"
        );
    }
}
