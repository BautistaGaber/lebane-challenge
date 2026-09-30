package com.lebane.backend.department.mapper;

import com.lebane.backend.department.dto.DepartmentCreateRequest;
import com.lebane.backend.department.dto.DepartmentResponse;
import com.lebane.backend.department.dto.DepartmentUpdateRequest;
import com.lebane.backend.department.entity.Department;
import org.springframework.stereotype.Component;

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
}
