package com.example.tilecommerce.repository;
import com.example.tilecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
