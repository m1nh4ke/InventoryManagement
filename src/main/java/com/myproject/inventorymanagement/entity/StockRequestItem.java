package com.myproject.inventorymanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "stock_request_items")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    @JsonIgnoreProperties("items")
    private StockRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"stockMovements", "category", "supplier", "description", "minStockLevel", "maxStockLevel", "unit", "createdAt", "updatedAt"})
    private Product product;

    @Column(name = "quantity", nullable = false)
    @ToString.Include
    private Integer quantity;

    @Column(name = "system_quantity")
    @ToString.Include
    private Integer systemQuantity;

    @Column(name = "actual_quantity")
    @ToString.Include
    private Integer actualQuantity;
}
