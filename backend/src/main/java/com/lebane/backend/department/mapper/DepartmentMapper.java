package com.lebane.backend.department.mapper;

import com.lebane.backend.department.dto.*;
import com.lebane.backend.department.entity.Department;
import com.lebane.backend.image.dto.ImageResponse;
import com.lebane.backend.inquiry.dto.InquiryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentCreateRequest request) {
        Department department = Department.create(); //uso create porque la entidad Department es protected

        department.setTitle(request.title());
        department.setDescription(request.description());
        department.setPrice(request.price());
        department.setCurrency(request.currency());
        department.setSquareMeters(request.squareMeters());
        department.setAddress(request.address());
        department.setLatitude(request.latitude());
        department.setLongitude(request.longitude());
        department.setAvailable(request.available());

        return department;
    }

    public void updateEntity(
            Department department,
            DepartmentUpdateRequest request
    ) {
        department.setTitle(request.title());
        department.setDescription(request.description());
        department.setPrice(request.price());
        department.setCurrency(request.currency());
        department.setSquareMeters(request.squareMeters());
        department.setAddress(request.address());
        department.setLatitude(request.latitude());
        department.setLongitude(request.longitude());
        department.setAvailable(request.available());
    }

    public DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getTitle(),
                department.getDescription(),
                department.getPrice(),
                department.getCurrency(),
                department.getSquareMeters(),
                department.getAddress(),
                department.getLatitude(),
                department.getLongitude(),
                department.isAvailable(),
                department.getVersion()
        );
    }

    public DepartmentDetailResponse toDetailResponse(Department department, List<ImageResponse> images, List<InquiryResponse> inquiries) {
        return new DepartmentDetailResponse(
                department.getId(),
                department.getTitle(),
                department.getDescription(),
                department.getPrice(),
                department.getCurrency(),
                department.getSquareMeters(),
                department.getAddress(),
                department.getLatitude(),
                department.getLongitude(),
                department.isAvailable(),
                images,
                inquiries,
                department.getCreatedAt(),
                department.getUpdatedAt(),
                department.getVersion()
        );
    }

    public DepartmentListItemResponse toListItemResponse(Department department,String primaryImageUrl,long imageCount,long inquiryCount) {

        return new DepartmentListItemResponse(
                department.getId(),
                department.getTitle(),
                department.getPrice(),
                department.getCurrency(),
                department.getSquareMeters(),
                department.isAvailable(),
                primaryImageUrl,
                imageCount,
                inquiryCount
        );
    }
}
