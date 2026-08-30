# TileCommerce — Combined Customer + Shop Owner + Catalog Import

This archive combines the TileCommerce frontend and Spring Boot backend from the uploaded project and changes the UX to a real role-based ecommerce flow.

## Roles

### Customer
After login, a customer sees:
- Products
- Cart
- Orders
- Signed-in username
- Sign out

Customers cannot see or access the Admin page.

### Shop owner
A shop owner sees the same shopping features plus:
- Admin
- Anjani Tek import
- CSV product import
- My shop products

The backend enforces `SHOP_OWNER` / `SUPER_ADMIN` authorization. Hiding the Admin link in React is only a UX feature; the Spring Security rules are the actual protection.

## Catalog import design

Admin -> Catalog imports contains two options:

1. **Anjani Tek**
   - Fetches from the existing Anjani catalogue adapter at `VITE_ANJANI_IMPORTER_URL`.
   - Review products.
   - Select products.
   - Enter shop selling price and opening stock.
   - Import into the authenticated owner's shop.

2. **CSV upload**
   - Upload CSV.
   - Backend validates rows and required columns.
   - Creates/updates products.
   - Creates/updates a size variant.
   - Saves a product image URL when supplied.

The uploaded archive did not contain a separate Anjani Tek backend project; the supplied TileCommerce frontend referenced an Anjani service at `http://localhost:9196`. Therefore this combined project preserves that integration boundary instead of inventing a missing supplier backend.

## Image / variant fixes

The backend was hardened around the earlier image/variant issues:

- Product API uses JPA entity graphs so images and variants are initialized even with `spring.jpa.open-in-view=false`.
- Cart responses initialize product variant/product data before JSON serialization.
- Product variants never receive a blank size during import.
- Variant price falls back safely to the product base price.
- Stock is normalized to zero or greater.
- Image import stores only a non-blank URL and uses a known `ImageType.FRONT`.
- Multipart image upload safely handles an invalid imageType by falling back to `OTHER`.
- Upload size limits are enforced.
- `/uploads/**` is public so customer product images can load without a JWT.
- Product imports use `shopId + supplierImportKey`, so the same supplier product can exist in different shops without cross-shop collisions.
- Invoice access is restricted to the customer who owns the order or the relevant shop owner/admin.

## CSV format

The backend supports these columns:

```text
productId
name
brand
material
collection
size
finish
color
description
basePrice
discountPrice
taxPercentage
minimumOrderQuantity
unit
stockQuantity
imageUrl
supplierName
supplierProductCode
detailUrl
```

`productId`, `importKey`, or `supplierProductCode` is required as the import identity. `name` is required.

## Run frontend

```bash
cd tile-commerce-frontend
npm install
npm run dev
```

Default frontend URL:

```text
http://localhost:5173
```

Create `.env` from `.env.example`:

```env
VITE_API_BASE_URL=http://localhost:9191/api
VITE_ANJANI_IMPORTER_URL=http://localhost:9292
```

## Run backend

Java 17 is required.

Configure MySQL through:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
```

Default backend port:

```text
9191
```

## Main API flow

```text
POST /api/auth/register
POST /api/auth/shop-register
POST /api/auth/login

GET  /api/products/public
GET  /api/products/{id}/public

GET  /api/cart
POST /api/cart/shops/{shopId}/items
PUT  /api/cart/items/{itemId}
DELETE /api/cart/items/{itemId}

GET  /api/addresses
POST /api/addresses

POST /api/orders/checkout
POST /api/orders/{id}/payment
GET  /api/orders/mine
GET  /api/orders/{id}/invoice
GET  /api/orders/{id}/invoice/pdf

GET  /api/products/mine
POST /api/products/import/anjani
POST /api/products/import/csv
GET  /api/products/import/template.csv
```

## Important architecture rule

Do not trust `shopId` from the browser for authorization. The backend verifies the JWT and the authenticated shop owner's shop. The Anjani JSON endpoint still accepts `shopId` for compatibility with the supplied project, but it verifies that it matches the authenticated owner.

For the CSV endpoint, the backend derives the shop directly from the authenticated user and does not read a shop ID from the file.

## Register -> Invoice flow

```text
Customer
  -> Register
  -> Login
  -> Browse Products
  -> Product Details
  -> Select Variant
  -> Add to Cart
  -> Checkout
  -> Delivery Address
  -> Place Order
  -> Submit Payment Reference
  -> Shop Owner verifies payment
  -> Order CONFIRMED
  -> Invoice created
  -> Customer opens Invoice
  -> Invoice PDF
```

## No demo products

The frontend does not seed or display hard-coded sample products. Products are displayed only from `GET /api/products/public` or from an Anjani fetch after the owner explicitly requests it.

Existing rows already present in your MySQL database are, of course, still real database data; delete them separately if you want a completely empty database.
