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
        e.response?.data?.message ||
          "Unable to load cart."
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
        <div className="form-alert error">
          {err}
        </div>
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

  /*
   * ==========================================
   * CART CALCULATIONS
   * ==========================================
   */

  const subtotal = items.reduce((sum, item) => {
    const price = Number(
      item.unitPrice ||
        item.productVariant?.price ||
        0
    );

    return sum + price * Number(item.quantity || 0);
  }, 0);

  /*
   * Get GST/tax percentage.
   *
   * Depending on your backend response,
   * taxPercentage may be available at one
   * of these locations.
   */
  const getTaxPercentage = (item) => {
    return Number(
      item.taxPercentage ??
        item.productVariant?.taxPercentage ??
        item.productVariant?.product?.taxPercentage ??
        0
    );
  };

  /*
   * Calculate tax per item.
   *
   * Example:
   *
   * Product = ₹2,000
   * Quantity = 2
   * Subtotal = ₹4,000
   * GST = 18%
   * GST amount = ₹720
   */
  const taxAmount = items.reduce((sum, item) => {
    const price = Number(
      item.unitPrice ||
        item.productVariant?.price ||
        0
    );

    const quantity = Number(
      item.quantity || 0
    );

    const itemSubtotal = price * quantity;

    const taxPercentage =
      getTaxPercentage(item);

    return (
      sum +
      (itemSubtotal * taxPercentage) / 100
    );
  }, 0);

  /*
   * Final amount
   */
  const grandTotal = subtotal + taxAmount;

  /*
   * Total quantity
   */
  const totalItems = items.reduce(
    (sum, item) =>
      sum + Number(item.quantity || 0),
    0
  );

  /*
   * If all items have the same GST rate,
   * display that rate.
   *
   * Otherwise display "Applicable tax".
   */
  const taxPercentages = [
    ...new Set(
      items
        .map((item) => getTaxPercentage(item))
        .filter((tax) => tax > 0)
    ),
  ];

  const taxLabel =
    taxPercentages.length === 1
      ? `GST (${taxPercentages[0]}%)`
      : "GST / Tax";

  return (
    <div className="shop-page cart-page">

      {/* =========================================
          HEADER
      ========================================== */}

      <div className="cart-header">
        <div>
          <div className="eyebrow">
            YOUR SHOPPING BAG
          </div>

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

        /* =========================================
           EMPTY CART
        ========================================== */

        <div className="empty-state premium-empty">

          <div className="empty-icon">
            <ShoppingBag size={34} />
          </div>

          <h3>
            Your tile collection is waiting
          </h3>

          <p>
            Your cart is currently empty.
            Explore our collection and find
            the perfect tiles for your space.
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

          {/* =====================================
              PRODUCTS
          ====================================== */}

          <div className="cart-items">

            <div className="cart-section-title">

              <div>
                <h2>Selected tiles</h2>

                <span>
                  {items.length} product
                  {items.length === 1
                    ? ""
                    : "s"}
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

              const quantity = Number(
                i.quantity || 0
              );

              const lineTotal =
                price * quantity;

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
                          i.productVariant
                            .imageUrl
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
                        "Standard"}

                      {" · "}

                      {i.productVariant?.finish ||
                        "Premium finish"}

                    </span>

                    <span className="unit-price">

                      ₹{" "}
                      {price.toLocaleString(
                        "en-IN"
                      )}

                      {" "}

                      <small>
                        / box
                      </small>

                    </span>


                    <div className="cart-bottom-row">

                      <div>

                        <span className="quantity-label">
                          Quantity
                        </span>

                        <div className="cart-qty">

                          <button
                            onClick={() =>
                              quantity > 1 &&
                              update(
                                i.id,
                                quantity - 1
                              )
                            }
                            disabled={
                              quantity <= 1
                            }
                          >
                            <Minus size={13} />
                          </button>

                          <b>{quantity}</b>

                          <button
                            onClick={() =>
                              update(
                                i.id,
                                quantity + 1
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
                  <strong>
                    Secure checkout
                  </strong>

                  <small>
                    Your information is protected
                  </small>
                </span>
              </div>

              <div>
                <Truck size={19} />

                <span>
                  <strong>
                    Reliable delivery
                  </strong>

                  <small>
                    Carefully packed tiles
                  </small>
                </span>
              </div>

            </div>

          </div>


          {/* =====================================
              ORDER SUMMARY
          ====================================== */}

          <aside className="summary-card premium-summary">

            <div className="summary-heading">

              <h3>
                Order summary
              </h3>

              <span>
                {totalItems} item
                {totalItems === 1
                  ? ""
                  : "s"}
              </span>

            </div>


            {/* =================================
                INDIVIDUAL ITEMS
            ================================== */}

            <div className="summary-items">

              {items.map((item) => {

                const price = Number(
                  item.unitPrice ||
                    item.productVariant
                      ?.price ||
                    0
                );

                const quantity = Number(
                  item.quantity || 0
                );

                const lineTotal =
                  price * quantity;

                return (

                  <div
                    className="summary-item"
                    key={item.id}
                  >

                    <div className="summary-item-info">

                      <strong>
                        {item.productVariant
                          ?.productName ||
                          "Tile product"}
                      </strong>

                      <small>
                        ₹{" "}
                        {price.toLocaleString(
                          "en-IN"
                        )}

                        {" × "}

                        {quantity}
                      </small>

                    </div>

                    <strong className="summary-item-total">
                      ₹{" "}
                      {lineTotal.toLocaleString(
                        "en-IN"
                      )}
                    </strong>

                  </div>

                );
              })}

            </div>


            <hr />


            {/* SUBTOTAL */}

            <div className="summary-row">

              <span>
                Subtotal
              </span>

              <strong>
                ₹{" "}
                {subtotal.toLocaleString(
                  "en-IN"
                )}
              </strong>

            </div>


            {/* GST / TAX */}

            {taxAmount > 0 && (

              <div className="summary-row">

                <span>
                  {taxLabel}
                </span>

                <strong>
                  ₹{" "}
                  {taxAmount.toLocaleString(
                    "en-IN"
                  )}
                </strong>

              </div>

            )}


            {/* DELIVERY */}

            <div className="summary-row delivery-row">

              <span>
                Delivery
              </span>

              <span className="muted">
                Calculated at checkout
              </span>

            </div>


            {/* NOTE */}

            <div className="summary-note">

              <Check size={15} />

              <span>
                Final delivery charges will
                depend on your location and
                order size.
              </span>

            </div>


            <hr />


            {/* FINAL TOTAL */}

            <div className="summary-total">

              <div>

                <span>
                  Estimated total
                </span>

                <small>
                  {taxAmount > 0
                    ? "Including applicable GST / tax"
                    : "Taxes not included"}
                </small>

              </div>

              <strong>
                ₹{" "}
                {grandTotal.toLocaleString(
                  "en-IN"
                )}
              </strong>

            </div>


            {/* CHECKOUT */}

            <button
              className="wide-primary checkout-button"
              onClick={() =>
                nav("/checkout")
              }
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
