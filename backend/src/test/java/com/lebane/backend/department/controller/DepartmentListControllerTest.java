package com.lebane.backend.department.controller;

import com.lebane.backend.department.entity.Department;
import com.lebane.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("GET /api/departamentos")
class DepartmentListControllerTest extends AbstractIntegrationTest {

    private static final String ENDPOINT = "/api/departamentos";

    @Test
    @DisplayName("returns 200 with an empty page when there are no departments")
    void shouldReturn200WithEmptyContentWhenNoDataExists() throws Exception {

        // Act & Assert
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(0)))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.cantidad").value(20))
                .andExpect(jsonPath("$.totalElementos").value(0))
                .andExpect(jsonPath("$.totalPaginas").value(0))
                .andExpect(jsonPath("$.primera").value(true))
                .andExpect(jsonPath("$.ultima").value(true));
    }

    @Test
    @DisplayName("splits the catalogue across pages using pagina and cantidad")
    void shouldReturnRequestedPage() throws Exception {

        // Arrange
        List<Department> departments = IntStream.rangeClosed(1, 5)
                .mapToObj(index -> givenDepartment("Departamento " + index, true, "100000.00", "80.00"))
                .toList();

        // Act
        mockMvc.perform(get(ENDPOINT).param("pagina", "1").param("cantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(2)))
                .andExpect(jsonPath("$.contenido[0].id").value(departments.get(2).getId()))
                .andExpect(jsonPath("$.contenido[1].id").value(departments.get(3).getId()))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.totalElementos").value(5))
                .andExpect(jsonPath("$.totalPaginas").value(3))
                .andExpect(jsonPath("$.primera").value(false))
                .andExpect(jsonPath("$.ultima").value(false));
    }

    @Test
    @DisplayName("orders the results by id ascending so pagination is stable")
    void shouldOrderResultsByIdAscending() throws Exception {

        // Arrange
        List<Department> departments = IntStream.rangeClosed(1, 3)
                .mapToObj(index -> givenDepartment("Departamento " + index, true, "100000.00", "80.00"))
                .toList();

        // Act
        MvcResult result = mockMvc.perform(get(ENDPOINT).param("cantidad", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        assertThat(jsonListAt(result, "$.contenido[*].id", Long.class)).containsExactly(
                departments.get(0).getId(),
                departments.get(1).getId(),
                departments.get(2).getId()
        );
    }

    @Test
    @DisplayName("filters by availability")
    void shouldFilterByAvailable() throws Exception {

        // Arrange
        Department available = givenDepartment("Disponible", true, "150000.00", "80.00");
        givenDepartment("No disponible", false, "150000.00", "80.00");

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param("disponible", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(available.getId()));

        mockMvc.perform(get(ENDPOINT).param("disponible", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].disponible").value(false));
    }

    @Test
    @DisplayName("filters by minimum and maximum price, bounds included")
    void shouldFilterByPriceRange() throws Exception {

        // Arrange
        Department barato = givenDepartment("Barato", true, "80000.00", "80.00");
        Department medio = givenDepartment("Medio", true, "200000.00", "80.00");
        Department caro = givenDepartment("Caro", true, "450000.00", "80.00");

        // Act: 80000 and 200000 are both inside the range because the bounds are inclusive
        MvcResult inclusive = mockMvc.perform(get(ENDPOINT).param("precioMin", "80000").param("precioMax", "200000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andReturn();

        MvcResult aboveMinimum = mockMvc.perform(get(ENDPOINT).param("precioMin", "90000"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        assertThat(jsonListAt(inclusive, "$.contenido[*].id", Long.class))
                .containsExactly(barato.getId(), medio.getId());

        assertThat(jsonListAt(aboveMinimum, "$.contenido[*].id", Long.class))
                .containsExactly(medio.getId(), caro.getId());
    }

    @Test
    @DisplayName("filters by minimum and maximum square meters, bounds included")
    void shouldFilterBySquareMetersRange() throws Exception {

        // Arrange
        Department chico = givenDepartment("Chico", true, "150000.00", "40.00");
        Department medio = givenDepartment("Medio", true, "150000.00", "90.00");
        Department grande = givenDepartment("Grande", true, "150000.00", "220.00");

        // Act
        MvcResult inclusive = mockMvc.perform(get(ENDPOINT).param("metrosCuadradosMin", "40").param("metrosCuadradosMax", "90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andReturn();

        MvcResult belowMaximum = mockMvc.perform(get(ENDPOINT).param("metrosCuadradosMax", "100"))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        assertThat(jsonListAt(inclusive, "$.contenido[*].id", Long.class))
                .containsExactly(chico.getId(), medio.getId());

        assertThat(jsonListAt(belowMaximum, "$.contenido[*].id", Long.class))
                .containsExactly(chico.getId(), medio.getId());

        assertThat(jsonListAt(belowMaximum, "$.contenido[*].id", Long.class))
                .doesNotContain(grande.getId());
    }

    @Test
    @DisplayName("combines availability with the price range")
    void shouldCombineFilters() throws Exception {

        // Arrange
        Department keeping = givenDepartment("Cumple todo", true, "200000.00", "90.00");
        givenDepartment("Disponible pero caro", true, "900000.00", "90.00");
        givenDepartment("Precio correcto pero no disponible", false, "200000.00", "90.00");

        // Act & Assert
        mockMvc.perform(get(ENDPOINT)
                        .param("disponible", "true")
                        .param("precioMin", "100000")
                        .param("precioMax", "300000")
                        .param("metrosCuadradosMin", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(keeping.getId()));
    }

    @Test
    @DisplayName("returns an empty page when no department matches the filters")
    void shouldReturnEmptyContentWhenNoDepartmentMatches() throws Exception {

        // Arrange
        givenDepartment("Barato", true, "80000.00", "40.00");

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param("precioMin", "9000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(0)))
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    @DisplayName("reports the image and inquiry counts of every listed department")
    void shouldReportImageAndInquiryCounts() throws Exception {

        // Arrange: created first, therefore it is the first item of the id-ascending ordering
        Department conDatos = givenDepartment("Con fotos y consultas", true, "150000.00", "80.00");
        givenImages(conDatos, 3);
        givenInquiries(conDatos, 2);

        Department sinDatos = givenDepartment("Sin fotos ni consultas", true, "150000.00", "80.00");

        // Act
        MvcResult result = mockMvc.perform(get(ENDPOINT).param("cantidad", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andReturn();

        // Assert
        assertThat(jsonValueAt(result, "$.contenido[0].cantidadImagenes", Integer.class)).isEqualTo(3);
        assertThat(jsonValueAt(result, "$.contenido[0].cantidadConsultas", Integer.class)).isEqualTo(2);
        assertThat(jsonValueAt(result, "$.contenido[0].imagenPrincipal", String.class))
                .isEqualTo("https://s3.test/departments/" + conDatos.getId() + "/test-0.jpg");

        assertThat(jsonValueAt(result, "$.contenido[1].cantidadImagenes", Integer.class)).isZero();
        assertThat(jsonValueAt(result, "$.contenido[1].cantidadConsultas", Integer.class)).isZero();
        assertThat(jsonValueAt(result, "$.contenido[1].imagenPrincipal")).isNull();
        assertThat(jsonValueAt(result, "$.contenido[1].id", Long.class)).isEqualTo(sinDatos.getId());
    }

    @Test
    @DisplayName("returns 400 when the minimum price is greater than the maximum price")
    void shouldReturn400WhenMinimumPriceIsGreaterThanMaximumPrice() throws Exception {

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param("precioMin", "900").param("precioMax", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("Minimum price cannot be greater than maximum price"));
    }

    @ParameterizedTest(name = "{0}={1} -> {2}")
    @MethodSource("negativeFilters")
    @DisplayName("returns 400 when a filter value is negative")
    void shouldReturn400WhenAFilterValueIsNegative(String paramName, String paramValue, String expectedMessage) throws Exception {

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param(paramName, paramValue))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.ruta").value(ENDPOINT))
                .andExpect(jsonPath("$.mensaje").value(expectedMessage))
                .andExpect(jsonPath("$.erroresValidacion").isEmpty());
    }

    static Stream<Arguments> negativeFilters() {
        return Stream.of(
                Arguments.of("precioMin", "-1", "Minimum price cannot be negative"),
                Arguments.of("precioMax", "-1", "Maximum price cannot be negative"),
                Arguments.of("metrosCuadradosMin", "-1", "Minimum square meters cannot be negative"),
                Arguments.of("metrosCuadradosMax", "-1", "Maximum square meters cannot be negative")
        );
    }

    @Test
    @DisplayName("returns 400 when a negative filter value is combined with an inverted range")
    void shouldReturn400WhenANegativeFilterValueIsCombinedWithAnInvertedRange() throws Exception {

        // Act & Assert: the negative check runs before the range check
        mockMvc.perform(get(ENDPOINT).param("precioMin", "-500").param("precioMax", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.ruta").value(ENDPOINT))
                .andExpect(jsonPath("$.mensaje").value("Minimum price cannot be negative"))
                .andExpect(jsonPath("$.erroresValidacion").isEmpty());
    }

    @Test
    @DisplayName("returns 200 when a filter value is zero, which is a valid lower bound")
    void shouldReturn200WhenAFilterValueIsZero() throws Exception {

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param("precioMin", "0").param("metrosCuadradosMin", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(0)))
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    @DisplayName("returns 400 when the minimum surface is greater than the maximum surface")
    void shouldReturn400WhenMinimumSquareMetersIsGreaterThanMaximumSquareMeters() throws Exception {

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param("metrosCuadradosMin", "200").param("metrosCuadradosMax", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Minimum square meters cannot be greater than maximum square meters"));
    }

    @Test
    @DisplayName("returns 400 when the page number is negative")
    void shouldReturn400WhenPageIsNegative() throws Exception {

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Page cannot be negative"));
    }

    @Test
    @DisplayName("returns 400 when the page size is zero")
    void shouldReturn400WhenPageSizeIsNotPositive() throws Exception {

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param("cantidad", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Page size must be greater than zero"));
    }

    @Test
    @DisplayName("returns 400 when the page size exceeds the maximum")
    void shouldReturn400WhenPageSizeExceedsMaximum() throws Exception {

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param("cantidad", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Page size cannot exceed 100"));
    }

    @Test
    @DisplayName("accepts a page size equal to the maximum")
    void shouldAcceptPageSizeEqualToMaximum() throws Exception {

        // Arrange
        givenDepartment("Único", true, "150000.00", "80.00");

        // Act & Assert
        mockMvc.perform(get(ENDPOINT).param("cantidad", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(100))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }
}