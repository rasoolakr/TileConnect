package com.example.tilecommerce.repository;
import com.example.tilecommerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser_Id(Long userId);

}
