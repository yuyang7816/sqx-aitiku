/** Banner 展示位置：与 banner.classify 一致 */
export const BANNER_CLASSIFY_OPTIONS = [
  { label: '首页 Banner 轮播', value: 1 },
  { label: '金刚区', value: 2 },
]

/** Banner 上下架状态：与 banner.state 一致 */
export const BANNER_STATE_OPTIONS = [
  { label: '已上架', value: 1 },
  { label: '已下架', value: 2 },
]

export function getBannerClassifyLabel(value) {
  return BANNER_CLASSIFY_OPTIONS.find((item) => item.value === value)?.label || '-'
}

export function getBannerStateLabel(value) {
  return BANNER_STATE_OPTIONS.find((item) => item.value === value)?.label || '-'
}
