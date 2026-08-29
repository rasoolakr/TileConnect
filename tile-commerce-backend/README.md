# Tile Commerce Backend

Java 17 + Spring Boot + MySQL + JPA + Spring Security JWT.

## Important local configuration

The project intentionally contains no SQL CREATE scripts. For local development, Hibernate uses `ddl-auto: update` so the database tables are created/updated from the JPA entities. For production, use controlled migrations and `ddl-auto: validate`.

Backend port: `9191`.

JWT property: `app.jwt.secret` with a Base64 development default. Override it using `JWT_SECRET`.

## Anjani Tek integration

`POST /api/products/import/anjani` is protected and available to `SHOP_OWNER` and `SUPER_ADMIN`.

It accepts only products selected from the live Anjani catalogue importer and requires local selling price and opening stock. The ecommerce database generates its own `productId`; Anjani's product code is stored separately as `supplierProductCode`.

Example:

```json
{
  "shopId": 1,
  "products": [
    {
      "supplierName": "ANJANI_TEK",
      "supplierProductCode": "6103",
      "name": "6103 AMAZON BLACK",
      "collection": "GVT Collections",
      "size": "600x600mm",
      "finish": "Glossy Surface",
      "color": "BLACK",
      "application": "",
      "detailUrl": "https://www.anjanitiles.com/product-detail.php?id=6103-AMAZON-BLACK&size=3",
      "imageUrl": "https://www.anjanitiles.com/...",
      "sourceUrl": "https://www.anjanitiles.com/product-list.php",
      "importKey": "6103|600x600mm",
      "basePrice": 85.00,
      "discountPrice": null,
      "taxPercentage": 18,
      "minimumOrderQuantity": 1,
      "unit": "box",
      "stockQuantity": 20
    }
  ]
}
```

## Registration

- `POST /api/auth/register` - customer registration; address may be supplied in the same request.
- `POST /api/auth/shop-register` - creates shop owner + shop + default owner address atomically.

## Run

```bash
mvn spring-boot:run
```
Swagger: `http://localhost:9191/swagger-ui.html`
