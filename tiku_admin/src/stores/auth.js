import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getNavApi, loginApi, logoutApi } from '@/api/modules/auth'
import { DEFAULT_ADMIN_PERMISSIONS } from '@/constants/admin-nav'
import { resolveAdminMenuTree } from '@/constants/admin-menu'
import {
  clearPermissions,
  collectMenuPermissions,
  isAuth,
  setPermissions,
} from '@/utils/tool'

const STORAGE_KEY = 'admin-auth'

function getStoredAuth() {
  if (typeof window === 'undefined') {
    return null
  }
  const raw = window.sessionStorage.getItem(STORAGE_KEY)
  return raw ? JSON.parse(raw) : null
}

export const useAuthStore = defineStore('auth', () => {
  const stored = getStoredAuth()
  const token = ref(stored?.token || '')
  const refreshToken = ref(stored?.refreshToken || '')
  const expiresAt = ref(stored?.expiresAt || 0)
  const profile = ref(stored?.profile || { name: '', roleCode: '', roleName: '', email: '' })
  const permissions = ref(stored?.permissions || [])
  const menuList = ref(stored?.menuList || [])
  const navLoaded = ref(Boolean((stored?.permissions || []).length || (stored?.menuList || []).length))
  const isLoggedIn = computed(() => Boolean(token.value) && expiresAt.value > Date.now())

  function persist() {
    if (token.value) {
      window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify({
        token: token.value,
        refreshToken: refreshToken.value,
        expiresAt: expiresAt.value,
        profile: profile.value,
        permissions: permissions.value,
        menuList: menuList.value,
      }))
    } else {
      window.sessionStorage.removeItem(STORAGE_KEY)
    }
  }

  function applyStoredAuth(cached) {
    token.value = cached.token
    refreshToken.value = cached.refreshToken
    expiresAt.value = cached.expiresAt
    profile.value = cached.profile
    permissions.value = cached.permissions || []
    menuList.value = cached.menuList || []
    navLoaded.value = Boolean(permissions.value.length || menuList.value.length)
    setPermissions(permissions.value)
  }

  function clearSession() {
    token.value = ''
    refreshToken.value = ''
    expiresAt.value = 0
    profile.value = { name: '', roleCode: '', roleName: '', email: '' }
    permissions.value = []
    menuList.value = []
    navLoaded.value = false
    clearPermissions()
    persist()
  }

  function extractNavPayload(navData) {
    return navData?.data || navData || {}
  }

  function normalizePermissions(raw) {
    if (!raw) return []
    if (Array.isArray(raw)) return raw
    if (raw instanceof Set) return [...raw]
    if (typeof raw === 'object') {
      return Object.values(raw).filter((item) => typeof item === 'string' && item)
    }
    return []
  }

  function mergeUniquePermissions(base = [], extra = []) {
    return [...new Set([...base, ...extra].filter(Boolean))]
  }

  function flattenMenuEntries(items = []) {
    return items.flatMap((item) => {
      const current = item.path && !String(item.path).startsWith('/404')
        ? [{ path: item.path, perms: item.perms || '' }]
        : []
      return current.concat(flattenMenuEntries(item.children || []))
    })
  }

  function applyNavPayload(payload = {}) {
    let nextPermissions = normalizePermissions(payload.permissions)
    const menuSource = payload.menuList || []
    const menuPerms = collectMenuPermissions(menuSource)

    // 始终使用前端 canonical 菜单结构，避免依赖数据库 sys_menu 配置
    const nextMenus = resolveAdminMenuTree()

    nextPermissions = mergeUniquePermissions(
      nextPermissions,
      menuPerms,
      collectMenuPermissions(nextMenus),
      DEFAULT_ADMIN_PERMISSIONS,
    )

    if (!nextPermissions.length) {
      nextPermissions = [...DEFAULT_ADMIN_PERMISSIONS]
    }

    permissions.value = nextPermissions
    menuList.value = nextMenus
    navLoaded.value = true
    setPermissions(nextPermissions)
    persist()
    return {
      permissions: nextPermissions,
      menuList: nextMenus,
    }
  }

  async function fetchNavData() {
    try {
      const navData = await getNavApi()
      return applyNavPayload(extractNavPayload(navData))
    } catch (error) {
      return applyNavPayload({})
    }
  }

  async function restore() {
    const cached = getStoredAuth()
    if (!cached || !cached.token || cached.expiresAt <= Date.now()) {
      clearSession()
      return false
    }
    applyStoredAuth(cached)
    try {
      await fetchNavData()
    } catch (error) {
      if (!menuList.value.length) {
        clearSession()
        throw error
      }
    }
    return true
  }

  async function login(form) {
    const data = await loginApi(form)
    token.value = data.token
    refreshToken.value = data.refreshToken || ''
    expiresAt.value = data.expiresAt || Date.now() + 2 * 60 * 60 * 1000
    profile.value = data.profile || { name: form.username, roleCode: '', roleName: '', email: '' }
    permissions.value = []
    menuList.value = []
    navLoaded.value = false
    persist()
    const navData = await fetchNavData()
    return { ...data, permissions: navData.permissions || permissions.value, menuList: menuList.value }
  }

  async function logout() {
    const hadSession = Boolean(token.value)
    clearSession()
    if (hadSession) {
      await logoutApi()
    }
  }

  function hasPermission(permission) {
    if (!permission) {
      return true
    }
    if (permissions.value.includes(permission)) {
      return true
    }
    if (isAuth(permission)) {
      return true
    }
    return collectMenuPermissions(menuList.value).includes(permission)
  }

  function hasMenuPathAccess(path) {
    if (!path) {
      return false
    }
    return flattenMenuEntries(menuList.value).some((item) => item.path === path)
  }

  return {
    token,
    refreshToken,
    expiresAt,
    profile,
    permissions,
    menuList,
    navLoaded,
    isLoggedIn,
    clearSession,
    restore,
    login,
    logout,
    hasPermission,
    hasMenuPathAccess,
  }
})
