package com.myproject.inventorymanagement.repository;

import com.myproject.inventorymanagement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>{
    Optional<Supplier> findByNameIgnoreCase(String name);

    List<Supplier> findByNameContainingIgnoreCase(String name);

    List<Supplier> findByIsActiveTrue();

    Optional<Supplier> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNameIgnoreCase(String name);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByPhoneAndIdNot(String phone, Long id);
}
