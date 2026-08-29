package com.example.tilecommerce.service;

import com.example.tilecommerce.dto.AnjaniImportDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CsvProductImportService {
    private final AnjaniProductImportService importer;

    public AnjaniImportDtos.ImportResponse importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("CSV file is empty");
        if (file.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("CSV file must be 10 MB or smaller");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        if (!name.toLowerCase(Locale.ROOT).endsWith(".csv")) throw new IllegalArgumentException("Only .csv files are supported");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) throw new IllegalArgumentException("CSV header is missing");

            List<String> headers = parseLine(headerLine);
            Map<String,Integer> index = new HashMap<>();
            for (int i=0;i<headers.size();i++) index.put(normalizeHeader(headers.get(i)), i);

            require(index, "name");
            require(index, "baseprice");
            if (!index.containsKey("productid") && !index.containsKey("importkey") && !index.containsKey("supplierproductcode")) {
                throw new IllegalArgumentException("CSV must contain productId, importKey or supplierProductCode");
            }

            List<AnjaniImportDtos.Item> items = new ArrayList<>();
            String line; int row=1;
            while ((line=reader.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                List<String> c = parseLine(line);
                String nameValue = val(c,index,"name");
                if (nameValue.isBlank()) throw new IllegalArgumentException("Row " + row + ": name is required");
                String productId = first(val(c,index,"productid"), val(c,index,"importkey"), val(c,index,"supplierproductcode"), nameValue);
                items.add(new AnjaniImportDtos.Item(
                    first(val(c,index,"suppliername"), "CSV"),
                    val(c,index,"supplierproductcode"),
                    nameValue,
                    first(val(c,index,"collection"), val(c,index,"tiletype")),
                    val(c,index,"size"),
                    val(c,index,"finish"),
                    val(c,index,"color"),
                    val(c,index,"description"),
                    val(c,index,"application"),
                    val(c,index,"detailurl"),
                    val(c,index,"imageurl"),
                    val(c,index,"sourceurl"),
                    productId,
                    decimal(c,index,"baseprice", BigDecimal.ONE),
                    nullableDecimal(c,index,"discountprice"),
                    decimal(c,index,"taxpercentage", BigDecimal.ZERO),
                    integer(c,index,"minimumorderquantity",1),
                    first(val(c,index,"unit"),"box"),
                    integer(c,index,"stockquantity",0)
                ));
            }
            if (items.isEmpty()) throw new IllegalArgumentException("CSV contains no product rows");
            return importer.importItemsForCurrentShop(items);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read CSV file", e);
        }
    }

    private void require(Map<String,Integer> i,String key){if(!i.containsKey(key))throw new IllegalArgumentException("CSV column is required: "+key);}
    private String val(List<String> row,Map<String,Integer> i,String key){Integer n=i.get(key);return n==null||n>=row.size()?"":row.get(n).trim();}
    private String first(String... x){for(String s:x)if(s!=null&&!s.isBlank())return s.trim();return "";}
    private BigDecimal decimal(List<String> r,Map<String,Integer> i,String k,BigDecimal d){String x=val(r,i,k);if(x.isBlank())return d;try{return new BigDecimal(x);}catch(Exception e){throw new IllegalArgumentException("Invalid "+k+" value: "+x);}}
    private BigDecimal nullableDecimal(List<String> r,Map<String,Integer> i,String k){String x=val(r,i,k);return x.isBlank()?null:decimal(r,i,k,BigDecimal.ZERO);}
    private int integer(List<String> r,Map<String,Integer> i,String k,int d){String x=val(r,i,k);if(x.isBlank())return d;try{return Integer.parseInt(x);}catch(Exception e){throw new IllegalArgumentException("Invalid "+k+" value: "+x);}}
    private String normalizeHeader(String h){return h==null?"":h.replace("\uFEFF","").trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","");}
    private List<String> parseLine(String line){
        List<String> out=new ArrayList<>();StringBuilder b=new StringBuilder();boolean q=false;
        for(int i=0;i<line.length();i++){char ch=line.charAt(i);if(ch=='"'){if(q&&i+1<line.length()&&line.charAt(i+1)=='"'){b.append('"');i++;}else q=!q;}else if(ch==','&&!q){out.add(b.toString());b.setLength(0);}else b.append(ch);}
        out.add(b.toString());return out;
    }
}
