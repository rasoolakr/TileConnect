import { Link } from 'react-router-dom'
import { ArrowUpRight, Layers3 } from 'lucide-react'

export default function ProductCard({ product }) {

  // Get primary image first, otherwise use first image
  const image =
    product.images?.find((x) => x.primaryImage)?.imageUrl ||
    product.images?.[0]?.imageUrl

  const price =
    product.discountPrice || product.basePrice

  // Convert relative image URL to absolute URL
  const imageUrl = image
    ? image.startsWith('http')
      ? image
      : `${(
          import.meta.env.VITE_API_BASE_URL ||
          'http://localhost:9191/api'
        ).replace(/\/api$/, '')}${image}`
    : null

  return (
    <article className="product-card">

      {/* =========================
          PRODUCT IMAGE
      ========================== */}

      <Link
        to={`/products/${product.id}`}
        className="product-image"
      >

        {imageUrl ? (
          <img
            src={imageUrl}
            alt={product.name || 'Tile product'}
          />
        ) : (
          <div className="image-placeholder">
            <Layers3 size={28} />
            <span>Tile</span>
          </div>
        )}

        <span className="view-pill">
          View <ArrowUpRight size={13} />
        </span>

      </Link>


      {/* =========================
          PRODUCT INFORMATION
      ========================== */}

      <div className="product-card-body">

        <div className="product-meta">
          {product.brand || 'TileCommerce'}

          {product.finish && (
            <> · {product.finish}</>
          )}
        </div>

        <h3>
          {product.name || 'Unnamed product'}
        </h3>

        <p>
          {product.shortDescription ||
            'Premium tile product'}
        </p>


        {/* =========================
            PRICE
        ========================== */}

        <div className="product-bottom">

          <div>
            <small>
              Starting from
            </small>

            <strong>
              ₹{' '}
              {Number(price || 0).toLocaleString(
                'en-IN'
              )}
            </strong>
          </div>

          <span>
            {product.unit || 'box'}
          </span>

        </div>

      </div>

    </article>
  )
}
