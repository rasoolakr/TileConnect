package com.example.tilecommerce.dto;

import com.example.tilecommerce.enumeration.ImageType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public final class ProductDtos {

    private ProductDtos() {
    }

    // =========================
    // REQUEST DTOs
    // =========================

    public record VariantRequest(
            @NotBlank String size,
            String thickness,
            @Min(0) int stockQuantity,
            @NotNull @DecimalMin("0.01") BigDecimal price
    ) {
    }

    public record ProductRequest(
            @NotBlank String name,
            String shortDescription,
            String detailedDescription,
            String brand,
            String material,
            String color,
            String finish,
            String tileType,

            @NotNull
            @DecimalMin("0.01")
            BigDecimal basePrice,

            @DecimalMin("0.00")
            BigDecimal discountPrice,

            @DecimalMin("0.00")
            BigDecimal taxPercentage,

            @Min(1)
            int minimumOrderQuantity,

            String unit,
            Long categoryId,

            @NotNull
            Long shopId,

            List<VariantRequest> variants
    ) {
    }

    // =========================
    // RESPONSE DTOs
    // =========================

    public record ProductImageResponse(
            Long id,
            String imageUrl,
            ImageType imageType,
            int displayOrder,
            boolean primaryImage,
            String altText
    ) {
    }

    public record ProductVariantResponse(
            Long id,
            String size,
            String thickness,
            int stockQuantity,
            BigDecimal price,
            boolean active
    ) {
    }

    public record ProductResponse(
            Long id,
            Long shopId,
            Long categoryId,

            String name,
            String slug,

            String shortDescription,
            String detailedDescription,

            String brand,
            String material,
            String color,
            String finish,
            String tileType,

            String productCode,

            String supplierName,
            String supplierProductCode,
            String supplierImportKey,
            String supplierSourceUrl,

            BigDecimal basePrice,
            BigDecimal discountPrice,
            BigDecimal taxPercentage,

            int minimumOrderQuantity,
            String unit,
            String activeSizeLabel,

            boolean active,
            boolean featured,

            List<ProductImageResponse> images,
            List<ProductVariantResponse> variants
    ) {
    }
}
