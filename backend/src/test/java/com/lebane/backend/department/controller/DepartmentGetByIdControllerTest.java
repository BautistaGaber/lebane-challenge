package com.lebane.backend.department.controller;

import com.lebane.backend.department.entity.Department;
import com.lebane.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("GET /api/departamentos/{id}")
class DepartmentGetByIdControllerTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("returns 200 with the full department detail")
    void shouldReturnDepartmentDetail() throws Exception {

        // Arrange
        Department department = givenDepartment("Departamento en Recoleta", false, "275000.00", "110.00");

        // Act & Assert
        mockMvc.perform(get("/api/departamentos/" + department.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(department.getId()))
                .andExpect(jsonPath("$.titulo").value("Departamento en Recoleta"))
                .andExpect(jsonPath("$.descripcion").isNotEmpty())
                .andExpect(jsonPath("$.precio").value(275000.00))
                .andExpect(jsonPath("$.moneda").value("USD"))
                .andExpect(jsonPath("$.metrosCuadrados").value(110.00))
                .andExpect(jsonPath("$.direccion").value("Av. Santa Fe 1234, Palermo"))
                .andExpect(jsonPath("$.latitud").value(-34.588300))
                .andExpect(jsonPath("$.longitud").value(-58.430000))
                .andExpect(jsonPath("$.disponible").value(false))
                .andExpect(jsonPath("$.imagenes", hasSize(0)))
                .andExpect(jsonPath("$.consultas", hasSize(0)))
                .andExpect(jsonPath("$.creadoEn").isNotEmpty())
                .andExpect(jsonPath("$.actualizadoEn").isNotEmpty())
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    @DisplayName("returns the images ordered with their generated URLs and the primary flag")
    void shouldReturnImagesWithGeneratedUrls() throws Exception {

        // Arrange
        Department department = givenDepartment();
        givenImages(department, 2);

        // Act
        MvcResult result = mockMvc.perform(get("/api/departamentos/" + department.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenes", hasSize(2)))
                .andReturn();

        // Assert
        assertThat(jsonValueAt(result, "$.imagenes[0].url", String.class))
                .isEqualTo("https://s3.test/departments/" + department.getId() + "/test-0.jpg");
        assertThat(jsonValueAt(result, "$.imagenes[0].orden", Integer.class)).isZero();
        assertThat(jsonValueAt(result, "$.imagenes[0].principal", Boolean.class)).isTrue();

        assertThat(jsonValueAt(result, "$.imagenes[1].url", String.class))
                .isEqualTo("https://s3.test/departments/" + department.getId() + "/test-1.jpg");
        assertThat(jsonValueAt(result, "$.imagenes[1].orden", Integer.class)).isEqualTo(1);
        assertThat(jsonValueAt(result, "$.imagenes[1].principal", Boolean.class)).isFalse();
    }

    @Test
    @DisplayName("returns the inquiries of the department")
    void shouldReturnInquiries() throws Exception {

        // Arrange
        Department department = givenDepartment();
        givenInquiries(department, 3);

        // Act
        MvcResult result = mockMvc.perform(get("/api/departamentos/" + department.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultas", hasSize(3)))
                .andReturn();

        // Assert
        assertThat(jsonListAt(result, "$.consultas[*].nombre", String.class))
                .containsExactly("Cliente 1", "Cliente 2", "Cliente 3");
        assertThat(jsonListAt(result, "$.consultas[*].mensaje", String.class))
                .allMatch(mensaje -> mensaje.startsWith("Quisiera recibir"));
    }

    @Test
    @DisplayName("returns 404 when the department does not exist")
    void shouldReturn404WhenDepartmentDoesNotExist() throws Exception {

        // Act & Assert
        mockMvc.perform(get("/api/departamentos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("Department with id 999999 was not found"))
                .andExpect(jsonPath("$.ruta").value("/api/departamentos/999999"))
                .andExpect(jsonPath("$.erroresValidacion", hasSize(0)));
    }

    @Test
    @DisplayName("returns 503 when the storage service fails while resolving an image URL")
    void shouldReturn503WhenStorageIsUnavailable() throws Exception {

        // Arrange
        Department department = givenDepartment();
        givenImages(department, 1);
        givenUrlResolutionFails(new com.lebane.backend.common.exception.StorageException("Could not generate image URL"));

        // Act & Assert
        mockMvc.perform(get("/api/departamentos/" + department.getId()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value("Image storage service is temporarily unavailable"));
    }
}