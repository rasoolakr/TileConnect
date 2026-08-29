package com.example.tilecommerce.service;

import com.example.tilecommerce.dto.AnjaniImportDtos;
import com.example.tilecommerce.entity.Product;
import com.example.tilecommerce.entity.ProductImage;
import com.example.tilecommerce.entity.ProductVariant;
import com.example.tilecommerce.entity.Shop;
import com.example.tilecommerce.enumeration.ImageType;
import com.example.tilecommerce.repository.ProductImageRepository;
import com.example.tilecommerce.repository.ProductRepository;
import com.example.tilecommerce.repository.ProductVariantRepository;
import com.example.tilecommerce.repository.ShopRepository;
import com.example.tilecommerce.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AnjaniProductImportService {
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final ProductImageRepository images;
    private final ShopRepository shops;

    @Transactional
    public AnjaniImportDtos.ImportResponse importProducts(AnjaniImportDtos.ImportRequest request) {
        Shop shop = shops.findById(request.shopId())
                .orElseThrow(() -> new NoSuchElementException("Shop not found"));
        authorize(shop);
        return importItems(shop, request.products());
    }

    /**
     * Shared import path for both Anjani Tek JSON data and CSV data.
     * The authenticated user's shop is supplied by the backend, never trusted from a file.
     */
    @Transactional
    public AnjaniImportDtos.ImportResponse importItemsForCurrentShop(List<AnjaniImportDtos.Item> items) {
        Long shopId = CurrentUser.shopId();
        if (shopId == null) throw new AccessDeniedException("Shop owner is not linked to a shop");
        Shop shop = shops.findById(shopId)
                .orElseThrow(() -> new NoSuchElementException("Shop not found"));
        authorize(shop);
        return importItems(shop, items);
    }

    private AnjaniImportDtos.ImportResponse importItems(Shop shop, List<AnjaniImportDtos.Item> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No products supplied for import");
        }

        int created = 0, updated = 0, skipped = 0;
        List<Long> ids = new ArrayList<>();

        for (AnjaniImportDtos.Item raw : items) {
            AnjaniImportDtos.Item item = normalize(raw);
            Product p = products.findByShop_IdAndSupplierImportKey(shop.getId(), item.importKey()).orElse(null);

            if (p == null) {
                p = new Product();
                p.setShop(shop);
                p.setSupplierImportKey(item.importKey());
                created++;
            } else {
                updated++;
            }

            p.setName(item.name());
            p.setSlug(uniqueSlug(item.name(), item.importKey()));
            p.setShortDescription(buildShortDescription(item));
            p.setDetailedDescription(buildDetailedDescription(item));
            p.setBrand(blankToNull(item.supplierName()));
            p.setColor(blankToNull(item.color()));
            p.setFinish(blankToNull(item.finish()));
            p.setTileType(blankToNull(item.collection()));
            p.setBasePrice(item.basePrice());
            p.setDiscountPrice(item.discountPrice());
            p.setTaxPercentage(item.taxPercentage() == null ? BigDecimal.ZERO : item.taxPercentage());
            p.setMinimumOrderQuantity(item.minimumOrderQuantity());
            p.setUnit(defaultIfBlank(item.unit(), "box"));
            p.setProductCode(firstNonBlank(item.supplierProductCode(), item.importKey()));
            p.setSupplierName(blankToNull(item.supplierName()));
            p.setSupplierProductCode(blankToNull(item.supplierProductCode()));
            p.setSupplierSourceUrl(firstNonBlank(item.detailUrl(), item.sourceUrl()));
            p.setActive(true);

            Product saved = products.saveAndFlush(p);

            // Variant is deterministic by (shop product, size). Never create a variant with null/blank size.
            String size = defaultIfBlank(item.size(), "Standard");
            ProductVariant variant = variants.findByProduct_IdAndSize(saved.getId(), size).orElse(null);
            if (variant == null) {
                variant = new ProductVariant();
                variant.setProduct(saved);
                variant.setSize(size);
            }
            variant.setStockQuantity(Math.max(0, item.stockQuantity()));
            variant.setPrice(validPrice(item.discountPrice(), item.basePrice()));
            variant.setActive(true);
            variants.save(variant);

            // Do not deserialize supplier image JSON into a ProductImage entity.
            // Store only a non-blank URL and always use a known ImageType enum value.
            String imageUrl = blankToNull(item.imageUrl());
            if (imageUrl != null && images.findByProduct_IdOrderByDisplayOrder(saved.getId())
                    .stream().noneMatch(i -> imageUrl.equals(i.getImageUrl()))) {
                List<ProductImage> existing = images.findByProduct_IdOrderByDisplayOrder(saved.getId());
                ProductImage image = new ProductImage();
                image.setProduct(saved);
                image.setImageUrl(imageUrl);
                image.setImageType(ImageType.FRONT);
                image.setPrimaryImage(existing.isEmpty());
                image.setAltText(saved.getName());
                image.setDisplayOrder(existing.size());
                images.save(image);
            }

            ids.add(saved.getId());
        }

        return new AnjaniImportDtos.ImportResponse(created, updated, skipped, ids);
    }

    private AnjaniImportDtos.Item normalize(AnjaniImportDtos.Item i) {
        if (i == null) throw new IllegalArgumentException("Product row cannot be null");
        String name = blankToNull(i.name());
        if (name == null) throw new IllegalArgumentException("Product name is required");
        String key = firstNonBlank(i.importKey(), i.supplierProductCode(), name);
        BigDecimal base = i.basePrice() == null ? BigDecimal.ONE : i.basePrice();
        if (base.signum() <= 0) throw new IllegalArgumentException("Base price must be greater than zero for " + name);
        BigDecimal discount = i.discountPrice();
        if (discount != null && discount.signum() < 0) throw new IllegalArgumentException("Discount price cannot be negative for " + name);
        int moq = Math.max(1, i.minimumOrderQuantity());
        int stock = Math.max(0, i.stockQuantity());
        return new AnjaniImportDtos.Item(
                defaultIfBlank(i.supplierName(), "CSV"),
                i.supplierProductCode(),
                name, i.collection(), i.size(), i.finish(), i.color(), i.description(), i.application(),
                i.detailUrl(), i.imageUrl(), i.sourceUrl(), key, base, discount,
                i.taxPercentage() == null ? BigDecimal.ZERO : i.taxPercentage(), moq,
                defaultIfBlank(i.unit(), "box"), stock
        );
    }

    private BigDecimal validPrice(BigDecimal discount, BigDecimal base) {
        return discount != null && discount.signum() > 0 ? discount : base;
    }

    private void authorize(Shop shop) {
        var u = CurrentUser.get().getUser();
        if (u.getRole().name().equals("SUPER_ADMIN")) return;
        if (u.getShop() == null || !u.getShop().getId().equals(shop.getId())) {
            throw new AccessDeniedException("Cross-shop access denied");
        }
    }

    private String buildShortDescription(AnjaniImportDtos.Item i) {
        return Stream.of(i.collection(), i.size(), i.finish(), i.color())
                .filter(x -> x != null && !x.isBlank()).reduce((a,b) -> a + " | " + b).orElse("Tile product");
    }

    private String buildDetailedDescription(AnjaniImportDtos.Item i) {
        StringBuilder b = new StringBuilder(
                i.description() == null || i.description().isBlank()
                        ? "Imported into TileCommerce from supplier catalogue."
                        : i.description().trim());
        if (i.application() != null && !i.application().isBlank()) b.append(" Application: ").append(i.application()).append('.');
        if (i.detailUrl() != null && !i.detailUrl().isBlank()) b.append(" Source: ").append(i.detailUrl());
        return b.toString();
    }

    private String uniqueSlug(String name, String key) {
        return slug(name) + "-" + safe(key);
    }
    private String slug(String n) { return n.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9]+","-").replaceAll("^-|-$",""); }
    private String safe(String s) { return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-"); }
    private String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private String defaultIfBlank(String s, String fallback) { return s == null || s.isBlank() ? fallback : s.trim(); }
    private String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v.trim();
        return null;
    }
}
