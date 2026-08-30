package com.example.anjaniimport.service;

import com.example.anjaniimport.dto.ProductRecord;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvService {

    public byte[] toCsv(List<ProductRecord> products) {

        StringBuilder b = new StringBuilder();

        // UTF-8 BOM for Microsoft Excel
        b.append('\uFEFF');

        // CSV header
        b.append("supplierName,supplierProductCode,name,collection,size,finish,color,application,detailUrl,imageUrl,sourceUrl,importKey\n");

        if (products == null || products.isEmpty()) {
            return b.toString().getBytes(StandardCharsets.UTF_8);
        }

        for (ProductRecord p : products) {

            b.append(row(p.supplierName())).append(',')
             .append(row(p.supplierProductCode())).append(',')
             .append(row(p.name())).append(',')
             .append(row(p.collection())).append(',')
             .append(row(p.size())).append(',')
             .append(row(p.finish())).append(',')
             .append(row(p.color())).append(',')
             .append(row(p.application())).append(',')
             .append(row(p.detailUrl())).append(',')
             .append(row(p.imageUrl())).append(',')
             .append(row(p.sourceUrl())).append(',')
             .append(row(p.importKey()))
             .append('\n');
        }

        return b.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String row(String value) {

        if (value == null) {
            value = "";
        }

        // Escape double quotes according to CSV specification
        value = value.replace("\"", "\"\"");

        // Always wrap values in quotes
        return "\"" + value + "\"";
    }
}