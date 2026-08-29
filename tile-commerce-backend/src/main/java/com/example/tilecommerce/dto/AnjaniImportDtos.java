package com.example.tilecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public final class AnjaniImportDtos {
    private AnjaniImportDtos() {}

    public record Item(
            @NotBlank String supplierName,
            String supplierProductCode,
            @NotBlank String name,
            String collection,
            String size,
            String finish,
            String color,
            String description,
            String application,
            String detailUrl,
            String imageUrl,
            String sourceUrl,
            @NotBlank String importKey,
            @NotNull @DecimalMin("0.01") BigDecimal basePrice,
            @DecimalMin("0.00") BigDecimal discountPrice,
            @DecimalMin("0.00") BigDecimal taxPercentage,
            @Min(1) int minimumOrderQuantity,
            String unit,
            @Min(0) int stockQuantity
    ) {}

    public record ImportRequest(
            @NotNull Long shopId,
            @Valid @NotNull List<Item> products
    ) {}

    public record ImportResponse(
            int created,
            int updated,
            int skipped,
            List<Long> productIds
    ) {}
}
