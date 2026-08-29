package com.example.tilecommerce.service;

import com.example.tilecommerce.dto.CartDtos.*;
import com.example.tilecommerce.entity.Cart;
import com.example.tilecommerce.entity.CartItem;
import com.example.tilecommerce.entity.Product;
import com.example.tilecommerce.entity.ProductImage;
import com.example.tilecommerce.entity.ProductVariant;
import com.example.tilecommerce.entity.User;
import com.example.tilecommerce.enumeration.CartStatus;
import com.example.tilecommerce.repository.CartRepository;
import com.example.tilecommerce.repository.ProductVariantRepository;
import com.example.tilecommerce.security.CurrentUser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository carts;
    private final ProductVariantRepository variants;

    // =========================================================
    // ADD ITEM TO CART
    // =========================================================

    @Transactional
    public CartResponse add(Long shopId, AddCartItemRequest r) {

        User user = CurrentUser.get().getUser();

        ProductVariant variant = variants.findById(r.productVariantId())
                .orElseThrow(() ->
                        new NoSuchElementException("Variant not found"));

        Product product = variant.getProduct();

        if (product == null) {
            throw new NoSuchElementException("Product not found");
        }

        // -----------------------------------------------------
        // Validate product
        // -----------------------------------------------------

        if (!variant.isActive() || !product.isActive()) {
            throw new IllegalArgumentException(
                    "Product is inactive"
            );
        }

        // -----------------------------------------------------
        // Validate stock
        // -----------------------------------------------------

        if (variant.getStockQuantity() < r.quantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock"
            );
        }

        // -----------------------------------------------------
        // Validate shop
        // -----------------------------------------------------

        if (product.getShop() == null ||
                !product.getShop().getId().equals(shopId)) {

            throw new IllegalArgumentException(
                    "Variant does not belong to shop"
            );
        }

        // -----------------------------------------------------
        // Find active cart or create one
        // -----------------------------------------------------

        Cart cart = carts.findByUser_IdAndStatus(
                user.getId(),
                CartStatus.ACTIVE
        ).orElseGet(() -> {

            Cart newCart = new Cart();

            newCart.setUser(user);
            newCart.setShop(product.getShop());
            newCart.setStatus(CartStatus.ACTIVE);

            return newCart;
        });

        // -----------------------------------------------------
        // Validate cart shop
        // -----------------------------------------------------

        if (cart.getShop() != null &&
                !cart.getShop().getId().equals(shopId)) {

            throw new IllegalArgumentException(
                    "Cart can contain products from one shop only"
            );
        }

        // -----------------------------------------------------
        // Find existing cart item
        // -----------------------------------------------------

        CartItem item = cart.getItems()
                .stream()
                .filter(existing ->
                        existing.getProductVariant() != null &&
                        existing.getProductVariant()
                                .getId()
                                .equals(variant.getId())
                )
                .findFirst()
                .orElse(null);

        // -----------------------------------------------------
        // Create or update item
        // -----------------------------------------------------

        if (item == null) {

            item = new CartItem();

            item.setCart(cart);
            item.setProductVariant(variant);
            item.setQuantity(r.quantity());

        } else {

            item.setQuantity(
                    item.getQuantity() + r.quantity()
            );
        }

        // -----------------------------------------------------
        // Validate final quantity
        // -----------------------------------------------------

        if (item.getQuantity() > variant.getStockQuantity()) {

            throw new IllegalArgumentException(
                    "Insufficient stock"
            );
        }

        // -----------------------------------------------------
        // Set current price
        // -----------------------------------------------------

        item.setUnitPrice(variant.getPrice());

        // -----------------------------------------------------
        // Make sure there is only one item for the variant
        // -----------------------------------------------------

        CartItem finalItem = item;

        cart.getItems().removeIf(existing ->
                existing != finalItem &&
                existing.getProductVariant() != null &&
                existing.getProductVariant()
                        .getId()
                        .equals(variant.getId())
        );

        if (!cart.getItems().contains(item)) {
            cart.getItems().add(item);
        }

        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        Cart saved = carts.save(cart);

        /*
         * IMPORTANT:
         *
         * Convert the entity to DTO while the transaction is
         * still active.
         *
         * This allows lazy relationships such as:
         *
         * Cart -> Items
         * Item -> ProductVariant
         * ProductVariant -> Product
         * Product -> Images
         *
         * to be safely accessed.
         */
        return toResponse(saved);
    }

    // =========================================================
    // GET CART
    // =========================================================

    @Transactional(readOnly = true)
    public CartResponse get() {

        Cart cart = carts.findByUser_IdAndStatus(
                CurrentUser.id(),
                CartStatus.ACTIVE
        ).orElse(null);

        if (cart == null) {
            return new CartResponse(
                    null,
                    CartStatus.ACTIVE,
                    List.of()
            );
        }

        return toResponse(cart);
    }

    // =========================================================
    // UPDATE CART ITEM
    // =========================================================

    @Transactional
    public CartResponse update(Long itemId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        Cart cart = getActiveCartEntity();

        CartItem item = cart.getItems()
                .stream()
                .filter(existing ->
                        existing.getId() != null &&
                        existing.getId().equals(itemId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Cart item not found"
                        ));

        ProductVariant variant = item.getProductVariant();

        if (variant == null) {
            throw new NoSuchElementException(
                    "Product variant not found"
            );
        }

        // -----------------------------------------------------
        // Check stock
        // -----------------------------------------------------

        if (quantity > variant.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock"
            );
        }

        // -----------------------------------------------------
        // Update quantity
        // -----------------------------------------------------

        item.setQuantity(quantity);

        // -----------------------------------------------------
        // Refresh unit price
        // -----------------------------------------------------

        item.setUnitPrice(variant.getPrice());

        Cart saved = carts.save(cart);

        return toResponse(saved);
    }

    // =========================================================
    // REMOVE CART ITEM
    // =========================================================

    @Transactional
    public CartResponse remove(Long itemId) {

        Cart cart = getActiveCartEntity();

        boolean removed = cart.getItems()
                .removeIf(item ->
                        item.getId() != null &&
                        item.getId().equals(itemId)
                );

        if (!removed) {
            throw new NoSuchElementException(
                    "Cart item not found"
            );
        }

        Cart saved = carts.save(cart);

        return toResponse(saved);
    }

    // =========================================================
    // GET ACTIVE CART ENTITY
    // =========================================================

    private Cart getActiveCartEntity() {

        return carts.findByUser_IdAndStatus(
                CurrentUser.id(),
                CartStatus.ACTIVE
        ).orElseThrow(() ->
                new NoSuchElementException(
                        "Cart is empty"
                ));
    }

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private CartResponse toResponse(Cart cart) {

        if (cart == null) {
            return new CartResponse(
                    null,
                    CartStatus.ACTIVE,
                    List.of()
            );
        }

        List<CartItemResponse> itemResponses =
                cart.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return new CartResponse(
                cart.getId(),
                cart.getStatus(),
                itemResponses
        );
    }

    // =========================================================
    // CART ITEM -> DTO
    // =========================================================

    private CartItemResponse toItemResponse(CartItem item) {

        ProductVariant variant =
                item.getProductVariant();

        ProductVariantResponse variantResponse =
                toVariantResponse(variant);

        BigDecimal unitPrice =
                item.getUnitPrice() == null
                        ? BigDecimal.ZERO
                        : item.getUnitPrice();

        BigDecimal subtotal =
                unitPrice.multiply(
                        BigDecimal.valueOf(item.getQuantity())
                );

        return new CartItemResponse(
                item.getId(),
                variantResponse,
                item.getQuantity(),
                unitPrice,
                subtotal
        );
    }

    // =========================================================
    // PRODUCT VARIANT -> DTO
    // =========================================================

    private ProductVariantResponse toVariantResponse(
            ProductVariant variant) {

        if (variant == null) {
            return null;
        }

        Product product = variant.getProduct();

        if (product == null) {
            return new ProductVariantResponse(
                    variant.getId(),
                    null,
                    null,
                    null,
                    variant.getSize(),
                    variant.getThickness(),
                    variant.getStockQuantity(),
                    variant.getPrice(),
                    variant.isActive()
            );
        }

        /*
         * IMPORTANT:
         *
         * We access product.getImages() HERE, while the
         * @Transactional method is still running.
         *
         * Therefore Hibernate can initialize the LAZY
         * collection safely.
         */
        String imageUrl = findPrimaryImage(product);

        return new ProductVariantResponse(
                variant.getId(),
                product.getId(),
                product.getName(),
                imageUrl,
                variant.getSize(),
                variant.getThickness(),
                variant.getStockQuantity(),
                variant.getPrice(),
                variant.isActive()
        );
    }

    // =========================================================
    // FIND PRODUCT IMAGE
    // =========================================================

    private String findPrimaryImage(Product product) {

        if (product.getImages() == null ||
                product.getImages().isEmpty()) {

            return null;
        }

        ProductImage image = product.getImages()
                .stream()
                .filter(ProductImage::isPrimaryImage)
                .findFirst()
                .orElseGet(() ->
                        product.getImages()
                                .stream()
                                .min(
                                        Comparator.comparingInt(
                                                ProductImage::getDisplayOrder
                                        )
                                )
                                .orElse(null)
                );

        return image == null
                ? null
                : image.getImageUrl();
    }
}
