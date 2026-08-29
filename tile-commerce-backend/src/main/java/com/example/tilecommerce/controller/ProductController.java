package com.example.tilecommerce.controller;

import com.example.tilecommerce.dto.ProductDtos.ProductRequest;
import com.example.tilecommerce.entity.Product;
import com.example.tilecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
	private final ProductService service;

	@GetMapping("/public")
	public List<Product> publicProducts() {
		return service.publicProducts();
	}

	@GetMapping("/mine")
	@PreAuthorize("hasAnyRole('SHOP_OWNER','SUPER_ADMIN')")
	public List<Product> mine() {
		return service.myProducts();
	}

	@GetMapping("/{id}/public")
	public Product publicProduct(@PathVariable Long id) {
		return service.get(id);
	}

	@GetMapping("/{id}")
	public Product get(@PathVariable Long id) {
		return service.get(id);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_OWNER')")
	public Product create(@Valid @RequestBody ProductRequest r) {
		return service.create(r);
	}
}
