import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Download, FileText } from 'lucide-react'
import api from '../api/axiosClient'

export default function Invoice(){
 const {id}=useParams();const [invoice,setInvoice]=useState(null),[err,setErr]=useState('')
 useEffect(()=>api.get(`/orders/${id}/invoice`).then(r=>setInvoice(r.data)).catch(e=>setErr(e.response?.data?.message||'Invoice is not available yet.')), [id])
 const pdf=`${import.meta.env.VITE_API_BASE_URL||'http://localhost:9191/api'}/orders/${id}/invoice/pdf`
 if(err)return <div className="shop-page narrow"><Link className="back-link" to="/orders"><ArrowLeft size={15}/> Back to orders</Link><div className="empty-state"><FileText size={32}/><h3>Invoice not available</h3><p>{err}</p></div></div>
 if(!invoice)return <div className="shop-page narrow loading-text">Loading invoice…</div>
 return <div className="shop-page narrow"><Link className="back-link" to="/orders"><ArrowLeft size={15}/> Back to orders</Link><div className="invoice-card"><div className="invoice-head"><div><div className="eyebrow">TILECOMMERCE INVOICE</div><h1>{invoice.invoiceNumber}</h1><p>Order {invoice.orderNumber} · {invoice.invoiceDate}</p></div><a className="secondary-button" href={pdf} target="_blank" rel="noreferrer"><Download size={16}/> Download PDF</a></div><div className="invoice-shop"><strong>{invoice.shopName||'Tile Shop'}</strong><span>{invoice.shopEmail||''}</span></div><table className="invoice-table"><thead><tr><th>Product</th><th>Size</th><th>Qty</th><th>Unit price</th><th>Total</th></tr></thead><tbody>{(invoice.items||[]).map((x,i)=><tr key={i}><td>{x.productName}</td><td>{x.variantSize}</td><td>{x.quantity}</td><td>₹ {Number(x.unitPrice).toLocaleString('en-IN')}</td><td>₹ {Number(x.total).toLocaleString('en-IN')}</td></tr>)}</tbody></table><div className="invoice-total"><span>Grand total</span><strong>₹ {Number(invoice.grandTotal).toLocaleString('en-IN')}</strong></div></div></div>
}
