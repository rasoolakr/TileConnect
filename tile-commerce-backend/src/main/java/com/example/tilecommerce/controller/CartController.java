package com.example.tilecommerce.controller;

import com.example.tilecommerce.dto.CartDtos.*;
import com.example.tilecommerce.entity.Cart;
import com.example.tilecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
	private final CartService s;

	@GetMapping
	public Cart get() {
		return s.get();
	}

	@PostMapping("/shops/{shopId}/items")
	public Cart add(@PathVariable Long shopId, @Valid @RequestBody AddCartItemRequest r) {
		return s.add(shopId, r);
	}

	@PutMapping("/items/{itemId}")
	public Cart update(@PathVariable Long itemId, @Valid @RequestBody UpdateCartItemRequest r) {
		return s.update(itemId, r.quantity());
	}

	@DeleteMapping("/items/{itemId}")
	public Cart remove(@PathVariable Long itemId) {
		return s.remove(itemId);
	}
}
