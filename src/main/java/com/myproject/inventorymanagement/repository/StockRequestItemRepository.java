package com.myproject.inventorymanagement.repository;

import com.myproject.inventorymanagement.entity.StockRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRequestItemRepository extends JpaRepository<StockRequestItem, Long> {
}
