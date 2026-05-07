package com.jobradar.application.dto.gmail;

import java.util.List;

public record GmailMessageDetailResponse(
        String id,
        String threadId,
        String snippet,
        Payload payload
) {
    public record Payload(
            List<Header> headers,
            String mimeType,
            Body body,
            List<Part> parts
    ) {}

    public record Header(
            String name,
            String value
    ) {}

    public record Body(
            String data,
            Integer size
    ) {}

    public record Part(
            String mimeType,
            Body body,
            List<Part> parts
    ) {}
}
