/** 用户端（uniapp）/app/** API 目录，供管理端 API 文档页展示；Swagger 分组见 Knife4j「业务模块 - APP端」 */

export const API_DOC_CONVENTIONS = {
  baseUrl: 'https://tikuadmin.xianmaxiong.com/sqx_fast',
  responseShape: '{ "code": number, "msg": string, "data": T }',
  successCode: 0,
  authHeader: 'Authorization: Bearer {token}',
  swaggerDoc: 'doc.html',
  openApiJson: 'v3/api-docs',
  swaggerGroup: '业务模块 - APP端',
}

/** 服务端 Markdown 对照文档（仓库路径 tiku_server/docs/） */
export const API_DOC_MARKDOWN_INDEX = [
  { file: 'auth-api.md', title: '认证与验证码', section: '21.1' },
  { file: 'user-api.md', title: '用户资料', section: '21.1' },
  { file: 'existing-api-audit.md', title: '已有 API 接入清单（13 项）', section: '21.1' },
  { file: 'question-tree-api.md', title: '章节树', section: '21.2' },
  { file: 'question-list-api.md', title: '题目列表', section: '21.2' },
  { file: 'question-detail-api.md', title: '题目详情', section: '21.2' },
  { file: 'practice-submit-api.md', title: '练习提交与记录', section: '21.2' },
  { file: 'notebook-api.md', title: '错题本', section: '21.2' },
  { file: 'collection-api.md', title: '题目收藏', section: '21.2' },
  { file: 'ocr-api.md', title: 'OCR 搜题', section: '21.2' },
  { file: 'ai-chat-api.md', title: 'AI 答疑', section: '21.2' },
  { file: 'analysis-api.md', title: '学情统计', section: '21.2' },
  { file: 'search-records-api.md', title: '搜题记录', section: '21.2' },
]

/**
 * @typedef {Object} AppApiEndpoint
 * @property {string} module - Swagger 标签 / 业务模块
 * @property {string} method - HTTP 方法
 * @property {string} path - App 端路径（含 /app 前缀）
 * @property {string} summary - 接口说明
 * @property {boolean} auth - 是否需要登录
 * @property {string} [docFile] - 对照 Markdown 文件名
 * @property {'21.1'|'21.2'|'扩展'} section - 需求文档章节
 */

