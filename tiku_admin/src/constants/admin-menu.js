/** AI 题库管理后台菜单策略（不依赖数据库菜单结构） */

import { getDefaultAdminMenuTree } from '@/constants/admin-nav'
import { decorateMenuIcons } from '@/constants/menu-icons'

/** 侧边栏隐藏：仍可通过路由或系统配置页内链进入 */
export const ADMIN_HIDDEN_MENU_URLS = new Set([
  'apiDoc',
  'businessRules',
  'permission',
  'perms',
  'dict',
  'referralCommission',
  'withdrawRecord',
  'bannerList',
  'smsTemplate',
])

export const ADMIN_HIDDEN_MENU_PATHS = new Set([
  '/system/api-doc',
  '/system/business-rules',
  '/system/permissions',
  '/system/dicts',
  '/business/referral-commissions',
  '/business/withdraw-records',
  '/business/banners',
  '/system/sms-templates',
  '/business/sms-templates',
  '/business/startup-config',
  '/business/customer-config',
  '/business/agreement-config',
  '/business/ocr-config',
  '/business/ai-config',
  '/business/promo-config',
  '/business/vip-config',
  '/system/basic-config',
  '/system/auth-config',
  '/system/pay-config',
  '/system/sms-channel-config',
  '/system/file-config',
])

export const ADMIN_HIDDEN_MENU_TITLES = new Set([
  'API 文档',
  '业务规则文档',
  '权限管理',
  '字典管理',
  '佣金流水',
  '提现记录',
  '轮播图管理',
  '短信模板',
  '启动配置',
  '客服配置',
  '协议 CMS',
  'OCR 配置',
  'AI 配置',
  '推广佣金规则',
  '会员提醒',
  '基础配置',
  '登录认证',
  '支付配置',
  '短信通道',
  '文件存储',
  '运营配置',
  '智能服务',
])

export function isHiddenAdminMenu({ path = '', url = '', title = '' } = {}) {
  const normalizedUrl = String(url || '').trim()
  const normalizedPath = String(path || '').trim()
  const normalizedTitle = String(title || '').trim()

  if (ADMIN_HIDDEN_MENU_URLS.has(normalizedUrl)) return true
  if (ADMIN_HIDDEN_MENU_PATHS.has(normalizedPath)) return true
  if (ADMIN_HIDDEN_MENU_TITLES.has(normalizedTitle)) return true
  if (normalizedPath.startsWith('/404?')) {
    const menuUrl = new URLSearchParams(normalizedPath.split('?')[1] || '').get('menuUrl') || ''
    if (ADMIN_HIDDEN_MENU_URLS.has(menuUrl)) return true
  }
  return false
}

export function filterAdminMenuTree(menuList = []) {
  return menuList
    .map((item) => ({
      ...item,
      children: filterAdminMenuTree(item.children || []),
    }))
    .filter((item) => {
      if (isHiddenAdminMenu(item)) return false
      if (item.path && !item.path.startsWith('/404')) return true
      return (item.children || []).length > 0
    })
}

/** 统一输出前端 canonical 菜单（登录后始终使用，无需改数据库） */
export function resolveAdminMenuTree() {
  return decorateMenuIcons(filterAdminMenuTree(getDefaultAdminMenuTree()))
}
