package com.myproject.inventorymanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    @ToString.Include
    private String name;

    @Column(name = "contact_name", length = 150)
    @ToString.Include
    private String contactName;

    @Column(name = "email", length = 100)
    @ToString.Include
    private String email;

    @Column(name = "phone", length = 20)
    @ToString.Include
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    @ToString.Include
    private String address;

    @Column(name = "is_active")
    @ToString.Include
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = true)
    @ToString.Include
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @ToString.Include
    private LocalDateTime updateddAt;

    @OneToMany(mappedBy = "supplier", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
        this.updateddAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        this.updateddAt = LocalDateTime.now();
    }
}
