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

    public List<Product> getAllProducts() {
        return productRepository.findAllByOrderByNameAsc();
    }

    public List<Product> getActiveProducts() {
        return productRepository.findAllByOrderByNameAsc();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdOrderByNameAsc((categoryId));
    }

    public List<Product> getProductsBySupplier(Long supplierId) {
        return productRepository.findBySupplierIdOrderByNameAsc(supplierId);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findByQuantity(0);
    }

    @Transactional
    public Product createProduct(Product product, Long categoryId, Long supplierId) {
        if (product == null) {
            throw new RuntimeException("Product data cannot be null");
        }
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new RuntimeException("Mã SKU không được để trống");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên sản phẩm không được để trống");
        }

        if (productRepository.existsBySkuIgnoreCase(product.getSku())) {
            throw new RuntimeException("SKU already exists: " + product.getSku());
        }

        if (productRepository.existsByNameIgnoreCase(product.getName())) {
            throw new RuntimeException("Product name already exists: " + product.getName());
        }

        if (product.getMinStockLevel() == null || product.getMinStockLevel() < 0) {
            throw new RuntimeException("Min stock level must be >= 0");
        }

        if (product.getMaxStockLevel() == null || product.getMaxStockLevel() < 0) {
            throw new RuntimeException("Max stock level must be >= 0");
        }

        if (product.getMinStockLevel() > product.getMaxStockLevel()) {
            throw new RuntimeException("Min stock level must be less than or equal to max stock level");
        }

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
            product.setCategory(category);
        }

        if (supplierId != null) {
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));
            product.setSupplier(supplier);
        }

        if (product.getQuantity() == null) {
            product.setQuantity(0);
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updated, Long categoryId, Long supplierId) {
        Product existing = getProductById(id);

        if (updated.getMinStockLevel() == null || updated.getMinStockLevel() < 0) {
            throw new RuntimeException("Min stock level must be >= 0");
        }

        if (updated.getMaxStockLevel() == null || updated.getMaxStockLevel() < 0) {
            throw new RuntimeException("Max stock level must be >= 0");
        }

        if (updated.getMinStockLevel() > updated.getMaxStockLevel()) {
            throw new RuntimeException("Min stock level must be less than or equal to max stock level");
        }

        existing.setDescription(updated.getDescription());
        existing.setMinStockLevel(updated.getMinStockLevel());
        existing.setMaxStockLevel(updated.getMaxStockLevel());
        existing.setUnit(updated.getUnit());

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
            existing.setCategory(category);
        }

        if (supplierId != null) {
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));
            existing.setSupplier(supplier);
        }

        return productRepository.save(existing);
    }

    @Transactional
    public Product increaseStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0.");
        }

        Product product = getProductById(productId);
        product.setQuantity(product.getQuantity() + quantity);
        return productRepository.save(product);
    }

    @Transactional
    public Product decreaseStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0.");
        }

        Product product = getProductById(productId);
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock. Available: " + product.getQuantity());
        }
        product.setQuantity(product.getQuantity() - quantity);
        return productRepository.save(product);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        deleteProduct(id);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
