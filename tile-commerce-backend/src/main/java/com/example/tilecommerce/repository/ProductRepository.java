package com.example.tilecommerce.repository;

import com.example.tilecommerce.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> findBySupplierImportKey(String supplierImportKey);
    Optional<Product> findByShop_IdAndSupplierImportKey(Long shopId, String supplierImportKey);

    @EntityGraph(attributePaths = {"images","variants","category","shop"})
    List<Product> findByActiveTrue();

    @Override
    @EntityGraph(attributePaths = {"images","variants","category","shop"})
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = {"images","variants","category","shop"})
    List<Product> findByShop_IdOrderByCreatedAtDesc(Long shopId);
}
