/** 从旧版 gradient CSS 或纯色中提取背景色，供颜色选择器回显 */
export function parseCardBgColor(bgStyle = '', fallback = '#EEF4FF') {
  const value = String(bgStyle || '').trim()
  if (!value) return fallback
  if (/^#[0-9A-Fa-f]{3,8}$/.test(value)) return value
  const match = value.match(/#[0-9A-Fa-f]{3,8}/)
  return match ? match[0] : fallback
}

/** 保存前规范背景色（仅支持纯色） */
export function normalizeCardBgColor(color = '') {
  const value = String(color || '').trim()
  if (/^#[0-9A-Fa-f]{3,8}$/.test(value)) return value
  return ''
}
