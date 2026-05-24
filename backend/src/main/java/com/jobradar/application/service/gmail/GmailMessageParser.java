package com.jobradar.application.service.gmail;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class GmailMessageParser {
    public String extractBodyText(Map fullMessage){
        Map payload = (Map) fullMessage.get("payload");

        if(payload==null) {return "";}

        return extractBodyFromPayload(payload);
    }

    private String extractBodyFromPayload(Map payload) {
        String mimeType = (String) payload.get("mimeType");

        if ("text/plain".equalsIgnoreCase(mimeType)) {
            return decodeBody(payload);
        }

        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) payload.get("parts");

        if (parts == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (Map<String, Object> part : parts) {
            result.append(extractBodyFromPayload(part)).append(" ");
        }

        return result.toString().trim();
    }

    private String decodeBody(Map payload){
        Map body = (Map) payload.get("body");

        if(body ==null || body.get("data")==null){ return "";}

        String data = (String) body.get("data");

        byte[] decodedBytes = Base64.getUrlDecoder().decode(data);

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public String extractHeader(Map fullMessage, String headerName){
        Map payload = (Map) fullMessage.get("payload");

        if (payload == null) {
            return null;
        }

        List<Map<String, String>> headers =
                (List<Map<String, String>>) payload.get("headers");

        if (headers == null) { return null; }

        return headers.stream()
                .filter(header -> headerName.equalsIgnoreCase(header.get("name")))
                .map(header -> header.get("value"))
                .findFirst()
                .orElse(null);

    }
}
