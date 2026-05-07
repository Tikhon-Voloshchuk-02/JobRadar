package com.jobradar.application.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleRefreshTokenResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("scope")
        String scope,

        @JsonProperty("token_type")
        String tokenType

) {
}
