# Changes in this version

## Authentication / roles
- Removed default shop-owner session behavior from the frontend.
- Versioned auth storage key so the old `tileUser` demo/session is not reused.
- Public users see Products + Sign in.
- Customers see Products + Cart + Orders + username + Sign out.
- Shop owners additionally see Admin.
- Admin routes are protected by React route guards and Spring Security.

## Admin catalogue
- Anjani Tek import is now inside Admin.
- CSV upload is now inside Admin.
- No hard-coded products are displayed in the import screen.
- My Products tab shows only the authenticated shop owner's catalogue.

## Product data
- Added `productCode` to Product for a business/supplier product identifier.
- Added `GET /api/products/mine`.
- CSV imports derive the destination shop from the authenticated user.
- Anjani imports validate ownership and use shop + supplier import key.

## Image / variant stability
- Public product queries use JPA EntityGraph to load images/variants/category/shop.
- Cart/order/invoice services initialize required lazy relationships before serialization.
- Invalid image type falls back to `OTHER`.
- Image URL import only stores non-blank values.
- Variant size is normalized to `Standard` when absent.
- Variant price safely falls back to base price.
- `/uploads/**` is publicly readable for customer product images.
- Invoice access is authorization checked.

## Checkout / invoice
- Customer checkout no longer requires clicking Place Order twice when a new address is entered.
- Added an Invoice page with invoice details and PDF access.
- Invoice access is limited to the customer, relevant shop owner, or super admin.

## CSV
- Added:
  - `POST /api/products/import/csv`
  - `GET /api/products/import/template.csv`
- CSV validation includes required `name`, `basePrice`, and product identity (`productId` / `importKey` / `supplierProductCode`).
- Supports quoted CSV fields.
- Maximum CSV size is 10 MB.
