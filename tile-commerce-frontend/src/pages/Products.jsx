import { useEffect, useState } from "react";
import { Search, PackageOpen, RefreshCw } from "lucide-react";
import { Link } from "react-router-dom";
import api from "../api/axiosClient";
import ProductCard from "../components/ProductCard";

export default function Products() {
  const [products, setProducts] = useState([]),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(""),
    [q, setQ] = useState("");
  const load = () => {
    setLoading(true);
    api
      .get("/products/public")
      .then((r) => setProducts(Array.isArray(r.data) ? r.data : []))
      .catch((e) =>
        setError(e.response?.data?.message || "Unable to load products."),
      )
      .finally(() => setLoading(false));
  };
  useEffect(load, []);
  const filtered = products.filter((p) =>
    [p.name, p.brand, p.color, p.finish, p.tileType, p.shortDescription]
      .join(" ")
      .toLowerCase()
      .includes(q.toLowerCase()),
  );
  return (
    <div className="shop-page">
      <section className="store-hero">
        <div>
          <div className="eyebrow">TILECOMMERCE MARKETPLACE</div>
          <h1>
            Tiles for every room,
            <br />
            <span>from trusted local shops.</span>
          </h1>
          <p>
            Compare tile products, choose your size and finish, then order
            directly from the shop.
          </p>
        </div>
        <div className="hero-stat">
          <strong>{products.length}</strong>
          <span>active products</span>
        </div>
      </section>
      <div className="catalog-toolbar">
        <div>
          <h2>Product catalogue</h2>
          <p>
            {loading
              ? "Loading catalogue…"
              : `${filtered.length} product${filtered.length === 1 ? "" : "s"} available`}
          </p>
        </div>
        <div className="catalog-search">
          <Search size={17} />
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Search tiles, finish, colour…"
          />
        </div>
      </div>
      {error && (
        <div className="form-alert error">
          {error}
          <button onClick={load}>
            <RefreshCw size={14} /> Retry
          </button>
        </div>
      )}
      {loading ? (
        <div className="loading-grid">
          {[1, 2, 3, 4].map((i) => (
            <div className="skeleton-card" key={i} />
          ))}
        </div>
      ) : filtered.length ? (
        <div className="product-grid">
          {filtered.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      ) : (
        <div className="empty-state">
          <PackageOpen size={30} />
          <h3>No products available yet</h3>
          <p>
            The catalogue is empty. A shop owner can add products from the Admin
            → Catalog imports area.
          </p>
          <Link className="secondary-button" to="/login">
            Sign in
          </Link>
        </div>
      )}
    </div>
  );
}
