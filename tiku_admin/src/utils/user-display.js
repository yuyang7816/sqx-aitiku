/**
 * 管理端列表用户展示名（优先用户名/昵称，不展示纯 ID）
 */
export function resolveUserDisplayName(row = {}) {
  if (!row || typeof row !== 'object') {
    return '—'
  }
  const nested = row.userEntity || {}
  const name = [
    row.userName,
    row.nickname,
    nested.nickname,
    nested.userName,
  ].find((item) => String(item || '').trim())
  if (name) {
    return String(name).trim()
  }
  if (row.phone || nested.phone) {
    return String(row.phone || nested.phone).trim()
  }
  return row.userId ? `用户${row.userId}` : '—'
}

export function resolveUserPhone(row = {}) {
  return row.phone || row.userEntity?.phone || ''
}
