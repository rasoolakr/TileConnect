package com.example.tilecommerce.controller;

import com.example.tilecommerce.dto.AnjaniImportDtos;
import com.example.tilecommerce.service.AnjaniProductImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/import/anjani")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SHOP_OWNER','SUPER_ADMIN')")
public class AnjaniImportController {
    private final AnjaniProductImportService service;

    @PostMapping
    public AnjaniImportDtos.ImportResponse importProducts(@Valid @RequestBody AnjaniImportDtos.ImportRequest request) {
        return service.importProducts(request);
    }
}
