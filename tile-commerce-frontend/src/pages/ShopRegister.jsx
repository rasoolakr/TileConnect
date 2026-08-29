import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import api from '../api/axiosClient'
import { useAuth } from '../context/AuthContext'
import { useState } from 'react'

export default function ShopRegister() {
  const { register, handleSubmit } = useForm({ defaultValues: { country: 'India' } })
  const { login } = useAuth()
  const nav = useNavigate()
  const [error, setError] = useState('')

  const submit = async d => {
    try {
      const payload = {
        username: d.username,
        email: d.email,
        password: d.password,
        shopName: d.shopName,
        shopDescription: d.shopDescription,
        shopPhoneNumber: d.shopPhoneNumber,
        shopEmail: d.shopEmail,
        logoUrl: d.logoUrl,
        upiId: d.upiId,
        paymentPhoneNumber: d.paymentPhoneNumber,
        bankAccountNumber: d.bankAccountNumber,
        bankIfsc: d.bankIfsc,
        qrCodeUrl: d.qrCodeUrl,
        address: {
          addressLine1: d.addressLine1,
          addressLine2: d.addressLine2,
          city: d.city,
          state: d.state,
          postalCode: d.postalCode,
          country: d.country,
          phoneNumber: d.addressPhoneNumber || d.shopPhoneNumber
        }
      }
      const r = await api.post('/auth/shop-register', payload)
      login(r.data)
      nav('/admin')
    } catch (e) {
      setError(e.response?.data?.message || 'Shop registration failed')
    }
  }

  return <div className="container py-5">
    <div className="col-md-8 mx-auto">
      <h2>Shop Owner Registration</h2>
      <p className="text-muted">This creates the shop owner user, shop, and default address together.</p>
      {error && <div className="alert alert-danger">{error}</div>}
      <form onSubmit={handleSubmit(submit)}>
        <h5>Owner Account</h5>
        <input className="form-control mb-3" placeholder="Username" {...register('username', { required: true })}/>
        <input className="form-control mb-3" type="email" placeholder="Owner Email" {...register('email', { required: true })}/>
        <input className="form-control mb-3" type="password" placeholder="Password" {...register('password', { required: true, minLength: 8 })}/>

        <h5>Shop Details</h5>
        <input className="form-control mb-3" placeholder="Shop Name" {...register('shopName', { required: true })}/>
        <textarea className="form-control mb-3" placeholder="Shop Description" {...register('shopDescription')}/>
        <input className="form-control mb-3" placeholder="Shop Phone Number" {...register('shopPhoneNumber', { required: true })}/>
        <input className="form-control mb-3" type="email" placeholder="Shop Email" {...register('shopEmail')}/>
        <input className="form-control mb-3" placeholder="Logo URL" {...register('logoUrl')}/>

        <h5>Offline Payment Details</h5>
        <input className="form-control mb-3" placeholder="UPI ID" {...register('upiId')}/>
        <input className="form-control mb-3" placeholder="Payment Phone Number" {...register('paymentPhoneNumber')}/>
        <input className="form-control mb-3" placeholder="Bank Account Number" {...register('bankAccountNumber')}/>
        <input className="form-control mb-3" placeholder="Bank IFSC" {...register('bankIfsc')}/>
        <input className="form-control mb-3" placeholder="QR Code URL" {...register('qrCodeUrl')}/>

        <h5>Shop Address</h5>
        <input className="form-control mb-3" placeholder="Address Line 1" {...register('addressLine1', { required: true })}/>
        <input className="form-control mb-3" placeholder="Address Line 2" {...register('addressLine2')}/>
        <input className="form-control mb-3" placeholder="Address Phone Number" {...register('addressPhoneNumber')}/>
        <div className="row">
          <div className="col-md-6"><input className="form-control mb-3" placeholder="City" {...register('city', { required: true })}/></div>
          <div className="col-md-6"><input className="form-control mb-3" placeholder="State" {...register('state', { required: true })}/></div>
          <div className="col-md-6"><input className="form-control mb-3" placeholder="Postal Code" {...register('postalCode', { required: true })}/></div>
          <div className="col-md-6"><input className="form-control mb-3" placeholder="Country" {...register('country', { required: true })}/></div>
        </div>
        <button className="btn btn-primary w-100">Register Shop</button>
      </form>
    </div>
  </div>
}
