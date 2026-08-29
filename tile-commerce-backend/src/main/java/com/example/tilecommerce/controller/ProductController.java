package com.example.tilecommerce.controller;

import com.example.tilecommerce.dto.ProductDtos.ProductRequest;
import com.example.tilecommerce.dto.ProductDtos.ProductResponse;
import com.example.tilecommerce.entity.Product;
import com.example.tilecommerce.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService service;
    // =========================================================
    // PUBLIC PRODUCTS
    // =========================================================

    @GetMapping("/public")
    public List<ProductResponse> publicProducts() {
        return service.publicProducts();
    }

    // =========================================================
    // SHOP OWNER PRODUCTS
    // =========================================================

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('SHOP_OWNER','SUPER_ADMIN')")
    public List<ProductResponse> mine() {
        return service.myProducts();
    }

    // =========================================================
    // PUBLIC PRODUCT BY ID
    // =========================================================

    @GetMapping("/{id}/public")
    public ProductResponse publicProduct(@PathVariable Long id) {
        return service.get(id);
    }

    // =========================================================
    // PRODUCT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // =========================================================
    // CREATE PRODUCT
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_OWNER')")
    public Product create(@Valid @RequestBody ProductRequest r) {
        return service.create(r);
    }
}
