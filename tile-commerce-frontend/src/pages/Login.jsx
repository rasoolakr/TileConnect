import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { LockKeyhole, Mail, ShieldCheck, Store, UserRound } from 'lucide-react'
import { useForm } from 'react-hook-form'
import api from '../api/axiosClient'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { register, handleSubmit, formState: { errors } } = useForm()
  const { login } = useAuth()
  const nav = useNavigate()
  const location = useLocation()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async data => {
    setLoading(true); setError('')
    try {
      const response = await api.post('/auth/login', data)
      login(response.data)
      const destination = response.data.role === 'SHOP_OWNER' || response.data.role === 'SUPER_ADMIN'
        ? '/admin'
        : (location.state?.from || '/products')
      nav(destination, { replace: true })
    } catch (e) {
      setError(e.response?.data?.message || 'Invalid username or password.')
    } finally { setLoading(false) }
  }

  return (
    <div className="auth-page">
      <div className="auth-visual">
        <div className="auth-brand"><span className="brand-logo large"><Store size={24}/></span>TileCommerce</div>
        <div>
          <div className="eyebrow light"><ShieldCheck size={14}/> SECURE SHOPPING</div>
          <h1>Find the right tile for every space.</h1>
          <p>Browse products from participating tile shops, manage your cart and track every order from one place.</p>
        </div>
        <div className="auth-trust"><span>✓ Verified shop catalogues</span><span>✓ Secure account access</span><span>✓ Invoice after payment verification</span></div>
      </div>

      <div className="auth-panel">
        <div className="auth-card">
          <div className="auth-icon"><LockKeyhole size={21}/></div>
          <div className="eyebrow">WELCOME BACK</div>
          <h2>Sign in to TileCommerce</h2>
          <p className="auth-subtitle">Use your customer or shop-owner account.</p>
          {error && <div className="form-alert error">{error}</div>}
          <form onSubmit={handleSubmit(submit)} className="modern-form">
            <label>Username<input autoComplete="username" placeholder="Enter username" {...register('username', { required: 'Username is required' })}/>{errors.username && <small>{errors.username.message}</small>}</label>
            <label>Password<input autoComplete="current-password" type="password" placeholder="Enter password" {...register('password', { required: 'Password is required' })}/>{errors.password && <small>{errors.password.message}</small>}</label>
            <button className="wide-primary" disabled={loading}>{loading ? 'Signing in…' : 'Sign in'}</button>
          </form>
          <div className="auth-divider"><span>New to TileCommerce?</span></div>
          <div className="auth-links">
            <Link to="/register"><UserRound size={15}/> Create customer account</Link>
            <Link to="/shop-register"><Store size={15}/> Register a shop</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
