package com.myproject.inventorymanagement.service;

import com.myproject.inventorymanagement.entity.Supplier;
import com.myproject.inventorymanagement.entity.Product;
import com.myproject.inventorymanagement.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByIsActiveTrue();
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
    }

    public List<Supplier> searchSupplier(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        if (supplier == null) {
            throw new RuntimeException("Supplier data cannot be null");
        }
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên nhà cung cấp không được để trống");
        }

        if (supplierRepository.existsByNameIgnoreCase(supplier.getName())) {
            throw new RuntimeException("Supplier name already exists: " + supplier.getName());
        }
        if (supplier.getEmail() != null && !supplier.getEmail().isEmpty()
                && supplierRepository.existsByEmailIgnoreCase(supplier.getEmail())) {
            throw new RuntimeException("Supplier email already exists: " + supplier.getEmail());
        }
        if (supplier.getPhone() != null && !supplier.getPhone().isEmpty()
                && supplierRepository.existsByPhone(supplier.getPhone())) {
            throw new RuntimeException("Supplier phone already exists: " + supplier.getPhone());
        }

        supplier.setIsActive(true);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier updated) {
        Supplier existing = getSupplierById(id);

        if (updated.getEmail() != null && !updated.getEmail().isEmpty()
                && supplierRepository.existsByEmailIgnoreCaseAndIdNot(updated.getEmail(), id)) {
            throw new RuntimeException("Email already used: " + updated.getEmail());
        }
        if (updated.getPhone() != null && !updated.getPhone().isEmpty()
                && supplierRepository.existsByPhoneAndIdNot(updated.getPhone(), id)) {
            throw new RuntimeException("Phone already used: " + updated.getPhone());
        }

        existing.setContactName(updated.getContactName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        return supplierRepository.save(existing);
    }

    @Transactional
    public void deactivateSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplier.setIsActive(supplier.getIsActive() == null || !supplier.getIsActive());
        supplierRepository.save(supplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        if (supplier.getProducts() != null) {
            for (Product product : supplier.getProducts()) {
                product.setSupplier(null);
            }
            supplier.getProducts().clear();
        }
        supplierRepository.delete(supplier);
    }
}
