package com.lebane.backend.department.service;

import com.lebane.backend.common.dto.PageResponse;
import com.lebane.backend.common.exception.BusinessRuleException;
import com.lebane.backend.common.exception.InvalidFileException;
import com.lebane.backend.common.exception.ResourceConflictException;
import com.lebane.backend.common.exception.ResourceNotFoundException;
import com.lebane.backend.common.exception.StorageException;
import com.lebane.backend.department.dto.DepartmentDetailResponse;
import com.lebane.backend.department.dto.DepartmentFilter;
import com.lebane.backend.department.dto.DepartmentListItemResponse;
import com.lebane.backend.department.dto.DepartmentResponse;
import com.lebane.backend.department.entity.Department;
import com.lebane.backend.department.mapper.DepartmentMapper;
import com.lebane.backend.department.repository.DepartmentListQueryRepository;
import com.lebane.backend.department.repository.DepartmentRepository;
import com.lebane.backend.department.repository.projection.DepartmentImageStats;
import com.lebane.backend.department.repository.projection.DepartmentInquiryStats;
import com.lebane.backend.image.dto.ImageResponse;
import com.lebane.backend.image.entity.Image;
import com.lebane.backend.image.mapper.ImageMapper;
import com.lebane.backend.inquiry.dto.InquiryResponse;
import com.lebane.backend.inquiry.mapper.InquiryMapper;
import com.lebane.backend.storage.StorageService;
import com.lebane.backend.storage.StoredObject;
import com.lebane.backend.support.DepartmentFixtures;
import com.lebane.backend.support.DepartmentRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for the department business rules.
 *
 * <p>Only the outbound collaborators (repositories and {@link StorageService}) are mocked; the
 * mappers are real spies so the assertions run against the actual DTOs the API exposes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService")
class DepartmentServiceTest {

    private static final Long DEPARTMENT_ID = 77L;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private DepartmentListQueryRepository departmentListQueryRepository;

    @Spy
    private DepartmentMapper departmentMapper = new DepartmentMapper();

    @Spy
    private ImageMapper imageMapper = new ImageMapper();

    @Spy
    private InquiryMapper inquiryMapper = new InquiryMapper();

    @InjectMocks
    private DepartmentService departmentService;

    private Department savedDepartment;

    // ------------------------------------------------------------------ CREATE

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates a department when no images are sent")
        void shouldCreateDepartmentWhenNoImagesAreSent() {

            // Arrange
            givenDepartmentWillBePersisted();

            // Act
            DepartmentResponse response = departmentService.create(DepartmentRequests.createRequest(), null);

            // Assert
            assertThat(response.id()).isEqualTo(DEPARTMENT_ID);
            assertThat(response.title()).isEqualTo("Departamento en Palermo");
            assertThat(response.price()).isEqualByComparingTo("185000.00");
            assertThat(response.currency().name()).isEqualTo("USD");
            assertThat(response.available()).isTrue();
            assertThat(response.version()).isZero();

            verifyNoInteractions(storageService);
        }

        @Test
        @DisplayName("creates a department when an empty image list is sent")
        void shouldCreateDepartmentWhenEmptyImageListIsSent() {

            // Arrange
            givenDepartmentWillBePersisted();

            // Act
            DepartmentResponse response = departmentService.create(DepartmentRequests.createRequest(), List.of());

            // Assert
            assertThat(response.id()).isEqualTo(DEPARTMENT_ID);
            verify(departmentRepository).save(any(Department.class));
            verifyNoInteractions(storageService);
        }

        @Test
        @DisplayName("persists every uploaded image attached to the department")
        void shouldCreateDepartmentWithImages() {

            // Arrange
            givenDepartmentWillBePersisted();
            givenUploadsSucceed();

            // Act
            DepartmentResponse response = departmentService.create(
                    DepartmentRequests.createRequest(),
                    List.of(image("living.jpg"), image("balcon.jpg"), image("dormitorio.jpg"))
            );

            // Assert
            assertThat(response.id()).isEqualTo(DEPARTMENT_ID);
            assertThat(savedDepartment.getImages()).hasSize(3);
            assertThat(savedDepartment.getImages())
                    .extracting(Image::getObjectKey)
                    .allMatch(objectKey -> objectKey.startsWith("departments/" + DEPARTMENT_ID + "/"));
            verify(storageService, times(3)).upload(any(MultipartFile.class), anyString());
        }

