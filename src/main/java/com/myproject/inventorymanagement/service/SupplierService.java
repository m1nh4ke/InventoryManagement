package com.myproject.inventorymanagement.service;

import com.myproject.inventorymanagement.entity.Supplier;
import com.myproject.inventorymanagement.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers(){
        return supplierRepository.findAll();
    }

    public List<Supplier> getActiveSuppliers(){
        return supplierRepository.findByIsActiveTrue();
    }

    public Supplier getSupplierById(Long id){
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
    }

    public List<Supplier>  searchSupplier(String name){
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier){
        if(supplier.getEmail() != null && supplierRepository.existsByEmail(supplier.getEmail())){
            throw new RuntimeException("Supplier email already exists: " + supplier.getEmail());
        }

        supplier.setIsActive(true);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier updated){
        Supplier existing = getSupplierById(id);

        if(updated.getEmail() != null && !updated.getEmail().equalsIgnoreCase(existing.getEmail()) && supplierRepository.existsByEmail(updated.getEmail())){
            throw new RuntimeException("Email already used: " + updated.getEmail());
        }

        existing.setName(updated.getName());
        existing.setContactName(updated.getContactName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        return supplierRepository.save(existing);
    }

    @Transactional
    public void deactivateSupplier(Long id){
        Supplier supplier = getSupplierById(id);
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }

    @Transactional
    public void deleteSupplier(Long id){
        if(!supplierRepository.existsById(id)){
            throw new RuntimeException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }
}
