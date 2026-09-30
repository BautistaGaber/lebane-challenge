package com.lebane.backend.image.entity;

import com.lebane.backend.department.entity.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Setter
    @Column(name = "object_key", nullable = false, unique = true, length = 512)
    private String objectKey;

    @Setter
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Setter
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Setter
    @Column(name = "primary_image", nullable = false)
    private boolean primaryImage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public void assignTo(Department department) {
        this.department = department;
    }

    public void removeFromDepartment() {
        this.department = null;
    }
}
