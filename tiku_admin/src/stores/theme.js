import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { defaultThemeId, themeList } from '@/config/themes'

const THEME_KEY = 'admin-theme'

function hexToRgb(hex) {
  const normalized = String(hex || '').replace('#', '')
  if (!/^[0-9a-fA-F]{6}$/.test(normalized)) {
    return { r: 0, g: 0, b: 0 }
  }
  return {
    r: Number.parseInt(normalized.slice(0, 2), 16),
    g: Number.parseInt(normalized.slice(2, 4), 16),
    b: Number.parseInt(normalized.slice(4, 6), 16),
  }
}

function rgbToHex({ r, g, b }) {
  const toHex = (value) => Math.max(0, Math.min(255, Math.round(value))).toString(16).padStart(2, '0')
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`
}

function mixColor(colorA, colorB, weight) {
  const a = hexToRgb(colorA)
  const b = hexToRgb(colorB)
  return rgbToHex({
    r: a.r * (1 - weight) + b.r * weight,
    g: a.g * (1 - weight) + b.g * weight,
    b: a.b * (1 - weight) + b.b * weight,
  })
}

function buildElementThemeVars(brand, isDark) {
  const base = brand || '#409eff'
  return {
    '--el-color-primary': base,
    '--el-color-primary-dark-2': mixColor(base, isDark ? '#000000' : '#111827', 0.18),
    '--el-color-primary-light-3': mixColor(base, '#ffffff', 0.3),
    '--el-color-primary-light-5': mixColor(base, '#ffffff', 0.5),
    '--el-color-primary-light-7': mixColor(base, '#ffffff', 0.7),
    '--el-color-primary-light-8': mixColor(base, '#ffffff', 0.8),
    '--el-color-primary-light-9': mixColor(base, '#ffffff', 0.9),
  }
}

function applyThemeToDocument(theme) {
  if (typeof document === 'undefined' || !theme) return
  const root = document.documentElement
  root.dataset.theme = theme.id
  const vars = {
    '--brand': theme.colors.brand,
    '--brand-dark': theme.colors.brandDark,
    '--ink': theme.colors.ink,
    '--muted': theme.colors.muted,
    '--line': theme.colors.line,
    '--page-bg': theme.colors.pageBg,
    '--body-gradient': theme.colors.bodyGradient,
    '--body-glow-a': theme.colors.bodyGlowA,
    '--body-glow-b': theme.colors.bodyGlowB,
    '--frame-bg': theme.colors.frameBg,
    '--aside-bg': theme.colors.asideBg,
    '--header-bg': theme.colors.headerBg,
    '--login-gradient': theme.colors.loginGradient,
    '--login-glow-a': theme.colors.loginGlowA,
    '--login-glow-b': theme.colors.loginGlowB,
    '--card-bg': theme.colors.cardBg,
    '--tab-border': theme.colors.tabBorder,
    '--tab-text': theme.colors.tabText,
    '--tab-hover-border': theme.colors.tabHoverBorder,
    '--tab-hover-bg': theme.colors.tabHoverBg,
    '--tab-hover-text': theme.colors.tabHoverText,
    '--tab-active-border': theme.colors.tabActiveBorder,
    '--tab-active-bg': theme.colors.tabActiveBg,
    '--tab-active-shadow': theme.colors.tabActiveShadow,
    '--menu-hover-bg': theme.colors.menuHoverBg,
    '--menu-active-bg': theme.colors.menuActiveBg,
    '--submenu-bg': theme.colors.submenuBg,
    '--logo-gradient': theme.colors.logoGradient,
    '--scroll-hover-bg': theme.colors.scrollHoverBg,
  }
  Object.entries({ ...vars, ...buildElementThemeVars(theme.colors.brand, theme.id === 'night') }).forEach(([key, value]) => {
    root.style.setProperty(key, value)
  })
}

function readThemeId() {
  if (typeof window === 'undefined') return defaultThemeId
  const stored = window.localStorage.getItem(THEME_KEY)
  if (!stored) return defaultThemeId
  return themeList.some((item) => item.id === stored) ? stored : defaultThemeId
}

export const useThemeStore = defineStore('theme', () => {
  const currentThemeId = ref(defaultThemeId)
  const themes = themeList
  const currentTheme = computed(() => themes.find((item) => item.id === currentThemeId.value) || themes[0])

  function setTheme(themeId) {
    const theme = themes.find((item) => item.id === themeId) || themes[0]
    currentThemeId.value = theme.id
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(THEME_KEY, theme.id)
    }
    applyThemeToDocument(theme)
  }

  function initTheme() {
    setTheme(readThemeId())
  }

  return {
    themes,
    currentThemeId,
    currentTheme,
    setTheme,
    initTheme,
  }
})
