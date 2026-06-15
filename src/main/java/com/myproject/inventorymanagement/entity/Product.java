package com.myproject.inventorymanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "products")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    @ToString.Include
    private String sku;

    @Column(name = "name", nullable = false, length = 200)
    @ToString.Include
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @ToString.Include
    private String description;

    @Column(name = "quantity", nullable = false)
    @ToString.Include
    private Integer quantity = 0;

    @Column(name = "min_stock_level")
    @ToString.Include
    private Integer minStockLevel = 10;

    @Column(name = "max_stock_level")
    @ToString.Include
    private Integer maxStockLevel = 1000;

    @Column(name = "unit", length = 50)
    @ToString.Include
    private String unit;

    @Column(name = "created_at", updatable = false)
    @ToString.Include
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @ToString.Include
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({ "products", "stockMovements" })
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonIgnoreProperties({ "products", "stockMovements" })
    private Supplier supplier;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<StockMovement> stockMovements = new ArrayList<>();

    @Transient
    public boolean isLowStock() {
        return this.quantity <= this.minStockLevel;
    }

    @Transient
    public boolean isOutOfStock() {
        return this.quantity == 0;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
