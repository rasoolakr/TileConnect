package com.example.tilecommerce.dto;
import jakarta.validation.constraints.*;
public final class OrderDtos { private OrderDtos(){} public record CheckoutRequest(@NotNull Long addressId,String deliveryNotes){} public record StatusRequest(@NotBlank String status){} public record PaymentSubmitRequest(@NotBlank String paymentReference){} }
