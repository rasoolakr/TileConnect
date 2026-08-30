import { useState } from 'react'
import {
  AlertCircle,
  CheckCircle2,
  CloudDownload,
  FileSpreadsheet,
  Filter,
  Import,
  RefreshCw,
  Search,
  ShieldCheck,
  X
} from 'lucide-react'
import api from '../api/axiosClient'
import { useAuth } from '../context/AuthContext'

const IMPORTER =
  import.meta.env.VITE_ANJANI_IMPORTER_URL || 'http://localhost:9196'

export default function AnjaniImport() {
  const { user } = useAuth()

  const [filters, setFilters] = useState({
    collection: '',
    maxProducts: ''
  })

  const [products, setProducts] = useState([])
  const [selected, setSelected] = useState(new Set())
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')
  const [price, setPrice] = useState({})
  const [stock, setStock] = useState({})

  const fetchProducts = async () => {
    setLoading(true)
    setMsg('')
    setError('')

    try {
      const r = await fetch(`${IMPORTER}/api/anjani/fetch`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(filters)
      })

      if (!r.ok) {
        throw Error(await r.text())
      }

      const data = await r.json()

      setProducts(Array.isArray(data) ? data : [])
      setSelected(new Set())

      setMsg(
        `${data.length} products fetched. Review them before importing.`
      )
    } catch (e) {
      setError(
        e.message ||
          'Unable to fetch Anjani Tek products. Check the importer service URL.'
      )
    } finally {
      setLoading(false)
    }
  }

  const toggleAll = () => {
    const keys = products.map((p) => p.importKey)

    setSelected(
      selected.size === keys.length
        ? new Set()
        : new Set(keys)
    )
  }

  const toggle = (key) => {
    setSelected((current) => {
      const next = new Set(current)

      if (next.has(key)) {
        next.delete(key)
      } else {
        next.add(key)
      }

      return next
    })
  }

  const push = async () => {
    if (!user?.shopId) {
      setError('Your shop owner account does not have a shop ID.')
      return
    }

    const chosen = products
      .filter((p) => selected.has(p.importKey))
      .map((p) => ({
        ...p,
        basePrice: Number(price[p.importKey] || 1),
        discountPrice: null,
        taxPercentage: 0,
        minimumOrderQuantity: 1,
        unit: 'box',
        stockQuantity: Number(stock[p.importKey] || 0)
      }))

    if (!chosen.length) {
      setError('Select at least one product.')
      return
    }

    setLoading(true)
    setError('')

    try {
      const r = await api.post('/products/import/anjani', {
        shopId: user.shopId,
        products: chosen
      })

      setMsg(
        `Import complete — created ${r.data.created}, updated ${r.data.updated}.`
      )

      setSelected(new Set())
    } catch (e) {
      setError(
        e.response?.data?.message ||
          'Import failed. Check the product data and backend logs.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-import">

      {/* HEADER */}
      <div className="import-intro">
        <div>
          <div className="eyebrow">
            <ShieldCheck size={14} />
            SHOP OWNER CATALOGUE
          </div>

          <h2>Import from Anjani Tek</h2>

          <p>
            Fetch products from Anjani Tek, review the product information,
            set your shop price and opening stock, then import the products
            you select.
          </p>
        </div>

        <div className="protected-badge">
          <CheckCircle2 size={14} />
          Owner protected
        </div>
      </div>

      {/* FILTERS */}
      <div className="import-filter-card">
        <div className="filter-grid">

          {/* COLLECTION */}
          <label>
            Collection

            <input
              value={filters.collection}
              onChange={(e) =>
                setFilters({
                  ...filters,
                  collection: e.target.value
                })
              }
              placeholder="e.g. Cool Roof"
            />
          </label>

          {/* MAX PRODUCTS */}
          <label>
            Max products

            <input
              type="number"
              min="1"
              max="200"
              value={filters.maxProducts}
              onChange={(e) =>
                setFilters({
                  ...filters,
                  maxProducts: Number(e.target.value)
                })
              }
            />
          </label>

          {/* ACTION BUTTONS */}
          <div className="filter-button-row">

            <button
              className="primary-button"
              onClick={fetchProducts}
              disabled={loading}
            >
              {loading ? (
                <RefreshCw className="spin" size={16} />
              ) : (
                <Search size={16} />
              )}

              {loading ? 'Fetching…' : 'Fetch products'}
            </button>

            <button
              className="secondary-button"
              onClick={() =>
                setFilters({
                  collection: '',
                  maxProducts: 30
                })
              }
              disabled={loading}
            >
              <Filter size={15} />
              Clear
            </button>

          </div>
        </div>
      </div>

      {/* SUCCESS MESSAGE */}
      {msg && (
        <div className="form-alert success">
          <CheckCircle2 size={16} />
          {msg}
        </div>
      )}

      {/* ERROR MESSAGE */}
      {error && (
        <div className="form-alert error">
          <AlertCircle size={16} />
          <span>{error}</span>

          <button onClick={() => setError('')}>
            <X size={14} />
          </button>
        </div>
      )}

      {/* TOOLBAR */}
      <div className="import-toolbar">

        <div className="import-count">
          <strong>{products.length}</strong>
          <span>products fetched</span>

          <span className="separator">·</span>

          <strong>{selected.size}</strong>
          <span>selected</span>
        </div>

        <div className="import-toolbar-actions">

          <button
            className="secondary-button small"
            onClick={toggleAll}
            disabled={!products.length}
          >
            {selected.size === products.length && products.length
              ? 'Clear selection'
              : 'Select all'}
          </button>

          <button
            className="primary-button small"
            onClick={push}
            disabled={!selected.size || loading}
          >
            <Import size={15} />
            Import selected
          </button>

        </div>
      </div>

      {/* EMPTY STATE */}
      {!products.length ? (
        <div className="catalog-empty">

          <CloudDownload size={29} />

          <h3>Nothing fetched yet</h3>

          <p>
            Enter a collection such as <strong>Cool Roof</strong> and
            click <strong>Fetch products</strong> to load the products.
          </p>

        </div>
      ) : (

        /* PRODUCT GRID */
        <div className="import-product-grid">

          {products.map((p) => (

            <article
              className={`import-product ${
                selected.has(p.importKey) ? 'selected' : ''
              }`}
              key={p.importKey}
            >

              {/* IMAGE */}
              <div className="import-image">

                {p.imageUrl ? (
                  <img
                    src={p.imageUrl}
                    alt={p.name || 'Tile product'}
                  />
                ) : (
                  <FileSpreadsheet size={25} />
                )}

              </div>

              {/* PRODUCT INFORMATION */}
              <div className="import-product-body">

                {/* PRODUCT NAME */}
                <h3>
                  {p.name || 'Unnamed product'}
                </h3>

                {/* PRODUCT DETAILS */}
                <p className="product-details">
                  {[
                    p.collection,
                    p.size,
                    p.finish,
                    p.color
                  ]
                    .filter(Boolean)
                    .join(' · ') || 'No additional details'}
                </p>

                {/* IMPORT CHECKBOX */}
                <label className="select-line">

                  <input
                    type="checkbox"
                    checked={selected.has(p.importKey)}
                    onChange={() => toggle(p.importKey)}
                  />

                  <span>Import this product</span>

                </label>

                {/* PRICE AND STOCK */}
                {selected.has(p.importKey) && (

                  <div className="import-values">

                    <label>
                      Your selling price

                      <input
                        type="number"
                        min="0.01"
                        value={price[p.importKey] || ''}
                        onChange={(e) =>
                          setPrice({
                            ...price,
                            [p.importKey]: e.target.value
                          })
                        }
                      />
                    </label>

                    <label>
                      Opening stock

                      <input
                        type="number"
                        min="0"
                        value={stock[p.importKey] || ''}
                        onChange={(e) =>
                          setStock({
                            ...stock,
                            [p.importKey]: e.target.value
                          })
                        }
                      />
                    </label>

                  </div>

                )}

              </div>

            </article>

          ))}

        </div>
      )}

    </div>
  )
}