/** @type {AppApiEndpoint[]} */
export const APP_API_ENDPOINTS = [
  // 21.1 认证
  { module: '用户端-认证', method: 'POST', path: '/app/verifyCode', summary: '发送短信验证码', auth: false, docFile: 'auth-api.md', section: '21.1' },
  { module: '用户端-认证', method: 'POST', path: '/app/auth/sms', summary: '短信验证码登录', auth: false, docFile: 'auth-api.md', section: '21.1' },
  { module: '用户端-认证', method: 'POST', path: '/app/auth/wechat', summary: '微信小程序登录', auth: false, docFile: 'auth-api.md', section: '21.1' },
  // 21.1 用户
  { module: '用户端-用户资料', method: 'GET', path: '/app/user', summary: '获取用户 profile（年级/学科/版本/VIP 等）', auth: true, docFile: 'user-api.md', section: '21.1' },
  { module: '用户端-用户资料', method: 'PUT', path: '/app/user', summary: '更新用户 profile', auth: true, docFile: 'user-api.md', section: '21.1' },
  { module: '用户端-用户资料', method: 'POST', path: '/app/user', summary: '更新用户 profile（兼容 POST）', auth: true, docFile: 'user-api.md', section: '21.1' },
  // 21.1 已有 API
  { module: '用户端-Banner轮播', method: 'GET', path: '/app/banner', summary: '按位置/状态返回启用轮播图', auth: false, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-消息信息表', method: 'GET', path: '/app/messages', summary: '消息分页列表（支持 read 筛选）', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-消息信息表', method: 'POST', path: '/app/messages/read-all', summary: '当前用户全部消息标记已读', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-会员套餐', method: 'GET', path: '/app/vipPlan', summary: '上架 VIP 套餐列表（name/price/days）', auth: false, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-会员特权', method: 'GET', path: '/app/vipRights', summary: 'VIP 权益对比表（免费 vs 会员文案）', auth: false, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-会员特权', method: 'POST', path: '/app/vipRights/list', summary: 'VIP 权益图标列表（含 sort）', auth: false, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-会员支付', method: 'POST', path: '/app/pay', summary: '创建会员订单并返回 prepay 参数', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-用户钱包', method: 'GET', path: '/app/userWallet', summary: '钱包余额', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-用户钱包交易记录表', method: 'GET', path: '/app/userWalletTransaction', summary: '收益/提现流水', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-用户佣金', method: 'GET', path: '/app/referral/userCommission', summary: '推广佣金汇总', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-推广下级成员', method: 'GET', path: '/app/referral/members', summary: '直接下级列表', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-推广邀请码', method: 'GET', path: '/app/referral/inviteCode', summary: '邀请码与小程序码', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-提现记录', method: 'POST', path: '/app/withdrawRecord', summary: '提交提现申请', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-提现账号', method: 'POST', path: '/app/withdrawalAccount', summary: '绑定/更新提现账户', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-系统配置', method: 'POST', path: '/app/config/info', summary: '按 param_key 拉取单项配置', auth: false, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '用户端-系统配置', method: 'GET', path: '/app/config/customer', summary: '客服电话/链接/入口开关', auth: false, docFile: 'existing-api-audit.md', section: '21.1' },
  { module: '文件', method: 'POST', path: '/file/upload', summary: '图片/文件上传（multipart）', auth: true, docFile: 'existing-api-audit.md', section: '21.1' },
  // 21.2 缺口 API
  { module: '用户端-题库章节', method: 'GET', path: '/app/question/chapter/tree', summary: '章→节树（grade/subject/version 筛选）', auth: false, docFile: 'question-tree-api.md', section: '21.2' },
  { module: '用户端-题库题目', method: 'GET', path: '/app/question/list', summary: '分页题目列表（chapterId/paperId 等）', auth: true, docFile: 'question-list-api.md', section: '21.2' },
  { module: '用户端-题库题目', method: 'GET', path: '/app/question/{id}', summary: '单题详情（id 或 question_code）', auth: true, docFile: 'question-detail-api.md', section: '21.2' },
  { module: '用户端-练习进度', method: 'POST', path: '/app/practice/submit', summary: '章节练习提交（得分/答题明细）', auth: true, docFile: 'practice-submit-api.md', section: '21.2' },
  { module: '用户端-练习进度', method: 'GET', path: '/app/practice/records', summary: '练习记录分页', auth: true, docFile: 'practice-submit-api.md', section: '21.2' },
  { module: '用户端-错题本', method: 'GET', path: '/app/notebook', summary: '错题列表', auth: true, docFile: 'notebook-api.md', section: '21.2' },
  { module: '用户端-错题本', method: 'POST', path: '/app/notebook', summary: '添加错题', auth: true, docFile: 'notebook-api.md', section: '21.2' },
  { module: '用户端-错题本', method: 'DELETE', path: '/app/notebook', summary: '删除错题', auth: true, docFile: 'notebook-api.md', section: '21.2' },
  { module: '用户端-题目收藏', method: 'GET', path: '/app/collection', summary: '收藏列表', auth: true, docFile: 'collection-api.md', section: '21.2' },
  { module: '用户端-题目收藏', method: 'POST', path: '/app/collection', summary: '添加收藏', auth: true, docFile: 'collection-api.md', section: '21.2' },
  { module: '用户端-题目收藏', method: 'DELETE', path: '/app/collection', summary: '取消收藏', auth: true, docFile: 'collection-api.md', section: '21.2' },
  { module: '用户端-OCR', method: 'POST', path: '/app/ocr/search', summary: 'OCR 文字/图片搜题', auth: true, docFile: 'ocr-api.md', section: '21.2' },
  { module: '用户端-OCR', method: 'POST', path: '/app/ocr/upload', summary: 'OCR 拍照图片上传', auth: true, docFile: 'ocr-api.md', section: '21.2' },
  { module: '用户端-AI 答疑', method: 'POST', path: '/app/ai/chat', summary: 'AI 多轮答疑（题目上下文 + history）', auth: true, docFile: 'ai-chat-api.md', section: '21.2' },
  { module: '用户端-学情统计', method: 'GET', path: '/app/analysis/overview', summary: '学情概览（正确率/进度等）', auth: true, docFile: 'analysis-api.md', section: '21.2' },
  { module: '用户端-学情统计', method: 'GET', path: '/app/analysis/report', summary: '月度学情报告（month=yyyy-MM）', auth: true, docFile: 'analysis-api.md', section: '21.2' },
  { module: '用户端-学情统计', method: 'GET', path: '/app/analysis/today', summary: '今日练习统计', auth: true, docFile: 'analysis-api.md', section: '21.2' },
  { module: '用户端-搜题记录', method: 'GET', path: '/app/search/records', summary: '搜题历史列表', auth: true, docFile: 'search-records-api.md', section: '21.2' },
  { module: '用户端-搜题记录', method: 'POST', path: '/app/search/records', summary: '新增搜题记录', auth: true, docFile: 'search-records-api.md', section: '21.2' },
  { module: '用户端-搜题记录', method: 'DELETE', path: '/app/search/records', summary: '删除搜题记录', auth: true, docFile: 'search-records-api.md', section: '21.2' },
  // 扩展（Swagger 可见，供 uniapp 联调）
  { module: '用户端-题集', method: 'GET', path: '/app/question/paper/list', summary: '题集列表（模拟卷等）', auth: false, section: '扩展' },
  { module: '用户端-学习计划选项', method: 'GET', path: '/app/setup/options', summary: '年级/学科/教材版本选项', auth: false, section: '扩展' },
  { module: '用户端/协议与政策', method: 'GET', path: '/app/article/{type}', summary: '协议/隐私/关于我们富文本', auth: false, section: '扩展' },
  { module: '用户端-通用配置项', method: 'GET', path: '/app/common/type', summary: 'Legacy 单项配置拉取', auth: false, section: '扩展' },
  { module: '用户端-学习统计', method: 'GET', path: '/app/user/stats', summary: '个人中心三栏统计', auth: true, section: '扩展' },
  { module: '用户端-勋章', method: 'GET', path: '/app/user/badge', summary: '练习里程碑勋章', auth: true, section: '扩展' },
  { module: '用户端-会员订单', method: 'GET', path: '/app/vipOrder', summary: '历史会员订单', auth: true, section: '扩展' },
  { module: '用户端-佣金信息', method: 'GET', path: '/app/referral/commission', summary: '佣金规则说明', auth: true, section: '扩展' },
]

export const HTTP_METHOD_OPTIONS = ['GET', 'POST', 'PUT', 'DELETE']

export const API_SECTION_OPTIONS = [
  { label: '全部章节', value: '' },
  { label: '21.1 已有 API', value: '21.1' },
  { label: '21.2 缺口 API', value: '21.2' },
  { label: '扩展接口', value: '扩展' },
]
