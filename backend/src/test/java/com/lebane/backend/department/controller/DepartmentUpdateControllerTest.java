package com.lebane.backend.department.controller;

import com.lebane.backend.department.entity.CurrencyCode;
import com.lebane.backend.department.entity.Department;
import com.lebane.backend.support.AbstractIntegrationTest;
import com.lebane.backend.support.DepartmentPayloads;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PUT /api/departamentos/{id}")
class DepartmentUpdateControllerTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("returns 200 and the updated department when the version matches")
    void shouldUpdateDepartmentWhenVersionIsValid() throws Exception {

        // Arrange
        Department department = givenDepartment("Título original", true, "150000.00", "85.00");

        // Act
        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(DepartmentPayloads.updatePayload(0L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(department.getId()))
                .andExpect(jsonPath("$.titulo").value("Departamento actualizado en Belgrano"))
                .andExpect(jsonPath("$.descripcion").value("Descripción actualizada tras reformas."))
                .andExpect(jsonPath("$.precio").value(199999.99))
                .andExpect(jsonPath("$.moneda").value("ARS"))
                .andExpect(jsonPath("$.metrosCuadrados").value(75.00))
                .andExpect(jsonPath("$.direccion").value("Av. Cabildo 2222, Belgrano"))
                .andExpect(jsonPath("$.disponible").value(false));
    }

    @Test
    @DisplayName("persists the changes in PostgreSQL")
    void shouldPersistChangesWhenVersionIsValid() throws Exception {

        // Arrange
        Department department = givenDepartment("Título original", true, "150000.00", "85.00");

        // Act
        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(DepartmentPayloads.updatePayload(0L))))
                .andExpect(status().isOk());

        // Assert
        flush();

        assertThat(departmentRepository.findById(department.getId())).isPresent().get()
                .satisfies(persisted -> {
                    assertThat(persisted.getTitle()).isEqualTo("Departamento actualizado en Belgrano");
                    assertThat(persisted.getPrice()).isEqualByComparingTo("199999.99");
                    assertThat(persisted.getCurrency()).isEqualTo(CurrencyCode.ARS);
                    assertThat(persisted.getSquareMeters()).isEqualByComparingTo("75.00");
                    assertThat(persisted.getAddress()).isEqualTo("Av. Cabildo 2222, Belgrano");
                    assertThat(persisted.isAvailable()).isFalse();
                });
    }

    @Test
    @DisplayName("increments the version and returns it, so the next update can be validated")
    void shouldIncrementAndReturnTheVersion() throws Exception {

        // Arrange
        Department department = givenDepartment();

        // Act
        MvcResult result = mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(DepartmentPayloads.updatePayload(0L))))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        flush();

        assertThat(jsonValueAt(result, "$.version", Long.class)).isEqualTo(1L);
        assertThat(departmentRepository.findById(department.getId()).orElseThrow().getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("returns 404 when the department does not exist")
    void shouldReturn404WhenDepartmentDoesNotExist() throws Exception {

        // Act & Assert
        mockMvc.perform(put("/api/departamentos/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(DepartmentPayloads.updatePayload(0L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("Department with id 999999 was not found"));
    }

    @Test
    @DisplayName("returns 409 and leaves the department untouched when the version is stale")
    void shouldReturn409WhenVersionIsStale() throws Exception {

        // Arrange: a successful update moves the department to version 1
        Department department = givenDepartment("Título original", true, "150000.00", "85.00");

        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(DepartmentPayloads.updatePayload(0L))))
                .andExpect(status().isOk());

        flush();

        // Act: replaying version 0 must be rejected
        Map<String, Object> replayed = DepartmentPayloads.updatePayload(0L);
        replayed.put("titulo", "Escritura perdedora");

        // Assert
        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(replayed)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409))
                .andExpect(jsonPath("$.mensaje").value("The department was modified by another operation"));

        assertThat(departmentRepository.findById(department.getId()).orElseThrow().getTitle())
                .isEqualTo("Departamento actualizado en Belgrano");
    }

    @Test
    @DisplayName("returns 409 when the version is ahead of the persisted one")
    void shouldReturn409WhenVersionIsUnknown() throws Exception {

        // Arrange
        Department department = givenDepartment();

        // Act & Assert
        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(DepartmentPayloads.updatePayload(99L))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409));
    }

    @Test
    @DisplayName("accepts consecutive updates as long as each one uses the latest version")
    void shouldAcceptConsecutiveUpdatesWithTheLatestVersion() throws Exception {

        // Arrange
        Department department = givenDepartment();
        long currentVersion = 0L;

        // Act & Assert
        for (int iteration = 1; iteration <= 3; iteration++) {
            Map<String, Object> payload = DepartmentPayloads.updatePayload(currentVersion);
            payload.put("titulo", "Título versión " + currentVersion);

            MvcResult result = mockMvc.perform(put("/api/departamentos/" + department.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(payload)))
                    .andExpect(status().isOk())
                    .andReturn();

            flush();

            currentVersion = jsonValueAt(result, "$.version", Long.class);
            assertThat(currentVersion).isEqualTo(iteration);
        }

        assertThat(departmentRepository.findById(department.getId()).orElseThrow().getTitle())
                .isEqualTo("Título versión 2");
    }

    @Test
    @DisplayName("returns 400 when the version is missing from the payload")
    void shouldReturn400WhenVersionIsMissing() throws Exception {

        // Arrange
        Department department = givenDepartment();

        Map<String, Object> payload = DepartmentPayloads.updatePayload(0L);
        payload.remove("version");

        // Act & Assert
        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erroresValidacion", hasSize(1)))
                .andExpect(jsonPath("$.erroresValidacion[0].field").value("version"))
                .andExpect(jsonPath("$.erroresValidacion[0].message").value("Version is required"));
    }

    @Test
    @DisplayName("returns 400 when a mandatory field of the payload is missing")
    void shouldReturn400WhenMandatoryFieldIsMissing() throws Exception {

        // Arrange
        Department department = givenDepartment();

        Map<String, Object> payload = DepartmentPayloads.updatePayload(0L);
        payload.remove("metrosCuadrados");

        // Act & Assert
        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erroresValidacion[0].field").value("squareMeters"));
    }

    @Test
    @DisplayName("returns 415 when the update is not sent as JSON")
    void shouldReturn415WhenContentTypeIsNotJson() throws Exception {

        // Arrange
        Department department = givenDepartment();

        // Act & Assert
        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsBytes(DepartmentPayloads.updatePayload(0L))))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.estado").value(415));
    }

    @Test
    @DisplayName("returns 400 when the payload is not valid JSON")
    void shouldReturn400WhenBodyIsMalformed() throws Exception {

        // Arrange
        Department department = givenDepartment();

        // Act & Assert
        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": ".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Malformed request body or unsupported value"));
    }

    @Test
    @DisplayName("keeps the images and inquiries untouched after an update")
    void shouldNotAffectImagesOrInquiries() throws Exception {

        // Arrange
        Department department = givenDepartment();
        givenImages(department, 2);
        givenInquiries(department, 3);
        flush();

        // Act
        mockMvc.perform(put("/api/departamentos/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(DepartmentPayloads.updatePayload(0L))))
                .andExpect(status().isOk());

        // Assert
        mockMvc.perform(get("/api/departamentos/" + department.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenes", hasSize(2)))
                .andExpect(jsonPath("$.consultas", hasSize(3)));

        assertThat(imageRepository.count()).isEqualTo(2);
        assertThat(inquiryRepository.count()).isEqualTo(3);
        assertThat(departmentRepository.findById(department.getId()).orElseThrow().getPrice())
                .isEqualByComparingTo(new BigDecimal("199999.99"));
    }
}