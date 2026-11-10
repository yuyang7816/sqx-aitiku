import {
  getSystemConfigListApi,
  refreshSystemConfigApi,
  saveSystemConfigApi,
  updateSystemConfigApi,
} from '@/api/modules/system-config'
import {
  COMMISSION_RATE_CODE_DEFAULTS,
  setGlobalCommissionDefaults,
} from '@/constants/app-user'
import {
  PROMO_CONFIG_DEFS,
  PROMO_PARAM_TYPE,
  PROMO_PARAM_TYPE_DESC,
} from '@/constants/promo-config'

function flattenConfigList(groups) {
  return (groups || []).flatMap((group) => group.sysConfigEntityList || group.list || [])
}

function buildDefaultRecord(def) {
  return {
    id: null,
    paramKey: def.key,
    paramValue: def.defaultValue,
    status: '1',
    remark: def.remark,
    paramType: PROMO_PARAM_TYPE,
    paramTypeDesc: PROMO_PARAM_TYPE_DESC,
  }
}

export async function getPromoConfigApi() {
  const groups = await getSystemConfigListApi()
  const allRows = flattenConfigList(groups)
  const keyMap = new Map(allRows.map((row) => [row.paramKey, row]))

  const records = PROMO_CONFIG_DEFS.map((def) => {
    const existing = keyMap.get(def.key)
    if (existing) {
      return { ...existing, paramTypeDesc: existing.paramTypeDesc || PROMO_PARAM_TYPE_DESC }
    }
    return buildDefaultRecord(def)
  })

  return { records }
}

function parseStoredCommissionRate(raw, codeDefault) {
  const num = Number(raw)
  if (Number.isFinite(num) && num > 0) return num
  return codeDefault
}

let loadingPromise = null

/** 加载推广佣金全局默认比例（供 C 端用户列表/详情展示） */
export async function ensureGlobalCommissionDefaults() {
  if (loadingPromise) return loadingPromise
  loadingPromise = (async () => {
    const { records } = await getPromoConfigApi()
    const keyMap = new Map(records.map((row) => [row.paramKey, row.paramValue]))
    const level1Def = PROMO_CONFIG_DEFS.find((def) => def.key === 'biz_referral_commission_rate_level1')
    const level2Def = PROMO_CONFIG_DEFS.find((def) => def.key === 'biz_referral_commission_rate_level2')
    const level1Raw = keyMap.get('biz_referral_commission_rate_level1')
      ?? keyMap.get('biz_referral_commission_rate')
      ?? level1Def?.defaultValue
    const level2Raw = keyMap.get('biz_referral_commission_rate_level2') ?? level2Def?.defaultValue
    setGlobalCommissionDefaults({
      level1: parseStoredCommissionRate(level1Raw, COMMISSION_RATE_CODE_DEFAULTS.level1),
      level2: parseStoredCommissionRate(level2Raw, COMMISSION_RATE_CODE_DEFAULTS.level2),
    })
  })().catch(() => {
    setGlobalCommissionDefaults(COMMISSION_RATE_CODE_DEFAULTS)
  }).finally(() => {
    loadingPromise = null
  })
  return loadingPromise
}

export async function savePromoConfigApi(formValues) {
  const records = PROMO_CONFIG_DEFS.map((def) => ({
    id: formValues[`${def.key}_id`] || null,
    paramKey: def.key,
    paramValue: String(formValues[def.key] ?? def.defaultValue),
    status: '1',
    remark: def.remark,
    paramType: PROMO_PARAM_TYPE,
    paramTypeDesc: PROMO_PARAM_TYPE_DESC,
  }))

  for (const record of records) {
    const payload = {
      id: record.id,
      paramKey: record.paramKey,
      paramValue: record.paramValue,
      status: record.status,
      remark: record.remark,
      paramType: PROMO_PARAM_TYPE,
      paramTypeDesc: PROMO_PARAM_TYPE_DESC,
    }
    if (record.id) {
      await updateSystemConfigApi(payload)
    } else {
      await saveSystemConfigApi(payload)
    }
  }
  await refreshSystemConfigApi()
  return getPromoConfigApi()
}
