package com.lebane.backend.department.controller;

import com.lebane.backend.common.exception.InvalidFileException;
import com.lebane.backend.common.exception.StorageException;
import com.lebane.backend.department.entity.Department;
import com.lebane.backend.department.repository.DepartmentRepository;
import com.lebane.backend.image.entity.Image;
import com.lebane.backend.image.repository.ImageRepository;
import com.lebane.backend.support.AbstractIntegrationTest;
import com.lebane.backend.support.DepartmentPayloads;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("POST /api/departamentos")
class DepartmentCreateControllerTest extends AbstractIntegrationTest {

    private static final String ENDPOINT = "/api/departamentos";

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ImageRepository imageRepository;

    private MockMultipartFile jsonPart(Map<String, Object> payload) throws Exception {
        return new MockMultipartFile(
                "departamento",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(payload)
        );
    }

    private static MockMultipartFile imagePart(String fileName) {
        return new MockMultipartFile("imagenes", fileName, "image/jpeg", new byte[]{1, 2, 3, 4});
    }

    private Long persistedDepartmentId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("returns 202 and the created department for a valid multipart request")
    void shouldReturn202WhenRequestIsValid() throws Exception {

        // Arrange
        givenUploadsSucceed();

        // Act
        MvcResult result = mockMvc.perform(multipart(ENDPOINT).file(jsonPart(DepartmentPayloads.createPayload())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Departamento en Palermo"))
                .andExpect(jsonPath("$.precio").value(185000.00))
                .andExpect(jsonPath("$.moneda").value("USD"))
                .andExpect(jsonPath("$.metrosCuadrados").value(92.50))
                .andExpect(jsonPath("$.direccion").value("Av. Santa Fe 1234, Palermo"))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn();
    }

    @Test
    @DisplayName("persists the department in PostgreSQL")
    void shouldPersistDepartmentWhenRequestIsValid() throws Exception {

        // Arrange
        givenUploadsSucceed();

        // Act
        MvcResult result = mockMvc.perform(multipart(ENDPOINT).file(jsonPart(DepartmentPayloads.createPayload())))
                .andExpect(status().isAccepted())
                .andReturn();

        // Assert
        flush();

        Long id = persistedDepartmentId(result);

        assertThat(departmentRepository.findById(id)).isPresent().get()
                .satisfies(department -> {
                    assertThat(department.getTitle()).isEqualTo("Departamento en Palermo");
                    assertThat(department.getPrice()).isEqualByComparingTo("185000.00");
                    assertThat(department.getSquareMeters()).isEqualByComparingTo("92.50");
                    assertThat(department.isAvailable()).isTrue();
                    assertThat(department.getVersion()).isZero();
                    assertThat(department.getImages()).isEmpty();
                });
    }

    @Test
    @DisplayName("creates a department without images when the imagenes part is absent")
    void shouldCreateDepartmentWhenNoImagesAreSent() throws Exception {

        // Arrange
        givenUploadsSucceed();

        // Act
        mockMvc.perform(multipart(ENDPOINT).file(jsonPart(DepartmentPayloads.createPayload())))
                .andExpect(status().isAccepted());

        // Assert
        flush();

        assertThat(departmentRepository.count()).isOne();
        assertThat(imageRepository.count()).isZero();
    }

    @Test
    @DisplayName("creates a department with one image flagged as the primary one")
    void shouldCreateDepartmentWithOneImage() throws Exception {

        // Arrange
        givenUploadsSucceed();

        // Act
        MvcResult result = mockMvc.perform(multipart(ENDPOINT)
                        .file(jsonPart(DepartmentPayloads.createPayload()))
                        .file(imagePart("living.jpg")))
                .andExpect(status().isAccepted())
                .andReturn();

        // Assert
        flush();

        Long departmentId = persistedDepartmentId(result);
        List<Image> images = imageRepository.findAll();

        assertThat(images).hasSize(1);
        assertThat(images.get(0).getDepartment().getId()).isEqualTo(departmentId);
        assertThat(images.get(0).isPrimaryImage()).isTrue();
        assertThat(images.get(0).getDisplayOrder()).isZero();
        assertThat(images.get(0).getContentType()).isEqualTo("image/jpeg");
        assertThat(images.get(0).getObjectKey()).startsWith("departments/" + departmentId + "/").endsWith(".jpg");
    }

    @Test
    @DisplayName("accepts exactly five images and keeps them ordered with a single primary")
    void shouldAcceptExactlyFiveImages() throws Exception {

        // Arrange
        givenUploadsSucceed();

        // Act
        mockMvc.perform(multipart(ENDPOINT)
                        .file(jsonPart(DepartmentPayloads.createPayload()))
                        .file(imagePart("1.jpg")).file(imagePart("2.jpg")).file(imagePart("3.jpg"))
                        .file(imagePart("4.jpg")).file(imagePart("5.jpg")))
                .andExpect(status().isAccepted());

        // Assert
        flush();

        List<Image> images = imageRepository.findAll();

        assertThat(images).hasSize(5);
        assertThat(images).extracting(Image::getDisplayOrder).containsExactly(0, 1, 2, 3, 4);
        assertThat(images).filteredOn(Image::isPrimaryImage).hasSize(1);
        assertThat(images).extracting(Image::getObjectKey).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("returns 400 and lists the missing mandatory fields")
    void shouldReturn400WhenMandatoryFieldsAreMissing() throws Exception {

        // Arrange
        Map<String, Object> payload = DepartmentPayloads.createPayload();
        payload.remove("titulo");
        payload.remove("precio");
        payload.remove("disponible");

        // Act & Assert
        mockMvc.perform(multipart(ENDPOINT).file(jsonPart(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.ruta").value(ENDPOINT))
                .andExpect(jsonPath("$.erroresValidacion", hasSize(3)))
                .andExpect(jsonPath("$.erroresValidacion[*].field",
                        containsInAnyOrder("title", "price", "available")));
    }

    @Test
    @DisplayName("returns 400 when the title is shorter than the allowed minimum")
    void shouldReturn400WhenTitleIsTooShort() throws Exception {

        // Arrange
        Map<String, Object> payload = DepartmentPayloads.createPayload();
        payload.put("titulo", "AB");

        // Act & Assert
        mockMvc.perform(multipart(ENDPOINT).file(jsonPart(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erroresValidacion", hasSize(1)))
                .andExpect(jsonPath("$.erroresValidacion[0].field").value("title"))
                .andExpect(jsonPath("$.erroresValidacion[0].message")
                        .value("Title must contain between 3 and 120 characters"));
    }

    @Test
    @DisplayName("returns 400 when more than five images are sent")
    void shouldReturn400WhenMoreThanFiveImagesAreSent() throws Exception {

        // Arrange
        givenUploadsSucceed();

        // Act & Assert
        mockMvc.perform(multipart(ENDPOINT)
                        .file(jsonPart(DepartmentPayloads.createPayload()))
                        .file(imagePart("1.jpg")).file(imagePart("2.jpg")).file(imagePart("3.jpg"))
                        .file(imagePart("4.jpg")).file(imagePart("5.jpg")).file(imagePart("6.jpg")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("A department cannot contain more than 5 images"));

        assertThat(departmentRepository.count()).isZero();
    }

    @Test
    @DisplayName("returns 400 when the storage layer rejects the file format")
    void shouldReturn400WhenFileIsRejectedByStorage() throws Exception {

        // Arrange
        givenUploadsFail(new InvalidFileException("Unsupported image format"));

        // Act & Assert
        mockMvc.perform(multipart(ENDPOINT)
                        .file(jsonPart(DepartmentPayloads.createPayload()))
                        .file(imagePart("documento.txt")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("Unsupported image format"));
    }

    @Test
    @DisplayName("returns 503 when the storage service is unavailable")
    void shouldReturn503WhenStorageFails() throws Exception {

        // Arrange
        givenUploadsFail(new StorageException("Could not upload image to storage"));

        // Act & Assert
        mockMvc.perform(multipart(ENDPOINT)
                        .file(jsonPart(DepartmentPayloads.createPayload()))
                        .file(imagePart("living.jpg")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.estado").value(503))
                .andExpect(jsonPath("$.mensaje").value("Image storage service is temporarily unavailable"));
    }

    @Test
    @DisplayName("returns 415 when the request is not sent as multipart")
    void shouldReturn415WhenContentTypeIsNotMultipart() throws Exception {

        // Arrange
        String body = objectMapper.writeValueAsString(DepartmentPayloads.createPayload());

        // Act & Assert
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.estado").value(415))
                .andExpect(jsonPath("$.mensaje").value("Unsupported media type"));
    }

    @Test
    @DisplayName("returns 400 when the JSON part is not parseable")
    void shouldReturn400WhenJsonPartIsMalformed() throws Exception {

        // Arrange
        MockMultipartFile malformed = new MockMultipartFile(
                "departamento", "", MediaType.APPLICATION_JSON_VALUE, "{ no soy json".getBytes());

        // Act & Assert
        mockMvc.perform(multipart(ENDPOINT).file(malformed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Malformed request body or unsupported value"));
    }

    @Test
    @DisplayName("discards the department when an upload fails mid-request")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldNotPersistDepartmentWhenUploadFails() throws Exception {

        // Arrange: without the surrounding test transaction the service runs in its own physical
        // transaction, so this assertion observes the real rollback instead of uncommitted state.
        givenUploadsFail(new StorageException("Could not upload image to storage"));

        // Act
        mockMvc.perform(multipart(ENDPOINT)
                        .file(jsonPart(DepartmentPayloads.createPayload()))
                        .file(imagePart("living.jpg")))
                .andExpect(status().isServiceUnavailable());

        // Assert
        assertThat(departmentRepository.findAll())
                .extracting(Department::getTitle)
                .doesNotContain("Departamento en Palermo");
    }
}