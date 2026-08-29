package com.example.tilecommerce.repository;
import com.example.tilecommerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrder_Id(Long orderId);
}
