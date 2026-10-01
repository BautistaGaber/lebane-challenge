package com.lebane.backend.department.specification;

import com.lebane.backend.department.entity.Department;
import com.lebane.backend.department.repository.DepartmentRepository;
import com.lebane.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the JPA Criteria API against real PostgreSQL instead of mocking {@code CriteriaBuilder},
 * which is where the actual mistakes would show up (wrong operand order, inclusive vs exclusive
 * bounds, static metamodel typos).
 */
@DisplayName("DepartmentSpecifications against PostgreSQL")
class DepartmentSpecificationsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    private Long availableCheapDepartmentId;
    private Long availableExpensiveDepartmentId;
    private Long unavailableDepartmentId;

    private void givenCatalogue() {
        availableCheapDepartmentId = givenDepartment("Disponible barato", true, "80000.00", "40.00").getId();
        availableExpensiveDepartmentId = givenDepartment("Disponible caro", true, "450000.00", "180.00").getId();
        unavailableDepartmentId = givenDepartment("No disponible", false, "120000.00", "90.00").getId();
    }

    private List<Department> findAll(Specification<Department> specification) {
        return departmentRepository.findAll(
                specification,
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "id"))
        ).getContent();
    }

    private List<Long> idsOf(Specification<Department> specification) {
        return findAll(specification).stream().map(Department::getId).toList();
    }

    @Nested
    @DisplayName("available")
    class Available {

        @Test
        @DisplayName("keeps only available departments when true")
        void shouldFilterAvailableDepartments() {

            // Arrange
            givenCatalogue();

            // Act
            List<Long> ids = idsOf(DepartmentSpecifications.hasAvailable(Boolean.TRUE));

            // Assert
            assertThat(ids).containsExactlyInAnyOrder(availableCheapDepartmentId, availableExpensiveDepartmentId);
        }

        @Test
        @DisplayName("keeps only unavailable departments when false")
        void shouldFilterUnavailableDepartments() {

            // Arrange
            givenCatalogue();

            // Act
            List<Long> ids = idsOf(DepartmentSpecifications.hasAvailable(Boolean.FALSE));

            // Assert
            assertThat(ids).containsExactly(unavailableDepartmentId);
        }

        @Test
        @DisplayName("does not restrict the result when the filter is null")
        void shouldNotRestrictResultsWhenNull() {

            // Arrange
            givenCatalogue();

            // Act
            List<Long> ids = idsOf(DepartmentSpecifications.hasAvailable(null));

            // Assert
            assertThat(ids).hasSize(3);
        }
    }

    @Nested
    @DisplayName("price")
    class Price {

        @Test
        @DisplayName("keeps departments priced at or above the minimum, bound included")
        void shouldFilterByMinimumPriceInclusive() {

            // Arrange
            givenCatalogue();

            // Act
            List<Long> ids = idsOf(DepartmentSpecifications.hasMinimumPrice(new BigDecimal("120000.00")));

            // Assert
            assertThat(ids).containsExactlyInAnyOrder(availableExpensiveDepartmentId, unavailableDepartmentId);
        }

        @Test
        @DisplayName("keeps departments priced at or below the maximum, bound included")
        void shouldFilterByMaximumPriceInclusive() {

            // Arrange
            givenCatalogue();

            // Act
            List<Long> ids = idsOf(DepartmentSpecifications.hasMaximumPrice(new BigDecimal("120000.00")));

            // Assert
            assertThat(ids).containsExactlyInAnyOrder(availableCheapDepartmentId, unavailableDepartmentId);
        }

        @Test
        @DisplayName("does not restrict the result when both price bounds are null")
        void shouldNotRestrictResultsWhenBothBoundsAreNull() {

            // Arrange
            givenCatalogue();

            // Act
            List<Long> ids = idsOf(Specification
                    .allOf(DepartmentSpecifications.hasMinimumPrice(null))
                    .and(DepartmentSpecifications.hasMaximumPrice(null)));

            // Assert
            assertThat(ids).hasSize(3);
        }
    }

    @Nested
    @DisplayName("square meters")
    class SquareMeters {

        @Test
        @DisplayName("keeps departments of at least the given surface, bound included")
        void shouldFilterByMinimumSquareMetersInclusive() {

            // Arrange
            givenCatalogue();

            // Act
            List<Long> ids = idsOf(DepartmentSpecifications.hasMinimumSquareMeters(new BigDecimal("90.00")));

            // Assert
            assertThat(ids).containsExactlyInAnyOrder(availableExpensiveDepartmentId, unavailableDepartmentId);
        }

        @Test
        @DisplayName("keeps departments of at most the given surface, bound included")
        void shouldFilterByMaximumSquareMetersInclusive() {

            // Arrange
            givenCatalogue();

            // Act
            List<Long> ids = idsOf(DepartmentSpecifications.hasMaximumSquareMeters(new BigDecimal("90.00")));

            // Assert
            assertThat(ids).containsExactlyInAnyOrder(availableCheapDepartmentId, unavailableDepartmentId);
        }

        @Test
        @DisplayName("does not restrict the result when both surface bounds are null")
        void shouldNotRestrictResultsWhenBothBoundsAreNull() {

            // Arrange
            givenCatalogue();

            // Act
            List<Long> ids = idsOf(Specification
                    .allOf(DepartmentSpecifications.hasMinimumSquareMeters(null))
                    .and(DepartmentSpecifications.hasMaximumSquareMeters(null)));

            // Assert
            assertThat(ids).hasSize(3);
        }
    }

    @Nested
    @DisplayName("combination")
    class Combination {

        @Test
        @DisplayName("intersects every active specification")
        void shouldIntersectAllSpecifications() {

            // Arrange
            givenCatalogue();

            // Act
            Specification<Department> specification = Specification
                    .allOf(DepartmentSpecifications.hasAvailable(Boolean.TRUE))
                    .and(DepartmentSpecifications.hasMinimumPrice(new BigDecimal("100000.00")))
                    .and(DepartmentSpecifications.hasMaximumPrice(new BigDecimal("400000.00")))
                    .and(DepartmentSpecifications.hasMinimumSquareMeters(new BigDecimal("60.00")))
                    .and(DepartmentSpecifications.hasMaximumSquareMeters(new BigDecimal("150.00")));

            // Act
            List<Long> ids = idsOf(specification);

            // Assert
            assertThat(ids).isEmpty();
        }

        @Test
        @DisplayName("returns the single matching department when all the ranges overlap")
        void shouldReturnTheSingleMatchingDepartment() {

            // Arrange
            Long matchingId = givenDepartment("El único que cumple todo", true, "200000.00", "100.00").getId();
            givenDepartment("Barato y no disponible", false, "1000.00", "10.00");
            givenDepartment(" Caro y demasiado grande", true, "900000.00", "900.00");

            // Act
            Specification<Department> specification = Specification
                    .allOf(DepartmentSpecifications.hasAvailable(Boolean.TRUE))
                    .and(DepartmentSpecifications.hasMinimumPrice(new BigDecimal("100000.00")))
                    .and(DepartmentSpecifications.hasMaximumPrice(new BigDecimal("400000.00")))
                    .and(DepartmentSpecifications.hasMinimumSquareMeters(new BigDecimal("60.00")))
                    .and(DepartmentSpecifications.hasMaximumSquareMeters(new BigDecimal("150.00")));

            // Assert
            assertThat(idsOf(specification)).containsExactly(matchingId);
        }
    }
}