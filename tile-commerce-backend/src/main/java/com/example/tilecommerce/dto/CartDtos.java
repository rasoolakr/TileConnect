package com.example.tilecommerce.dto;

import com.example.tilecommerce.enumeration.CartStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    // =========================================================
    // REQUEST DTOs
    // =========================================================

    public record AddCartItemRequest(
            @NotNull Long productVariantId,
            @Min(1) int quantity
    ) {
    }

    public record UpdateCartItemRequest(
            @Min(1) int quantity
    ) {
    }

    // =========================================================
    // RESPONSE DTOs
    // =========================================================

    public record CartResponse(
            Long id,
            CartStatus status,
            List<CartItemResponse> items
    ) {
    }

    public record CartItemResponse(
            Long id,
            ProductVariantResponse productVariant,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }

    public record ProductVariantResponse(
            Long id,
            Long productId,
            String productName,
            String imageUrl,
            String size,
            String thickness,
            int stockQuantity,
            BigDecimal price,
            boolean active
    ) {
    }
}
