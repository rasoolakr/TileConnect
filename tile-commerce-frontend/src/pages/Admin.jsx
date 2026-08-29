import { useEffect, useState } from 'react'
import { Boxes, FileSpreadsheet, Import, Package, RefreshCw, Store } from 'lucide-react'
import api from '../api/axiosClient'
import AnjaniImport from './AnjaniImport'
import CsvImport from './CsvImport'

export default function Admin(){
 const [tab,setTab]=useState('anjani'),[products,setProducts]=useState([]),[loading,setLoading]=useState(false),[error,setError]=useState('')
 const load=()=>{setLoading(true);api.get('/products/mine').then(r=>setProducts(r.data||[])).catch(e=>setError(e.response?.data?.message||'Unable to load shop products.')).finally(()=>setLoading(false))}
 useEffect(load,[])
 return <div className="admin-page">
  <section className="admin-heading"><div><div className="eyebrow"><ShieldIcon/> SHOP OWNER ADMIN</div><h1>Manage your catalogue</h1><p>Import products into your shop, review what is currently published and keep your customer catalogue up to date.</p></div><button className="secondary-button" onClick={load}><RefreshCw size={16}/> Refresh products</button></section>
  <div className="admin-summary"><div><span><Store size={16}/> Your shop</span><strong>Shop product catalogue</strong><small>Only this shop's products can be managed from this account.</small></div><div><Package size={18}/><strong>{products.length}</strong><small>products in catalogue</small></div><div><Boxes size={18}/><strong>Owner</strong><small>protected write access</small></div></div>
  <div className="admin-tabs"><button className={tab==='anjani'?'active':''} onClick={()=>setTab('anjani')}><Import size={17}/> Anjani Tek</button><button className={tab==='csv'?'active':''} onClick={()=>setTab('csv')}><FileSpreadsheet size={17}/> CSV upload</button><button className={tab==='products'?'active':''} onClick={()=>setTab('products')}><Package size={17}/> My products</button></div>
  {tab==='anjani'&&<AnjaniImport/>}{tab==='csv'&&<CsvImport/>}{tab==='products'&&<MyProducts products={products} loading={loading} error={error}/>}
 </div>
}
function ShieldIcon(){return <span style={{display:'inline-flex'}}><Boxes size={14}/></span>}
function MyProducts({products,loading,error}){
 if(loading)return <div className="catalog-empty"><RefreshCw className="spin" size={25}/><h3>Loading your products…</h3></div>
 if(error)return <div className="form-alert error">{error}</div>
 if(!products.length)return <div className="catalog-empty"><Package size={29}/><h3>No shop products yet</h3><p>Import from Anjani Tek or upload your CSV to create products.</p></div>
 return <div className="admin-product-table"><div className="table-head"><span>PRODUCT</span><span>DETAILS</span><span>PRICE</span><span>STATUS</span></div>{products.map(p=><div className="table-row" key={p.id}><div><strong>{p.name}</strong><small>{p.supplierProductCode||p.id}</small></div><div>{[p.tileType,p.color,p.finish].filter(Boolean).join(' · ')||'—'}</div><strong>₹ {Number(p.discountPrice||p.basePrice||0).toLocaleString('en-IN')}</strong><span className="status-chip">{p.active?'Active':'Inactive'}</span></div>)}</div>
}
