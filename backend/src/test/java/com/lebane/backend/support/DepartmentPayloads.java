package com.lebane.backend.support;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builders for the JSON part of the multipart create/update payloads.
 *
 * <p>They return a mutable map so a test only has to declare the deviation it exercises, for
 * example {@code Map<String, Object> payload = createPayload(); payload.remove("precio");}.
 */
public final class DepartmentPayloads {

    private DepartmentPayloads() {
    }

    public static Map<String, Object> createPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("titulo", "Departamento en Palermo");
        payload.put("descripcion", "Luminoso departamento con balcón y living amplio.");
        payload.put("precio", new BigDecimal("185000.00"));
        payload.put("moneda", "USD");
        payload.put("metrosCuadrados", new BigDecimal("92.50"));
        payload.put("direccion", "Av. Santa Fe 1234, Palermo");
        payload.put("latitud", new BigDecimal("-34.588300"));
        payload.put("longitud", new BigDecimal("-58.430000"));
        payload.put("disponible", true);

        return payload;
    }

    public static Map<String, Object> updatePayload(Long version) {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("titulo", "Departamento actualizado en Belgrano");
        payload.put("descripcion", "Descripción actualizada tras reformas.");
        payload.put("precio", new BigDecimal("199999.99"));
        payload.put("moneda", "ARS");
        payload.put("metrosCuadrados", new BigDecimal("75.00"));
        payload.put("direccion", "Av. Cabildo 2222, Belgrano");
        payload.put("latitud", new BigDecimal("-34.562000"));
        payload.put("longitud", new BigDecimal("-58.450000"));
        payload.put("disponible", false);
        payload.put("version", version);

        return payload;
    }
}