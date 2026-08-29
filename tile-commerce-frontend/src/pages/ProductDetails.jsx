import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import {
  ArrowLeft,
  ShoppingCart,
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

  const [p, setP] = useState(null);
  const [error, setError] = useState("");
  const [variant, setVariant] = useState(null);
  const [qty, setQty] = useState(1);
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState("");

  // =========================================================
  // LOAD PRODUCT
  // =========================================================

  useEffect(() => {
    api
      .get(`/products/${id}/public`)
      .then((r) => {
        setP(r.data);

        const activeVariant =
          r.data.variants?.find((v) => v.active) || null;

        setVariant(activeVariant);
      })
      .catch((e) => {
        setError(
          e.response?.data?.message ||
            "Product not found."
        );
      });
  }, [id]);

  // =========================================================
  // LOADING / ERROR
  // =========================================================

  if (error) {
    return (
      <div className="detail-page">
        <div className="form-alert error">
          {error}
        </div>
      </div>
    );
  }

  if (!p) {
    return (
      <div className="detail-page loading-text">
        Loading product…
      </div>
    );
  }

  // =========================================================
  // PRODUCT IMAGE
  // =========================================================

  const image =
    p.images?.find((x) => x.primaryImage)?.imageUrl ||
    p.images?.[0]?.imageUrl;

  const price =
    variant?.price ||
    p.discountPrice ||
    p.basePrice;

  const imageSrc = image?.startsWith("http")
    ? image
    : image
      ? `${
          (
            import.meta.env.VITE_API_BASE_URL ||
            "http://localhost:9191/api"
          ).replace(/\/api$/, "")
        }${image}`
      : "";

  // =========================================================
  // ADD TO CART
  // =========================================================

  const add = async () => {
    // -------------------------------------------------------
    // LOGIN CHECK
    // -------------------------------------------------------

    if (!user?.token) {
      nav("/login", {
        state: {
          from: `/products/${id}`,
        },
      });

      return;
    }

    // -------------------------------------------------------
    // SHOP CHECK
    // -------------------------------------------------------

    if (!p.shopId) {
      setMsg(
        "This product is missing its shop information."
      );

      return;
    }

    // -------------------------------------------------------
    // VARIANT CHECK
    // -------------------------------------------------------

    if (!variant) {
      setMsg(
        "This product has no purchasable variant yet."
      );

      return;
    }

    // -------------------------------------------------------
    // STOCK CHECK
    // -------------------------------------------------------

    if (variant.stockQuantity < qty) {
      setMsg(
        "Not enough stock available."
      );

      return;
    }

    setBusy(true);
    setMsg("");

    try {
      // -----------------------------------------------------
      // ADD ITEM
      // -----------------------------------------------------

      const response = await api.post(
        `/cart/shops/${p.shopId}/items`,
        {
          productVariantId: variant.id,
          quantity: qty,
        }
      );

      console.log(
        "Add to cart response:",
        response.data
      );

      /*
       * =====================================================
       * IMPORTANT
       * =====================================================
       *
       * Notify Navbar that the cart has changed.
       *
       * Navbar is already listening for:
       *
       * window.addEventListener("cart-updated", ...)
       *
       * We also pass the returned CartResponse so Navbar
       * can immediately calculate the count.
       */

      window.dispatchEvent(
        new CustomEvent("cart-updated", {
          detail: response.data,
        })
      );

      setMsg(
        "✓ Item added to cart successfully."
      );

    } catch (e) {
      console.error(
        "Add to cart failed:",
        e
      );

      console.error(
        "Status:",
        e.response?.status
      );

      console.error(
        "Response:",
        e.response?.data
      );

      setMsg(
        e.response?.data?.message ||
          e.response?.data?.error ||
          "Unable to add to cart."
      );

    } finally {
      setBusy(false);
    }
  };

  // =========================================================
  // UI
  // =========================================================

  return (
    <div className="detail-page">

      {/* BACK */}

      <Link
        className="back-link"
        to="/products"
      >
        <ArrowLeft size={15} />
        Back to products
      </Link>

      <div className="detail-card">

        {/* =================================================
            IMAGE
        ================================================== */}

        <div className="detail-image">

          {imageSrc ? (
            <img
              src={imageSrc}
              alt={p.name}
            />
          ) : (
            <div className="image-placeholder large">
              <PackageCheck size={48} />
              <span>Tile image</span>
            </div>
          )}

        </div>

        {/* =================================================
            DETAILS
        ================================================== */}

        <div className="detail-copy">

          <div className="product-meta">
            {p.brand || "TileCommerce"}
            {p.finish && ` · ${p.finish}`}
          </div>

          <h1>{p.name}</h1>

          <p className="detail-description">
            {p.detailedDescription ||
              p.shortDescription ||
              "Premium tile product."}
          </p>

          <div className="price-large">
            ₹{" "}
            {Number(
              price || 0
            ).toLocaleString("en-IN")}{" "}

            <small>
              / {p.unit || "box"}
            </small>
          </div>

          {/* =================================================
              SPECIFICATIONS
          ================================================== */}

          <div className="detail-spec-grid">

            <div>
              <span>Colour</span>
              <strong>
                {p.color || "—"}
              </strong>
            </div>

            <div>
              <span>Finish</span>
              <strong>
                {p.finish || "—"}
              </strong>
            </div>

            <div>
              <span>Collection</span>
              <strong>
                {p.tileType || "—"}
              </strong>
            </div>

            <div>
              <span>Minimum order</span>
              <strong>
                {p.minimumOrderQuantity || 1}{" "}
                {p.unit || "box"}
              </strong>
            </div>

          </div>

          {/* =================================================
              VARIANTS
          ================================================== */}

          <h3>Choose size</h3>

          <div className="variant-list">

            {p.variants
              ?.filter((v) => v.active)
              .map((v) => (

                <button
                  className={`variant-option ${
                    variant?.id === v.id
                      ? "selected"
                      : ""
                  }`}
                  key={v.id}
                  onClick={() => {
                    setVariant(v);
                    setQty(1);
                    setMsg("");
                  }}
                >

                  <span>
                    {v.size}
                  </span>

                  <small>
                    ₹{" "}
                    {Number(
                      v.price
                    ).toLocaleString("en-IN")}{" "}
                    ·{" "}
                    {v.stockQuantity} in stock
                  </small>

                </button>

              ))}

          </div>

          {/* =================================================
              BUY ROW
          ================================================== */}

          <div className="buy-row">

            {/* QUANTITY */}

            <div className="qty">

              <button
                onClick={() =>
                  setQty(
                    Math.max(
                      1,
                      qty - 1
                    )
                  )
                }
                disabled={busy}
              >
                <Minus size={15} />
              </button>

              <strong>
                {qty}
              </strong>

              <button
                onClick={() =>
                  setQty(
                    Math.min(
                      variant?.stockQuantity || 99,
                      qty + 1
                    )
                  )
                }
                disabled={busy}
              >
                <Plus size={15} />
              </button>

            </div>

            {/* ADD TO CART */}

            <button
              className="wide-primary buy-button"
              onClick={add}
              disabled={
                busy ||
                !variant ||
                variant.stockQuantity < 1
              }
            >

              {busy ? (
                "Adding…"
              ) : (
                <>
                  <ShoppingCart size={17} />
                  Add to cart
                </>
              )}

            </button>

          </div>

          {/* =================================================
              MESSAGE
          ================================================== */}

          {msg && (
            <div
              className={`form-alert ${
                msg.startsWith("✓")
                  ? "success"
                  : "error"
              }`}
            >
              {msg}
            </div>
          )}

          {/* LOGIN NOTE */}

          {!user && (
            <p className="login-note">
              You can browse without signing in.
              Sign in is required when you add a
              product to your cart.
            </p>
          )}

        </div>
      </div>
    </div>
  );
}
