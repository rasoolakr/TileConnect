package com.example.tilecommerce.repository;
import com.example.tilecommerce.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrder_Id(Long orderId);

}
