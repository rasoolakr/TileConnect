import { useEffect, useState } from 'react'
import {
  Boxes,
  FileSpreadsheet,
  Import,
  Package,
  RefreshCw,
  Store,
  Trash2
} from 'lucide-react'

import api from '../api/axiosClient'
import AnjaniImport from './AnjaniImport'
import CsvImport from './CsvImport'

export default function Admin() {
  const [tab, setTab] = useState('anjani')
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = () => {
    setLoading(true)
    setError('')

    api
      .get('/products/mine')
      .then((r) => {
        setProducts(r.data || [])
      })
      .catch((e) => {
        setError(
          e.response?.data?.message ||
            'Unable to load shop products.'
        )
      })
      .finally(() => {
        setLoading(false)
      })
  }

  useEffect(() => {
    load()
  }, [])

  return (
    <div className="admin-page">

      {/* PAGE HEADER */}

      <section className="admin-heading">
        <div>
          <div className="eyebrow">
            <ShieldIcon />
            SHOP OWNER ADMIN
          </div>

          <h1>Manage your catalogue</h1>

          <p>
            Import products into your shop, review what is currently
            published and keep your customer catalogue up to date.
          </p>
        </div>

        <button
          className="secondary-button"
          onClick={load}
        >
          <RefreshCw size={16} />
          Refresh products
        </button>
      </section>


      {/* SUMMARY */}

      <div className="admin-summary">

        <div>
          <span>
            <Store size={16} />
            Your shop
          </span>

          <strong>
            Shop product catalogue
          </strong>

          <small>
            Only this shop's products can be managed from this account.
          </small>
        </div>

        <div>
          <Package size={18} />

          <strong>
            {products.length}
          </strong>

          <small>
            products in catalogue
          </small>
        </div>

        <div>
          <Boxes size={18} />

          <strong>
            Owner
          </strong>

          <small>
            protected write access
          </small>
        </div>

      </div>


      {/* TABS */}

      <div className="admin-tabs">

        <button
          className={tab === 'anjani' ? 'active' : ''}
          onClick={() => setTab('anjani')}
        >
          <Import size={17} />
          Anjani Tek
        </button>

        <button
          className={tab === 'csv' ? 'active' : ''}
          onClick={() => setTab('csv')}
        >
          <FileSpreadsheet size={17} />
          CSV upload
        </button>

        <button
          className={tab === 'products' ? 'active' : ''}
          onClick={() => setTab('products')}
        >
          <Package size={17} />
          My products
        </button>

      </div>


      {/* TAB CONTENT */}

      {tab === 'anjani' && (
        <AnjaniImport />
      )}

      {tab === 'csv' && (
        <CsvImport />
      )}

      {tab === 'products' && (
        <MyProducts
          products={products}
          loading={loading}
          error={error}
          onRefresh={load}
        />
      )}

    </div>
  )
}


/* =====================================================
   SHIELD ICON
===================================================== */

function ShieldIcon() {
  return (
    <span
      style={{
        display: 'inline-flex'
      }}
    >
      <Boxes size={14} />
    </span>
  )
}


/* =====================================================
   MY PRODUCTS
===================================================== */

function MyProducts({
  products,
  loading,
  error,
  onRefresh
}) {

  const [removingId, setRemovingId] = useState(null)
  const [removeError, setRemoveError] = useState('')


  /* ===================================================
     REMOVE PRODUCT
  =================================================== */

  const removeProduct = async (product) => {

    const productName =
      product.name || 'this product'

    const confirmed = window.confirm(
      `Are you sure you want to remove "${productName}" from your catalogue?`
    )

    if (!confirmed) {
      return
    }

    setRemovingId(product.id)
    setRemoveError('')

    try {

      await api.delete(
        `/products/${product.id}`
      )

      /*
       * Refresh product list after
       * successful deletion.
       */
      if (onRefresh) {
        await onRefresh()
      }

    } catch (e) {

      setRemoveError(
        e.response?.data?.message ||
          'Unable to remove this product. Please try again.'
      )

    } finally {

      setRemovingId(null)

    }
  }


  /* ===================================================
     LOADING
  =================================================== */

  if (loading) {
    return (
      <div className="catalog-empty">

        <RefreshCw
          className="spin"
          size={25}
        />

        <h3>
          Loading your products…
        </h3>

      </div>
    )
  }


  /* ===================================================
     ERROR
  =================================================== */

  if (error) {
    return (
      <div className="form-alert error">
        {error}
      </div>
    )
  }


  /* ===================================================
     EMPTY
  =================================================== */

  if (!products.length) {
    return (
      <div className="catalog-empty">

        <Package size={29} />

        <h3>
          No shop products yet
        </h3>

        <p>
          Import from Anjani Tek or upload your CSV
          to create products.
        </p>

      </div>
    )
  }


  /* ===================================================
     PRODUCT TABLE
  =================================================== */

  return (
    <div className="admin-product-table">

      {/* REMOVE ERROR */}

      {removeError && (
        <div className="form-alert error">
          {removeError}
        </div>
      )}


      {/* TABLE HEADER */}

      <div className="table-head">

        <span>IMAGE</span>

        <span>PRODUCT NAME</span>

        <span>PRODUCT CODE</span>

        <span>DETAILS</span>

        <span>PRICE</span>

        <span>STATUS</span>

        <span>ACTION</span>

      </div>


      {/* PRODUCT LIST */}

      <div className="product-list">

        {products.map((p) => {

          /*
           * Support different image field names.
           */

          const imageUrl =
            p.imageUrl ||
            p.image ||
            p.productImage ||
            p.imageName ||
            null


          return (
            <div
              className="table-row"
              key={p.id}
            >

              {/* IMAGE */}

              <div className="product-image-cell">

                {imageUrl ? (

                  <img
                    src={imageUrl}
                    alt={p.name || 'Product image'}
                    className="product-image"
                  />

                ) : (

                  <div className="no-image">
                    No Image
                  </div>

                )}

              </div>


              {/* PRODUCT NAME */}

              <div className="product-name-cell">

                <strong
                  title={p.name || ''}
                >
                  {p.name || 'Unnamed product'}
                </strong>

              </div>


              {/* PRODUCT CODE */}

              <div className="product-code-cell">

                <span
                  title={
                    p.supplierProductCode ||
                    p.id ||
                    ''
                  }
                >
                  {p.supplierProductCode ||
                    p.id ||
                    '—'}
                </span>

              </div>


              {/* DETAILS */}

              <div className="product-details">

                {[
                  p.tileType,
                  p.collection,
                  p.size,
                  p.color,
                  p.finish
                ]
                  .filter(Boolean)
                  .join(' · ') || '—'}

              </div>


              {/* PRICE */}

              <div className="product-price">

                ₹{' '}

                {Number(
                  p.discountPrice ||
                  p.basePrice ||
                  0
                ).toLocaleString('en-IN')}

              </div>


              {/* STATUS */}

              <div className="product-status">

                <span className="status-chip">
                  {p.active
                    ? 'Active'
                    : 'Inactive'}
                </span>

              </div>


              {/* REMOVE */}

              <div className="product-action">

                <button
                  type="button"
                  className="remove-product-button"
                  onClick={() =>
                    removeProduct(p)
                  }
                  disabled={
                    removingId === p.id
                  }
                >

                  {removingId === p.id ? (

                    <>
                      <RefreshCw
                        size={14}
                        className="spin"
                      />

                      Removing...
                    </>

                  ) : (

                    <>
                      <Trash2 size={14} />

                      Remove
                    </>

                  )}

                </button>

              </div>

            </div>
          )
        })}

      </div>

    </div>
  )
}
