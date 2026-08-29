package com.example.tilecommerce.entity;

import com.example.tilecommerce.enumeration.CartStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", indexes = { @Index(name = "idx_cart_user", columnList = "user_id"),
		@Index(name = "idx_cart_shop", columnList = "shop_id") })
@Getter
@Setter
@NoArgsConstructor
public class Cart extends BaseEntity {

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CartStatus status = CartStatus.ACTIVE;

	@OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartItem> items = new ArrayList<>();
}
