package com.lebane.backend.image.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageResponse(

        Long id,

        @JsonProperty("url")
        String url,

        @JsonProperty("orden")
        int displayOrder,

        @JsonProperty("principal")
        boolean primaryImage

) {
}
