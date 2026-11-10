import { ElMessage, ElMessageBox } from 'element-plus'

export function showSuccess(message) {
  ElMessage.success(message)
}

export function showError(message) {
  ElMessage.error(message)
}

export function confirmAction(message, title = '提示') {
  return ElMessageBox.confirm(message, title, { type: 'warning' })
}
