# Architecture

```text
                 Anjani public catalogue
                         |
                    Jsoup scraper
                         |
                Normalized ProductRecord
                    /             \
                   /               \
              React UI              CSV
                  |                  |
                  +--------+---------+
                           |
                    Product Import API
                           |
                 Tile Commerce Backend
                           |
                       MySQL
```

## Identity rule
Your application owns `productId`. Anjani's product code is `supplierProductCode` and is not the primary key.

## Future supplier purchase integration
```text
Dealer/Supplier CSV or API
        |
 Purchase/Invoice Importer
        |
 supplierProductCode
        |
 Existing product master
        |
 Inventory / purchase price / stock
```
