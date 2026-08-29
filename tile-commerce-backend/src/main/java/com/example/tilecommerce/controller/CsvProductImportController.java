package com.example.tilecommerce.controller;

import com.example.tilecommerce.dto.AnjaniImportDtos;
import com.example.tilecommerce.service.CsvProductImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products/import")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SHOP_OWNER','SUPER_ADMIN')")
public class CsvProductImportController {
    private final CsvProductImportService service;

    @PostMapping(value="/csv", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnjaniImportDtos.ImportResponse importCsv(@RequestPart("file") MultipartFile file) {
        return service.importCsv(file);
    }

    @GetMapping(value="/template.csv", produces="text/csv")
    public String template() {
        return "productId,name,brand,material,collection,size,finish,color,description,basePrice,discountPrice,taxPercentage,minimumOrderQuantity,unit,stockQuantity,imageUrl,supplierName,supplierProductCode,detailUrl\n"
             + "AT-001,Sample Ivory Tile,Anjani Tek,Porcelain,GVT Collections,600x600mm,Glossy,Ivory,Sample product,899.00,,0,1,box,100,https://example.com/tile.jpg,Anjani Tek,AT-001,https://example.com/product/AT-001\n";
    }
}
