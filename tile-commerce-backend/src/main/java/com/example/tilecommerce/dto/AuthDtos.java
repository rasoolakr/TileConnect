package com.example.tilecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(String username, String password) {}

    public record RegisterAddressRequest(
            @NotBlank String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            String country,
            String phoneNumber
    ) {}

    /**
     * Public customer registration. The initial address is optional; when supplied it is
     * stored as the customer's default address.
     */
    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @Valid RegisterAddressRequest address
    ) {}

    /**
     * Public shop-owner registration. This creates the SHOP_OWNER user, the shop, and
     * the owner's default address in one transaction.
     */
    public record ShopRegisterRequest(
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String shopName,
            String shopDescription,
            String shopPhoneNumber,
            String shopEmail,
            String logoUrl,
            String upiId,
            String paymentPhoneNumber,
            String bankAccountNumber,
            String bankIfsc,
            String qrCodeUrl,
            @Valid @jakarta.validation.constraints.NotNull RegisterAddressRequest address
    ) {}

    public record LoginResponse(String token, Long userId, String username, String role, Long shopId) {}
}
