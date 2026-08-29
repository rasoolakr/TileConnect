package com.example.tilecommerce.entity;

import com.example.tilecommerce.enumeration.ImageType;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
public class ProductImage extends BaseEntity {
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;
	@Column(nullable = false)
	private String imageUrl;
	@Enumerated(EnumType.STRING)
	private ImageType imageType;
	private int displayOrder;
	private boolean primaryImage;
	private String altText;
}
