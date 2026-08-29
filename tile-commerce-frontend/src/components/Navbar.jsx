import { useCallback, useEffect, useState } from "react";
import { Link, NavLink } from "react-router-dom";
import {
  ShoppingCart,
  PackageSearch,
  ClipboardList,
  ShieldCheck,
  LogIn,
  LogOut,
  UserRound,
  Store,
} from "lucide-react";

import { useAuth } from "../context/AuthContext";
import api from "../api/axiosClient";

export default function Navbar() {
  const { user, logout, isShopOwner } = useAuth();

  const [cartCount, setCartCount] = useState(0);

  /*
   * ==========================================
   * NAVIGATION LINK STYLE
   * ==========================================
   */
  const link = ({ isActive }) =>
    `nav-link ${isActive ? "active" : ""}`;

  /*
   * ==========================================
   * LOAD CART COUNT
   * ==========================================
   *
   * Calls:
   * GET /cart
   *
   * Then calculates:
   *
   * quantity of item 1
   * + quantity of item 2
   * + quantity of item 3
   *
   * Example:
   *
   * Product A = 2
   * Product B = 1
   *
   * Cart badge = 3
   */
  const loadCartCount = useCallback(async () => {
    /*
     * If user is not logged in,
     * cart count should be zero.
     */
    if (!user?.token) {
      setCartCount(0);
      return;
    }

    try {
      const response = await api.get("/cart");

      const items = response.data?.items || [];

      const total = items.reduce(
        (sum, item) =>
          sum + Number(item.quantity || 0),
        0
      );

      setCartCount(total);
    } catch (error) {
      console.error(
        "Unable to load cart count:",
        error
      );

      /*
       * Do not break Navbar if cart API fails.
       */
      setCartCount(0);
    }
  }, [user?.token]);

  /*
   * ==========================================
   * LOAD CART WHEN NAVBAR LOADS
   * ==========================================
   *
   * This also runs when the user logs in/out.
   */
  useEffect(() => {
    loadCartCount();
  }, [loadCartCount]);

  /*
   * ==========================================
   * LISTEN FOR CART CHANGES
   * ==========================================
   *
   * ProductDetails.jsx and Cart.jsx will
   * dispatch:
   *
   * window.dispatchEvent(
   *   new Event("cart-updated")
   * );
   *
   * Navbar receives that event and reloads
   * the cart count.
   */
  useEffect(() => {
    const handleCartUpdated = () => {
      loadCartCount();
    };

    window.addEventListener(
      "cart-updated",
      handleCartUpdated
    );

    return () => {
      window.removeEventListener(
        "cart-updated",
        handleCartUpdated
      );
    };
  }, [loadCartCount]);

  return (
    <header className="site-nav">
      <div className="nav-inner">

        {/* =====================================
            BRAND
        ====================================== */}
        <Link
          className="brand-link"
          to="/products"
        >
          <span className="brand-logo">
            <Store size={19} />
          </span>

          <span>
            <strong>TileCommerce</strong>
            <small>Tiles. Shops. Simple.</small>
          </span>
        </Link>

        {/* =====================================
            MAIN NAVIGATION
        ====================================== */}
        <nav className="main-nav">

          {/* PRODUCTS */}
          <NavLink
            className={link}
            to="/products"
          >
            <PackageSearch size={16} />
            Products
          </NavLink>

          {/* CART */}
          {user?.token && (
            <NavLink
              className={link}
              to="/cart"
            >
              <span className="navbar-cart-icon">
                <ShoppingCart size={16} />

                {/* CART BADGE */}
                {cartCount > 0 && (
                  <span className="navbar-cart-badge">
                    {cartCount > 99
                      ? "99+"
                      : cartCount}
                  </span>
                )}
              </span>

              <span>Cart</span>
            </NavLink>
          )}

          {/* ORDERS */}
          {user?.token && (
            <NavLink
              className={link}
              to="/orders"
            >
              <ClipboardList size={16} />
              Orders
            </NavLink>
          )}

          {/* ADMIN */}
          {isShopOwner && (
            <NavLink
              className={link}
              to="/admin"
            >
              <ShieldCheck size={16} />
              Admin
            </NavLink>
          )}

        </nav>

        {/* =====================================
            ACCOUNT SECTION
        ====================================== */}
        <div className="nav-account">

          {user?.token ? (
            <>
              {/* ACCOUNT */}
              <div className="account-chip">
                <span className="account-avatar">
                  <UserRound size={14} />
                </span>

                <span>
                  <small>Signed in as</small>
                  <strong>
                    {user.username}
                  </strong>
                </span>
              </div>

              {/* SIGN OUT */}
              <button
                className="nav-signout"
                onClick={logout}
              >
                <LogOut size={15} />
                Sign out
              </button>
            </>
          ) : (
            /* SIGN IN */
            <Link
              className="signin-button"
              to="/login"
            >
              <LogIn size={16} />
              Sign in
            </Link>
          )}

        </div>
      </div>
    </header>
  );
}
