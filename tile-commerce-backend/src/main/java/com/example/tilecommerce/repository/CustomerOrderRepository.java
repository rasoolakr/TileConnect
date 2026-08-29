package com.example.tilecommerce.repository;
import com.example.tilecommerce.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
    List<CustomerOrder> findByShop_IdOrderByCreatedAtDesc(Long shopId);

}
