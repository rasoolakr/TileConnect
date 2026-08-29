import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowRight, Minus, Plus, ShoppingBag, Trash2 } from 'lucide-react'
import api from '../api/axiosClient'

export default function Cart(){
 const [cart,setCart]=useState(null),[err,setErr]=useState('');const nav=useNavigate()
 const load=()=>api.get('/cart').then(r=>setCart(r.data)).catch(e=>setErr(e.response?.data?.message||'Unable to load cart.'))
 useEffect(load,[])
 const update=async(id,qty)=>{try{await api.put(`/cart/items/${id}`,{quantity:qty});load()}catch(e){setErr(e.response?.data?.message||'Unable to update cart.')}}
 const remove=async id=>{try{await api.delete(`/cart/items/${id}`);load()}catch(e){setErr(e.response?.data?.message||'Unable to remove item.')}}
 if(err&&!cart)return <div className="shop-page"><div className="form-alert error">{err}</div></div>
 if(!cart)return <div className="shop-page loading-text">Loading your cart…</div>
 const items=cart.items||[];const subtotal=items.reduce((s,i)=>s+Number(i.unitPrice||i.productVariant?.price||0)*i.quantity,0)
 return <div className="shop-page narrow">
   <div className="section-heading"><div><div className="eyebrow">YOUR SHOPPING BAG</div><h1>Cart</h1><p>{items.length} item{items.length===1?'':'s'} from one shop.</p></div></div>
   {err&&<div className="form-alert error">{err}</div>}
   {!items.length?<div className="empty-state"><ShoppingBag size={32}/><h3>Your cart is empty</h3><p>Add products from the catalogue to start your order.</p><Link className="primary-button" to="/products">Browse products <ArrowRight size={15}/></Link></div>:
   <div className="cart-layout"><div className="cart-items">{items.map(i=>{return <div className="cart-item" key={i.id}><div className="cart-thumb"><ShoppingBag size={20}/></div><div className="cart-info"><strong>{i.productVariant?.productName||'Tile product'}</strong><span>{i.productVariant?.size||'Standard'} · ₹ {Number(i.unitPrice||0).toLocaleString('en-IN')}</span><div className="cart-qty"><button onClick={()=>i.quantity>1&&update(i.id,i.quantity-1)}><Minus size={13}/></button><b>{i.quantity}</b><button onClick={()=>update(i.id,i.quantity+1)}><Plus size={13}/></button></div></div><strong className="line-total">₹ {(Number(i.unitPrice||0)*i.quantity).toLocaleString('en-IN')}</strong><button className="remove-button" onClick={()=>remove(i.id)}><Trash2 size={16}/></button></div>})}</div>
   <aside className="summary-card"><h3>Order summary</h3><div><span>Subtotal</span><strong>₹ {subtotal.toLocaleString('en-IN')}</strong></div><div><span>Delivery</span><span>Calculated at checkout</span></div><hr/><div className="summary-total"><span>Total</span><strong>₹ {subtotal.toLocaleString('en-IN')}</strong></div><button className="wide-primary" onClick={()=>nav('/checkout')}>Continue to checkout <ArrowRight size={16}/></button><Link to="/products" className="continue-link">Continue shopping</Link></aside></div>}
 </div>
}
