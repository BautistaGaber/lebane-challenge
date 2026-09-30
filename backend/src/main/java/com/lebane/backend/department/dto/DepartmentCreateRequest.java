package com.lebane.backend.department.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lebane.backend.department.entity.CurrencyCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record DepartmentCreateRequest(

        @JsonProperty("titulo")
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 120, message = "Title must contain between 3 and 120 characters")
        String title,

        @JsonProperty("descripcion")
        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must contain at most 2000 characters")
        String description,

        @JsonProperty("precio")
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "Price has an invalid format")
        BigDecimal price,

        @JsonProperty("moneda")
        @NotNull(message = "Currency is required")
        CurrencyCode currency,

        @JsonProperty("metrosCuadrados")
        @NotNull(message = "Square meters are required")
        @DecimalMin(value = "0.01", message = "Square meters must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Square meters have an invalid format")
        BigDecimal squareMeters,

        @JsonProperty("direccion")
        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must contain at most 255 characters")
        String address,

        @JsonProperty("latitud")
        @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
        @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
        BigDecimal latitude,

        @JsonProperty("longitud")
        @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
        @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
        BigDecimal longitude,

        @JsonProperty("disponible")
        @NotNull(message = "Availability is required")
        Boolean available

)
{}
