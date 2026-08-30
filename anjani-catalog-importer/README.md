# Anjani Tek Live Catalog Fetcher

This project is the supplier-catalog integration used by the Tile Commerce application.

Primary flow:

Anjani Tek public catalogue -> this Spring Boot scraper -> React preview -> selected products -> Tile Commerce `/api/products/import/anjani`.

There is intentionally **no sample product data** in this version. The importer always reads the live public Anjani catalogue.

## Backend

```bash
cd backend
mvn spring-boot:run
```
Runs on `http://localhost:9292`.

### APIs

- `POST /api/anjani/fetch` - fetch live products
- `POST /api/anjani/csv` - export the currently fetched live catalogue as CSV
- `GET /api/anjani/health` - health check

Example:

```json
{
  "collection": "GVT Collections",
  "size": "600x600mm",
  "finish": "Glossy Surface",
  "color": "BLACK",
  "maxProducts": 20
}
```

The public site currently exposes product lists and product-detail pages with product name/code, collection, size, finish, color and sometimes application. The scraper follows product-detail links so the product data is not fabricated.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`.

For the complete ecommerce flow, use the Anjani Import page inside the Tile Commerce frontend. It fetches from this service and sends only the products you select to the authenticated shop's Tile Commerce backend.
