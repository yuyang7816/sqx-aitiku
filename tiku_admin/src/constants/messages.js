/** 消息类型：与 messages.type 字段一致 */
export const MESSAGE_TYPE_OPTIONS = [
  { label: '系统公告', value: 1 },
  { label: '用户消息', value: 2 },
]

/** 已读状态筛选项 */
export const MESSAGE_READ_OPTIONS = [
  { label: '未读', value: 0 },
  { label: '已读', value: 1 },
]

/**
 * 跳转标识（url 字段）：与 uniapp notice.vue / MessagesServiceImpl.resolveJumpId 对齐
 * 管理端配置后，C 端 GET /app/messages 返回 item.id 优先取此值
 */
export const MESSAGE_JUMP_OPTIONS = [
  { label: '无跳转', value: '' },
  { label: '待复习错题（notice-review）', value: 'notice-review' },
  { label: '会员到期/续费提醒（notice-member）', value: 'notice-member' },
  { label: '学习月报（notice-report）', value: 'notice-report' },
  { label: '会员权益生效（notice-vip-active）', value: 'notice-vip-active' },
  { label: 'AI 次数提醒（notice-vip-guide）', value: 'notice-vip-guide' },
  { label: '完善资料引导（notice-plan）', value: 'notice-plan' },
]

/** 常用推送模板：一键填充标题、内容与跳转标识 */
export const MESSAGE_PUSH_TEMPLATES = [
  {
    key: 'member-renew',
    label: '会员到期/续费提醒',
    title: '会员即将到期',
    content: '您的会员权益即将到期，请及时续费以继续享受 AI 答疑、搜题等专属服务。',
    url: 'notice-member',
    type: 2,
  },
  {
    key: 'monthly-report',
    label: '学习月报',
    title: '本月学习月报已生成',
    content: '您本月的学习数据已汇总完成，点击查看练习时长、正确率与薄弱知识点分析。',
    url: 'notice-report',
    type: 2,
  },
  {
    key: 'review-remind',
    label: '错题复习提醒',
    title: '待复习错题提醒',
    content: '您还有待复习的错题，建议优先完成错题回顾以巩固薄弱知识点。',
    url: 'notice-review',
    type: 2,
  },
  {
    key: 'vip-active',
    label: '会员权益生效',
    title: '会员权益生效中',
    content: '您的会员权益已生效，可无限次使用 AI 答疑与搜题功能。',
    url: 'notice-vip-active',
    type: 2,
  },
  {
    key: 'vip-guide',
    label: 'AI 次数提醒',
    title: 'AI 答疑次数不足',
    content: '今日免费 AI 答疑次数即将用完，开通会员可无限次使用 AI 答疑与搜题。',
    url: 'notice-vip-guide',
    type: 2,
  },
  {
    key: 'system-notice',
    label: '系统公告',
    title: '系统公告',
    content: '平台有新的功能或活动上线，请及时查看详情。',
    url: '',
    type: 1,
  },
]

export function getMessageTypeLabel(type) {
  return MESSAGE_TYPE_OPTIONS.find((item) => item.value === type)?.label || String(type ?? '')
}

export function getMessageJumpLabel(url) {
  if (!url) return '无跳转'
  return MESSAGE_JUMP_OPTIONS.find((item) => item.value === url)?.label || url
}
