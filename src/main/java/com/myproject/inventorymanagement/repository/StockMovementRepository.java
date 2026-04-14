package com.myproject.inventorymanagement.repository;

import com.myproject.inventorymanagement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductId(Long productId);

    List<StockMovement> findByUserId(Long userId);

    List<StockMovement> findByType(StockMovement.MovementType type);

    List<StockMovement> findByProductIdAndType(Long productId, StockMovement.MovementType type);

    List<StockMovement> findAllByOrderByCreatedAtDesc();

    @Query("SELECT s FROM StockMovement s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<StockMovement> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockMovement s WHERE s.product.id = :productId AND s.type = 'IN'")
    int getTotalStockIn(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockMovement s WHERE s.product.id = :productId AND s.type = 'OUT'")
    int getTotalStockOut(@Param("productId") Long productId);
}
