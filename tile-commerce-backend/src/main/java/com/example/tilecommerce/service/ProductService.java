package com.example.tilecommerce.service;

import com.example.tilecommerce.dto.ProductDtos.*;
import com.example.tilecommerce.entity.*;
import com.example.tilecommerce.repository.*;
import com.example.tilecommerce.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class ProductService {
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final ShopRepository shops;
    private final CategoryRepository categories;

    @Transactional
    public Product create(ProductRequest r){
        Shop shop=shops.findById(r.shopId()).orElseThrow(()->new NoSuchElementException("Shop not found"));
        authorizeShop(shop);
        Product p=new Product();
        p.setShop(shop); p.setName(r.name()); p.setSlug(slug(r.name()));
        p.setShortDescription(r.shortDescription()); p.setDetailedDescription(r.detailedDescription());
        p.setBrand(r.brand()); p.setMaterial(r.material()); p.setColor(r.color()); p.setFinish(r.finish());
        p.setTileType(r.tileType()); p.setBasePrice(r.basePrice()); p.setDiscountPrice(r.discountPrice());
        p.setTaxPercentage(r.taxPercentage()==null?java.math.BigDecimal.ZERO:r.taxPercentage());
        p.setMinimumOrderQuantity(r.minimumOrderQuantity()); p.setUnit(r.unit());
        if(r.categoryId()!=null) p.setCategory(categories.findById(r.categoryId()).orElseThrow());
        Product saved=products.save(p);
        if(r.variants()!=null) for(VariantRequest vr:r.variants()){
            if (vr == null || vr.size() == null || vr.size().isBlank()) continue;
            java.math.BigDecimal price = vr.price() != null ? vr.price() : r.basePrice();
            if (price == null || price.signum() <= 0) throw new IllegalArgumentException("Variant price must be greater than zero");
            ProductVariant v=variants.findByProduct_IdAndSize(saved.getId(), vr.size().trim()).orElseGet(ProductVariant::new);
            v.setProduct(saved); v.setSize(vr.size().trim());
            v.setThickness(vr.thickness()); v.setStockQuantity(Math.max(0,vr.stockQuantity())); v.setPrice(price); v.setActive(true);
            variants.save(v);
        }
        return saved;
    }

    public List<Product> publicProducts(){ return products.findByActiveTrue(); }
    public List<Product> myProducts(){
        Long shopId=CurrentUser.shopId();
        if(shopId==null) throw new org.springframework.security.access.AccessDeniedException("Shop owner is not linked to a shop");
        return products.findByShop_IdOrderByCreatedAtDesc(shopId);
    }
    public Product get(Long id){ return products.findById(id).orElseThrow(()->new NoSuchElementException("Product not found")); }

    private void authorizeShop(Shop shop){
        var u=CurrentUser.get().getUser();
        if(u.getRole().name().equals("SUPER_ADMIN")) return;
        if(u.getShop()==null || !u.getShop().getId().equals(shop.getId()))
            throw new org.springframework.security.access.AccessDeniedException("Cross-shop access denied");
    }
    private String slug(String n){ return n.toLowerCase().trim().replaceAll("[^a-z0-9]+","-").replaceAll("^-|-$",""); }
}