        @Test
        @DisplayName("rejects more than five images before touching the database")
        void shouldRejectMoreThanFiveImages() {

            // Arrange
            List<MultipartFile> images = List.of(
                    image("1.jpg"), image("2.jpg"), image("3.jpg"),
                    image("4.jpg"), image("5.jpg"), image("6.jpg")
            );

            // Act & Assert
            assertThatThrownBy(() -> departmentService.create(DepartmentRequests.createRequest(), images))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot contain more than 5 images");

            verify(departmentRepository, never()).save(any(Department.class));
            verifyNoInteractions(storageService);
        }

        @Test
        @DisplayName("accepts exactly five images")
        void shouldAcceptExactlyFiveImages() {

            // Arrange
            givenDepartmentWillBePersisted();
            givenUploadsSucceed();

            // Act
            departmentService.create(DepartmentRequests.createRequest(), List.of(
                    image("1.jpg"), image("2.jpg"), image("3.jpg"), image("4.jpg"), image("5.jpg")
            ));

            // Assert
            assertThat(savedDepartment.getImages()).hasSize(5);
            verify(storageService, times(5)).upload(any(MultipartFile.class), anyString());
        }

        @Test
        @DisplayName("flags only the first image as the primary one and orders them by position")
        void shouldMarkFirstImageAsPrimary() {

            // Arrange
            givenDepartmentWillBePersisted();
            givenUploadsSucceed();

            // Act
            departmentService.create(DepartmentRequests.createRequest(), List.of(
                    image("primera.jpg"), image("segunda.jpg"), image("tercera.jpg")
            ));

            // Assert
            assertThat(savedDepartment.getImages())
                    .extracting(Image::getDisplayOrder, Image::isPrimaryImage)
                    .containsExactly(
                            tuple(0, true),
                            tuple(1, false),
                            tuple(2, false)
                    );
        }

        @Test
        @DisplayName("generates a unique object key per image, scoped to the department")
        void shouldGenerateIndependentObjectKeysPerImage() {

            // Arrange
            givenDepartmentWillBePersisted();
            givenUploadsSucceed();

            // Act
            departmentService.create(DepartmentRequests.createRequest(), List.of(
                    image("1.jpg"), image("2.png"), image("3.webp")
            ));

            // Assert
            ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
            verify(storageService, times(3)).upload(any(MultipartFile.class), keys.capture());

            Set<String> captured = Set.copyOf(keys.getAllValues());

            assertThat(captured).hasSize(3);
            assertThat(captured).allSatisfy(objectKey ->
                    assertThat(objectKey).matches("departments/" + DEPARTMENT_ID + "/[0-9a-f-]{36}\\.(jpg|png|webp)"));
        }

        @Test
        @DisplayName("removes the already uploaded objects when a later upload fails")
        void shouldCompensateUploadedObjectsWhenOneUploadFails() {

            // Arrange
            givenDepartmentWillBePersisted();

            AtomicInteger uploadAttempts = new AtomicInteger();

            given(storageService.upload(
                    any(MultipartFile.class),
                    anyString()
            )).willAnswer(invocation -> {

                String objectKey = invocation.getArgument(1);

                if (uploadAttempts.getAndIncrement() == 0) {
                    return new StoredObject(
                            objectKey,
                            "image/jpeg"
                    );
                }

                throw new StorageException(
                        "Could not upload image to storage"
                );
            });

            // Act & Assert
            assertThatThrownBy(() ->
                    departmentService.create(
                            DepartmentRequests.createRequest(),
                            List.of(
                                    image("1.jpg"),
                                    image("2.jpg")
                            )
                    )
            ).isInstanceOf(StorageException.class);

            ArgumentCaptor<String> uploadedKeys =
                    ArgumentCaptor.forClass(String.class);

            ArgumentCaptor<String> deletedKeys =
                    ArgumentCaptor.forClass(String.class);

            verify(storageService, times(2))
                    .upload(
                            any(MultipartFile.class),
                            uploadedKeys.capture()
                    );

            verify(storageService, times(2))
                    .delete(deletedKeys.capture());

            assertThat(deletedKeys.getAllValues())
                    .containsExactlyElementsOf(
                            uploadedKeys.getAllValues()
                    );
        }

