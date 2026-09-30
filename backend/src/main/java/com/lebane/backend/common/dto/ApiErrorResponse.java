package com.lebane.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(

        @JsonProperty("fecha")
        OffsetDateTime timestamp,

        @JsonProperty("estado")
        int status,

        @JsonProperty("error")
        String error,

        @JsonProperty("mensaje")
        String message,

        @JsonProperty("ruta")
        String path,

        @JsonProperty("erroresValidacion")
        List<FieldValidationError> validationErrors

) {
}
