package com.lebane.backend.inquiry.entity;

import com.lebane.backend.department.entity.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Setter
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Setter
    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Setter
    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public void assignTo(Department department) {
        this.department = department;
    }

    public static Inquiry create(){
        return new Inquiry();
    }
}
