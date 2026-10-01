package com.lebane.backend.support;

import com.lebane.backend.department.dto.DepartmentCreateRequest;
import com.lebane.backend.department.dto.DepartmentUpdateRequest;
import com.lebane.backend.department.entity.CurrencyCode;

import java.math.BigDecimal;

/**
 * Valid payloads for the request DTOs. Callers derive invalid variants from these factories by
 * overriding or clearing single components, so each test only has to declare the deviation it
 * cares about.
 */
public final class DepartmentRequests {

    private DepartmentRequests() {
    }

    public static DepartmentCreateRequest createRequest() {
        return new DepartmentCreateRequest(
                "Departamento en Palermo",
                "Luminoso departamento con balcón y living amplio.",
                new BigDecimal("185000.00"),
                CurrencyCode.USD,
                new BigDecimal("92.50"),
                "Av. Santa Fe 1234, Palermo",
                new BigDecimal("-34.588300"),
                new BigDecimal("-58.430000"),
                Boolean.TRUE
        );
    }

    public static DepartmentUpdateRequest updateRequest(Long version) {
        return new DepartmentUpdateRequest(
                "Departamento actualizado en Belgrano",
                "Descripción actualizada tras reforms.",
                new BigDecimal("199999.99"),
                CurrencyCode.ARS,
                new BigDecimal("75.00"),
                "Av. Cabildo 2222, Belgrano",
                new BigDecimal("-34.562000"),
                new BigDecimal("-58.450000"),
                Boolean.FALSE,
                version
        );
    }
}