export function formatRecordValue(value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }
  if (Array.isArray(value)) {
    return value.length ? value.join('、') : '—'
  }
  if (typeof value === 'object') {
    try {
      return JSON.stringify(value, null, 2)
    } catch {
      return String(value)
    }
  }
  return value
}

export function buildRecordDetailItems(row = {}, extraFields = []) {
  if (Array.isArray(extraFields) && extraFields.length) {
    return extraFields.map((field) => ({ ...field }))
  }

  const hiddenKeys = new Set(['password', 'salt'])
  const nestedSources = [
    { prefix: 'userEntity', labelMap: { userName: '用户名', phone: '手机号', nickname: '昵称', invitationCode: '邀请码' } },
    { prefix: 'withdrawalAccount', labelMap: { accountName: '账户名称', accountNumber: '账号', type: '账户类型', realName: '真实姓名' } },
  ]

  const nestedKeys = []
  for (const source of nestedSources) {
    const nested = row?.[source.prefix]
    if (!nested || typeof nested !== 'object') continue
    Object.keys(nested).forEach((key) => {
      if (hiddenKeys.has(key)) return
      nestedKeys.push({
        key: `${source.prefix}.${key}`,
        label: source.labelMap[key] || `${source.prefix}.${key}`,
        formatter: extraFields.find((item) => item.key === key)?.formatter,
        nestedKey: key,
        nestedSource: source.prefix,
      })
    })
  }

  const keys = [...new Set([
    ...Object.keys(row || {}),
    ...extraFields.map((item) => item.key),
    ...nestedKeys.map((item) => item.key),
  ])]

  return keys
    .filter((key) => !hiddenKeys.has(key) && !['userEntity', 'withdrawalAccount'].includes(key))
    .map((key) => {
      const nested = nestedKeys.find((item) => item.key === key)
      if (nested) {
        return {
          key,
          label: nested.label,
          formatter: (value, data) => {
            const raw = data?.[nested.nestedSource]?.[nested.nestedKey]
            if (typeof nested.formatter === 'function') {
              return nested.formatter(raw, data)
            }
            return formatRecordValue(raw)
          },
        }
      }
      const extra = extraFields.find((item) => item.key === key)
      return {
        key,
        label: extra?.label || key,
        formatter: extra?.formatter,
      }
    })
}

export function resolveRecordFieldValue(field, row = {}) {
  const raw = getNestedValue(row, field.key)
  if (typeof field.formatter === 'function') {
    return field.formatter(raw, row)
  }
  return formatRecordValue(raw)
}

export function getNestedValue(row = {}, key = '') {
  if (!key || !row) {
    return undefined
  }
  if (!String(key).includes('.')) {
    return row[key]
  }
  return String(key).split('.').reduce((acc, part) => (acc == null ? acc : acc[part]), row)
}

export function resolveLinkedUserId(row = {}) {
  return row?.userId || row?.userEntity?.userId || null
}

export function resolveLinkedOrderId(row = {}, fieldKey = '') {
  if (fieldKey === 'orderId' || fieldKey === 'orderNo') {
    return row?.orderId || null
  }
  if (fieldKey === 'dataId' && row?.dataId) {
    return row.dataId
  }
  return row?.orderId || null
}

export function isUserLinkField(fieldKey) {
  const key = String(fieldKey)
  return ['userId', 'userName', 'phone', 'nickname'].includes(key)
    || key.endsWith('.phone')
    || key.endsWith('.userName')
    || key.endsWith('.nickname')
}

export function isOrderLinkField(fieldKey) {
  return ['orderId', 'orderNo', 'dataId'].includes(fieldKey)
}
