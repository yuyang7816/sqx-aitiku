/** 旧配置页路径 → 系统配置 Tab（会员/年级学科/勋章定义走独立菜单，不在此列） */

function redirectToSystemConfigTab(tab) {
  return { path: '/system/configs', query: { tab } }
}

export const protectedRoutes = [
  {
    path: '/404',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面不存在', hidden: true },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { title: '运营看板', icon: 'DataBoard', affix: true },
  },
  {
    path: '/users',
    name: 'users',
    component: () => import('@/views/user/UserListView.vue'),
    meta: { title: 'C端用户', icon: 'User' },
  },
  {
    path: '/user-delete-requests',
    name: 'user-delete-requests',
    component: () => import('@/views/user/UserDeleteRequestView.vue'),
    meta: { title: '账号注销申请', icon: 'Warning' },
  },
  {
    path: '/setup-options',
    name: 'setup-options',
    component: () => import('@/views/setup/SetupDictView.vue'),
    meta: { title: '年级学科', permission: 'sys:dict:list' },
  },
  {
    path: '/study-plan/setup-options',
    redirect: '/setup-options',
    meta: { hidden: true },
  },
  {
    path: '/study-plan',
    redirect: '/setup-options',
    meta: { hidden: true },
  },
  {
    path: '/user-badges',
    name: 'user-badges',
    component: () => import('@/views/user/UserBadgeView.vue'),
    meta: { title: '勋章定义', permission: 'sys:config:list' },
  },
  {
    path: '/business',
    name: 'business',
    redirect: '/business/chapter-tree',
    meta: { title: '业务管理', icon: 'Files' },
    children: [
      {
        path: '/business/banners',
        redirect: () => redirectToSystemConfigTab('basic'),
        meta: { hidden: true },
      },
      {
        path: '/business/startup-config',
        redirect: () => redirectToSystemConfigTab('customer'),
        meta: { hidden: true },
      },
      {
        path: '/business/customer-config',
        redirect: () => redirectToSystemConfigTab('customer'),
        meta: { hidden: true },
      },
      {
        path: '/business/kefu-chat',
        name: 'business-kefu-chat',
        component: () => import('@/views/customer/KefuChatView.vue'),
        meta: { title: '在线客服', permission: 'sys:config:list' },
      },
      {
        path: '/business/agreement-config',
        redirect: () => redirectToSystemConfigTab('agreement'),
        meta: { hidden: true },
      },
      {
        path: '/business/setup-dict',
        redirect: '/setup-options',
        meta: { hidden: true },
      },
      {
        path: '/business/home-shortcuts',
        name: 'business-home-shortcuts',
        component: () => import('@/views/business/HomeShortcutView.vue'),
        meta: { title: '首页快捷入口', permission: 'sys:config:list' },
      },
      {
        path: '/business/practice-entries',
        name: 'business-practice-entries',
        component: () => import('@/views/business/PracticeEntryView.vue'),
        meta: { title: '练习入口', permission: 'sys:config:list' },
      },
      {
        path: '/business/chapter-tree',
        name: 'business-chapter-tree',
        component: () => import('@/views/business/ChapterTreeView.vue'),
        meta: { title: '章节题库' },
      },
      {
        path: '/business/questions',
        name: 'business-questions',
        component: () => import('@/views/business/QuestionView.vue'),
        meta: { title: '题目管理' },
      },
      {
        path: '/business/ocr-config',
        redirect: () => redirectToSystemConfigTab('ocr'),
        meta: { hidden: true },
      },
      {
        path: '/business/ai-config',
        redirect: () => redirectToSystemConfigTab('ai'),
        meta: { hidden: true },
      },
      {
        path: '/business/paper-sets',
        name: 'business-paper-sets',
        component: () => import('@/views/business/PaperSetView.vue'),
        meta: { title: '题集管理' },
      },
      {
        path: '/business/messages',
        name: 'business-messages',
        component: () => import('@/views/business/MessagesView.vue'),
        meta: { title: '消息管理' },
      },
      {
        path: '/business/user-feedback',
        name: 'business-user-feedback',
        component: () => import('@/views/business/UserFeedbackView.vue'),
        meta: { title: '用户反馈管理' },
      },
      {
        path: '/business/analysis',
        name: 'business-analysis',
        component: () => import('@/views/analysis/AnalysisView.vue'),
        meta: { title: '学情统计' },
      },
      {
        path: '/business/user-notebooks',
        name: 'business-user-notebooks',
        component: () => import('@/views/analysis/UserNotebookView.vue'),
        meta: { title: '用户错题本' },
      },
      {
        path: '/business/user-notebooks/detail',
        name: 'business-user-notebooks-detail',
        component: () => import('@/views/analysis/UserNotebookDetailView.vue'),
        meta: { title: '用户错题列表', hidden: true },
      },
      {
        path: '/business/wallets',
        name: 'business-wallets',
        component: () => import('@/views/business/UserWalletView.vue'),
        meta: { title: '用户钱包', hidden: true, activeMenu: '/users' },
      },
      {
        path: '/business/wallet-transactions',
        name: 'business-wallet-transactions',
        component: () => import('@/views/business/UserWalletTransactionView.vue'),
        meta: { title: '钱包流水', hidden: true, activeMenu: '/users' },
      },
      {
        path: '/business/vip-config',
        name: 'business-vip-config',
        component: () => import('@/views/vip/VipConfigView.vue'),
        meta: { title: '会员提醒', permission: 'sys:config:list', hidden: true },
      },
      {
        path: '/business/vip-plans',
        name: 'business-vip-plans',
        component: () => import('@/views/business/VipPlanView.vue'),
        meta: { title: '会员套餐', permission: 'sys:config:list' },
      },
      {
        path: '/business/vip-rights',
        name: 'business-vip-rights',
        component: () => import('@/views/business/VipRightsView.vue'),
        meta: { title: '会员权益', permission: 'sys:config:list' },
      },
      {
        path: '/business/promo-config',
        redirect: () => redirectToSystemConfigTab('promo'),
        meta: { hidden: true },
      },
      {
        path: '/business/withdraw-config',
        redirect: () => redirectToSystemConfigTab('withdraw'),
        meta: { hidden: true },
      },
      {
        path: '/business/promo-withdraw',
        name: 'business-promo-withdraw',
        component: () => import('@/views/promo/WithdrawReviewView.vue'),
        meta: { title: '提现审核' },
      },
      {
        path: '/business/promo-members',
        name: 'business-promo-members',
        component: () => import('@/views/promo/ReferralMembersView.vue'),
        meta: { title: '推广下级关系', hidden: true, activeMenu: '/users' },
      },
      {
        path: '/business/withdrawal-accounts',
        name: 'business-withdrawal-accounts',
        component: () => import('@/views/business/WithdrawalAccountView.vue'),
        meta: { title: '提现账户', hidden: true, activeMenu: '/users' },
      },
      {
        path: '/business/sms-templates',
        redirect: () => redirectToSystemConfigTab('sms'),
        meta: { hidden: true },
      },
    ],
  },
  {
    path: '/system',
    name: 'system',
    redirect: '/system/admins',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: '/system/admins',
        name: 'system-admins',
        component: () => import('@/views/system/AdminManagerView.vue'),
        meta: { title: '管理员管理', permission: 'sys:user:list' },
      },
      {
        path: '/system/roles',
        name: 'system-roles',
        component: () => import('@/views/system/RoleManagerView.vue'),
        meta: { title: '角色管理', permission: 'sys:role:list' },
      },
      {
        path: '/system/menus',
        name: 'system-menus',
        component: () => import('@/views/system/MenuManagerView.vue'),
        meta: { title: '菜单管理', permission: 'sys:menu:list' },
      },
      {
        path: '/system/configs',
        name: 'system-configs',
        component: () => import('@/views/system/SystemConfigView.vue'),
        meta: { title: '系统配置', permission: 'sys:config:list' },
      },
      {
        path: '/system/basic-config',
        redirect: () => redirectToSystemConfigTab('basic'),
        meta: { hidden: true },
      },
      {
        path: '/system/auth-config',
        redirect: () => redirectToSystemConfigTab('auth'),
        meta: { hidden: true },
      },
      {
        path: '/system/pay-config',
        redirect: () => redirectToSystemConfigTab('pay'),
        meta: { hidden: true },
      },
      {
        path: '/system/sms-channel-config',
        redirect: () => redirectToSystemConfigTab('smsChannel'),
        meta: { hidden: true },
      },
      {
        path: '/system/file-config',
        redirect: () => redirectToSystemConfigTab('file'),
        meta: { hidden: true },
      },
      {
        path: '/system/sms-templates',
        redirect: () => redirectToSystemConfigTab('sms'),
        meta: { hidden: true },
      },
      {
        path: '/system/dicts',
        redirect: () => redirectToSystemConfigTab('dict'),
        meta: { hidden: true },
      },
      {
        path: '/system/logs',
        name: 'system-logs',
        component: () => import('@/views/system/LogManagerView.vue'),
        meta: { title: '操作日志', permission: 'sys:log:list' },
      },
    ],
  },
]

export const whiteRoutes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' },
  },
]
