import { useState } from 'react'
import { AlertCircle, CheckCircle2, CloudDownload, FileSpreadsheet, Filter, Import, RefreshCw, Search, ShieldCheck, Upload, X } from 'lucide-react'
import api from '../api/axiosClient'
import { useAuth } from '../context/AuthContext'

const IMPORTER = import.meta.env.VITE_ANJANI_IMPORTER_URL || 'http://localhost:9292'

export default function AnjaniImport() {
 const { user } = useAuth()
 const [filters,setFilters]=useState({collection:'',size:'',finish:'',color:'',maxProducts:30})
 const [products,setProducts]=useState([]),[selected,setSelected]=useState(new Set()),[loading,setLoading]=useState(false),[msg,setMsg]=useState(''),[error,setError]=useState('')
 const [price,setPrice]=useState({}),[stock,setStock]=useState({})
 const fetchProducts=async()=>{setLoading(true);setMsg('');setError('');try{const r=await fetch(`${IMPORTER}/api/anjani/fetch`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(filters)});if(!r.ok)throw Error(await r.text());const data=await r.json();setProducts(Array.isArray(data)?data:[]);setSelected(new Set());setMsg(`${data.length} products fetched. Review them before importing.`)}catch(e){setError(e.message||'Unable to fetch Anjani Tek catalogue. Check the importer service URL.')}finally{setLoading(false)}}
 const toggleAll=()=>{const keys=products.map(p=>p.importKey);setSelected(selected.size===keys.length?new Set():new Set(keys))}
 const toggle=(key)=>setSelected(s=>{const n=new Set(s);n.has(key)?n.delete(key):n.add(key);return n})
 const push=async()=>{if(!user?.shopId){setError('Your shop owner account does not have a shop ID.');return}const chosen=products.filter(p=>selected.has(p.importKey)).map(p=>({...p,basePrice:Number(price[p.importKey]||1),discountPrice:null,taxPercentage:0,minimumOrderQuantity:1,unit:'box',stockQuantity:Number(stock[p.importKey]||0)}));if(!chosen.length){setError('Select at least one product.');return}setLoading(true);setError('');try{const r=await api.post('/products/import/anjani',{shopId:user.shopId,products:chosen});setMsg(`Import complete — created ${r.data.created}, updated ${r.data.updated}.`);setSelected(new Set())}catch(e){setError(e.response?.data?.message||'Import failed. Check the product data and backend logs.')}finally{setLoading(false)}}
 return <div className="admin-import">
   <div className="import-intro"><div><div className="eyebrow"><ShieldCheck size={14}/> SHOP OWNER CATALOGUE</div><h2>Import from Anjani Tek</h2><p>Fetch the supplier catalogue, review the data, set your shop price and opening stock, then import only the products you select.</p></div><div className="protected-badge"><CheckCircle2 size={14}/> Owner protected</div></div>
   <div className="import-filter-card"><div className="filter-grid">
    {['collection','size','finish','color'].map(k=><label key={k}>{k==='size'?'Tile size':k[0].toUpperCase()+k.slice(1)}<input value={filters[k]} onChange={e=>setFilters({...filters,[k]:e.target.value})} placeholder={k==='collection'?'e.g. GVT Collections':k==='size'?'e.g. 600x600mm':k==='finish'?'e.g. Glossy':'e.g. BLACK'}/></label>)}
    <label>Max products<input type="number" min="1" max="200" value={filters.maxProducts} onChange={e=>setFilters({...filters,maxProducts:Number(e.target.value)})}/></label>
    <div className="filter-button-row"><button className="primary-button" onClick={fetchProducts} disabled={loading}>{loading?<RefreshCw className="spin" size={16}/>:<Search size={16}/>} {loading?'Fetching…':'Fetch catalogue'}</button><button className="secondary-button" onClick={()=>setFilters({collection:'',size:'',finish:'',color:'',maxProducts:30})}><Filter size={15}/> Clear</button></div>
   </div></div>
   {msg&&<div className="form-alert success"><CheckCircle2 size={16}/>{msg}</div>}{error&&<div className="form-alert error"><AlertCircle size={16}/>{error}<button onClick={()=>setError('')}><X size={14}/></button></div>}
   <div className="import-toolbar"><div><strong>{products.length}</strong> fetched · <strong>{selected.size}</strong> selected</div><div><button className="secondary-button small" onClick={toggleAll}>{selected.size===products.length&&products.length?'Clear selection':'Select all'}</button><button className="primary-button small" onClick={push} disabled={!selected.size||loading}><Import size={15}/> Import selected</button></div></div>
   {!products.length?<div className="catalog-empty"><CloudDownload size={29}/><h3>Nothing fetched yet</h3><p>Use the filters above and fetch the Anjani Tek public catalogue. No products are preloaded into this screen.</p></div>:
   <div className="import-product-grid">{products.map(p=><article className={`import-product ${selected.has(p.importKey)?'selected':''}`} key={p.importKey}>
     <div className="import-image">{p.imageUrl?<img src={p.imageUrl} alt={p.name}/>:<FileSpreadsheet size={25}/>}</div>
     <div className="import-product-body"><div className="supplier-code">{p.supplierProductCode||p.importKey}</div><h3>{p.name}</h3><p>{[p.collection,p.size,p.finish,p.color].filter(Boolean).join(' · ')}</p>
     <label className="select-line"><input type="checkbox" checked={selected.has(p.importKey)} onChange={()=>toggle(p.importKey)}/> Import this product</label>
     {selected.has(p.importKey)&&<div className="import-values"><label>Your selling price<input type="number" min="0.01" value={price[p.importKey]||''} onChange={e=>setPrice({...price,[p.importKey]:e.target.value})}/></label><label>Opening stock<input type="number" min="0" value={stock[p.importKey]||''} onChange={e=>setStock({...stock,[p.importKey]:e.target.value})}/></label></div>}</div>
   </article>)}</div>}
 </div>
}
