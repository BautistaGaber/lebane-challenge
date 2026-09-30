package com.lebane.backend.department.repository;

import com.lebane.backend.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepartmentRepository  extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
}
