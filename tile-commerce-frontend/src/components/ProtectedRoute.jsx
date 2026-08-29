import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ roles }) {
  const { user } = useAuth()
  const location = useLocation()

  if (!user?.token) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (roles?.length && !roles.includes(user.role)) return <Navigate to="/products" replace />
  return <Outlet />
}
