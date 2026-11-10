/** 首页快捷入口启用状态 */
export const HOME_SHORTCUT_STATE_OPTIONS = [
  { label: '已启用', value: 1 },
  { label: '已禁用', value: 2 },
]

/** 特殊动作类型 */
export const HOME_SHORTCUT_ACTION_OPTIONS = [
  { label: '无（仅跳转）', value: '' },
  { label: '拍照搜题', value: 'ocr-camera' },
  { label: '在线客服', value: 'customer-service' },
]

export function getHomeShortcutStateLabel(value) {
  return HOME_SHORTCUT_STATE_OPTIONS.find((item) => item.value === value)?.label || '-'
}

export function getHomeShortcutActionLabel(value) {
  const key = String(value || '').trim()
  if (!key) return '无'
  return HOME_SHORTCUT_ACTION_OPTIONS.find((item) => item.value === key)?.label || key
}
