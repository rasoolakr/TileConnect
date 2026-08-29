package com.example.tilecommerce.repository;

import com.example.tilecommerce.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
	Optional<ProductVariant> findByIdAndProduct_Id(Long id, Long productId);

	Optional<ProductVariant> findByProduct_IdAndSize(Long productId, String size);
	
}
