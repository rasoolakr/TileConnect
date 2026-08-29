import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CheckCircle2,
  CreditCard,
  MapPin,
  Package,
  PackageCheck,
} from "lucide-react";
import api from "../api/axiosClient";

export default function Checkout() {
  const [addresses, setAddresses] = useState([]);
  const [selected, setSelected] = useState("");

  const [cart, setCart] = useState(null);
  const [cartItems, setCartItems] = useState([]);

  const [form, setForm] = useState({
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    postalCode: "",
    country: "India",
    phoneNumber: "",
    defaultAddress: true,
  });

  const [order, setOrder] = useState(null);
  const [ref, setRef] = useState("");

  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);
  const [loadingCart, setLoadingCart] = useState(true);

  const nav = useNavigate();

  /* =========================================================
     LOAD CHECKOUT DATA
     ========================================================= */

  useEffect(() => {
    loadAddresses();
    loadCart();
  }, []);

  /* =========================================================
     LOAD ADDRESSES
     ========================================================= */

  const loadAddresses = async () => {
    try {
      const r = await api.get("/addresses");

      const addressList = Array.isArray(r.data)
        ? r.data
        : r.data?.content || r.data?.addresses || [];

      setAddresses(addressList);

      const defaultAddress = addressList.find(
        (x) => x.defaultAddress === true
      );

      if (defaultAddress) {
        setSelected(String(defaultAddress.id));
      }
    } catch (e) {
      setErr(
        e.response?.data?.message ||
          "Unable to load addresses."
      );
    }
  };

  /* =========================================================
     LOAD CART
     ========================================================= */

  const loadCart = async () => {
    setLoadingCart(true);

    try {
      const r = await api.get("/cart");

      const data = r.data || {};

      setCart(data);

      /*
       * Support common backend response formats:
       *
       * {
       *   items: [...]
       * }
       *
       * {
       *   cartItems: [...]
       * }
       *
       * {
       *   products: [...]
       * }
       */

      const items =
        data.items ||
        data.cartItems ||
        data.products ||
        [];

      setCartItems(Array.isArray(items) ? items : []);
    } catch (e) {
      setErr(
        e.response?.data?.message ||
          "Unable to load cart details."
      );
    } finally {
      setLoadingCart(false);
    }
  };

  /* =========================================================
     FORM
     ========================================================= */

  const setField = (key, value) => {
    setForm((current) => ({
      ...current,
      [key]: value,
    }));
  };

  /* =========================================================
     SAVE ADDRESS
     ========================================================= */

  const saveAddress = async () => {
    const r = await api.post("/addresses", form);

    setAddresses((current) => [
      ...current,
      r.data,
    ]);

    setSelected(String(r.data.id));

    return r.data.id;
  };

  /* =========================================================
     CHECKOUT
     ========================================================= */

  const checkout = async () => {
    setBusy(true);
    setErr("");

    try {
      if (!cartItems.length) {
        setErr(
          "Your cart is empty. Please add a product before checkout."
        );
        return;
      }

      const addressId =
        selected || (await saveAddress());

      const r = await api.post(
        "/orders/checkout",
        {
          addressId: Number(addressId),
        }
      );

      setOrder(r.data);
    } catch (e) {
      setErr(
        e.response?.data?.message ||
          "Checkout failed."
      );
    } finally {
      setBusy(false);
    }
  };

  /* =========================================================
     PAYMENT
     ========================================================= */

  const pay = async () => {
    setBusy(true);
    setErr("");

    try {
      await api.post(
        `/orders/${order.id}/payment`,
        {
          paymentReference: ref,
        }
      );

      nav("/orders");
    } catch (e) {
      setErr(
        e.response?.data?.message ||
          "Payment submission failed."
      );
    } finally {
      setBusy(false);
    }
  };

  /* =========================================================
     PRODUCT HELPERS
     ========================================================= */

  const getProduct = (item) => {
    return (
      item.product ||
      item.productDetails ||
      item.productResponse ||
      item
    );
  };

  const getProductName = (item) => {
    const product = getProduct(item);

    return (
      product.name ||
      product.productName ||
      product.title ||
      item.productName ||
      "Tile product"
    );
  };

  const getProductImage = (item) => {
    const product = getProduct(item);

    return (
      product.imageUrl ||
      product.image ||
      product.imageURL ||
      product.productImage ||
      item.imageUrl ||
      item.image ||
      null
    );
  };

  const getQuantity = (item) => {
    return Number(
      item.quantity ||
        item.qty ||
        item.orderQuantity ||
        1
    );
  };

  const getUnitPrice = (item) => {
    const product = getProduct(item);

    return Number(
      item.unitPrice ??
        item.price ??
        product.price ??
        product.unitPrice ??
        0
    );
  };

  const getLineTotal = (item) => {
    const quantity = getQuantity(item);
    const unitPrice = getUnitPrice(item);

    return Number(
      item.lineTotal ??
        item.totalPrice ??
        item.subtotal ??
        quantity * unitPrice
    );
  };

  const getProductDetails = (item) => {
    const product = getProduct(item);

    const details = [];

    if (product.brand) {
      details.push(product.brand);
    }

    if (product.size) {
      details.push(product.size);
    }

    if (product.dimensions) {
      details.push(product.dimensions);
    }

    if (product.finish) {
      details.push(product.finish);
    }

    if (product.color) {
      details.push(product.color);
    }

    if (product.category) {
      details.push(product.category);
    }

    return details.join(" • ");
  };

  /* =========================================================
     TOTALS
     ========================================================= */

  const calculatedSubtotal = cartItems.reduce(
    (total, item) =>
      total + getLineTotal(item),
    0
  );

  const subtotal = Number(
    cart?.subtotal ??
      cart?.subTotal ??
      cart?.totalAmount ??
      calculatedSubtotal
  );

  const deliveryCharge = Number(
    cart?.deliveryCharge ??
      cart?.shippingCharge ??
      cart?.deliveryFee ??
      0
  );

  const calculatedGrandTotal =
    subtotal + deliveryCharge;

  const grandTotal = Number(
    cart?.grandTotal ??
      cart?.total ??
      cart?.totalPrice ??
      calculatedGrandTotal
  );

  /* =========================================================
     FORMAT CURRENCY
     ========================================================= */

  const money = (value) =>
    Number(value || 0).toLocaleString(
      "en-IN",
      {
        maximumFractionDigits: 2,
      }
    );

  /* =========================================================
     RENDER
     ========================================================= */

  return (
    <div className="shop-page narrow">
      {/* BACK */}

      <button
        className="back-link button-link"
        onClick={() => nav("/cart")}
      >
        <ArrowLeft size={15} />
        Back to cart
      </button>

      {/* HEADER */}

      <div className="section-heading">
        <div>
          <div className="eyebrow">
            CHECKOUT
          </div>

          <h1>
            {order
              ? "Payment submission"
              : "Complete your order"}
          </h1>

          <p>
            {order
              ? "Your order is created and awaiting payment verification."
              : "Review your products, choose a delivery address and place your order."}
          </p>
        </div>
      </div>

      {/* ERROR */}

      {err && (
        <div className="form-alert error">
          {err}
        </div>
      )}

      {/* =====================================================
          PAYMENT SCREEN
         ===================================================== */}

      {order ? (
        <div className="payment-card">
          <div className="success-mark">
            <CheckCircle2 size={32} />
          </div>

          <div className="eyebrow">
            ORDER CREATED
          </div>

          <h2>{order.orderNumber}</h2>

          <p>
            Grand total:{" "}
            <strong>
              ₹ {money(order.grandTotal)}
            </strong>
          </p>

          <div className="payment-instructions">
            <CreditCard size={20} />

            <div>
              <strong>
                Submit your payment reference
              </strong>

              <span>
                Enter your UPI / bank payment
                reference. A shop owner will verify
                the payment before the invoice is
                issued.
              </span>
            </div>
          </div>

          <label className="payment-field">
            Payment reference

            <input
              value={ref}
              onChange={(e) =>
                setRef(e.target.value)
              }
              placeholder="e.g. UPI transaction reference"
            />
          </label>

          <button
            className="wide-primary"
            disabled={!ref.trim() || busy}
            onClick={pay}
          >
            {busy
              ? "Submitting…"
              : "Submit payment reference"}
          </button>
        </div>
      ) : (
        /* =====================================================
           CHECKOUT SCREEN
           ===================================================== */

        <div className="checkout-layout">
          {/* =================================================
              LEFT SIDE
             ================================================= */}

          <section className="checkout-card">
            {/* PRODUCT REVIEW */}

            <div className="checkout-products">
              <h2>
                <Package size={18} />
                Order items
              </h2>

              {loadingCart ? (
                <div className="checkout-loading">
                  Loading your products…
                </div>
              ) : cartItems.length === 0 ? (
                <div className="checkout-empty">
                  <Package size={25} />

                  <strong>
                    Your cart is empty
                  </strong>

                  <span>
                    Add products to your cart before
                    continuing to checkout.
                  </span>

                  <button
                    className="secondary-button"
                    onClick={() => nav("/cart")}
                  >
                    Return to cart
                  </button>
                </div>
              ) : (
                <div className="checkout-product-list">
                  {cartItems.map(
                    (item, index) => {
                      const image =
                        getProductImage(item);

                      const name =
                        getProductName(item);

                      const details =
                        getProductDetails(item);

                      const quantity =
                        getQuantity(item);

                      const unitPrice =
                        getUnitPrice(item);

                      const lineTotal =
                        getLineTotal(item);

                      return (
                        <div
                          className="checkout-product"
                          key={
                            item.id ||
                            item.cartItemId ||
                            item.productId ||
                            index
                          }
                        >
                          {/* IMAGE */}

                          <div className="checkout-product-image">
                            {image ? (
                              <img
                                src={image}
                                alt={name}
                              />
                            ) : (
                              <Package
                                size={20}
                              />
                            )}
                          </div>

                          {/* INFO */}

                          <div className="checkout-product-info">
                            <strong>
                              {name}
                            </strong>

                            {details && (
                              <span>
                                {details}
                              </span>
                            )}

                            <small>
                              ₹ {money(unitPrice)} ×{" "}
                              {quantity}
                            </small>
                          </div>

                          {/* TOTAL */}

                          <div className="checkout-product-total">
                            ₹ {money(lineTotal)}
                          </div>
                        </div>
                      );
                    }
                  )}
                </div>
              )}
            </div>

            {/* =================================================
                DELIVERY ADDRESS
               ================================================= */}

            <div className="checkout-address-section">
              <h2>
                <MapPin size={18} />
                Delivery address
              </h2>

              {addresses.length > 0 && (
                <div className="address-options">
                  {addresses.map((a) => (
                    <button
                      type="button"
                      className={`address-option ${
                        String(a.id) ===
                        String(selected)
                          ? "selected"
                          : ""
                      }`}
                      key={a.id}
                      onClick={() =>
                        setSelected(
                          String(a.id)
                        )
                      }
                    >
                      <strong>
                        {a.addressLine1}
                      </strong>

                      <span>
                        {a.addressLine2 &&
                          `${a.addressLine2}, `}
                        {a.city}, {a.state}{" "}
                        {a.postalCode}
                      </span>

                      {a.phoneNumber && (
                        <span>
                          Phone:{" "}
                          {a.phoneNumber}
                        </span>
                      )}

                      {a.defaultAddress && (
                        <em>Default</em>
                      )}
                    </button>
                  ))}
                </div>
              )}

              {/* NEW ADDRESS */}

              <div className="new-address">
                <h3>
                  {addresses.length
                    ? "Add another address"
                    : "Enter delivery address"}
                </h3>

                <div className="modern-form two-col">
                  <label className="span-2">
                    Address line 1

                    <input
                      value={
                        form.addressLine1
                      }
                      onChange={(e) =>
                        setField(
                          "addressLine1",
                          e.target.value
                        )
                      }
                    />
                  </label>

                  <label className="span-2">
                    Address line 2

                    <input
                      value={
                        form.addressLine2
                      }
                      onChange={(e) =>
                        setField(
                          "addressLine2",
                          e.target.value
                        )
                      }
                    />
                  </label>

                  <label>
                    City

                    <input
                      value={form.city}
                      onChange={(e) =>
                        setField(
                          "city",
                          e.target.value
                        )
                      }
                    />
                  </label>

                  <label>
                    State

                    <input
                      value={form.state}
                      onChange={(e) =>
                        setField(
                          "state",
                          e.target.value
                        )
                      }
                    />
                  </label>

                  <label>
                    Postal code

                    <input
                      value={
                        form.postalCode
                      }
                      onChange={(e) =>
                        setField(
                          "postalCode",
                          e.target.value
                        )
                      }
                    />
                  </label>

                  <label>
                    Phone

                    <input
                      value={
                        form.phoneNumber
                      }
                      onChange={(e) =>
                        setField(
                          "phoneNumber",
                          e.target.value
                        )
                      }
                    />
                  </label>
                </div>
              </div>
            </div>
          </section>

          {/* =================================================
              RIGHT SIDE ORDER SUMMARY
             ================================================= */}

          <aside className="checkout-order-summary">
            {/* HEADER */}

            <div className="checkout-summary-header">
              <h3>Order summary</h3>

              <p>
                {cartItems.length}{" "}
                {cartItems.length === 1
                  ? "product"
                  : "products"}{" "}
                in your order
              </p>
            </div>

            {/* PRODUCTS */}

            <div className="checkout-summary-items">
              {cartItems.map(
                (item, index) => {
                  const image =
                    getProductImage(item);

                  const name =
                    getProductName(item);

                  const quantity =
                    getQuantity(item);

                  const lineTotal =
                    getLineTotal(item);

                  return (
                    <div
                      className="checkout-summary-item"
                      key={
                        item.id ||
                        item.cartItemId ||
                        item.productId ||
                        index
                      }
                    >
                      <div className="checkout-summary-image">
                        {image ? (
                          <img
                            src={image}
                            alt={name}
                          />
                        ) : (
                          <Package size={18} />
                        )}
                      </div>

                      <div className="checkout-summary-info">
                        <strong>
                          {name}
                        </strong>

                        <span>
                          Qty: {quantity}
                        </span>
                      </div>

                      <div className="checkout-summary-price">
                        <strong>
                          ₹ {money(lineTotal)}
                        </strong>
                      </div>
                    </div>
                  );
                }
              )}
            </div>

            {/* TOTALS */}

            <div className="checkout-summary-totals">
              <div className="checkout-summary-row">
                <span>Subtotal</span>

                <strong>
                  ₹ {money(subtotal)}
                </strong>
              </div>

              <div className="checkout-summary-row">
                <span>Delivery</span>

                <strong>
                  {deliveryCharge > 0
                    ? `₹ ${money(
                        deliveryCharge
                      )}`
                    : "FREE"}
                </strong>
              </div>

              <div className="checkout-summary-total">
                <div>
                  <span>Grand total</span>

                  <small>
                    Inclusive of all charges
                  </small>
                </div>

                <strong>
                  ₹ {money(grandTotal)}
                </strong>
              </div>
            </div>

            {/* ACTION */}

            <div className="checkout-summary-action">
              <button
                className="wide-primary"
                onClick={checkout}
                disabled={
                  busy ||
                  loadingCart ||
                  cartItems.length === 0
                }
              >
                {busy ? (
                  "Creating order…"
                ) : (
                  <>
                    Place order
                    <PackageCheck size={16} />
                  </>
                )}
              </button>
            </div>
          </aside>
        </div>
      )}
    </div>
  );
}