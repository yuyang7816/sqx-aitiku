/** 后端 /sys/menu/nav 无菜单时的 AI 题库默认导航（按运营模块分组） */

export const DEFAULT_ADMIN_PERMISSIONS = [
  'sys:user:list',
  'sys:role:list',
  'sys:menu:list',
  'sys:config:list',
  'sys:dict:list',
  'sys:dict:save',
  'sys:dict:update',
  'sys:dict:delete',
  'sys:log:list',
  'allocationList:list',
  'allocationList:update',
]

export function getDefaultAdminMenuTree() {
  return [
    {
      id: 'dashboard',
      title: '运营看板',
      icon: 'icon-shuju',
      path: '/dashboard',
      children: [],
    },
    {
      id: 'users',
      title: 'C端用户',
      icon: 'icon-yonghu',
      path: '/users',
      children: [],
    },
    {
      id: 'kefu-chat',
      title: '在线客服',
      icon: 'icon-xiaoxi',
      path: '/business/kefu-chat',
      children: [],
    },
    {
      id: 'app-display',
      title: '首页配置',
      icon: 'icon-peizhi',
      path: '',
      children: [
        { id: 'home-shortcuts', title: '首页快捷入口', icon: 'icon-menu', path: '/business/home-shortcuts', children: [] },
        { id: 'practice-entries', title: '练习入口', icon: 'icon-shangpin', path: '/business/practice-entries', children: [] },
      ],
    },
    {
      id: 'question-bank',
      title: '题库管理',
      icon: 'icon-shangpin',
      path: '',
      children: [
        { id: 'chapter-tree', title: '章节题库', icon: 'icon-menu', path: '/business/chapter-tree', children: [] },
        { id: 'questions', title: '题目管理', icon: 'icon-shangpin', path: '/business/questions', children: [] },
        { id: 'paper-sets', title: '题集管理', icon: 'icon-leibie', path: '/business/paper-sets', children: [] },
        { id: 'setup-dict', title: '年级学科', icon: 'icon-fenleilist', path: '/setup-options', children: [] },
      ],
    },
    {
      id: 'vip-center',
      title: '会员中心',
      icon: 'icon-shoucang',
      path: '',
      children: [
        { id: 'vip-plans', title: '会员套餐', icon: 'icon-order', path: '/business/vip-plans', children: [] },
        { id: 'vip-rights', title: '会员权益', icon: 'icon-shoucang', path: '/business/vip-rights', children: [] },
      ],
    },
    {
      id: 'finance-center',
      title: '财务钱包',
      icon: 'icon-caiwu',
      path: '',
      children: [
        { id: 'promo-withdraw', title: '提现审核', icon: 'icon-order', path: '/business/promo-withdraw', children: [] },
      ],
    },
    {
      id: 'analysis-center',
      title: '用户学情',
      icon: 'icon-shuju',
      path: '',
      children: [
        { id: 'analysis', title: '学情统计', icon: 'icon-shuju', path: '/business/analysis', children: [] },
        { id: 'user-notebooks', title: '用户错题本', icon: 'icon-order', path: '/business/user-notebooks', children: [] },
        { id: 'user-badges', title: '勋章定义', icon: 'icon-shoucang', path: '/user-badges', children: [] },
      ],
    },
    {
      id: 'messages',
      title: '消息管理',
      icon: 'icon-xiaoxi',
      path: '/business/messages',
      children: [],
    },
    {
      id: 'user-feedback',
      title: '用户反馈管理',
      icon: 'icon-log',
      path: '/business/user-feedback',
      children: [],
    },
    {
      id: 'user-delete-requests',
      title: '账号注销管理',
      icon: 'icon-log',
      path: '/user-delete-requests',
      children: [],
    },
    {
      id: 'configs',
      title: '系统配置',
      icon: 'icon-peizhi',
      path: '/system/configs',
      perms: 'sys:config:list',
      children: [],
    },
    {
      id: 'system',
      title: '系统管理',
      icon: 'icon-system',
      path: '',
      children: [
        { id: 'admins', title: '管理员管理', icon: 'icon-admin', path: '/system/admins', children: [] },
        { id: 'roles', title: '角色管理', icon: 'icon-role', path: '/system/roles', children: [] },
        { id: 'menus', title: '菜单管理', icon: 'icon-menu', path: '/system/menus', children: [] },
        { id: 'logs', title: '操作日志', icon: 'icon-log', path: '/system/logs', children: [] },
      ],
    },
  ]
}
