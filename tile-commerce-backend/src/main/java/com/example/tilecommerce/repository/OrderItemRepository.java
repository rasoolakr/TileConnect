package com.example.tilecommerce.repository;
import com.example.tilecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {}
