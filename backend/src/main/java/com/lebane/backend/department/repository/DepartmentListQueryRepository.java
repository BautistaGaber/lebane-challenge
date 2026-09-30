package com.lebane.backend.department.repository;

import com.lebane.backend.department.repository.projection.DepartmentImageStats;
import com.lebane.backend.department.repository.projection.DepartmentInquiryStats;
import com.lebane.backend.image.entity.Image;
import com.lebane.backend.inquiry.entity.Inquiry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DepartmentListQueryRepository {
    private final EntityManager entityManager;

    public DepartmentListQueryRepository(
            EntityManager entityManager
    ) {
        this.entityManager = entityManager;
    }

    public List<DepartmentImageStats> findImageStats(List<Long> departmentIds) {

        if (departmentIds.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<Image> image = query.from(Image.class);

        Expression<Long> departmentId = image.get("department").get("id");

        Expression<String> primaryImageKey = cb.<String>selectCase()
                        .when(cb.isTrue(image.get("primaryImage")), image.get("objectKey"))
                        .otherwise(cb.nullLiteral(String.class));

        query.multiselect(departmentId.alias("departmentId"), cb.count(image).alias("imageCount"), cb.greatest(primaryImageKey)
                        .alias("primaryImageObjectKey"));

        query.where(departmentId.in(departmentIds));

        query.groupBy(departmentId);

        return entityManager
                .createQuery(query)
                .getResultList()
                .stream()
                .map(tuple ->
                        new DepartmentImageStats(tuple.get("departmentId", Long.class),
                                tuple.get("imageCount", Long.class),
                                tuple.get("primaryImageObjectKey", String.class))).toList();
    }

    public List<DepartmentInquiryStats> findInquiryStats(List<Long> departmentIds) {

        if (departmentIds.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<Inquiry> inquiry = query.from(Inquiry.class);

        Expression<Long> departmentId = inquiry.get("department").get("id");

        query.multiselect(departmentId.alias("departmentId"), cb.count(inquiry).alias("inquiryCount"));

        query.where(departmentId.in(departmentIds));

        query.groupBy(departmentId);

        return entityManager
                .createQuery(query)
                .getResultList()
                .stream()
                .map(tuple ->
                        new DepartmentInquiryStats(tuple.get("departmentId", Long.class), tuple.get("inquiryCount", Long.class)))
                .toList();
    }
}
