package com.lebane.backend.department.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lebane.backend.department.entity.CurrencyCode;

import java.math.BigDecimal;

public record DepartmentResponse(

        Long id,

        @JsonProperty("titulo")
        String title,

        @JsonProperty("descripcion")
        String description,

        @JsonProperty("precio")
        BigDecimal price,

        @JsonProperty("moneda")
        CurrencyCode currency,

        @JsonProperty("metrosCuadrados")
        BigDecimal squareMeters,

        @JsonProperty("direccion")
        String address,

        @JsonProperty("latitud")
        BigDecimal latitude,

        @JsonProperty("longitud")
        BigDecimal longitude,

        @JsonProperty("disponible")
        boolean available,

        Long version

)
{}
