package com.lebane.backend.department.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record DepartmentFilter (

        Boolean available,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal minPrice,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal maxPrice,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal minSquareMeters,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal maxSquareMeters

)
{}
