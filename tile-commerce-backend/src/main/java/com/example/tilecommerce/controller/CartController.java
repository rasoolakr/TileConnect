package com.example.tilecommerce.controller;

import com.example.tilecommerce.dto.CartDtos.*;
import com.example.tilecommerce.service.CartService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService service;

    // =========================================================
    // GET CART
    // =========================================================

    @GetMapping
    public CartResponse get() {
        return service.get();
    }

    // =========================================================
    // ADD ITEM
    // =========================================================

    @PostMapping("/shops/{shopId}/items")
    public CartResponse add(
            @PathVariable Long shopId,
            @Valid @RequestBody AddCartItemRequest request) {

        return service.add(shopId, request);
    }

    // =========================================================
    // UPDATE ITEM
    // =========================================================

    @PutMapping("/items/{itemId}")
    public CartResponse update(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        return service.update(
                itemId,
                request.quantity()
        );
    }

    // =========================================================
    // REMOVE ITEM
    // =========================================================

    @DeleteMapping("/items/{itemId}")
    public CartResponse remove(
            @PathVariable Long itemId) {

        return service.remove(itemId);
    }
}
