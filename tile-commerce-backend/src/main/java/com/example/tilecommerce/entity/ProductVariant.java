package com.example.tilecommerce.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;
import java.util.Comparator;

@Entity
@Table(name = "product_variants", indexes = @Index(name = "idx_variant_product", columnList = "product_id"))
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductVariant extends BaseEntity {
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;
	@Column(nullable = false)
	private String size;
	private String thickness;
	@Column(nullable = false)
	private int stockQuantity;
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal price;
	private boolean active = true;

	@JsonProperty("productName")
	public String getProductName() {
		return product == null ? null : product.getName();
	}

	@JsonProperty("productId")
	public Long getProductId() {
		return product == null ? null : product.getId();
	}
	
	@JsonProperty("imageUrl")
    public String getImageUrl() {

        if (product == null ||
            product.getImages() == null ||
            product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages()
                .stream()
                .filter(ProductImage::isPrimaryImage)
                .findFirst()
                .orElseGet(() ->
                    product.getImages()
                            .stream()
                            .min(Comparator.comparingInt(
                                    ProductImage::getDisplayOrder
                            ))
                            .orElse(null)
                )
                .getImageUrl();
    }
}
