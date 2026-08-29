package com.example.tilecommerce.controller;

import com.example.tilecommerce.entity.*;
import com.example.tilecommerce.enumeration.ImageType;
import com.example.tilecommerce.repository.*;
import com.example.tilecommerce.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ImageController {
    private final ProductRepository products;
    private final ProductImageRepository images;

    @PostMapping(value="/{productId}/images", consumes="multipart/form-data")
    @PreAuthorize("hasAnyRole('SHOP_OWNER','SUPER_ADMIN')")
    public ProductImage upload(@PathVariable Long productId,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam(defaultValue="OTHER") String imageType,
                               @RequestParam(defaultValue="false") boolean primaryImage) throws Exception {
        Product p=products.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
        if(!Objects.equals(p.getShop().getId(), CurrentUser.shopId()) &&
                !CurrentUser.get().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_SUPER_ADMIN")))
            throw new org.springframework.security.access.AccessDeniedException("Product access denied");
        if(file==null || file.isEmpty()) throw new IllegalArgumentException("Empty image file");
        if(file.getSize()>5*1024*1024) throw new IllegalArgumentException("Image must be 5 MB or smaller");

        ImageType type;
        try { type=ImageType.valueOf(imageType.trim().toUpperCase(Locale.ROOT)); }
        catch(Exception ex) { type=ImageType.OTHER; }

        String original=Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String ext=(original.lastIndexOf('.')>=0)?original.substring(original.lastIndexOf('.')).replaceAll("[^A-Za-z0-9.]",""):"";
        if(ext.isBlank()) ext=".bin";
        String name=UUID.randomUUID()+ext;
        Path dir=Paths.get("uploads");Files.createDirectories(dir);
        Files.copy(file.getInputStream(),dir.resolve(name),StandardCopyOption.REPLACE_EXISTING);

        List<ProductImage> existing=images.findByProduct_IdOrderByDisplayOrder(productId);
        ProductImage i=new ProductImage();
        i.setProduct(p);i.setImageUrl("/uploads/"+name);i.setImageType(type);
        i.setPrimaryImage(primaryImage || existing.isEmpty());i.setAltText(p.getName());i.setDisplayOrder(existing.size());
        return images.save(i);
    }
}
