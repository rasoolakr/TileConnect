package com.example.anjaniimport.controller;

import com.example.anjaniimport.dto.ImportRequest;
import com.example.anjaniimport.dto.ProductRecord;
import com.example.anjaniimport.service.AnjaniScraperService;
import com.example.anjaniimport.service.CsvService;
import com.example.anjaniimport.service.PushService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anjani")
public class CatalogController {

    private final AnjaniScraperService scraper;
    private final CsvService csv;

    public CatalogController(
            AnjaniScraperService scraper,
            CsvService csv,
            PushService ignored) {

        this.scraper = scraper;
        this.csv = csv;
    }

    /**
     * Fetch products from Anjani.
     *
     * Example:
     *
     * POST /api/anjani/fetch
     *
     * {
     *     "collection": "Wall Collections",
     *     "size": null,
     *     "finish": null,
     *     "color": null,
     *     "maxProducts": 50
     * }
     */
    @PostMapping("/fetch")
    public List<ProductRecord> fetch(
            @RequestBody(required = false) ImportRequest request)
            throws Exception {

        if (request == null) {
            request = new ImportRequest(
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return scraper.fetch(request);
    }

    /**
     * Fetch products and download them as CSV.
     */
    @PostMapping(
            value = "/csv",
            produces = "text/csv"
    )
    public ResponseEntity<byte[]> csv(
            @RequestBody(required = false) ImportRequest request)
            throws Exception {

        if (request == null) {
            request = new ImportRequest(
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        List<ProductRecord> products =
                scraper.fetch(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=anjani-products.csv"
                )
                .contentType(
                        MediaType.parseMediaType("text/csv")
                )
                .body(
                        csv.toCsv(products)
                );
    }

    /**
     * Health check.
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
