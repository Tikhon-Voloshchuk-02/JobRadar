package com.jobradar.application.service.gmail;

import com.jobradar.application.dto.gmail.GmailMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobEmailDetector {

    private static final Logger log = LoggerFactory.getLogger(JobEmailDetector.class);

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

    public boolean isJobRelated(GmailMessageDto email) {
        String text = normalize(String.join(" ",
                safe(email.subject()),
                safe(email.from()),
                safe(email.snippet()),
                safe(email.bodyText())
        ));

        log.info("JOB DETECTOR TEXT = {}", text);


        boolean result = text.contains("job")
                || text.contains("career")
                || text.contains("application")
                || text.contains("applied")
                || text.contains("interview")
                || text.contains("recruit")
                || text.contains("offer")
                || text.contains("unfortunately")
                || text.contains("thank you for applying")

                || text.contains("bewerbung")
                || text.contains("beworben")
                || text.contains("bewerber")
                || text.contains("bewerbungsunterlagen")
                || text.contains("bewerbungsprozess")

                || text.contains("indeed")
                || text.contains("stepstone")
                || text.contains("linkedin")
                || text.contains("xing")

                || text.contains("karriere")
                || text.contains("stelle")
                || text.contains("stellenanzeige")
                || text.contains("praktikum")
                || text.contains("werkstudent")
                || text.contains("vorstellungsgespraech")
                || text.contains("einladung")
                || text.contains("absage")
                || text.contains("zusage")

                || text.contains("assessment")
                || text.contains("recruiting")
                || text.contains("talent acquisition");

        log.info("JOB DETECTOR RESULT = {}", result);
        return result;
    }
}
