package com.example.tilecommerce.service;

import com.example.tilecommerce.dto.CartDtos.*;
import com.example.tilecommerce.entity.*;
import com.example.tilecommerce.enumeration.CartStatus;
import com.example.tilecommerce.repository.*;
import com.example.tilecommerce.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {
	private final CartRepository carts;
	private final ProductVariantRepository variants;
	private final ShopRepository shops;

	@Transactional
	public Cart add(Long shopId, AddCartItemRequest r) {
		User u = CurrentUser.get().getUser();
		ProductVariant v = variants.findById(r.productVariantId())
				.orElseThrow(() -> new NoSuchElementException("Variant not found"));
		if (!v.isActive() || !v.getProduct().isActive())
			throw new IllegalArgumentException("Product is inactive");
		if (v.getStockQuantity() < r.quantity())
			throw new IllegalArgumentException("Insufficient stock");
		if (!v.getProduct().getShop().getId().equals(shopId))
			throw new IllegalArgumentException("Variant does not belong to shop");
		Cart c = carts.findByUser_IdAndStatus(u.getId(), CartStatus.ACTIVE).orElseGet(() -> {
			Cart x = new Cart();
			x.setUser(u);
			x.setShop(v.getProduct().getShop());
			return x;
		});
		if (!c.getShop().getId().equals(shopId))
			throw new IllegalArgumentException("Cart can contain products from one shop only");
		CartItem item = c.getItems().stream().filter(i -> i.getProductVariant().getId().equals(v.getId())).findFirst()
				.orElse(null);
		if (item == null) {
			item = new CartItem();
			item.setCart(c);
			item.setProductVariant(v);
			item.setQuantity(r.quantity());
		} else
			item.setQuantity(item.getQuantity() + r.quantity());
		if (item.getQuantity() > v.getStockQuantity())
			throw new IllegalArgumentException("Insufficient stock");
		item.setUnitPrice(v.getPrice());
		c.getItems().removeIf(i -> i.getId() != null && i.getProductVariant().getId().equals(v.getId()));
		c.getItems().add(item);
		touch(c);
		return carts.save(c);
	}

	@Transactional(readOnly = true)
	public Cart get() {
		Cart c = carts.findByUser_IdAndStatus(CurrentUser.id(), CartStatus.ACTIVE).orElseGet(() -> new Cart());
		c.getItems().forEach(i -> {
			if (i.getProductVariant() != null && i.getProductVariant().getProduct() != null) {
				i.getProductVariant().getProduct().getName();
			}
		});
		return c;
	}

	@Transactional
	public Cart update(Long itemId, int qty) {
		Cart c = cart();
		CartItem i = c.getItems().stream().filter(x -> x.getId().equals(itemId)).findFirst().orElseThrow();
		if (qty > i.getProductVariant().getStockQuantity())
			throw new IllegalArgumentException("Insufficient stock");
		i.setQuantity(qty);
		touch(c);
		return carts.save(c);
	}

	@Transactional
	public Cart remove(Long itemId) {
		Cart c = cart();
		c.getItems().removeIf(i -> i.getId().equals(itemId));
		touch(c);
		return carts.save(c);
	}

	private void touch(Cart c) {
		c.getItems().forEach(i -> {
			if (i.getProductVariant() != null && i.getProductVariant().getProduct() != null)
				i.getProductVariant().getProduct().getName();
		});
	}

	private Cart cart() {
		return carts.findByUser_IdAndStatus(CurrentUser.id(), CartStatus.ACTIVE)
				.orElseThrow(() -> new NoSuchElementException("Cart is empty"));
	}
}
