package com.myproject.inventorymanagement.controller;

import com.myproject.inventorymanagement.dto.CreateStockRequestDto;
import com.myproject.inventorymanagement.entity.StockRequest;
import com.myproject.inventorymanagement.service.StockRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/stock-requests")
@RequiredArgsConstructor
public class StockRequestController {

    private final StockRequestService stockRequestService;

    @GetMapping
    public ResponseEntity<List<StockRequest>> getAllRequests() {
        return ResponseEntity.ok(stockRequestService.getAllRequests());
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<StockRequest>> getMyRequests(Principal principal) {
        return ResponseEntity.ok(stockRequestService.getRequestsByStaff(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockRequest> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(stockRequestService.getRequestById(id));
    }

    @PostMapping
    public ResponseEntity<StockRequest> createRequest(@RequestBody CreateStockRequestDto dto, Principal principal) {
        return ResponseEntity.status(201).body(stockRequestService.createRequest(dto, principal.getName()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<StockRequest> approveRequest(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(stockRequestService.approveRequest(id, principal.getName()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<StockRequest> rejectRequest(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(stockRequestService.rejectRequest(id, principal.getName()));
    }

}
