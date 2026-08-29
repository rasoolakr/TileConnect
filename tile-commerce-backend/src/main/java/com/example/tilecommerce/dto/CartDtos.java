package com.example.tilecommerce.dto;
import jakarta.validation.constraints.*;
public final class CartDtos { private CartDtos(){} public record AddCartItemRequest(@NotNull Long productVariantId,@Min(1) int quantity){} public record UpdateCartItemRequest(@Min(1) int quantity){} }
