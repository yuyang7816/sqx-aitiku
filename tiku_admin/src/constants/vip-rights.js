/** 权益业务键：与 vip_rights.rights_code 一致，供对比表 GET /app/vipRights 使用 */
export const VIP_RIGHTS_CODE_OPTIONS = [
  { label: 'AI 答疑（ai）', value: 'ai' },
  { label: 'OCR 拍照搜题（ocr）', value: 'ocr' },
  { label: '高清视频解析（video）', value: 'video' },
  { label: '专属学情报告（report）', value: 'report' },
  { label: '广告过滤（ad_free）', value: 'ad_free' },
]

export function getVipRightsCodeLabel(value) {
  if (!value) return '-'
  return VIP_RIGHTS_CODE_OPTIONS.find((item) => item.value === value)?.label || value
}
