package com.myproject.inventorymanagement.repository;

import com.myproject.inventorymanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
    Optional<Product> findBySku(String sku);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryIdOrderByNameAsc(Long categoryId);

    List<Product> findBySupplierIdOrderByNameAsc(Long supplierId);

    List<Product> findAllByOrderByNameAsc();

    boolean existsBySku(String sku);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.minStockLevel")
    List<Product> findLowStockProducts();

    List<Product> findByQuantity(int quantity);

    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.sku)  LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.name ASC")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
}
