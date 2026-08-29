package com.example.tilecommerce.repository;
import com.example.tilecommerce.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProductImageRepository extends JpaRepository<ProductImage,Long> { List<ProductImage> findByProduct_IdOrderByDisplayOrder(Long productId); }
