package com.lebane.backend.department.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lebane.backend.department.entity.CurrencyCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record DepartmentUpdateRequest(

        @JsonProperty("titulo")
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 120)
        String title,

        @JsonProperty("descripcion")
        @NotBlank(message = "Description is required")
        @Size(max = 2000)
        String description,

        @JsonProperty("precio")
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01")
        @Digits(integer = 13, fraction = 2)
        BigDecimal price,

        @JsonProperty("moneda")
        @NotNull(message = "Currency is required")
        CurrencyCode currency,

        @JsonProperty("metrosCuadrados")
        @NotNull(message = "Square meters are required")
        @DecimalMin(value = "0.01")
        @Digits(integer = 8, fraction = 2)
        BigDecimal squareMeters,

        @JsonProperty("direccion")
        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String address,

        @JsonProperty("latitud")
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal latitude,

        @JsonProperty("longitud")
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal longitude,

        @JsonProperty("disponible")
        @NotNull(message = "Availability is required")
        Boolean available,

        @JsonProperty("version")
        @NotNull(message = "Version is required")
        Long version

)
{}
