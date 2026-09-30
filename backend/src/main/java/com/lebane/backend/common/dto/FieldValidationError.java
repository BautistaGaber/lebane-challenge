package com.lebane.backend.common.dto;

public record FieldValidationError(
        String field,
        String message
) {
}