        @Test
        @DisplayName("compensates the attempted object key even when the very first upload fails")
        void shouldCompensateTheAttemptedKeyWhenTheFirstUploadFails() {

            // Arrange
            givenDepartmentWillBePersisted();

            StorageException originalFailure =
                    new StorageException(
                            "Could not upload image to storage"
                    );

            given(storageService.upload(
                    any(MultipartFile.class),
                    anyString()
            )).willThrow(originalFailure);

            // Act & Assert
            assertThatThrownBy(() ->
                    departmentService.create(
                            DepartmentRequests.createRequest(),
                            List.of(
                                    image("1.jpg"),
                                    image("2.jpg")
                            )
                    )
            ).isSameAs(originalFailure);

            ArgumentCaptor<String> uploadedKeys =
                    ArgumentCaptor.forClass(String.class);

            ArgumentCaptor<String> deletedKeys =
                    ArgumentCaptor.forClass(String.class);

            verify(storageService, times(1))
                    .upload(
                            any(MultipartFile.class),
                            uploadedKeys.capture()
                    );

            verify(storageService, times(1))
                    .delete(deletedKeys.capture());

            assertThat(deletedKeys.getAllValues())
                    .containsExactlyElementsOf(
                            uploadedKeys.getAllValues()
                    );
        }

