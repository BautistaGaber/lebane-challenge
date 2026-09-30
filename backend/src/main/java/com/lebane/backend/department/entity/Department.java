package com.lebane.backend.department.entity;

import com.lebane.backend.image.entity.Image;
import com.lebane.backend.inquiry.entity.Inquiry;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Setter
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Setter
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private CurrencyCode currency;

    @Setter
    @Column(name = "square_meters", nullable = false, precision = 10, scale = 2)
    private BigDecimal squareMeters;

    @Setter
    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Setter
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Setter
    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Setter
    @Column(name = "available", nullable = false)
    private boolean available;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)

    @OrderBy("displayOrder ASC")
    private final List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})

    private final List<Inquiry> inquiries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void addImage(Image image) {
        images.add(image);
        image.assignTo(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.removeFromDepartment();
    }

    public void addInquiry(Inquiry inquiry) {
        inquiries.add(inquiry);
        inquiry.assignTo(this);
    }
}
