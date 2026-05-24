package com.jobradar.application.service.gmail;

import com.jobradar.application.dto.gmail.GmailMessageDto;
import org.springframework.stereotype.Service;

@Service
public class JobEmailDetector {
    private String safe(String value){
        return value == null ? "" : value;
    }

    public boolean isJobRelated(GmailMessageDto email){
        String text = String.join(" ",
                safe(email.subject()),
                safe(email.from()),
                safe(email.snippet()),
                safe(email.bodyText())
        ).toLowerCase();

        return text.contains("job")
                || text.contains("career")
                || text.contains("application")
                || text.contains("applied")
                || text.contains("interview")
                || text.contains("recruit")
                || text.contains("hr")
                || text.contains("offer")
                || text.contains("unfortunately")
                || text.contains("thank you for applying")
                || text.contains("bewerbung")
                || text.contains("karriere")
                || text.contains("stelle")
                || text.contains("position")
                || text.contains("praktikum")
                || text.contains("werkstudent")
                || text.contains("vorstellungsgespräch")
                || text.contains("absage")
                || text.contains("zusage");
    }
}
