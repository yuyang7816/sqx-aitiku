import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuthCache, getApiBaseUrl } from '@/utils/tool'

const service = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 12000,
})

let redirectingToLogin = false

function isBusinessSuccess(data) {
  return data?.code === 0 || data?.code === 200 || data?.status === 0 || data?.status === 200
}

function handleUnauthorized(message) {
  const currentPath = window.location.pathname + window.location.search
  const onLoginPage = currentPath.startsWith('/login')
  clearAuthCache()
  if (onLoginPage || redirectingToLogin) {
    return
  }
  redirectingToLogin = true
  ElMessage.error(message || '登录已失效，请重新登录')
  const redirect = currentPath || '/dashboard'
  window.location.replace(`/login?redirect=${encodeURIComponent(redirect)}`)
  window.setTimeout(() => {
    redirectingToLogin = false
  }, 300)
}

service.interceptors.request.use((config) => {
  const auth = JSON.parse(window.sessionStorage.getItem('admin-auth') || '{}')
  const hasValidSession = auth.token && Number(auth.expiresAt) > Date.now()
  if (hasValidSession) {
    config.headers.Authorization = `Bearer ${auth.token}`
    config.headers.token = auth.token
  }
  const isFormData = typeof FormData !== 'undefined' && config.data instanceof FormData
  if (isFormData) {
    delete config.headers['Content-Type']
  } else if (!config.headers['Content-Type']) {
    config.headers['Content-Type'] = 'application/json'
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const { data } = response
    if (typeof data?.code === 'number' || typeof data?.status === 'number') {
      if (isBusinessSuccess(data)) {
        return data
      }
      if (data.code === 401 || data.status === 401) {
        handleUnauthorized(data.msg || data.message || 'invalid token')
        return Promise.reject(data)
      }
      if (!response.config?.silentError) {
        ElMessage.error(data.msg || data.message || '业务请求失败')
      }
      return Promise.reject(data)
    }
    return data
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      handleUnauthorized(error.response?.data?.msg || error.response?.data?.message || 'invalid token')
      return Promise.reject(error)
    }
    if (!error.config?.silentError) {
      ElMessage.error(error.response?.data?.msg || error.response?.data?.message || error.message || '网络请求失败')
    }
    return Promise.reject(error)
  },
)

export function get(url, params, config = {}) {
  return service.get(url, { params, ...config })
}

export function post(url, data, config = {}) {
  return service.post(url, data, config)
}

export function resolveBusinessResponse(result) {
  if (!isBusinessSuccess(result)) {
    return Promise.reject(result)
  }
  return Promise.resolve(result.data || result)
}

export default service
