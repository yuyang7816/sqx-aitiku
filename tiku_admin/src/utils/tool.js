import { filterAdminMenuTree } from '@/constants/admin-menu'
import { decorateMenuIcons } from '@/constants/menu-icons'

export function getUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const random = (Math.random() * 16) | 0
    const value = c === 'x' ? random : (random & 0x3) | 0x8
    return value.toString(16)
  })
}

export function getApiBaseUrl() {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'https://tikuadmin.xianmaxiong.com/sqx_fast/'
  return baseUrl.endsWith('/') ? baseUrl : `${baseUrl}/`
}

export function getCaptchaUrl(uuid) {
  return `${getApiBaseUrl()}captcha.jpg?uuid=${uuid}`
}

/** Knife4j 在线文档（用户端 /app/** 接口说明） */
export function getSwaggerDocUrl() {
  return `${getApiBaseUrl()}doc.html`
}

/** OpenAPI JSON，供调试或第三方工具导入 */
export function getOpenApiDocsUrl() {
  return `${getApiBaseUrl()}v3/api-docs`
}

export function isAuth(key) {
  const permissions = JSON.parse(window.sessionStorage.getItem('permissions') || '[]')
  if (permissions.indexOf(key) !== -1) return true
  // 兼容旧菜单权限标识
  const legacyMap = {
    'sys:config:list': 'allocationList:list',
    'sys:config:update': 'allocationList:update',
    'sys:config:save': 'allocationList:update',
    'sys:config:delete': 'allocationList:update',
    'sys:dict:save': 'sys:dict:list',
    'sys:dict:update': 'sys:dict:list',
    'sys:dict:delete': 'sys:dict:list',
    'sys:user:list': 'sys:user:list',
  }
  const legacy = legacyMap[key]
  return legacy ? permissions.indexOf(legacy) !== -1 : false
}

export function setPermissions(permissions = []) {
  window.sessionStorage.setItem('permissions', JSON.stringify(permissions))
}

export function clearPermissions() {
  window.sessionStorage.removeItem('permissions')
}

export function clearAuthCache() {
  window.sessionStorage.removeItem('admin-auth')
  clearPermissions()
}

const routePathMap = {
  home: '/dashboard',
  dashboard: '/dashboard',
  user: '/system/admins',
  role: '/system/roles',
  menu: '/system/menus',
  dict: '/system/dicts',
  log: '/system/logs',
  permission: '/system/permissions',
  perms: '/system/permissions',
  'sys/user': '/system/admins',
  'sys/role': '/system/roles',
  'sys/menu': '/system/menus',
  'sys/dict': '/system/dicts',
  'sys/config': '/system/configs',
  'sys/log': '/system/logs',
  userList: '/users',
  userDeleteRequest: '/user-delete-requests',
  userBadge: '/user-badges',
  bannerList: '/business/banners',
  message: '/business/messages',
  messageList: '/business/messages',
  userWallet: '/business/wallets',
  walletTransaction: '/business/wallet-transactions',
  vipConfig: '/business/vip-config',
  vipPlan: '/business/vip-plans',
  vipRights: '/business/vip-rights',
  referralCommission: '/business/referral-commissions',
  withdrawRecord: '/business/withdraw-records',
  withdrawalAccount: '/business/withdrawal-accounts',
  smsTemplate: '/system/sms-templates',
  startupConfig: '/business/startup-config',
  customerConfig: '/business/customer-config',
  agreementConfig: '/business/agreement-config',
  setupDict: '/setup-options',
  chapterTree: '/business/chapter-tree',
  questionList: '/business/questions',
  paperSet: '/business/paper-sets',
  aiConfig: '/business/ai-config',
  analysis: '/business/analysis',
  promoConfig: '/business/promo-config',
  withdrawConfig: '/business/withdraw-config',
  promoWithdraw: '/business/promo-withdraw',
  promoMembers: '/business/promo-members',
  ocrConfig: '/business/ocr-config',
  allocationList: '/system/configs',
  common: '/system/configs',
}

export function mapMenuUrlToPath(url) {
  const key = String(url || '').trim()
  return routePathMap[key] || ''
}

export function buildMissingPagePath(menuUrl = '', title = '') {
  const query = new URLSearchParams({
    menuUrl,
    title,
  })
  return `/404?${query.toString()}`
}

const menuTitleByUrl = {
  home: '运营看板',
  userList: 'C端用户',
  userDeleteRequest: '账号注销管理',
  userBadge: '勋章管理',
  setupDict: '年级学科配置',
  bannerList: '轮播图管理',
  startupConfig: '启动配置',
  customerConfig: '客服配置',
  kefuChat: '在线客服会话',
  agreementConfig: '协议 CMS',
  messageList: '消息管理',
  smsTemplate: '短信模板',
  chapterTree: '章节题库',
  questionList: '题目管理',
  paperSet: '题集管理',
  ocrConfig: 'OCR 配置',
  aiConfig: 'AI 配置',
  vipConfig: '会员配置',
  vipPlan: 'VIP套餐',
  vipRights: 'VIP权益',
  promoConfig: '推广佣金规则',
  promoWithdraw: '提现审核',
  promoMembers: '推广下级关系',
  userWallet: '用户钱包',
  walletTransaction: '钱包流水',
  withdrawalAccount: '提现账户',
  analysis: '学情统计',
}

function isGarbledText(text = '') {
  const normalized = String(text).trim()
  return !normalized || /^[?？]+$/.test(normalized)
}

function isGarbledMenuName(name = '') {
  return isGarbledText(name)
}

function resolveMenuTitle(name = '', url = '') {
  const normalizedName = String(name || '').trim()
  if (!isGarbledMenuName(normalizedName)) {
    return normalizedName
  }
  return menuTitleByUrl[String(url || '').trim()] || normalizedName
}

/** 从菜单树 perms 字段提取权限（Shiro 权限缓存异常时的兜底） */
export function collectMenuPermissions(menuList = []) {
  const perms = new Set()
  const walk = (items = []) => {
    items.forEach((item) => {
      String(item.perms || '')
        .split(',')
        .map((part) => part.trim())
        .filter(Boolean)
        .forEach((part) => perms.add(part))
      walk(item.children || item.list || [])
    })
  }
  walk(menuList)
  return [...perms]
}

export function normalizeMenuTree(menuList = []) {
  const normalized = menuList
    .map((item) => {
      const title = resolveMenuTitle(item.name, item.url)
      const url = item.url || ''
      return {
        id: item.menuId,
        title,
        icon: item.icon,
        type: item.type,
        perms: item.perms || '',
        url,
        path: item.type === 1 ? mapMenuUrlToPath(url) || buildMissingPagePath(url, title) : '',
        children: normalizeMenuTree(item.list || []),
      }
    })
    .filter((item) => item.type !== 2 && !isGarbledMenuName(item.title))

  return decorateMenuIcons(filterAdminMenuTree(normalized))
}
