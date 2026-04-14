package com.myproject.inventorymanagement.controller;

import com.myproject.inventorymanagement.entity.StockMovement;
import com.myproject.inventorymanagement.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockMovementController {
    private final StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<List<StockMovement>> getAll(){
        return ResponseEntity.ok(stockMovementService.getAllMovements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMovement> getById(@PathVariable Long id){
        return ResponseEntity.ok(stockMovementService.getMovementById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockMovement>> getByProduct(@PathVariable Long productId){
        return ResponseEntity.ok(stockMovementService.getMovementsByProduct(productId));
    }

    @GetMapping("/user/{userId}")
    public  ResponseEntity<List<StockMovement>> getByUser(@PathVariable Long userId){
        return ResponseEntity.ok(stockMovementService.getMovementsByUser(userId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<StockMovement>> getByType(@PathVariable StockMovement.MovementType type){
        return ResponseEntity.ok(stockMovementService.getMovementsByType(type));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<StockMovement>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end){

        return ResponseEntity.ok(stockMovementService.getMovementsByDateRange(start, end));
    }

    @GetMapping("/product/{productId}/total-in")
    public ResponseEntity<Map<String, Integer>> getTotalIn(@PathVariable Long productId){
        int total = stockMovementService.getTotalStockIn(productId);
        return ResponseEntity.ok(Map.of("productId", productId.intValue(), "totalIn", total));
    }

    @GetMapping("/product/{productId}/total-out")
    public ResponseEntity<Map<String, Integer>> getTotalOut(@PathVariable Long productId){
        int total = stockMovementService.getTotalStockOut(productId);
        return ResponseEntity.ok(Map.of("productId", productId.intValue(), "totalOut", total));
    }

    @PostMapping("/stock-in")
    public ResponseEntity<StockMovement> stockIn(@RequestBody Map<String, Object> body){
        Long productId = Long.valueOf(body.get("productId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        String reason = body.getOrDefault("reason", "Stock received").toString();
        return ResponseEntity.status(201).body(stockMovementService.stockIn(productId, userId, quantity, reason));
    }

    @PostMapping("/stock-out")
    public ResponseEntity<StockMovement> stockOut(@RequestBody Map<String, Object> body){
        Long productId = Long.valueOf(body.get("productId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        String reason = body.getOrDefault("reason", "Stock removed").toString();
        return ResponseEntity.status(201).body(stockMovementService.stockOut(productId, userId, quantity, reason));
    }

    @PostMapping("/adjust")
    public ResponseEntity<StockMovement> adjust(@RequestBody Map<String, Object> body){
        Long productId = Long.valueOf(body.get("productId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        int newQuantity = Integer.parseInt(body.get("newQuantity").toString());
        String reason = body.getOrDefault("reason", "Stock adjusted").toString();
        return ResponseEntity.status(201).body(stockMovementService.adjustStock(productId, userId, newQuantity, reason));
    }

    @PostMapping("/return")
    public ResponseEntity<StockMovement> returnStock(@RequestBody Map<String, Object> body){
        Long productId = Long.valueOf(body.get("productId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        String reason = body.getOrDefault("reason", "Customer returned stock").toString();
        Long refId = body.containsKey("refOrderId") ? Long.valueOf(body.get("refOrderId").toString()) : null;
        return ResponseEntity.status(201).body(stockMovementService.returnStock(productId, userId, quantity, reason, refId));
    }
}
