/** 系统配置页 Tab 定义（集中维护，侧边栏不再单独展示这些配置菜单） */

/** 独立菜单维护（不在系统配置 Tab 内） */
export const STANDALONE_CONFIG_PATHS = {
  setupDict: '/setup-options',
  vipPlans: '/business/vip-plans',
  vipRights: '/business/vip-rights',
  vipSettings: '/business/vip-config',
  userBadges: '/user-badges',
}

export const SYSTEM_CONFIG_TABS = [
  { key: 'basic', label: '基础配置', permission: 'sys:config:list' },
  { key: 'auth', label: '登录认证', permission: 'sys:config:list' },
  { key: 'pay', label: '支付配置', permission: 'sys:config:list' },
  { key: 'smsChannel', label: '短信通道', permission: 'sys:config:list' },
  { key: 'file', label: '文件存储', permission: 'sys:config:list' },
  { key: 'customer', label: '客服配置', permission: 'sys:config:list' },
  { key: 'agreement', label: '协议 CMS', permission: 'sys:config:list' },
  { key: 'sms', label: '短信模板', permission: 'sys:config:list' },
  { key: 'ocr', label: 'OCR 配置', permission: 'sys:config:list' },
  { key: 'ai', label: 'AI 配置', permission: 'sys:config:list' },
  { key: 'promo', label: '推广佣金', permission: 'sys:config:list' },
  { key: 'withdraw', label: '提现配置', permission: 'sys:config:list' },
  { key: 'dict', label: '系统字典', permission: 'sys:dict:list' },
]

export const SYSTEM_CONFIG_TAB_KEYS = new Set(SYSTEM_CONFIG_TABS.map((item) => item.key))

export const SYSTEM_CONFIG_TAB_PATHS = {
  basic: '/system/basic-config',
  auth: '/system/auth-config',
  pay: '/system/pay-config',
  smsChannel: '/system/sms-channel-config',
  file: '/system/file-config',
  customer: '/business/customer-config',
  agreement: '/business/agreement-config',
  sms: '/system/sms-templates',
  ocr: '/business/ocr-config',
  ai: '/business/ai-config',
  promo: '/business/promo-config',
  withdraw: '/business/withdraw-config',
  dict: '/system/dicts',
}

/** @returns {{ path: string, query?: Record<string, string> }} */
export function buildSystemConfigRoute(tabKey = 'basic') {
  const key = SYSTEM_CONFIG_TAB_KEYS.has(tabKey) ? tabKey : 'basic'
  return { path: '/system/configs', query: { tab: key } }
}

export function resolveSystemConfigTabKey(tabKey) {
  const key = String(tabKey || '').trim()
  return SYSTEM_CONFIG_TAB_KEYS.has(key) ? key : 'basic'
}
