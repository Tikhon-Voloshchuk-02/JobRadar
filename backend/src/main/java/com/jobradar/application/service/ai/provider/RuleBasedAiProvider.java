package com.jobradar.application.service.ai.provider;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailMessageDto;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.ai.ConfidenceLevel;
import com.jobradar.application.service.ai.decision.AnalysisDecision;
import com.jobradar.application.service.ai.decision.AnalysisDecisionType;
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
    public AiProviderType getType() {
        return AiProviderType.RULE_BASED;
    }

    @Override
    public EmailAnalysisResult analyze(GmailMessageDto email) {

        //Time-LOG
        System.out.println("=== RULE BASED PROVIDER CALLED ===");

        String text = buildText(email).toLowerCase();

        if (containsAny(text,
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
                "new jobs for you",
                "similar jobs",
                "jobs you may be interested in",
                "this job looks like a great fit",
                "this popular role",
                "don't miss your chance",
                "is this the one you're looking for")) {

            return new EmailAnalysisResult(
                    false,
                    null,
                    ConfidenceLevel.LOW,
                    "Ignored job board recommendation email"
            );
        }

        //REJECTED
        if (containsAny(text,
                "unfortunately",
                "not move forward",

                "absage",
                "leider müssen wir ihnen mitteilen",
                "leider können wir ihnen keine",
                "leider haben wir uns",
                "wir haben uns für einen anderen kandidaten entschieden",
                "wir haben uns für andere bewerber entschieden",
                "we have decided to move forward with other candidates",
                "we will not be moving forward",
                "we regret to inform you",
                "sagen wir ihnen ab",
                "sagen wir ihnen auf diesem weg ab",
                "daher sagen wir ihnen",
                "nicht berücksichtigen",
                "nicht in die engere auswahl")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.REJECTED,
                    ConfidenceLevel.HIGH,
                    "Email contains rejection-related keywords"
            );
        }

        //OFFER
        if (containsAny(text,
                "job offer",
                "offer of employment",
                "employment offer",
                "we are pleased to offer",
                "we would like to offer you",
                "contract offer",
                "arbeitsvertrag",
                "vertragsangebot",
                "zusage",
                "wir freuen uns ihnen mitzuteilen",
                "wir freuen uns dir mitzuteilen",
                "wir möchten ihnen ein angebot machen",
                "wir möchten dir ein angebot machen",
                "angebot für die stelle",
                "vertragsunterlagen",
                "arbeitsvertrag erhalten",
                "willkommen im team",
                "welcome to the team",
                "we are happy to offer",
                "we are excited to offer",
                "we would like to extend an offer",
                "offer letter",
                "employment contract")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.OFFER,
                    ConfidenceLevel.HIGH,
                    "Email contains offer-related keywords"
            );
        }

        //INTERVIEW
        if (containsAny(text,
                "interview",
                "gespräch",
                "vorstellungsgespräch",
                "termin",
                "meeting",
                "einladung zum gespräch",
                "einladung zum vorstellungsgespräch",
                "wir möchten sie kennenlernen",
                "wir möchten dich kennenlernen",
                "kennenlerngespräch",
                "telefoninterview",
                "phone interview",
                "video interview",
                "zoom",
                "teams meeting",
                "microsoft teams",
                "calendly",
                "termin vereinbaren",
                "termin abstimmen",
                "interview invitation",
                "invitation to interview",
                "schedule an interview",
                "let's schedule",
                "we would like to meet you")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.INTERVIEW,
                    ConfidenceLevel.HIGH,
                    "Email contains interview-related keywords"
            );
        }

        //WAITING
        if (containsAny(text,
                "wir prüfen ihre bewerbung",
                "wir prüfen ihre unterlagen",
                "wir prüfen ihre bewerbungsunterlagen",
                "ihre bewerbung wird geprüft",
                "ihre unterlagen werden geprüft",
                "ihre bewerbung befindet sich im auswahlprozess",
                "ihre bewerbung befindet sich im auswahlverfahren",
                "wir melden uns",
                "wir melden uns zeitnah",
                "wir werden uns bei ihnen melden",
                "we are reviewing your application",
                "your application is under review",
                "we will get back to you",
                "we will contact you")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.WAITING,
                    ConfidenceLevel.HIGH,
                    "Email indicates that the application is being reviewed"
            );
        }

        //APPLIED
        if (containsAny(text,
                "bewerbung gesendet",
                "bewerbung über indeed",
                "bewerbung über stepstone",
                "ihre bewerbung wurde gesendet",
                "ihre bewerbung wurde versendet",
                "vielen dank für ihre bewerbung",
                "danke für ihre bewerbung",
                "wir haben ihre bewerbung erhalten",
                "wir haben ihre bewerbungsunterlagen erhalten",
                "ihre bewerbung ist bei uns eingegangen",
                "bewerbung eingegangen",
                "eingang ihrer bewerbung",
                "thank you for your application",
                "thank you for applying",
                "we received your application",
                "your application has been received",
                "application received",
                "ist raus",
                "deine bewerbung ist raus",
                "bewerbung ist raus",
                "eingangsbestätigung",
                "deine bewerbung auf die stelle",
                "bewerbung auf die stelle")) {

            return new EmailAnalysisResult(
                    true,
                    ApplicationStatus.APPLIED,
                    ConfidenceLevel.HIGH,
                    "Email confirms that the application was submitted or received"
            );
        }


        //assessment
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
                    ApplicationStatus.APPLIED,
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
                    ApplicationStatus.APPLIED,
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

    public AnalysisDecision analyzeDecision(GmailMessageDto email) {
        EmailAnalysisResult result = analyze(email);

        if (!result.jobRelated()) {
            return AnalysisDecision.notJobRelated(result.reason());
        }

        if (result.suggestedStatus() == ApplicationStatus.REJECTED) {
            return AnalysisDecision.needsLlm(
                    "Rejection detection requires OpenAI verification: " + result.reason()
            );
        }

        if (result.suggestedStatus() == ApplicationStatus.INTERVIEW) {
            return AnalysisDecision.needsLlm(
                    "Interview detection requires OpenAI verification: " + result.reason()
            );
        }

        if (result.suggestedStatus() == ApplicationStatus.OFFER) {
            return AnalysisDecision.needsLlm(
                    "Offer detection requires OpenAI verification: " + result.reason()
            );
        }

        if (result.confidence() == ConfidenceLevel.HIGH) {
            return AnalysisDecision.ruleBasedResult(
                    result,
                    "Rule-based provider produced HIGH confidence result: " + result.reason()
            );
        }

        return AnalysisDecision.needsLlm(
                "Rule-based provider produced non-HIGH confidence result: " + result.reason()
        );
    }

}
