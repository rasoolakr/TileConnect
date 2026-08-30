# Integration contract

## Live source

The scraper reads the public Anjani Tek product list and follows product-detail links. The public pages currently expose fields such as product name, size, collection, finish, color and application on product-detail pages.

## Tile Commerce import

The Tile Commerce backend exposes:

`POST /api/products/import/anjani`

Authorization:

`Bearer <SHOP_OWNER_OR_SUPER_ADMIN_JWT>`

Request contains `shopId` plus selected products. Each selected product must include local `basePrice` and `stockQuantity` because the public catalogue does not provide the dealer's purchase price or inventory.

The Tile Commerce backend generates the ecommerce `productId` and stores:

- supplierName
- supplierProductCode
- supplierImportKey
- supplierSourceUrl
- ProductImage.imageUrl
- ProductVariant.size
- ProductVariant.price
- ProductVariant.stockQuantity

`supplierImportKey` is used to make repeated imports idempotent for the same shop/catalog item.
