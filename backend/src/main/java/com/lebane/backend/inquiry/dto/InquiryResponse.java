package com.lebane.backend.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record InquiryResponse(

        Long id,

        @JsonProperty("nombre")
        String name,

        @JsonProperty("email")
        String email,

        @JsonProperty("mensaje")
        String message,

        @JsonProperty("fecha")
        OffsetDateTime createdAt

) {
}
