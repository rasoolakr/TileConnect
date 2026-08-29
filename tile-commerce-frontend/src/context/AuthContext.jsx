import { createContext, useContext, useMemo, useState } from 'react'

const AuthContext = createContext(null)
const STORAGE_KEY = 'tilecommerce.auth.v2'

function readUser() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null') } catch { return null }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readUser)

  const login = (data) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    setUser(data)
  }

  const logout = () => {
    localStorage.removeItem(STORAGE_KEY)
    setUser(null)
  }

  const value = useMemo(() => ({
    user,
    isAuthenticated: Boolean(user?.token),
    isShopOwner: ['SHOP_OWNER', 'SUPER_ADMIN'].includes(user?.role),
    login,
    logout
  }), [user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
