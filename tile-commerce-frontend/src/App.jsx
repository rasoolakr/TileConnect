import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import Products from './pages/Products'
import ProductDetails from './pages/ProductDetails'
import Cart from './pages/Cart'
import Checkout from './pages/Checkout'
import Orders from './pages/Orders'
import Invoice from './pages/Invoice'
import Admin from './pages/Admin'
import Login from './pages/Login'
import Register from './pages/Register'
import ShopRegister from './pages/ShopRegister'

function Landing() {
  return <Navigate to="/products" replace />
}

export default function App() {
  return <>
    <Navbar />
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/products" element={<Products />} />
      <Route path="/products/:id" element={<ProductDetails />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/shop-register" element={<ShopRegister />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/cart" element={<Cart />} />
        <Route path="/checkout" element={<Checkout />} />
        <Route path="/orders" element={<Orders />} />
        <Route path="/orders/:id/invoice" element={<Invoice />} />
      </Route>

      <Route element={<ProtectedRoute roles={['SHOP_OWNER', 'SUPER_ADMIN']} />}>
        <Route path="/admin" element={<Admin />} />
      </Route>

      <Route path="*" element={<Navigate to="/products" replace />} />
    </Routes>
  </>
}
