import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowLeft, MapPin, Store, UserPlus } from 'lucide-react'
import { useForm } from 'react-hook-form'
import api from '../api/axiosClient'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const { register, handleSubmit, formState: { errors } } = useForm({ defaultValues: { country: 'India' } })
  const { login } = useAuth(); const nav = useNavigate(); const [error, setError] = useState(''); const [loading, setLoading] = useState(false)

  const submit = async d => {
    setLoading(true); setError('')
    try {
      const payload = { username:d.username,email:d.email,password:d.password,address:{addressLine1:d.addressLine1,addressLine2:d.addressLine2,city:d.city,state:d.state,postalCode:d.postalCode,country:d.country,phoneNumber:d.phoneNumber} }
      const r = await api.post('/auth/register', payload); login(r.data); nav('/products', { replace:true })
    } catch(e) { setError(e.response?.data?.message || 'Registration failed.') } finally { setLoading(false) }
  }

  return <div className="form-page">
    <div className="form-shell">
      <Link className="back-link" to="/login"><ArrowLeft size={15}/> Back to sign in</Link>
      <div className="form-heading"><div className="auth-icon"><UserPlus size={21}/></div><div className="eyebrow">CUSTOMER ACCOUNT</div><h1>Create your account</h1><p>Register once to shop, checkout and track your tile orders.</p></div>
      {error && <div className="form-alert error">{error}</div>}
      <form onSubmit={handleSubmit(submit)} className="modern-form two-col">
        <label>Username<input placeholder="Choose a username" {...register('username',{required:'Required'})}/>{errors.username&&<small>{errors.username.message}</small>}</label>
        <label>Email<input type="email" placeholder="you@example.com" {...register('email',{required:'Required'})}/>{errors.email&&<small>{errors.email.message}</small>}</label>
        <label>Password<input type="password" placeholder="Minimum 8 characters" {...register('password',{required:'Required',minLength:{value:8,message:'Minimum 8 characters'}})}/>{errors.password&&<small>{errors.password.message}</small>}</label>
        <label>Phone number<input placeholder="10-digit mobile number" {...register('phoneNumber',{required:'Required'})}/>{errors.phoneNumber&&<small>{errors.phoneNumber.message}</small>}</label>
        <div className="form-section-title"><MapPin size={16}/> Delivery address</div>
        <label className="span-2">Address line 1<input {...register('addressLine1',{required:'Required'})}/></label>
        <label className="span-2">Address line 2<input {...register('addressLine2')}/></label>
        <label>City<input {...register('city',{required:'Required'})}/></label><label>State<input {...register('state',{required:'Required'})}/></label>
        <label>Postal code<input {...register('postalCode',{required:'Required'})}/></label><label>Country<input {...register('country',{required:'Required'})}/></label>
        <button className="wide-primary span-2" disabled={loading}>{loading?'Creating account…':'Create customer account'}</button>
      </form>
      <p className="center-note">Are you a shop owner? <Link to="/shop-register">Register your shop <Store size={13}/></Link></p>
    </div>
  </div>
}
