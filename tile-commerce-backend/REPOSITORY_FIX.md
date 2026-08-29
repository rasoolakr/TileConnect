# JPA Repository Fix

Spring Data JPA derived query method names must follow Java entity property paths, not database column names and not JSON getter names.

For example, Product has a Java property `shop` and `Product#getShopId()` is only a JSON convenience getter. Therefore `findByShopId...` is not a valid derived query path. It must use `findByShop_Id...`.

The same correction was applied to nested relationships:
- Product.shop -> `findByShop_Id...`
- ProductVariant.product -> `findByProduct_Id...`
- ProductImage.product -> `findByProduct_Id...`
- CustomerOrder.customer -> `findByCustomer_Id...`
- CustomerOrder.shop -> `findByShop_Id...`
- Address.user -> `findByUser_Id...`
- Cart.user -> `findByUser_Id...`
- Invoice.order -> `findByOrder_Id...`

Existing Java service call sites were updated to match the repository method names.
