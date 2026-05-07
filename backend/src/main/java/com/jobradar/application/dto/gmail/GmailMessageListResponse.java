package com.jobradar.application.dto.gmail;

import java.util.List;

public record GmailMessageListResponse(

        List<GmailMessageRef> messages,
        String nextPageToken,
        Integer resultSizeEstimate

) {
    public record GmailMessageRef(
            String id,
            String threadId
    ) {}

}