        @Test
        @DisplayName("compensates every attempted object key when a later upload fails")
        void shouldCompensateEveryAttemptedKeyWhenALaterUploadFails() {

            // Arrange
            givenDepartmentWillBePersisted();

            AtomicInteger uploadAttempts = new AtomicInteger();

            given(storageService.upload(
                    any(MultipartFile.class),
                    anyString()
            )).willAnswer(invocation -> {

                if (uploadAttempts.getAndIncrement() < 2) {
                    return new StoredObject(
                            invocation.getArgument(1),
                            "image/jpeg"
                    );
                }

                throw new StorageException(
                        "Could not upload image to storage"
                );
            });

            // Act & Assert
            assertThatThrownBy(() ->
                    departmentService.create(
                            DepartmentRequests.createRequest(),
                            List.of(
                                    image("1.jpg"),
                                    image("2.jpg"),
                                    image("3.jpg")
                            )
                    )
            ).isInstanceOf(StorageException.class);

            ArgumentCaptor<String> uploadedKeys =
                    ArgumentCaptor.forClass(String.class);

            ArgumentCaptor<String> deletedKeys =
                    ArgumentCaptor.forClass(String.class);

            verify(storageService, times(3))
                    .upload(
                            any(MultipartFile.class),
                            uploadedKeys.capture()
                    );

            verify(storageService, times(3))
                    .delete(deletedKeys.capture());

            assertThat(deletedKeys.getAllValues())
                    .containsExactlyElementsOf(
                            uploadedKeys.getAllValues()
                    )
                    .doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("keeps compensating the remaining keys when a delete fails")
        void shouldKeepCompensatingRemainingKeysWhenADeleteFails() {

            // Arrange
            givenDepartmentWillBePersisted();

            AtomicInteger uploadAttempts = new AtomicInteger();

            List<String> attemptedKeys = new ArrayList<>();

            given(storageService.upload(
                    any(MultipartFile.class),
                    anyString()
            )).willAnswer(invocation -> {

                String objectKey = invocation.getArgument(1);

                if (uploadAttempts.getAndIncrement() < 2) {
                    attemptedKeys.add(objectKey);
                    return new StoredObject(
                            objectKey,
                            "image/jpeg"
                    );
                }

                attemptedKeys.add(objectKey);
                throw new StorageException(
                        "Could not upload image to storage"
                );
            });

            doAnswer(invocation -> {

                if (invocation.getArgument(0, String.class)
                        .equals(attemptedKeys.get(1))) {
                    throw new StorageException(
                            "Could not delete image from storage"
                    );
                }

                return null;
            }).when(storageService).delete(anyString());

            // Act & Assert
            assertThatThrownBy(() ->
                    departmentService.create(
                            DepartmentRequests.createRequest(),
                            List.of(
                                    image("1.jpg"),
                                    image("2.jpg"),
                                    image("3.jpg")
                            )
                    )
            ).isInstanceOf(StorageException.class)
                    .hasMessage("Could not upload image to storage");

            ArgumentCaptor<String> deletedKeys =
                    ArgumentCaptor.forClass(String.class);

            verify(storageService, times(3))
                    .delete(deletedKeys.capture());

            assertThat(deletedKeys.getAllValues())
                    .containsExactlyElementsOf(attemptedKeys);
        }

        @Test
        @DisplayName("does not mask the original failure when the compensation delete also fails")
        void shouldPropagateOriginalFailureWhenCompensationFails() {

            // Arrange
            givenDepartmentWillBePersisted();

            AtomicInteger uploadAttempts = new AtomicInteger();

            StorageException originalFailure =
                    new StorageException(
                            "Could not upload image to storage"
                    );

            given(storageService.upload(
                    any(MultipartFile.class),
                    anyString()
            )).willAnswer(invocation -> {

                if (uploadAttempts.getAndIncrement() == 0) {
                    return new StoredObject(
                            invocation.getArgument(1),
                            "image/jpeg"
                    );
                }

                throw originalFailure;
            });

            doAnswer(invocation -> {
                throw new StorageException(
                        "Could not delete image from storage"
                );
            }).when(storageService).delete(anyString());

            // Act & Assert
            assertThatThrownBy(() ->
                    departmentService.create(
                            DepartmentRequests.createRequest(),
                            List.of(
                                    image("1.jpg"),
                                    image("2.jpg")
                            )
                    )
            )
                    .isSameAs(originalFailure);

            verify(storageService, times(2))
                    .delete(anyString());
        }

        @Test
        @DisplayName("propagates an invalid file rejection coming from the storage layer")
        void shouldPropagateInvalidFileException() {

            // Arrange
            givenDepartmentWillBePersisted();
            given(storageService.upload(any(MultipartFile.class), anyString()))
                    .willThrow(new InvalidFileException("Unsupported image format"));

            // Act & Assert
            assertThatThrownBy(() -> departmentService.create(DepartmentRequests.createRequest(), List.of(image("1.gif"))))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessage("Unsupported image format");
        }
    }

