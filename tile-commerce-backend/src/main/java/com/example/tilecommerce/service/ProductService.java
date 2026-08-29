package com.example.tilecommerce.service;

import com.example.tilecommerce.dto.ProductDtos.*;
import com.example.tilecommerce.entity.*;
import com.example.tilecommerce.repository.*;
import com.example.tilecommerce.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final ShopRepository shops;
    private final CategoryRepository categories;

    // =========================================================
    // CREATE PRODUCT
    // =========================================================

    @Transactional
    public Product create(ProductRequest r) {

        Shop shop = shops.findById(r.shopId())
                .orElseThrow(() ->
                        new NoSuchElementException("Shop not found"));

        authorizeShop(shop);

        Product p = new Product();

        p.setShop(shop);
        p.setName(r.name());
        p.setSlug(slug(r.name()));

        p.setShortDescription(r.shortDescription());
        p.setDetailedDescription(r.detailedDescription());

        p.setBrand(r.brand());
        p.setMaterial(r.material());
        p.setColor(r.color());
        p.setFinish(r.finish());
        p.setTileType(r.tileType());

        p.setBasePrice(r.basePrice());
        p.setDiscountPrice(r.discountPrice());

        p.setTaxPercentage(
                r.taxPercentage() == null
                        ? BigDecimal.ZERO
                        : r.taxPercentage()
        );

        p.setMinimumOrderQuantity(r.minimumOrderQuantity());
        p.setUnit(r.unit());

        if (r.categoryId() != null) {
            p.setCategory(
                    categories.findById(r.categoryId())
                            .orElseThrow(() ->
                                    new NoSuchElementException(
                                            "Category not found"))
            );
        }

        Product saved = products.save(p);

        // =====================================================
        // SAVE VARIANTS
        // =====================================================

        if (r.variants() != null) {

            for (VariantRequest vr : r.variants()) {

                if (vr == null ||
                        vr.size() == null ||
                        vr.size().isBlank()) {
                    continue;
                }

                BigDecimal price =
                        vr.price() != null
                                ? vr.price()
                                : r.basePrice();

                if (price == null || price.signum() <= 0) {
                    throw new IllegalArgumentException(
                            "Variant price must be greater than zero"
                    );
                }

                ProductVariant v =
                        variants.findByProduct_IdAndSize(
                                saved.getId(),
                                vr.size().trim()
                        ).orElseGet(ProductVariant::new);

                v.setProduct(saved);
                v.setSize(vr.size().trim());
                v.setThickness(vr.thickness());
                v.setStockQuantity(
                        Math.max(0, vr.stockQuantity())
                );
                v.setPrice(price);
                v.setActive(true);

                variants.save(v);
            }
        }

        return saved;
    }

    // =========================================================
    // GET PUBLIC PRODUCTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProductResponse> publicProducts() {

        return products.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET SHOP OWNER PRODUCTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProductResponse> myProducts() {

        Long shopId = CurrentUser.shopId();

        if (shopId == null) {
            throw new AccessDeniedException(
                    "Shop owner is not linked to a shop"
            );
        }

        return products.findByShop_IdOrderByCreatedAtDesc(shopId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET SINGLE PRODUCT
    // =========================================================

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {

        Product product = products.findWithDetailsById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Product not found"));

        return toResponse(product);
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private ProductResponse toResponse(Product p) {

        // -----------------------------------------------------
        // Images
        // -----------------------------------------------------

        List<ProductImageResponse> imageResponses =
                p.getImages()
                        .stream()
                        .sorted((a, b) ->
                                Integer.compare(
                                        a.getDisplayOrder(),
                                        b.getDisplayOrder()
                                ))
                        .map(image ->
                                new ProductImageResponse(
                                        image.getId(),
                                        image.getImageUrl(),
                                        image.getImageType(),
                                        image.getDisplayOrder(),
                                        image.isPrimaryImage(),
                                        image.getAltText()
                                )
                        )
                        .toList();

        // -----------------------------------------------------
        // Variants
        // -----------------------------------------------------

        List<ProductVariantResponse> variantResponses =
                p.getVariants()
                        .stream()
                        .map(variant ->
                                new ProductVariantResponse(
                                        variant.getId(),
                                        variant.getSize(),
                                        variant.getThickness(),
                                        variant.getStockQuantity(),
                                        variant.getPrice(),
                                        variant.isActive()
                                )
                        )
                        .toList();

        // -----------------------------------------------------
        // Category ID
        // -----------------------------------------------------

        Long categoryId =
                p.getCategory() == null
                        ? null
                        : p.getCategory().getId();

        // -----------------------------------------------------
        // Shop ID
        // -----------------------------------------------------

        Long shopId =
                p.getShop() == null
                        ? null
                        : p.getShop().getId();

        // -----------------------------------------------------
        // Build ProductResponse
        // -----------------------------------------------------

        return new ProductResponse(
                p.getId(),
                shopId,
                categoryId,

                p.getName(),
                p.getSlug(),

                p.getShortDescription(),
                p.getDetailedDescription(),

                p.getBrand(),
                p.getMaterial(),
                p.getColor(),
                p.getFinish(),
                p.getTileType(),

                p.getProductCode(),

                p.getSupplierName(),
                p.getSupplierProductCode(),
                p.getSupplierImportKey(),
                p.getSupplierSourceUrl(),

                p.getBasePrice(),
                p.getDiscountPrice(),
                p.getTaxPercentage(),

                p.getMinimumOrderQuantity(),
                p.getUnit(),
                p.getActiveSizeLabel(),

                p.isActive(),
                p.isFeatured(),

                imageResponses,
                variantResponses
        );
    }

    // =========================================================
    // SHOP AUTHORIZATION
    // =========================================================

    private void authorizeShop(Shop shop) {

        var u = CurrentUser.get().getUser();

        if (u.getRole().name().equals("SUPER_ADMIN")) {
            return;
        }

        if (u.getShop() == null ||
                !u.getShop().getId().equals(shop.getId())) {

            throw new AccessDeniedException(
                    "Cross-shop access denied"
            );
        }
    }

    // =========================================================
    // SLUG GENERATOR
    // =========================================================

    private String slug(String n) {

        return n.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
