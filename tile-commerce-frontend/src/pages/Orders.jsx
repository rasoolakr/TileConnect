import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ClipboardList, FileText, ArrowRight } from 'lucide-react'
import api from '../api/axiosClient'

export default function Orders(){
 const [orders,setOrders]=useState([]),[loading,setLoading]=useState(true),[err,setErr]=useState('')
 useEffect(()=>api.get('/orders/mine').then(r=>setOrders(r.data||[])).catch(e=>setErr(e.response?.data?.message||'Unable to load orders.')).finally(()=>setLoading(false)),[])
 return <div className="shop-page narrow"><div className="section-heading"><div><div className="eyebrow">ACCOUNT</div><h1>My orders</h1><p>Track your orders and access invoices after payment verification.</p></div></div>
 {err&&<div className="form-alert error">{err}</div>}
 {loading?<div className="loading-text">Loading orders…</div>:!orders.length?<div className="empty-state"><ClipboardList size={32}/><h3>No orders yet</h3><p>Your completed purchases will appear here.</p><Link className="primary-button" to="/products">Browse products</Link></div>:
 <div className="order-list">{orders.map(o=><article className="order-card" key={o.id}><div className="order-main"><div className="order-icon"><ClipboardList size={18}/></div><div><span className="order-number">{o.orderNumber}</span><strong>₹ {Number(o.grandTotal||0).toLocaleString('en-IN')}</strong><small>{o.items?.length||0} item(s)</small></div></div><span className={`order-status ${String(o.status).toLowerCase()}`}>{String(o.status).replaceAll('_',' ')}</span><div className="order-actions"><Link to={`/orders/${o.id}/invoice`}>View invoice <ArrowRight size={14}/></Link>{o.status==='CONFIRMED'&&<a href={`${(import.meta.env.VITE_API_BASE_URL||'http://localhost:9191/api')}/orders/${o.id}/invoice/pdf`} target="_blank" rel="noreferrer"><FileText size={14}/> PDF</a>}</div></article>)}</div>}
 </div>
}
