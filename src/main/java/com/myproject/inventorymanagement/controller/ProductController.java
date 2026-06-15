package com.myproject.inventorymanagement.controller;

import com.myproject.inventorymanagement.entity.Product;
import com.myproject.inventorymanagement.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAll(){
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActive(){
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> getBySku(@PathVariable String sku){
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam String keyword){
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStock(){
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStock(){
        return ResponseEntity.ok(productService.getOutOfStockProducts());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable Long categoryId){
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<Product>> getBySupplier(@PathVariable Long supplierId){
        return ResponseEntity.ok(productService.getProductsBySupplier(supplierId));
    }

    @PostMapping
    public ResponseEntity<Product> create(
            @RequestBody Product product,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId){

        return ResponseEntity.status(201).body(productService.createProduct(product,categoryId, supplierId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @RequestBody Product product,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId){

        return ResponseEntity.ok(productService.updateProduct(id, product, categoryId, supplierId));
    }

    @PatchMapping("/{id}/increase-stock")
    public ResponseEntity<Product> increaseStock(@PathVariable Long id, @RequestBody Map<String, Integer> body){
        return ResponseEntity.ok(productService.increaseStock(id, body.get("quantity")));
    }

    @PatchMapping("/{id}/decrease-stock")
    public ResponseEntity<Product> decreaseStock(@PathVariable Long id, @RequestBody Map<String, Integer> body){
        return ResponseEntity.ok(productService.decreaseStock(id, body.get("quantity")));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id){
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
