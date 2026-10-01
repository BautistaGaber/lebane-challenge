package com.lebane.backend.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.lebane.backend.common.exception.InvalidFileException;
import com.lebane.backend.common.exception.StorageException;
import com.lebane.backend.department.entity.CurrencyCode;
import com.lebane.backend.department.entity.Department;
import com.lebane.backend.department.repository.DepartmentRepository;
import com.lebane.backend.image.entity.Image;
import com.lebane.backend.image.repository.ImageRepository;
import com.lebane.backend.inquiry.entity.Inquiry;
import com.lebane.backend.inquiry.repository.InquiryRepository;
import com.lebane.backend.storage.StorageService;
import com.lebane.backend.storage.StoredObject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Base class for every integration test.
 *
 * <ul>
 *   <li>Real PostgreSQL from Testcontainers wired through {@code @DynamicPropertySource}, so no
 *       developer-local database is ever involved.</li>
 *   <li>Flyway keeps running, therefore the tests exercise the very same schema, indexes and
 *       constraints as production. H2 is not used anywhere.</li>
 *   <li>S3 is isolated behind a mocked {@link StorageService}, so no LocalStack container is
 *       required while PostgreSQL stays real.</li>
 *   <li>Each test runs inside a transaction that is rolled back, which isolates tests from each
 *       other without any manual clean-up and without relying on execution order.</li>
 * </ul>
 */
@SpringBootTest(properties = "app.seed.enabled=false")
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected DepartmentRepository departmentRepository;

    @Autowired
    protected ImageRepository imageRepository;

    @Autowired
    protected InquiryRepository inquiryRepository;

    @Autowired
    protected EntityManager entityManager;

    @MockitoBean
    protected StorageService storageService;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registerDatasource(registry);
    }

    /**
     * Default storage behaviour: URLs resolve to a deterministic fake location. Tests that upload
     * call {@link #givenUploadsSucceed()} explicitly; tests that simulate failures override this.
     */
    @BeforeEach
    void stubStorageUrlResolution() {
        when(storageService.getUrl(anyString()))
                .thenAnswer(invocation -> "https://s3.test/" + invocation.getArgument(0, String.class));
    }

    // ------------------------------------------------------------------ storage stubs

    protected void givenUploadsSucceed() {
        when(storageService.upload(any(MultipartFile.class), anyString()))
                .thenAnswer(invocation -> new StoredObject(
                        invocation.getArgument(1, String.class),
                        invocation.getArgument(0, MultipartFile.class).getContentType()));
    }

    protected void givenUploadsFail(StorageException failure) {
        when(storageService.upload(any(MultipartFile.class), anyString())).thenThrow(failure);
    }

    protected void givenUploadsFail(InvalidFileException failure) {
        when(storageService.upload(any(MultipartFile.class), anyString())).thenThrow(failure);
    }

    protected void givenUrlResolutionFails(StorageException failure) {
        when(storageService.getUrl(anyString())).thenThrow(failure);
    }

    /**
     * Pushes pending changes so assertions observe what Hibernate would send on commit.
     */
    protected void flush() {
        entityManager.flush();
    }

    // ------------------------------------------------------------------ response helpers

    /**
     * Reads a JSON path out of a MockMvc result. Preferred over {@code jsonPath(...).value(...)}
     * whenever several numeric values are compared, because the JSON number types then become
     * explicit instead of depending on the size of each literal.
     */
    protected Object jsonValueAt(MvcResult result, String jsonPath) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), jsonPath);
    }

    protected <T> T jsonValueAt(MvcResult result, String jsonPath, Class<T> type) throws Exception {
        return objectMapper.convertValue(jsonValueAt(result, jsonPath), type);
    }

    protected <T> List<T> jsonListAt(MvcResult result, String jsonPath, Class<T> elementType) throws Exception {
        return objectMapper.convertValue(
                jsonValueAt(result, jsonPath),
                objectMapper.getTypeFactory().constructCollectionType(List.class, elementType)
        );
    }

    // ------------------------------------------------------------------ arrange helpers

    protected Department givenDepartment() {
        return givenDepartment("Departamento en Palermo", true, "150000.00", "85.00");
    }

    protected Department givenDepartment(String title, boolean available, String price, String squareMeters) {
        Department department = Department.create();

        department.setTitle(title);
        department.setDescription("Propiedad usada para preparar el estado inicial de la prueba.");
        department.setPrice(new BigDecimal(price));
        department.setCurrency(CurrencyCode.USD);
        department.setSquareMeters(new BigDecimal(squareMeters));
        department.setAddress("Av. Santa Fe 1234, Palermo");
        department.setLatitude(new BigDecimal("-34.588300"));
        department.setLongitude(new BigDecimal("-58.430000"));
        department.setAvailable(available);

        return departmentRepository.saveAndFlush(department);
    }

    protected Department givenImages(Department department, int imageCount) {
        for (int index = 0; index < imageCount; index++) {
            addImage(department, index, index == 0);
        }

        return department;
    }

    protected Department givenInquiries(Department department, int inquiryCount) {
        for (int index = 0; index < inquiryCount; index++) {
            addInquiry(department, index);
        }

        return department;
    }

    private void addImage(Department department, int index, boolean primaryImage) {
        Image image = Image.create();

        image.setObjectKey("departments/" + department.getId() + "/test-" + index + ".jpg");
        image.setContentType("image/jpeg");
        image.setDisplayOrder(index);
        image.setPrimaryImage(primaryImage);

        department.addImage(image);

        imageRepository.saveAndFlush(image);
    }

    private void addInquiry(Department department, int index) {
        Inquiry inquiry = Inquiry.create();

        inquiry.setName("Cliente " + (index + 1));
        inquiry.setEmail("cliente" + department.getId() + "-" + index + "@example.com");
        inquiry.setMessage("Quisiera recibir más información sobre esta propiedad.");

        department.addInquiry(inquiry);

        inquiryRepository.saveAndFlush(inquiry);
    }
}