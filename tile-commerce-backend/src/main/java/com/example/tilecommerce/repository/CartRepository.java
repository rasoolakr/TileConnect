package com.example.tilecommerce.repository;
import com.example.tilecommerce.entity.Cart;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser_IdAndStatus(Long userId, com.example.tilecommerce.enumeration.CartStatus status);

}
