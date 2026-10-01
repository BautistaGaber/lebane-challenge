package com.lebane.backend.department.service;

import com.lebane.backend.common.dto.PageResponse;
import com.lebane.backend.common.exception.BusinessRuleException;
import com.lebane.backend.common.exception.ResourceConflictException;
import com.lebane.backend.common.exception.ResourceNotFoundException;
import com.lebane.backend.department.dto.*;
import com.lebane.backend.department.entity.Department;
import com.lebane.backend.department.mapper.DepartmentMapper;
import com.lebane.backend.department.repository.DepartmentListQueryRepository;
import com.lebane.backend.department.repository.DepartmentRepository;
import com.lebane.backend.department.repository.projection.DepartmentImageStats;
import com.lebane.backend.department.repository.projection.DepartmentInquiryStats;
import com.lebane.backend.department.specification.DepartmentSpecifications;
import com.lebane.backend.storage.StorageService;
import com.lebane.backend.storage.StoredObject;
import com.lebane.backend.image.dto.ImageResponse;
import com.lebane.backend.image.entity.Image;
import com.lebane.backend.image.mapper.ImageMapper;
import com.lebane.backend.inquiry.dto.InquiryResponse;
import com.lebane.backend.inquiry.mapper.InquiryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DepartmentService {

    private static final int MAX_IMAGES_PER_DEPARTMENT = 5;
    private static final int MAX_PAGE_SIZE = 100;

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final StorageService storageService;
    private final ImageMapper imageMapper;
    private final InquiryMapper inquiryMapper;
    private final DepartmentListQueryRepository departmentListQueryRepository;

    public DepartmentService(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper, StorageService storageService, ImageMapper imageMapper, InquiryMapper inquiryMapper, DepartmentListQueryRepository departmentListQueryRepository) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
        this.storageService = storageService;
        this.imageMapper = imageMapper;
        this.inquiryMapper = inquiryMapper;
        this.departmentListQueryRepository = departmentListQueryRepository;
    }

    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request, List<MultipartFile> images) {

        List<MultipartFile> safeImages = images == null ? List.of() : images;

        validateImageCount(safeImages);

        Department department = departmentMapper.toEntity(request);

        Department savedDepartment = departmentRepository.save(department);

        List<String> uploadedObjectKeys = new ArrayList<>();

        try {

            for (int index = 0; index < safeImages.size(); index++) {

                MultipartFile file = safeImages.get(index);

                String objectKey = buildObjectKey(savedDepartment.getId(), file);

                uploadedObjectKeys.add(objectKey);

                StoredObject storedObject = storageService.upload(file,objectKey);

                Image image = buildImage(storedObject,index);

                savedDepartment.addImage(image);
            }

            return departmentMapper.toResponse(savedDepartment);

        } catch (RuntimeException exception) {

            compensateUploads(uploadedObjectKeys);

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public DepartmentDetailResponse getById(Long id) {

        Department department = findDepartmentById(id);

        List<ImageResponse> images = department.getImages().stream().map(image -> imageMapper.toResponse(image, storageService.getUrl(image.getObjectKey()))).toList();

        List<InquiryResponse> inquiries = department.getInquiries().stream().map(inquiryMapper::toResponse).toList();

        return departmentMapper.toDetailResponse(department, images, inquiries);
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentUpdateRequest request) {

        Department department = findDepartmentById(id);

        validateVersion(department, request.version());

        departmentMapper.updateEntity(department, request);

        // The @Version increment happens on flush. Without it the response would carry the previous
        // version and a client replaying it would bypass optimistic locking.
        departmentRepository.flush();

        return departmentMapper.toResponse(department);
    }

    @Transactional(readOnly = true)
    public PageResponse<DepartmentListItemResponse> list(DepartmentFilter filter,int page,int size) {

        validatePagination(page, size);
        validateFilters(filter);

        Pageable pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.ASC,"id"));

        Specification<Department> specification =buildSpecification(filter);

        Page<Department> departmentPage = departmentRepository.findAll(specification,pageable);

        List<Long> departmentIds = departmentPage.getContent()
                        .stream()
                        .map(Department::getId)
                        .toList();

        Map<Long, DepartmentImageStats> imageStats = departmentListQueryRepository
                        .findImageStats(departmentIds)
                        .stream()
                        .collect( Collectors.toMap(DepartmentImageStats::departmentId,Function.identity()));

        Map<Long, Long> inquiryCounts = departmentListQueryRepository
                        .findInquiryStats(departmentIds)
                        .stream()
                        .collect(Collectors.toMap(DepartmentInquiryStats::departmentId,DepartmentInquiryStats::inquiryCount));

        List<DepartmentListItemResponse> content = departmentPage.getContent().stream().map(department ->
                                toListItemResponse(department,imageStats.get(department.getId()),
                                        inquiryCounts.getOrDefault(department.getId(),0L))).toList();

        return PageResponse.from(departmentPage,content);
    }

    private Specification<Department> buildSpecification(DepartmentFilter filter) {

        return Specification
                .allOf(DepartmentSpecifications.hasAvailable(filter.available()))
                .and(DepartmentSpecifications.hasMinimumPrice(filter.minPrice()))
                .and(DepartmentSpecifications.hasMaximumPrice(filter.maxPrice()))
                .and(DepartmentSpecifications.hasMinimumSquareMeters(filter.minSquareMeters()))
                .and(DepartmentSpecifications.hasMaximumSquareMeters(filter.maxSquareMeters()));
    }

    private void validateImageCount(List<MultipartFile> images) {

        if (images.size() > MAX_IMAGES_PER_DEPARTMENT) {
            throw new BusinessRuleException("A department cannot contain more than 5 images");
        }
    }

    private Image buildImage(StoredObject storedObject, int index) {

        Image image = Image.create();

        image.setObjectKey(storedObject.objectKey());
        image.setContentType(storedObject.contentType());
        image.setDisplayOrder(index);
        image.setPrimaryImage(index == 0);

        return image;
    }

    private String buildObjectKey(Long departmentId, MultipartFile file) {

        String extension = resolveExtension(file.getContentType());

        return "departments/" + departmentId + "/" + UUID.randomUUID() + extension;
    }

    private String resolveExtension(String contentType) {

        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";

            default -> "";
        };
    }

    private void compensateUploads(List<String> uploadedObjectKeys) {
        for (String objectKey : uploadedObjectKeys) {
            try {
                storageService.delete(objectKey);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department with id " + id + " was not found"));
    }

    private void validateVersion(Department department, Long requestedVersion) {
        if (!department.getVersion().equals(requestedVersion)) {
            throw new ResourceConflictException("The department was modified by another operation");
        }
    }

    private void validateFilters(DepartmentFilter filter) {

        validateNonNegative(filter.minPrice(),"Minimum price cannot be negative");
        validateNonNegative(filter.maxPrice(),"Maximum price cannot be negative");
        validateNonNegative(filter.minSquareMeters(),"Minimum square meters cannot be negative");
        validateNonNegative(filter.maxSquareMeters(),"Maximum square meters cannot be negative");

        if (filter.minPrice() != null && filter.maxPrice() != null && filter.minPrice().compareTo(filter.maxPrice()) > 0) {

            throw new BusinessRuleException("Minimum price cannot be greater than maximum price");
        }

        if (filter.minSquareMeters() != null && filter.maxSquareMeters() != null && filter.minSquareMeters().compareTo(filter.maxSquareMeters()) > 0) {

            throw new BusinessRuleException("Minimum square meters cannot be greater than maximum square meters");
        }
    }

    private DepartmentListItemResponse toListItemResponse(Department department,DepartmentImageStats imageStats,long inquiryCount) {

        long imageCount =imageStats == null? 0: imageStats.imageCount();

        String primaryImageUrl = null;

        if (imageStats != null && imageStats.primaryImageObjectKey() != null) {

            primaryImageUrl = storageService.getUrl(imageStats.primaryImageObjectKey());
        }

        return departmentMapper.toListItemResponse(
                department,
                primaryImageUrl,
                imageCount,
                inquiryCount
        );
    }

    private void validatePagination(int page,int size) {

        if (page < 0) {
            throw new BusinessRuleException("Page cannot be negative");
        }

        if (size < 1) {
            throw new BusinessRuleException("Page size must be greater than zero");
        }

        if (size > MAX_PAGE_SIZE) {
            throw new BusinessRuleException("Page size cannot exceed "+ MAX_PAGE_SIZE
            );
        }
    }

    private void validateNonNegative(BigDecimal value,String message) {

        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException(message);
        }
    }
}
