import { useRef, useState } from 'react'
import { CheckCircle2, CloudUpload, FileSpreadsheet, ShieldCheck, Upload } from 'lucide-react'
import api from '../api/axiosClient'

export default function CsvImport(){
 const ref=useRef();const [file,setFile]=useState(null),[drag,setDrag]=useState(false),[busy,setBusy]=useState(false),[msg,setMsg]=useState(''),[error,setError]=useState('')
 const choose=f=>{if(!f)return;if(!f.name.toLowerCase().endsWith('.csv')){setError('Please choose a .csv file.');return}setFile(f);setMsg('');setError('')}
 const upload=async()=>{if(!file)return;setBusy(true);setError('');setMsg('');try{const fd=new FormData();fd.append('file',file);const r=await api.post('/products/import/csv',fd,{headers:{'Content-Type':'multipart/form-data'}});setMsg(`CSV import complete — created ${r.data.created}, updated ${r.data.updated}.`);setFile(null);if(ref.current)ref.current.value=''}catch(e){setError(e.response?.data?.message||'CSV import failed.')}finally{setBusy(false)}}
 return <div className="csv-admin">
   <div className="import-intro"><div><div className="eyebrow"><ShieldCheck size={14}/> SHOP OWNER CATALOGUE</div><h2>Import products from CSV</h2><p>Use CSV when your shop maintains product data outside TileCommerce. The backend validates each row before writing to your shop.</p></div><div className="protected-badge"><CheckCircle2 size={14}/> Owner protected</div></div>
   {msg&&<div className="form-alert success"><CheckCircle2 size={16}/>{msg}</div>}{error&&<div className="form-alert error">{error}</div>}
   <div className={`csv-drop ${drag?'drag':''}`} onDragOver={e=>{e.preventDefault();setDrag(true)}} onDragLeave={()=>setDrag(false)} onDrop={e=>{e.preventDefault();setDrag(false);choose(e.dataTransfer.files?.[0])}}>
     <div className="csv-drop-icon"><CloudUpload size={29}/></div><h3>{file?file.name:'Drop your CSV file here'}</h3><p>{file?'Ready to validate and import.':'or choose a file from your computer'}</p>
     <button className="primary-button large" onClick={()=>ref.current?.click()}><Upload size={17}/> Choose CSV</button><input ref={ref} hidden type="file" accept=".csv,text/csv" onChange={e=>choose(e.target.files?.[0])}/><small>CSV only · UTF-8 recommended · max 10 MB</small>
   </div>
   <div className="csv-actions"><button className="secondary-button" onClick={async()=>{try{const r=await api.get('/products/import/template.csv',{responseType:'blob'});const url=URL.createObjectURL(r.data);const a=document.createElement('a');a.href=url;a.download='tilecommerce-product-template.csv';a.click();URL.revokeObjectURL(url)}catch(e){setError(e.response?.data?.message||'Unable to download template.')}}}><FileSpreadsheet size={16}/> Download template</button><button className="primary-button" disabled={!file||busy} onClick={upload}>{busy?'Validating & importing…':<>Validate & import <Upload size={16}/></>}</button></div>
   <div className="csv-help"><h3>Supported columns</h3><div className="column-grid">{['productId','name','brand','material','collection','size','finish','color','description','basePrice','discountPrice','taxPercentage','minimumOrderQuantity','unit','stockQuantity','imageUrl','supplierName','supplierProductCode','detailUrl'].map(x=><span key={x}>{x}</span>)}</div><div className="csv-flow"><span>1. Upload</span><span>2. Validate</span><span>3. Create/update product</span><span>4. Create variant</span><span>5. Save image</span></div></div>
 </div>
}
