package com.lebane.backend.department.repository.projection;

public record DepartmentImageStats(Long departmentId, long imageCount, String primaryImageObjectKey) {
}
