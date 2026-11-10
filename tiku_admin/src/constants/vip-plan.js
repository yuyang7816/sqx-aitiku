/** VIP 套餐上下架：与 vip_plan.status 一致（1 启用 / 0 禁用） */
export const VIP_PLAN_STATUS_OPTIONS = [
  { label: '已上架', value: 1 },
  { label: '已下架', value: 0 },
]

/** 常用月数预设，对应 C 端 days 换算规则 */
export const VIP_PLAN_MONTH_PRESETS = [
  { label: '月卡（1 月 / 31 天）', value: 1 },
  { label: '季卡（3 月 / 90 天）', value: 3 },
  { label: '年卡（12 月 / 360 天）', value: 12 },
]

export function getVipPlanStatusLabel(value) {
  return VIP_PLAN_STATUS_OPTIONS.find((item) => item.value === value)?.label || '-'
}

/** 与 server VipPlanServiceImpl.resolveDays 一致 */
export function resolveVipPlanDays(month) {
  const m = Number(month)
  if (!m || m <= 0) return 0
  if (m === 1) return 31
  return m * 30
}
