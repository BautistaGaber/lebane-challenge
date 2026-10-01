package com.lebane.backend.support;

import com.lebane.backend.department.entity.CurrencyCode;
import com.lebane.backend.department.entity.Department;
import com.lebane.backend.image.entity.Image;
import com.lebane.backend.inquiry.entity.Inquiry;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

/**
 * Builders for persistence entities.
 *
 * <p>The production entities expose a protected no-args constructor plus static factories and
 * expose no setters for their generated identifiers, so identifiers are injected with
 * {@link ReflectionTestUtils} to be able to assert on mapped responses without a database.
 */
public final class DepartmentFixtures {

    public static final Long NO_ID = null;

    private DepartmentFixtures() {
    }

    public static Department department(Long id) {
        return department(id, "Departamento en Palermo", true);
    }

    public static Department department(Long id, String title, boolean available) {
        Department department = Department.create();

        department.setTitle(title);
        department.setDescription("Luminoso departamento con balcón y-livingamplio.");
        department.setPrice(new BigDecimal("150000.00"));
        department.setCurrency(CurrencyCode.USD);
        department.setSquareMeters(new BigDecimal("85.00"));
        department.setAddress("Av. Santa Fe 1234, Palermo");
        department.setLatitude(new BigDecimal("-34.588300"));
        department.setLongitude(new BigDecimal("-58.430000"));
        department.setAvailable(available);

        ReflectionTestUtils.setField(department, "id", id);
        ReflectionTestUtils.setField(department, "version", 0L);

        return department;
    }

    public static Department withPrice(Department department, String price) {
        department.setPrice(new BigDecimal(price));
        return department;
    }

    public static Department withSquareMeters(Department department, String squareMeters) {
        department.setSquareMeters(new BigDecimal(squareMeters));
        return department;
    }

    public static Image image(Long id, String objectKey, int displayOrder, boolean primaryImage) {
        Image image = Image.create();

        image.setObjectKey(objectKey);
        image.setContentType("image/jpeg");
        image.setDisplayOrder(displayOrder);
        image.setPrimaryImage(primaryImage);

        ReflectionTestUtils.setField(image, "id", id);

        return image;
    }

    public static Inquiry inquiry(Long id, String name) {
        Inquiry inquiry = Inquiry.create();

        inquiry.setName(name);
        inquiry.setEmail(name.toLowerCase().replace(' ', '.') + "@example.com");
        inquiry.setMessage("Quisiera recibir más información sobre esta propiedad.");

        ReflectionTestUtils.setField(inquiry, "id", id);

        return inquiry;
    }
}