package com.example.tilecommerce.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "products", indexes = { @Index(name = "idx_product_shop", columnList = "shop_id"),
		@Index(name = "idx_product_category", columnList = "category_id"),
		@Index(name = "idx_product_slug", columnList = "slug") })
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private Category category;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String slug;
	private String shortDescription;
	@Column(columnDefinition = "TEXT")
	private String detailedDescription;
	private String brand, material, color, finish, tileType;
	private String productCode;
	private String supplierName, supplierProductCode, supplierImportKey, supplierSourceUrl;
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal basePrice;
	@Column(precision = 19, scale = 2)
	private BigDecimal discountPrice;
	@Column(precision = 5, scale = 2)
	private BigDecimal taxPercentage = BigDecimal.ZERO;
	private int minimumOrderQuantity = 1;
	private String unit, activeSizeLabel;
	private boolean active = true, featured = false;
	@JsonIgnore
	@OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProductImage> images = new HashSet<>();

	@OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProductVariant> variants = new HashSet<>();

	@JsonProperty("shopId")
	public Long getShopId() {
		return shop == null ? null : shop.getId();
	}
}
