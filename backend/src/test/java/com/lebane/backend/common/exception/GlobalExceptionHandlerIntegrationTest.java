package com.lebane.backend.common.exception;

import com.lebane.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks that {@link GlobalExceptionHandler} maps every exception of the API to the right status
 * code and payload, exercised through the real endpoints rather than calling the handlers
 * directly. The only case exercised outside an endpoint is {@link MaxUploadSizeExceededException},
 * which can only be produced by changing the multipart limits of the running context.
 */
@DisplayName("GlobalExceptionHandler status mapping")
class GlobalExceptionHandlerIntegrationTest extends AbstractIntegrationTest {

    private static final String DEPARTMENTS = "/api/departamentos";

    @Test
    @DisplayName("ResourceNotFoundException maps to 404 with a complete error body")
    void shouldMapResourceNotFoundToNotFound() throws Exception {

        // Act & Assert
        mockMvc.perform(get(DEPARTMENTS + "/424242"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.fecha").isNotEmpty())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("Department with id 424242 was not found"))
                .andExpect(jsonPath("$.ruta").value(DEPARTMENTS + "/424242"))
                .andExpect(jsonPath("$.erroresValidacion", hasSize(0)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("businessRuleViolations")
    @DisplayName("BusinessRuleException maps to 400")
    void shouldMapBusinessRuleToBadRequest(String scenario, MockHttpServletRequestBuilder request, String expectedMessage) throws Exception {

        // Act & Assert
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value(expectedMessage))
                .andExpect(jsonPath("$.ruta").value(DEPARTMENTS))
                .andExpect(jsonPath("$.erroresValidacion", hasSize(0)));
    }

    static Stream<Arguments> businessRuleViolations() {
        return Stream.of(
                Arguments.of("negative page",
                        get(DEPARTMENTS).param("pagina", "-1"),
                        "Page cannot be negative"),
                Arguments.of("page size below one",
                        get(DEPARTMENTS).param("cantidad", "0"),
                        "Page size must be greater than zero"),
                Arguments.of("page size above the maximum",
                        get(DEPARTMENTS).param("cantidad", "101"),
                        "Page size cannot exceed 100"),
                Arguments.of("minimum price above maximum price",
                        get(DEPARTMENTS).param("precioMin", "900").param("precioMax", "100"),
                        "Minimum price cannot be greater than maximum price"),
                Arguments.of("minimum surface above maximum surface",
                        get(DEPARTMENTS).param("metrosCuadradosMin", "200").param("metrosCuadradosMax", "50"),
                        "Minimum square meters cannot be greater than maximum square meters")
        );
    }

    @Test
    @DisplayName("ResourceConflictException maps to 409 with the service message")
    void shouldMapResourceConflictToConflict() throws Exception {

        // Arrange
        var department = givenDepartment();
        givenDepartment();

        // Act & Assert: replaying the already consumed version 0 must be a conflict
        mockMvc.perform(put(DEPARTMENTS + "/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validUpdatePayload(5L))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.mensaje").value("The department was modified by another operation"));
    }

    @Test
    @DisplayName("InvalidFileException maps to 400")
    void shouldMapInvalidFileToBadRequest() throws Exception {

        // Arrange: S3StorageService rejects unsupported formats with InvalidFileException
        givenUploadsFail(new InvalidFileException("Unsupported image format"));

        // Act & Assert
        mockMvc.perform(multipart(DEPARTMENTS)
                        .file(jsonPart(validCreatePayload()))
                        .file(new MockMultipartFile("imagenes", "documento.txt", "text/plain", "contenido".getBytes())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("Unsupported image format"))
                .andExpect(jsonPath("$.erroresValidacion", hasSize(0)));
    }

    @Test
    @DisplayName("StorageException maps to 503 with a generic message")
    void shouldMapStorageToServiceUnavailable() throws Exception {

        // Arrange
        givenUploadsFail(new StorageException("Conexión rechazada por el bucket"));

        // Act & Assert: the internal cause must not leak to the client
        mockMvc.perform(multipart(DEPARTMENTS)
                        .file(jsonPart(validCreatePayload()))
                        .file(new MockMultipartFile("imagenes", "living.jpg", "image/jpeg", new byte[]{1, 2, 3})))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.estado").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.mensaje").value("Image storage service is temporarily unavailable"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException maps to 400 with the offending fields")
    void shouldMapBeanValidationToBadRequest() throws Exception {

        // Arrange
        Map<String, Object> payload = validCreatePayload();
        payload.remove("titulo");
        payload.put("precio", "-1");

        // Act & Assert
        mockMvc.perform(multipart(DEPARTMENTS).file(jsonPart(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("Request validation failed"))
                .andExpect(jsonPath("$.ruta").value(DEPARTMENTS))
                .andExpect(jsonPath("$.erroresValidacion", hasSize(2)))
                .andExpect(jsonPath("$.erroresValidacion[*].field",
                        containsInAnyOrder("title", "price")));
    }

    @Test
    @DisplayName("HttpMessageNotReadableException maps to 400")
    void shouldMapUnreadableBodyToBadRequest() throws Exception {

        // Arrange
        var department = givenDepartment();

        // Act & Assert
        mockMvc.perform(put(DEPARTMENTS + "/" + department.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\": \"no-es-un-numero\"}".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Malformed request body or unsupported value"));
    }

    @Test
    @DisplayName("HttpMediaTypeNotSupportedException maps to 415")
    void shouldMapUnsupportedMediaTypeToUnsupportedMediaType() throws Exception {

        // Act & Assert
        mockMvc.perform(post(DEPARTMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validCreatePayload())))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.estado").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.mensaje").value("Unsupported media type"))
                .andExpect(jsonPath("$.ruta").value(DEPARTMENTS));
    }

    @Test
    @DisplayName("an unparseable query parameter maps to 400 instead of bubbling up as 500")
    void shouldMapTypeMismatchToBadRequest() throws Exception {

        // Act & Assert
        mockMvc.perform(get(DEPARTMENTS).param("pagina", "no-es-un-numero"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("Request validation failed"))
                .andExpect(jsonPath("$.erroresValidacion", hasSize(1)))
                .andExpect(jsonPath("$.erroresValidacion[0].field").value("pagina"));

        mockMvc.perform(get(DEPARTMENTS).param("precioMin", "mucho"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erroresValidacion[0].field").value("precioMin"));
    }

    private MockMultipartFile jsonPart(Map<String, Object> payload) throws Exception {
        return new MockMultipartFile(
                "departamento", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(payload));
    }

    private Map<String, Object> validCreatePayload() {
        return com.lebane.backend.support.DepartmentPayloads.createPayload();
    }

    private Map<String, Object> validUpdatePayload(Long version) {
        return com.lebane.backend.support.DepartmentPayloads.updatePayload(version);
    }
}