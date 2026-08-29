import axios from 'axios'

const STORAGE_KEY = 'tilecommerce.auth.v2'
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
})

api.interceptors.request.use(config => {
  let u = null
  try { u = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null') } catch {}
  if (u?.token) config.headers.Authorization = `Bearer ${u.token}`
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem(STORAGE_KEY)
      if (window.location.pathname !== '/login') window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
export default api
