package com.myproject.inventorymanagement.service;

import com.myproject.inventorymanagement.entity.Product;
import com.myproject.inventorymanagement.entity.StockMovement;
import com.myproject.inventorymanagement.entity.User;
import com.myproject.inventorymanagement.repository.ProductRepository;
import com.myproject.inventorymanagement.repository.StockMovementRepository;
import com.myproject.inventorymanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementService {
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<StockMovement> getAllMovements(){
        return stockMovementRepository.findAllByOrderByCreatedAtDesc();
    }

    public StockMovement getMovementById(Long id) {
        return stockMovementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock movement not found with id: " + id));
    }

    public List<StockMovement> getMovementsByProduct(Long productId){
        return stockMovementRepository.findByProductId(productId);
    }

    public List<StockMovement> getMovementsByUser(Long userId){
        return stockMovementRepository.findByUserId(userId);
    }

    public List<StockMovement> getMovementsByType(StockMovement.MovementType type){
        return stockMovementRepository.findByType(type);
    }

    public List<StockMovement> getMovementsByDateRange(LocalDateTime start, LocalDateTime end){
        return stockMovementRepository.findByDateRange(start, end);
    }

    public int getTotalStockIn(Long productId){
        return stockMovementRepository.getTotalStockIn(productId);
    }

    public int getTotalStockOut(Long productId){
        return stockMovementRepository.getTotalStockOut(productId);
    }

    @Transactional
    public StockMovement stockIn(Long productId, Long userId, int quantity, String reason){
        if(quantity <= 0){
            throw new RuntimeException("Quantity must be greater than 0.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setUser(user);
        movement.setType(StockMovement.MovementType.IN);
        movement.setQuantity(quantity);
        movement.setReason(reason != null ? reason : "Stock received");
        return stockMovementRepository.save(movement);
    }

    @Transactional
    public StockMovement stockOut(Long productId, Long userId, int quantity, String reason){
        if(quantity <= 0){
            throw new RuntimeException("Quantity must be greater than 0.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        if (product.getQuantity() < quantity){
            throw new RuntimeException("Not enough stock. Available: " + product.getQuantity());
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setUser(user);
        movement.setType(StockMovement.MovementType.OUT);
        movement.setQuantity(quantity);
        movement.setReason(reason != null ? reason : "Stock removed");
        return stockMovementRepository.save(movement);
    }

    @Transactional
    public StockMovement adjustStock(Long productId, Long userId, int newQuantity, String reason){
        if(newQuantity <= 0){
            throw new RuntimeException("Stock quantity cannot be negative.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        int diff = newQuantity - product.getQuantity();

        product.setQuantity(newQuantity);
        productRepository.save(product);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setUser(user);
        movement.setType(StockMovement.MovementType.ADJUSTMENT);
        movement.setQuantity(Math.abs(diff));
        movement.setReason(reason != null ? reason : "Stock adjusted");
        return stockMovementRepository.save(movement);
    }

    @Transactional
    public StockMovement returnStock(Long productId, Long userId, int quantity, String reason, Long refOrderId){
        if(quantity <= 0){
            throw new RuntimeException("Quantity must be greater than 0.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setUser(user);
        movement.setType(StockMovement.MovementType.RETURN);
        movement.setQuantity(quantity);
        movement.setReason(reason != null ? reason : "Customer returned stock");
        movement.setReferenceId(refOrderId);
        return stockMovementRepository.save(movement);
    }
}
