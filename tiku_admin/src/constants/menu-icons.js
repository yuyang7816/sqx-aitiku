/** 按菜单名称/URL 解析侧边栏图标（优先使用后端 icon，缺失时回退） */

export const MENU_ICON_BY_TITLE = {
  运营看板: 'icon-shuju',
  C端用户: 'icon-yonghu',
  消息管理: 'icon-xiaoxi',
  账号注销管理: 'icon-log',
  账号注销申请: 'icon-log',
  勋章管理: 'icon-shoucang',
  用户勋章: 'icon-shoucang',
  年级学科配置: 'icon-fenleilist',
  年级学科: 'icon-fenleilist',
  运营配置: 'icon-peizhi',
  题库管理: 'icon-shangpin',
  智能服务: 'icon-config',
  会员中心: 'icon-shoucang',
  推广分销: 'icon-jifen',
  财务钱包: 'icon-caiwu',
  用户学情: 'icon-shuju',
  系统管理: 'icon-system',
  轮播图管理: 'icon-tupian',
  启动配置: 'icon-peizhi',
  客服配置: 'icon-xinxi',
  在线客服会话: 'icon-xiaoxi',
  '协议 CMS': 'icon-editor',
  消息管理: 'icon-xiaoxi',
  短信模板: 'icon-duanxin',
  章节题库: 'icon-menu',
  题目管理: 'icon-shangpin',
  题集管理: 'icon-leibie',
  'OCR 配置': 'icon-peizhi',
  'AI 配置': 'icon-config',
  会员配置: 'icon-shoucang',
  会员套餐: 'icon-shoucangfill',
  会员权益: 'icon-shoucang',
  会员提醒: 'icon-peizhi',
  VIP套餐: 'icon-shoucangfill',
  VIP权益: 'icon-shoucang',
  勋章定义: 'icon-shoucang',
  用户错题本: 'icon-order',
  推广佣金规则: 'icon-jifen',
  提现审核: 'icon-order',
  推广下级关系: 'icon-yonghu',
  用户钱包: 'icon-order',
  钱包流水: 'icon-log',
  提现账户: 'icon-geren',
  学情统计: 'icon-shuju',
  管理员管理: 'icon-admin',
  角色管理: 'icon-role',
  菜单管理: 'icon-menu',
  系统配置: 'icon-peizhi',
  操作日志: 'icon-log',
}

export const MENU_ICON_BY_URL = {
  home: 'icon-shuju',
  userList: 'icon-yonghu',
  userDeleteRequest: 'icon-log',
  userBadge: 'icon-shoucang',
  setupDict: 'icon-fenleilist',
  bannerList: 'icon-tupian',
  startupConfig: 'icon-peizhi',
  customerConfig: 'icon-xinxi',
  kefuChat: 'icon-xiaoxi',
  agreementConfig: 'icon-editor',
  messageList: 'icon-xiaoxi',
  smsTemplate: 'icon-duanxin',
  chapterTree: 'icon-menu',
  questionList: 'icon-shangpin',
  paperSet: 'icon-leibie',
  ocrConfig: 'icon-peizhi',
  aiConfig: 'icon-config',
  vipConfig: 'icon-shoucang',
  vipPlan: 'icon-shoucangfill',
  vipRights: 'icon-shoucang',
  promoConfig: 'icon-jifen',
  promoWithdraw: 'icon-order',
  promoMembers: 'icon-yonghu',
  userWallet: 'icon-order',
  walletTransaction: 'icon-log',
  withdrawalAccount: 'icon-geren',
  analysis: 'icon-shuju',
}

const ICON_ALIASES = {
  'icon-vip': 'icon-shoucangfill',
  'icon-tupian': 'icon-tubiao',
}

const GENERIC_ICONS = new Set(['Files', 'DataBoard', 'Setting', 'User', 'Reading', ''])

export function resolveMenuIcon(item = {}) {
  const rawIcon = String(item.icon || '').trim()
  if (rawIcon && !GENERIC_ICONS.has(rawIcon)) {
    return ICON_ALIASES[rawIcon] || rawIcon
  }
  const byTitle = MENU_ICON_BY_TITLE[String(item.title || '').trim()]
  if (byTitle) return byTitle
  const byUrl = MENU_ICON_BY_URL[String(item.url || '').trim()]
  if (byUrl) return byUrl
  return rawIcon || 'icon-menu'
}

export function decorateMenuIcons(menuList = []) {
  return menuList.map((item) => ({
    ...item,
    icon: resolveMenuIcon(item),
    children: decorateMenuIcons(item.children || []),
  }))
}
