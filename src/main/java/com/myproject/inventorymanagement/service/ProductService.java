package com.myproject.inventorymanagement.service;

import com.myproject.inventorymanagement.entity.Category;
import com.myproject.inventorymanagement.entity.Product;
import com.myproject.inventorymanagement.entity.Supplier;
import com.myproject.inventorymanagement.repository.CategoryRepository;
import com.myproject.inventorymanagement.repository.ProductRepository;
import com.myproject.inventorymanagement.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    public List<Product> getActiveProducts(){
        return productRepository.findByIsActiveTrue();
    }

    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product getProductBySku(String sku){
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }

    public List<Product> getProductsByCategory(Long categoryId){
        return productRepository.findByCategoryId((categoryId));
    }

    public List<Product> getProductsBySupplier(Long supplierId){
        return productRepository.findBySupplierId(supplierId);
    }

    public List<Product> searchProducts(String keyword){
        return productRepository.searchByKeyword(keyword);
    }

    public List<Product> getLowStockProducts(){
        return productRepository.findLowStockProducts();
    }

    public List<Product> getOutOfStockProducts(){
        return productRepository.findByQuantityAndIsActiveTrue(0);
    }

    @Transactional
    public Product createProduct(Product product, Long categoryId, Long supplierId){
        if(productRepository.existsBySku(product.getSku())){
            throw new RuntimeException("SKU already exists: " + product.getSku());
        }

        if(categoryId != null){
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
            product.setCategory(category);
        }

        if(supplierId != null){
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));
            product.setSupplier(supplier);
        }

        product.setIsActive(true);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updated, Long categoryId, Long supplierId){
        Product existing = getProductById(id);

        if(!existing.getSku().equalsIgnoreCase(updated.getSku()) && productRepository.existsBySku(updated.getSku())){
            throw new RuntimeException("SKU already exists: " + updated.getSku());
        }

        existing.setSku(updated.getSku());
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setUnitPrice(updated.getUnitPrice());
        existing.setCostPrice(updated.getCostPrice());
        existing.setMinStockLevel(updated.getMinStockLevel());
        existing.setMaxStockLevel(updated.getMaxStockLevel());
        existing.setUnit(updated.getUnit());
        existing.setImageUrl(updated.getImageUrl());

        if(categoryId != null){
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
            existing.setCategory(category);
        }

        if(supplierId != null){
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));
            existing.setSupplier(supplier);
        }

        return productRepository.save(existing);
    }

    @Transactional
    public Product increaseStock(Long productId, int quantity){
        if(quantity <= 0){
            throw new RuntimeException("Quantity must be greater than 0.");
        }

        Product product = getProductById(productId);
        product.setQuantity(product.getQuantity() + quantity);
        return productRepository.save(product);
    }

    @Transactional
    public Product decreaseStock(Long productId, int quantity){
        if(quantity <= 0){
            throw new RuntimeException("Quantity must be greater than 0.");
        }

        Product product = getProductById(productId);
        if(product.getQuantity() < quantity){
            throw new RuntimeException("Not enough stock. Available: " + product.getQuantity());
        }
        product.setQuantity(product.getQuantity() + quantity);
        return productRepository.save(product);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = getProductById(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    // ── Hard delete ──────────────────────────────────────────────────────────
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
