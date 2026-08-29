import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import {
  ArrowLeft,
  ShoppingCart,
  Check,
  PackageCheck,
  Minus,
  Plus,
} from "lucide-react";
import api from "../api/axiosClient";
import { useAuth } from "../context/AuthContext";

export default function ProductDetails() {
  const { id } = useParams();
  const nav = useNavigate();
  const { user } = useAuth();
  const [p, setP] = useState(null),
    [error, setError] = useState(""),
    [variant, setVariant] = useState(null),
    [qty, setQty] = useState(1),
    [busy, setBusy] = useState(false),
    [msg, setMsg] = useState("");
  useEffect(() => {
    api
      .get(`/products/${id}/public`)
      .then((r) => {
        setP(r.data);
        setVariant(r.data.variants?.find((v) => v.active) || null);
      })
      .catch((e) =>
        setError(e.response?.data?.message || "Product not found."),
      );
  }, [id]);
  if (error)
    return (
      <div className="detail-page">
        <div className="form-alert error">{error}</div>
      </div>
    );
  if (!p)
    return <div className="detail-page loading-text">Loading product…</div>;
  const image =
    p.images?.find((x) => x.primaryImage)?.imageUrl || p.images?.[0]?.imageUrl;
  const price = variant?.price || p.discountPrice || p.basePrice;
  const imageSrc = image?.startsWith("http")
    ? image
    : image
      ? `${(import.meta.env.VITE_API_BASE_URL || "http://localhost:9191/api").replace(/\/api$/, "")}${image}`
      : "";
  const add = async () => {
  if (!user?.token) {
    nav("/login", {
      state: { from: `/products/${id}` },
    });
    return;
  }

  if (!p.shopId) {
    setMsg("This product is missing its shop information.");
    return;
  }

  if (!variant) {
    setMsg("This product has no purchasable variant yet.");
    return;
  }

  if (variant.stockQuantity < qty) {
    setMsg("Not enough stock available.");
    return;
  }

  setBusy(true);
  setMsg("");

  try {
    const response = await api.post(
      `/cart/shops/${p.shopId}/items`,
      {
        productVariantId: variant.id,
        quantity: qty,
      }
    );

    console.log("Add to cart response:", response);

    setMsg("✓ Item added to cart successfully.");
  } catch (e) {
    console.error("Add to cart failed:", e);
    console.error("Status:", e.response?.status);
    console.error("Response:", e.response?.data);

    setMsg(
      e.response?.data?.message ||
        e.response?.data?.error ||
        "Unable to add to cart."
    );
  } finally {
    setBusy(false);
  }
};
  return (
    <div className="detail-page">
      <Link className="back-link" to="/products">
        <ArrowLeft size={15} /> Back to products
      </Link>
      <div className="detail-card">
        <div className="detail-image">
          {imageSrc ? (
            <img src={imageSrc} alt={p.name} />
          ) : (
            <div className="image-placeholder large">
              <PackageCheck size={48} />
              <span>Tile image</span>
            </div>
          )}
        </div>
        <div className="detail-copy">
          <div className="product-meta">
            {p.brand || "TileCommerce"} {p.finish && `· ${p.finish}`}
          </div>
          <h1>{p.name}</h1>
          <p className="detail-description">
            {p.detailedDescription ||
              p.shortDescription ||
              "Premium tile product."}
          </p>
          <div className="price-large">
            ₹ {Number(price || 0).toLocaleString("en-IN")}{" "}
            <small>/ {p.unit || "box"}</small>
          </div>
          <div className="detail-spec-grid">
            <div>
              <span>Colour</span>
              <strong>{p.color || "—"}</strong>
            </div>
            <div>
              <span>Finish</span>
              <strong>{p.finish || "—"}</strong>
            </div>
            <div>
              <span>Collection</span>
              <strong>{p.tileType || "—"}</strong>
            </div>
            <div>
              <span>Minimum order</span>
              <strong>
                {p.minimumOrderQuantity || 1} {p.unit || "box"}
              </strong>
            </div>
          </div>
          <h3>Choose size</h3>
          <div className="variant-list">
            {p.variants
              ?.filter((v) => v.active)
              .map((v) => (
                <button
                  className={`variant-option ${variant?.id === v.id ? "selected" : ""}`}
                  key={v.id}
                  onClick={() => {
                    setVariant(v);
                    setQty(1);
                  }}
                >
                  <span>{v.size}</span>
                  <small>
                    ₹ {Number(v.price).toLocaleString("en-IN")} ·{" "}
                    {v.stockQuantity} in stock
                  </small>
                </button>
              ))}
          </div>
          <div className="buy-row">
            <div className="qty">
              <button onClick={() => setQty(Math.max(1, qty - 1))}>
                <Minus size={15} />
              </button>
              <strong>{qty}</strong>
              <button
                onClick={() =>
                  setQty(Math.min(variant?.stockQuantity || 99, qty + 1))
                }
              >
                <Plus size={15} />
              </button>
            </div>
            <button
              className="wide-primary buy-button"
              onClick={add}
              disabled={busy || !variant || variant.stockQuantity < 1}
            >
              {busy ? (
                "Adding…"
              ) : (
                <>
                  <ShoppingCart size={17} /> Add to cart
                </>
              )}
            </button>
          </div>
          {msg && <div className="form-alert error">{msg}</div>}
          {!user && (
            <p className="login-note">
              You can browse without signing in. Sign in is required when you
              add a product to your cart.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
