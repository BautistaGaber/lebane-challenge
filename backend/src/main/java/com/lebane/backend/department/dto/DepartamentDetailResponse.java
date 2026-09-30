package com.lebane.backend.department.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lebane.backend.department.entity.CurrencyCode;
import com.lebane.backend.image.dto.ImageResponse;
import com.lebane.backend.inquiry.dto.InquiryResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record DepartamentDetailResponse (

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

        @JsonProperty("imagenes")
        List<ImageResponse> images,

        @JsonProperty("consultas")
        List<InquiryResponse> inquiries,

        @JsonProperty("creadoEn")
        OffsetDateTime createdAt,

        @JsonProperty("actualizadoEn")
        OffsetDateTime updatedAt,

        Long version

){
}
