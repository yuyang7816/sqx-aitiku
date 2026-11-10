import { getApiBaseUrl } from '@/utils/tool'

const IMAGE_FIELD_PATTERN = /(image|avatar|photo|qr|icon|pic|cover|banner|poster|thumb|url)/i
const IMAGE_VALUE_PATTERN = /\.(png|jpe?g|gif|webp|bmp|svg)(\?|$)/i

export function isImageFieldKey(key = '') {
  const text = String(key)
  return IMAGE_FIELD_PATTERN.test(text)
    || text === 'accountNumber'
    || text.endsWith('.accountNumber')
}

export function isImageField(field = {}, value) {
  const raw = value ?? field?.rawValue
  if (typeof raw !== 'string' || !raw.trim()) {
    return false
  }
  const trimmed = raw.trim()
  const looksLikeImage = IMAGE_VALUE_PATTERN.test(trimmed)
    || trimmed.startsWith('/')
    || trimmed.startsWith('data:image/')
  if (field.type === 'text') {
    return false
  }
  if (field.type === 'image') {
    return looksLikeImage
  }
  if (isImageFieldKey(field.key)) {
    return looksLikeImage
  }
  return IMAGE_VALUE_PATTERN.test(trimmed)
}

/** 从完整 URL 或相对路径中提取本地上传路径，如 /upload/2026/08/26/xxx.mp4 */
export function extractUploadRelativePath(path = '') {
  const value = String(path || '').trim()
  if (!value) return ''
  const match = value.match(/\/upload\/[^\s?#]+/)
  return match ? match[0] : ''
}

/**
 * 入库/表单存储：优先保存 /upload/ 相对路径，避免 localhost:9135 等错误端口导致无法播放
 */
export function normalizeStoredMediaPath(path = '') {
  const value = String(path || '').trim()
  if (!value) return ''
  const uploadPath = extractUploadRelativePath(value)
  if (uploadPath) return uploadPath
  if (/^https?:\/\//i.test(value) || value.startsWith('data:')) return value
  return value.startsWith('/') ? value : `/${value}`
}

/** 解析图片/封面的可访问地址（走当前管理端 API 代理） */
export function resolveImageUrl(path = '') {
  return resolveMediaUrl(path)
}

/** 解析视频/文件的可访问地址 */
export function resolveMediaUrl(path = '') {
  const value = String(path || '').trim()
  if (!value) return ''
  if (value.startsWith('data:')) return value

  const uploadPath = extractUploadRelativePath(value)
  const base = getApiBaseUrl().replace(/\/$/, '')

  if (uploadPath) {
    return `${base}${uploadPath}`
  }

  if (/^https?:\/\//i.test(value)) {
    return value
  }

  return `${base}/${value.replace(/^\//, '')}`
}
