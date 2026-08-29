import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import {
  ArrowRight,
  Check,
  Minus,
  Plus,
  ShoppingBag,
  Trash2,
  ShieldCheck,
  Truck,
} from "../icons";

import api from "../api/axiosClient";

export default function Cart() {
  const [cart, setCart] = useState(null);
  const [err, setErr] = useState("");
  const nav = useNavigate();

  const load = useCallback(async () => {
    try {
      setErr("");
      const response = await api.get("/cart");
      setCart(response.data);
    } catch (e) {
      setErr(
        e.response?.data?.message || "Unable to load cart."
      );
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const update = async (id, qty) => {
  try {
    setErr("");

    await api.put(`/cart/items/${id}`, {
      quantity: qty,
    });

    // Notify Navbar
    window.dispatchEvent(
      new Event("cart-updated")
    );

    await load();
  } catch (e) {
    setErr(
      e.response?.data?.message ||
        "Unable to update cart."
    );
  }
};

  const remove = async (id) => {
  try {
    setErr("");

    await api.delete(`/cart/items/${id}`);

    // Notify Navbar
    window.dispatchEvent(
      new Event("cart-updated")
    );

    await load();
  } catch (e) {
    setErr(
      e.response?.data?.message ||
        "Unable to remove item."
    );
  }
};

  if (err && !cart) {
    return (
      <div className="shop-page">
        <div className="form-alert error">{err}</div>
      </div>
    );
  }

  if (!cart) {
    return (
      <div className="shop-page loading-text">
        Loading your cart…
      </div>
    );
  }

  const items = cart.items || [];

  const subtotal = items.reduce(
    (s, i) =>
      s +
      Number(
        i.unitPrice ||
          i.productVariant?.price ||
          0
      ) *
        i.quantity,
    0
  );

  const totalItems = items.reduce(
    (sum, item) => sum + item.quantity,
    0
  );

  return (
    <div className="shop-page cart-page">
      {/* HEADER */}
      <div className="cart-header">
        <div>
          <div className="eyebrow">YOUR SHOPPING BAG</div>

          <h1>Your Cart</h1>

          <p>
            Review your selected tiles before
            proceeding to checkout.
          </p>
        </div>

        <div className="cart-count-badge">
          <ShoppingBag size={18} />

          <span>
            {totalItems} tile
            {totalItems === 1 ? "" : "s"}
          </span>
        </div>
      </div>

      {err && (
        <div className="form-alert error">
          {err}
        </div>
      )}

      {!items.length ? (
        /* EMPTY CART */
        <div className="empty-state premium-empty">
          <div className="empty-icon">
            <ShoppingBag size={34} />
          </div>

          <h3>Your tile collection is waiting</h3>

          <p>
            Your cart is currently empty. Explore our
            collection and find the perfect tiles for
            your space.
          </p>

          <Link
            className="primary-button"
            to="/products"
          >
            Explore tiles
            <ArrowRight size={15} />
          </Link>
        </div>
      ) : (
        <div className="cart-layout">
          {/* PRODUCTS */}
          <div className="cart-items">
            <div className="cart-section-title">
              <div>
                <h2>Selected tiles</h2>
                <span>
                  {items.length} product
                  {items.length === 1 ? "" : "s"}
                </span>
              </div>

              <Link to="/products">
                + Add more tiles
              </Link>
            </div>

            {items.map((i) => {
              const price = Number(
                i.unitPrice ||
                  i.productVariant?.price ||
                  0
              );

              const lineTotal =
                price * i.quantity;

              return (
                <div
                  className="cart-item premium-cart-item"
                  key={i.id}
                >
                  {/* PRODUCT IMAGE */}
                  <div className="cart-thumb tile-thumb">
                    {i.productVariant?.imageUrl ? (
                      <img
                        src={
                          i.productVariant.imageUrl
                        }
                        alt={
                          i.productVariant
                            ?.productName ||
                          "Tile product"
                        }
                      />
                    ) : (
                      <ShoppingBag size={25} />
                    )}

                    <span className="tile-badge">
                      TILE
                    </span>
                  </div>

                  {/* PRODUCT INFO */}
                  <div className="cart-info">
                    <div className="product-title-row">
                      <strong>
                        {i.productVariant
                          ?.productName ||
                          "Tile product"}
                      </strong>

                      <span className="stock-label">
                        <Check size={13} />
                        In stock
                      </span>
                    </div>

                    <span className="tile-details">
                      {i.productVariant?.size ||
                        "Standard"}{" "}
                      ·{" "}
                      {i.productVariant?.finish ||
                        "Premium finish"}
                    </span>

                    <span className="unit-price">
                      ₹{" "}
                      {price.toLocaleString(
                        "en-IN"
                      )}{" "}
                      <small>/ box</small>
                    </span>

                    <div className="cart-bottom-row">
                      <div>
                        <span className="quantity-label">
                          Quantity
                        </span>

                        <div className="cart-qty">
                          <button
                            onClick={() =>
                              i.quantity > 1 &&
                              update(
                                i.id,
                                i.quantity - 1
                              )
                            }
                            disabled={
                              i.quantity <= 1
                            }
                          >
                            <Minus size={13} />
                          </button>

                          <b>{i.quantity}</b>

                          <button
                            onClick={() =>
                              update(
                                i.id,
                                i.quantity + 1
                              )
                            }
                          >
                            <Plus size={13} />
                          </button>
                        </div>
                      </div>

                      <button
                        className="remove-button"
                        onClick={() =>
                          remove(i.id)
                        }
                      >
                        <Trash2 size={15} />
                        Remove
                      </button>
                    </div>
                  </div>

                  {/* LINE TOTAL */}
                  <div className="cart-price">
                    <span>Total</span>

                    <strong>
                      ₹{" "}
                      {lineTotal.toLocaleString(
                        "en-IN"
                      )}
                    </strong>
                  </div>
                </div>
              );
            })}

            {/* TRUST STRIP */}
            <div className="cart-trust-strip">
              <div>
                <ShieldCheck size={19} />

                <span>
                  <strong>Secure checkout</strong>
                  <small>
                    Your information is protected
                  </small>
                </span>
              </div>

              <div>
                <Truck size={19} />

                <span>
                  <strong>Reliable delivery</strong>
                  <small>
                    Carefully packed tiles
                  </small>
                </span>
              </div>
            </div>
          </div>

          {/* SUMMARY */}
          <aside className="summary-card premium-summary">
            <div className="summary-heading">
              <h3>Order summary</h3>

              <span>
                {totalItems} item
                {totalItems === 1 ? "" : "s"}
              </span>
            </div>

            <div className="summary-row">
              <span>Tile subtotal</span>

              <strong>
                ₹ {subtotal.toLocaleString("en-IN")}
              </strong>
            </div>

            <div className="summary-row delivery-row">
              <span>Delivery</span>

              <span className="muted">
                Calculated at checkout
              </span>
            </div>

            <div className="summary-note">
              <Check size={15} />

              <span>
                Final delivery charges will depend on
                your location and order size.
              </span>
            </div>

            <hr />

            <div className="summary-total">
              <div>
                <span>Estimated total</span>
                <small>Inclusive of selected items</small>
              </div>

              <strong>
                ₹ {subtotal.toLocaleString("en-IN")}
              </strong>
            </div>

            <button
              className="wide-primary checkout-button"
              onClick={() => nav("/checkout")}
            >
              Continue to checkout
              <ArrowRight size={17} />
            </button>

            <Link
              to="/products"
              className="continue-link"
            >
              ← Continue shopping
            </Link>
          </aside>
        </div>
      )}
    </div>
  );
}