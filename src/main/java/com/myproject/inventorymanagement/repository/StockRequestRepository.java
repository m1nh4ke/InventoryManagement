package com.myproject.inventorymanagement.repository;

import com.myproject.inventorymanagement.entity.StockRequest;
import com.myproject.inventorymanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {
    List<StockRequest> findAllByOrderByCreatedAtDesc();
    List<StockRequest> findByStaffOrderByCreatedAtDesc(User staff);
    List<StockRequest> findByStatusOrderByCreatedAtDesc(StockRequest.RequestStatus status);
    List<StockRequest> findByStaffAndStatusOrderByCreatedAtDesc(User staff, StockRequest.RequestStatus status);
}
