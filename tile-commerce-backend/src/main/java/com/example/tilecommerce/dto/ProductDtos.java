package com.example.tilecommerce.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
public final class ProductDtos {
    private ProductDtos() {}
    public record VariantRequest(@NotBlank String size, String thickness,
                                  @Min(0) int stockQuantity,
                                  @NotNull @DecimalMin("0.01") BigDecimal price) {}
    public record ProductRequest(@NotBlank String name, String shortDescription,
                                 String detailedDescription, String brand, String material,
                                 String color, String finish, String tileType,
                                 @NotNull @DecimalMin("0.01") BigDecimal basePrice,
                                 @DecimalMin("0.00") BigDecimal discountPrice,
                                 @DecimalMin("0.00") BigDecimal taxPercentage,
                                 @Min(1) int minimumOrderQuantity, String unit,
                                 Long categoryId, @NotNull Long shopId,
                                 List<VariantRequest> variants) {}
}
