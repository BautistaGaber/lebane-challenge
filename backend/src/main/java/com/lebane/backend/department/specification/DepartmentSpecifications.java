package com.lebane.backend.department.specification;

import com.lebane.backend.department.entity.Department;
import com.lebane.backend.department.entity.Department_;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class DepartmentSpecifications {

    private DepartmentSpecifications() {
    }

    public static Specification<Department> hasAvailable(Boolean available) {
        return (root, query, criteriaBuilder) -> {
            if (available == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get(Department_.available), available);
        };
    }

    public static Specification<Department> hasMinimumPrice(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(root.get(Department_.price), minPrice);
        };
    }

    public static Specification<Department> hasMaximumPrice(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(root.get(Department_.price), maxPrice);
        };
    }

    public static Specification<Department> hasMinimumSquareMeters(
            BigDecimal minSquareMeters
    ) {
        return (root, query, criteriaBuilder) -> {
            if (minSquareMeters == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(root.get(Department_.squareMeters), minSquareMeters);
        };
    }

    public static Specification<Department> hasMaximumSquareMeters(
            BigDecimal maxSquareMeters
    ) {
        return (root, query, criteriaBuilder) -> {
            if (maxSquareMeters == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(root.get(Department_.squareMeters), maxSquareMeters);
        };
    }
}
