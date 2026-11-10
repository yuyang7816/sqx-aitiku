export const USER_DELETE_REQUEST_STATUS_OPTIONS = [
  { label: '冷静期中', value: 'pending', tagType: 'warning' },
  { label: '用户已撤销', value: 'cancelled', tagType: 'info' },
  { label: '管理员已撤销', value: 'admin_cancelled', tagType: 'info' },
  { label: '已执行注销', value: 'completed', tagType: 'danger' },
]

export function getUserDeleteRequestStatusLabel(status) {
  return USER_DELETE_REQUEST_STATUS_OPTIONS.find((item) => item.value === status)?.label || status || '—'
}

export function getUserDeleteRequestStatusTagType(status) {
  return USER_DELETE_REQUEST_STATUS_OPTIONS.find((item) => item.value === status)?.tagType || 'info'
}
