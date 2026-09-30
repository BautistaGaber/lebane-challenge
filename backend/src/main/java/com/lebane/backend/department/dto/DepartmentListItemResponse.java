package com.lebane.backend.department.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lebane.backend.department.entity.CurrencyCode;

import java.math.BigDecimal;

public record DepartmentListItemResponse (

        Long id,

        @JsonProperty("titulo")
        String title,

        @JsonProperty("precio")
        BigDecimal price,

        @JsonProperty("moneda")
        CurrencyCode currency,

        @JsonProperty("metrosCuadrados")
        BigDecimal squareMeters,

        @JsonProperty("disponible")
        boolean available,

        @JsonProperty("imagenPrincipal")
        String primaryImageUrl,

        @JsonProperty("cantidadImagenes")
        long imageCount,

        @JsonProperty("cantidadConsultas")
        long inquiryCount
)
{}