    // ------------------------------------------------------------------ GET BY ID

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns the department detail including images and inquiries")
        void shouldReturnDetailWhenDepartmentExists() {

            // Arrange
            Department department = DepartmentFixtures.department(DEPARTMENT_ID);
            department.addImage(DepartmentFixtures.image(1L, "departments/77/a.jpg", 0, true));
            department.addImage(DepartmentFixtures.image(2L, "departments/77/b.jpg", 1, false));
            department.addInquiry(DepartmentFixtures.inquiry(9L, "Cliente Uno"));

            given(departmentRepository.findById(DEPARTMENT_ID)).willReturn(Optional.of(department));
            given(storageService.getUrl(anyString()))
                    .willAnswer(invocation -> "https://s3.test/" + invocation.getArgument(0));

            // Act
            DepartmentDetailResponse response = departmentService.getById(DEPARTMENT_ID);

            // Assert
            assertThat(response.id()).isEqualTo(DEPARTMENT_ID);
            assertThat(response.title()).isEqualTo("Departamento en Palermo");
            assertThat(response.images()).extracting(ImageResponse::url)
                    .containsExactly("https://s3.test/departments/77/a.jpg", "https://s3.test/departments/77/b.jpg");
            assertThat(response.images()).extracting(ImageResponse::primaryImage).containsExactly(true, false);
            assertThat(response.inquiries()).extracting(InquiryResponse::name).containsExactly("Cliente Uno");
            assertThat(response.version()).isZero();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the department does not exist")
        void shouldThrowResourceNotFoundWhenDepartmentDoesNotExist() {

            // Arrange
            given(departmentRepository.findById(404L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> departmentService.getById(404L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Department with id 404 was not found");

            verifyNoInteractions(storageService);
        }

        @Test
        @DisplayName("delegates every image URL resolution to the storage service")
        void shouldResolveImageUrlsThroughStorageService() {

            // Arrange
            Department department = DepartmentFixtures.department(DEPARTMENT_ID);
            department.addImage(DepartmentFixtures.image(1L, "departments/77/a.jpg", 0, true));
            department.addImage(DepartmentFixtures.image(2L, "departments/77/b.jpg", 1, false));

            given(departmentRepository.findById(DEPARTMENT_ID)).willReturn(Optional.of(department));
            given(storageService.getUrl(anyString()))
                    .willAnswer(invocation -> "https://s3.test/" + invocation.getArgument(0));

            // Act
            departmentService.getById(DEPARTMENT_ID);

            // Assert
            verify(storageService).getUrl("departments/77/a.jpg");
            verify(storageService).getUrl("departments/77/b.jpg");
            verify(storageService, never()).delete(anyString());
        }
    }

    // ------------------------------------------------------------------ UPDATE

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates the managed entity and returns the new values")
        void shouldUpdateDepartmentWhenVersionIsValid() {

            // Arrange
            Department department = DepartmentFixtures.department(DEPARTMENT_ID);
            given(departmentRepository.findById(DEPARTMENT_ID)).willReturn(Optional.of(department));

            // Act
            DepartmentResponse response = departmentService.update(DEPARTMENT_ID, DepartmentRequests.updateRequest(0L));

            // Assert
            assertThat(department.getTitle()).isEqualTo("Departamento actualizado en Belgrano");
            assertThat(department.getAddress()).isEqualTo("Av. Cabildo 2222, Belgrano");
            assertThat(department.isAvailable()).isFalse();
            assertThat(response.title()).isEqualTo("Departamento actualizado en Belgrano");
            assertThat(response.price()).isEqualByComparingTo("199999.99");
            assertThat(response.currency().name()).isEqualTo("ARS");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the department does not exist")
        void shouldThrowResourceNotFoundWhenDepartmentDoesNotExist() {

            // Arrange
            given(departmentRepository.findById(404L)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> departmentService.update(404L, DepartmentRequests.updateRequest(0L)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Department with id 404 was not found");

            verify(departmentRepository, never()).flush();
        }

        @Test
        @DisplayName("throws ResourceConflictException when the submitted version is stale")
        void shouldThrowResourceConflictWhenVersionIsStale() {

            // Arrange
            Department department = DepartmentFixtures.department(DEPARTMENT_ID);
            ReflectionTestUtils.setField(department, "version", 7L);
            given(departmentRepository.findById(DEPARTMENT_ID)).willReturn(Optional.of(department));

            // Act & Assert
            assertThatThrownBy(() -> departmentService.update(DEPARTMENT_ID, DepartmentRequests.updateRequest(3L)))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessage("The department was modified by another operation");

            assertThat(department.getTitle()).isEqualTo("Departamento en Palermo");
            verify(departmentRepository, never()).flush();
        }

        @Test
        @DisplayName("flushes the pending update so the response carries the incremented version")
        void shouldFlushToReturnTheIncrementedVersion() {

            // Arrange
            Department department = DepartmentFixtures.department(DEPARTMENT_ID);
            given(departmentRepository.findById(DEPARTMENT_ID)).willReturn(Optional.of(department));

            doAnswer(invocation -> {
                ReflectionTestUtils.setField(department, "version", 1L);
                return null;
            }).when(departmentRepository).flush();

            // Act
            DepartmentResponse response = departmentService.update(DEPARTMENT_ID, DepartmentRequests.updateRequest(0L));

            // Assert
            assertThat(response.version()).isEqualTo(1L);

            InOrder inOrder = inOrder(departmentRepository, departmentMapper);
            inOrder.verify(departmentRepository).findById(DEPARTMENT_ID);
            inOrder.verify(departmentMapper).updateEntity(department, DepartmentRequests.updateRequest(0L));
            inOrder.verify(departmentRepository).flush();
            inOrder.verify(departmentMapper).toResponse(department);
        }
    }

    // ------------------------------------------------------------------ LIST

    @Nested
    @DisplayName("list")
    class List_ {

        @Test
        @DisplayName("rejects a negative page number")
        void shouldRejectNegativePage() {

            // Act & Assert
            assertThatThrownBy(() -> departmentService.list(filter(), -1, 20))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Page cannot be negative");

            verifyNoInteractions(departmentRepository, departmentListQueryRepository);
        }

        @ParameterizedTest(name = "size = {0}")
        @ValueSource(ints = {0, -5})
        @DisplayName("rejects a page size lower than one")
        void shouldRejectNonPositivePageSize(int size) {

            // Act & Assert
            assertThatThrownBy(() -> departmentService.list(filter(), 0, size))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Page size must be greater than zero");

            verifyNoInteractions(departmentRepository, departmentListQueryRepository);
        }

        @Test
        @DisplayName("rejects a page size greater than the maximum")
        void shouldRejectPageSizeGreaterThanMaximum() {

            // Act & Assert
            assertThatThrownBy(() -> departmentService.list(filter(), 0, 101))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Page size cannot exceed 100");

            verifyNoInteractions(departmentRepository, departmentListQueryRepository);
        }

        @Test
        @DisplayName("accepts a page size equal to the maximum")
        void shouldAcceptPageSizeEqualToMaximum() {

            // Arrange
            givenAnEmptyPageIsReturned();

            // Act
            PageResponse<DepartmentListItemResponse> response = departmentService.list(filter(), 0, 100);

            // Assert
            assertThat(response.size()).isEqualTo(100);
            assertThat(response.content()).isEmpty();
        }

        @ParameterizedTest(name = "{0} = {1} -> {2}")
        @MethodSource("negativeFilters")
        @DisplayName("rejects a negative filter value")
        void shouldRejectNegativeFilterValues(String filterName, BigDecimal value, String expectedMessage) {

            // Arrange
            DepartmentFilter filter = switch (filterName) {
                case "minPrice" -> new DepartmentFilter(null, value, null, null, null);
                case "maxPrice" -> new DepartmentFilter(null, null, value, null, null);
                case "minSquareMeters" -> new DepartmentFilter(null, null, null, value, null);
                default -> new DepartmentFilter(null, null, null, null, value);
            };

            // Act & Assert
            assertThatThrownBy(() -> departmentService.list(filter, 0, 20))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage(expectedMessage);

            verifyNoInteractions(departmentRepository, departmentListQueryRepository, storageService);
        }

        static Stream<Arguments> negativeFilters() {
            return Stream.of(
                    Arguments.of("minPrice", new BigDecimal("-1"), "Minimum price cannot be negative"),
                    Arguments.of("maxPrice", new BigDecimal("-1"), "Maximum price cannot be negative"),
                    Arguments.of("minSquareMeters", new BigDecimal("-1"), "Minimum square meters cannot be negative"),
                    Arguments.of("maxSquareMeters", new BigDecimal("-1"), "Maximum square meters cannot be negative")
            );
        }

        @Test
        @DisplayName("accepts a zero filter value, which is a valid lower bound")
        void shouldAcceptZeroFilterValue() {

            // Arrange
            givenAnEmptyPageIsReturned();
            DepartmentFilter filter = new DepartmentFilter(null, BigDecimal.ZERO, BigDecimal.ZERO, null, null);

            // Act
            PageResponse<DepartmentListItemResponse> response = departmentService.list(filter, 0, 20);

            // Assert
            assertThat(response.content()).isEmpty();
        }

        @Test
        @DisplayName("rejects a negative minimum price even when the maximum price is also negative")
        void shouldRejectNegativeMinimumPriceBeforeRangeValidation() {

            // Arrange
            DepartmentFilter filter = new DepartmentFilter(null, new BigDecimal("-500"), new BigDecimal("-100"), null, null);

            // Act & Assert
            assertThatThrownBy(() -> departmentService.list(filter, 0, 20))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Minimum price cannot be negative");

            verifyNoInteractions(departmentRepository, departmentListQueryRepository);
        }

        @Test
        @DisplayName("rejects a negative page even when the filters are also invalid")
        void shouldRejectNegativePageBeforeFilterValidation() {

            // Arrange
            DepartmentFilter filter = new DepartmentFilter(null, new BigDecimal("-1"), null, null, null);

            // Act & Assert: pagination is validated first, so the page error wins
            assertThatThrownBy(() -> departmentService.list(filter, -1, 20))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Page cannot be negative");

            verifyNoInteractions(departmentRepository, departmentListQueryRepository);
        }

        @Test
        @DisplayName("rejects a negative minimum price greater than maximum price")
        void shouldRejectMinimumPriceGreaterThanMaximumPrice() {

            // Arrange
            DepartmentFilter filter = new DepartmentFilter(null, new BigDecimal("900"), new BigDecimal("100"), null, null);

            // Act & Assert
            assertThatThrownBy(() -> departmentService.list(filter, 0, 20))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Minimum price cannot be greater than maximum price");

            verifyNoInteractions(departmentRepository, departmentListQueryRepository);
        }

        @Test
        @DisplayName("accepts a minimum price equal to the maximum price")
        void shouldAcceptMinimumPriceEqualToMaximumPrice() {

            // Arrange
            givenAnEmptyPageIsReturned();
            DepartmentFilter filter = new DepartmentFilter(null, new BigDecimal("100"), new BigDecimal("100"), null, null);

            // Act
            PageResponse<DepartmentListItemResponse> response = departmentService.list(filter, 0, 20);

            // Assert
            assertThat(response.content()).isEmpty();
        }

        @Test
        @DisplayName("rejects a minimum square meters greater than the maximum square meters")
        void shouldRejectMinimumSquareMetersGreaterThanMaximumSquareMeters() {

            // Arrange
            DepartmentFilter filter = new DepartmentFilter(null, null, null, new BigDecimal("200"), new BigDecimal("50"));

            // Act & Assert
            assertThatThrownBy(() -> departmentService.list(filter, 0, 20))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Minimum square meters cannot be greater than maximum square meters");

            verifyNoInteractions(departmentRepository, departmentListQueryRepository);
        }

        @Test
        @DisplayName("queries the repository with the requested pagination sorted by id ascending")
        void shouldQueryRepositoryWithRequestedPagination() {

            // Arrange
            givenAnEmptyPageIsReturned();

            // Act
            departmentService.list(filter(), 2, 5);

            // Assert
            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            verify(departmentRepository).findAll(any(Specification.class), pageable.capture());

            assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
            assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
            assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
        }

        @Test
        @DisplayName("merges the image and inquiry statistics returned by the criteria query repository")
        void shouldCombineRepositoryAndListQueryResults() {

            // Arrange
            Department withImages = DepartmentFixtures.department(1L, "Departamento con fotos", true);
            Department withInquiries = DepartmentFixtures.department(2L, "Departamento consultado", true);

            given(departmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(withImages, withInquiries), PageRequest.of(0, 20), 2));

            given(departmentListQueryRepository.findImageStats(List.of(1L, 2L)))
                    .willReturn(List.of(new DepartmentImageStats(1L, 3L, "departments/1/primary.jpg")));

            given(departmentListQueryRepository.findInquiryStats(List.of(1L, 2L)))
                    .willReturn(List.of(new DepartmentInquiryStats(2L, 7L)));

            given(storageService.getUrl("departments/1/primary.jpg")).willReturn("https://s3.test/departments/1/primary.jpg");

            // Act
            PageResponse<DepartmentListItemResponse> response = departmentService.list(filter(), 0, 20);

            // Assert
            assertThat(response.content()).hasSize(2);
            assertThat(response.totalElements()).isEqualTo(2);
            assertThat(response.totalPages()).isEqualTo(1);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();

            DepartmentListItemResponse first = response.content().get(0);
            assertThat(first.id()).isEqualTo(1L);
            assertThat(first.imageCount()).isEqualTo(3);
            assertThat(first.inquiryCount()).isZero();
            assertThat(first.primaryImageUrl()).isEqualTo("https://s3.test/departments/1/primary.jpg");

            DepartmentListItemResponse second = response.content().get(1);
            assertThat(second.id()).isEqualTo(2L);
            assertThat(second.imageCount()).isZero();
            assertThat(second.inquiryCount()).isEqualTo(7);
            assertThat(second.primaryImageUrl()).isNull();
        }

        @Test
        @DisplayName("reports zero images and zero inquiries when there are no statistics rows")
        void shouldReturnZeroCountsWhenStatsAreMissing() {

            // Arrange
            Department department = DepartmentFixtures.department(1L);
            given(departmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(department), PageRequest.of(0, 20), 1));
            given(departmentListQueryRepository.findImageStats(List.of(1L))).willReturn(List.of());
            given(departmentListQueryRepository.findInquiryStats(List.of(1L))).willReturn(List.of());

            // Act
            PageResponse<DepartmentListItemResponse> response = departmentService.list(filter(), 0, 20);

            // Assert
            assertThat(response.content()).singleElement().satisfies(item -> {
                assertThat(item.imageCount()).isZero();
                assertThat(item.inquiryCount()).isZero();
                assertThat(item.primaryImageUrl()).isNull();
            });

            verifyNoInteractions(storageService);
        }

        @Test
        @DisplayName("does not resolve a URL when the department has images but none of them is primary")
        void shouldNotResolveUrlWhenNoPrimaryImageExists() {

            // Arrange
            Department department = DepartmentFixtures.department(1L);
            given(departmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(department), PageRequest.of(0, 20), 1));
            given(departmentListQueryRepository.findImageStats(List.of(1L)))
                    .willReturn(List.of(new DepartmentImageStats(1L, 2L, null)));
            given(departmentListQueryRepository.findInquiryStats(List.of(1L))).willReturn(List.of());

            // Act
            PageResponse<DepartmentListItemResponse> response = departmentService.list(filter(), 0, 20);

            // Assert
            assertThat(response.content()).singleElement().satisfies(item -> {
                assertThat(item.imageCount()).isEqualTo(2);
                assertThat(item.primaryImageUrl()).isNull();
            });

            verifyNoInteractions(storageService);
        }

        @Test
        @DisplayName("returns an empty page without querying statistics when nothing matches the filters")
        void shouldReturnEmptyPageWhenNoDepartmentMatches() {

            // Arrange
            given(departmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            // Act
            PageResponse<DepartmentListItemResponse> response = departmentService.list(
                    new DepartmentFilter(Boolean.TRUE, new BigDecimal("9000000"), null, null, null), 0, 20);

            // Assert
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isZero();
            assertThat(response.totalPages()).isZero();

            verify(departmentListQueryRepository).findImageStats(List.of());
            verify(departmentListQueryRepository).findInquiryStats(List.of());
        }
    }

    // ------------------------------------------------------------------ helpers

    private static MultipartFile image(String fileName) {
        return new MockMultipartFile("imagenes", fileName, "image/jpeg", new byte[]{1, 2, 3});
    }

    private static DepartmentFilter filter() {
        return new DepartmentFilter(null, null, null, null, null);
    }

    /**
     * Stubs the persistence of the department built by the real {@link DepartmentMapper}, assigning
     * the identifier that the identity column strategy would generate.
     */
    private void givenDepartmentWillBePersisted() {
        given(departmentRepository.save(any(Department.class))).willAnswer(invocation -> {
            Department department = invocation.getArgument(0);
            ReflectionTestUtils.setField(department, "id", DEPARTMENT_ID);
            ReflectionTestUtils.setField(department, "version", 0L);
            savedDepartment = department;
            return department;
        });
    }

    private void givenUploadsSucceed() {
        given(storageService.upload(any(MultipartFile.class), anyString()))
                .willAnswer(invocation -> new StoredObject(
                        invocation.getArgument(1),
                        ((MultipartFile) invocation.getArgument(0)).getContentType()));
    }

    private void givenAnEmptyPageIsReturned() {
        given(departmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(1), 0));
        given(departmentListQueryRepository.findImageStats(anyList())).willReturn(List.of());
        given(departmentListQueryRepository.findInquiryStats(anyList())).willReturn(List.of());
    }
}