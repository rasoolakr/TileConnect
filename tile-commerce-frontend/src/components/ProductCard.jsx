import { Link } from 'react-router-dom'
import { ArrowUpRight, Layers3 } from 'lucide-react'

export default function ProductCard({ product }) {
  const image = product.images?.find(x=>x.primaryImage)?.imageUrl || product.images?.[0]?.imageUrl
  const price = product.discountPrice || product.basePrice
  return <article className="product-card">
    <Link to={`/products/${product.id}`} className="product-image">
      {image ? <img src={image.startsWith('http')?image:`${(import.meta.env.VITE_API_BASE_URL || 'http://localhost:9191/api').replace(/\/api$/, '')}${image}`} alt={product.name}/> : <div className="image-placeholder"><Layers3 size={28}/><span>Tile</span></div>}
      <span className="view-pill">View <ArrowUpRight size={13}/></span>
    </Link>
    <div className="product-card-body">
      <div className="product-meta">{product.brand || 'TileCommerce'} {product.finish && `· ${product.finish}`}</div>
      <h3>{product.name}</h3>
      <p>{product.shortDescription || 'Premium tile product'}</p>
      <div className="product-bottom"><div><small>Starting from</small><strong>₹ {Number(price||0).toLocaleString('en-IN')}</strong></div><span>{product.unit || 'box'}</span></div>
    </div>
  </article>
}
