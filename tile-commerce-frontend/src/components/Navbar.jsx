import { Link, NavLink } from 'react-router-dom'
import { ShoppingCart, PackageSearch, ClipboardList, ShieldCheck, LogIn, LogOut, UserRound, Store } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout, isShopOwner } = useAuth()
  const link = ({ isActive }) => `nav-link ${isActive ? 'active' : ''}`

  return (
    <header className="site-nav">
      <div className="nav-inner">
        <Link className="brand-link" to="/products">
          <span className="brand-logo"><Store size={19}/></span>
          <span><strong>TileCommerce</strong><small>Tiles. Shops. Simple.</small></span>
        </Link>

        <nav className="main-nav">
          <NavLink className={link} to="/products"><PackageSearch size={16}/>Products</NavLink>
          {user?.token && <NavLink className={link} to="/cart"><ShoppingCart size={16}/>Cart</NavLink>}
          {user?.token && <NavLink className={link} to="/orders"><ClipboardList size={16}/>Orders</NavLink>}
          {isShopOwner && <NavLink className={link} to="/admin"><ShieldCheck size={16}/>Admin</NavLink>}
        </nav>

        <div className="nav-account">
          {user?.token ? (
            <>
              <div className="account-chip">
                <span className="account-avatar"><UserRound size={14}/></span>
                <span><small>Signed in as</small><strong>{user.username}</strong></span>
              </div>
              <button className="nav-signout" onClick={logout}><LogOut size={15}/>Sign out</button>
            </>
          ) : (
            <Link className="signin-button" to="/login"><LogIn size={16}/>Sign in</Link>
          )}
        </div>
      </div>
    </header>
  )
}
