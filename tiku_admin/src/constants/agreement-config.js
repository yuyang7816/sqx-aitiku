/** 协议与政策配置分组（对应 sys_config.param_type） */
export const ARTICLE_PARAM_TYPE = 'app_article'
export const ARTICLE_PARAM_TYPE_DESC = '协议与政策'

/** 移动端 article 页路由前缀 */
export const UNIAPP_ARTICLE_ROUTE = '/package/common/article?type='

/**
 * 文章类型定义（用户协议 / 隐私政策 / 关于我们）
 * @type {Array<{ type: string, label: string, titleKey: string, subKey: string, contentKey: string, defaultTitle: string, defaultSub: string, defaultContent: string }>}
 */
export const ARTICLE_TYPES = [
  {
    type: 'agreement',
    label: '用户协议',
    titleKey: 'app_article_agreement_title',
    subKey: 'app_article_agreement_sub',
    contentKey: 'app_article_agreement_content',
    defaultTitle: '用户协议',
    defaultSub: '请在使用平台服务前仔细阅读以下协议内容。',
    defaultContent:
      '<h3>账号使用</h3><p>用户应确保登录信息真实有效，不得转借、出租或共享会员权益给他人。</p>' +
      '<h3>服务说明</h3><p>平台提供的题目解析与 AI 建议仅作为辅助学习参考，具体学习安排需结合个人实际情况。</p>' +
      '<h3>行为规范</h3><p>禁止利用平台从事违法违规活动，包括但不限于批量抓取内容、恶意攻击和传播不当信息。</p>',
  },
  {
    type: 'privacy',
    label: '隐私政策',
    titleKey: 'app_article_privacy_title',
    subKey: 'app_article_privacy_sub',
    contentKey: 'app_article_privacy_content',
    defaultTitle: '隐私政策',
    defaultSub: '我们重视你的个人信息安全，并尽量以最小化方式处理必要数据。',
    defaultContent:
      '<h3>信息收集</h3><p>为实现登录、学习记录、收藏和消息提醒等功能，平台会收集必要的账户和使用数据。</p>' +
      '<h3>信息使用</h3><p>收集的数据仅用于学习服务优化、功能实现和必要的运营通知，不会超范围使用。</p>' +
      '<h3>信息保护</h3><p>我们会采取合理的技术和管理措施保护数据安全，并支持用户查看、更新和删除相关信息。</p>',
  },
  {
    type: 'about',
    label: '关于我们',
    titleKey: 'app_article_about_title',
    subKey: 'app_article_about_sub',
    contentKey: 'app_article_about_content',
    defaultTitle: '关于我们',
    defaultSub: 'AI 题库，专注中小学智能刷题与错题提升的学习助手。',
    defaultContent:
      '<h3>产品理念</h3><p>我们相信 AI 不仅是工具，更是懂你学习节奏、陪你稳步提升的智能伙伴。</p>' +
      '<h3>核心能力</h3><p>拍照搜题、智能答疑、错题整理、学情分析、周计划与推广激励，覆盖学习全场景。</p>' +
      '<h3>服务承诺</h3><p>持续更新题库与解析质量，保护用户学习数据安全，提供稳定流畅的使用体验。</p>' +
      '<h3>联系我们</h3><p>如有任何问题或建议，欢迎通过「帮助与反馈」或 AI 在线客服随时联系。</p>' +
      '<p><strong>当前版本</strong>：1.0.0</p>',
  },
]

/**
 * 扁平化 sys_config 配置项定义（供读写 API 使用）
 */
export const ARTICLE_CONFIG_DEFS = ARTICLE_TYPES.flatMap((article) => [
  {
    key: article.titleKey,
    articleType: article.type,
    field: 'title',
    label: `${article.label}标题`,
    remark: `${article.label}页导航栏与正文标题`,
    defaultValue: article.defaultTitle,
  },
  {
    key: article.subKey,
    articleType: article.type,
    field: 'sub',
    label: `${article.label}副标题`,
    remark: `${article.label}页顶部说明文案`,
    defaultValue: article.defaultSub,
  },
  {
    key: article.contentKey,
    articleType: article.type,
    field: 'content',
    label: `${article.label}正文`,
    remark: `${article.label}富文本 HTML 正文，uniapp article 页渲染`,
    defaultValue: article.defaultContent,
  },
])

/** 移动端按 type 拉取文章（GET /app/article/{type}） */
export const APP_ARTICLE_API = 'GET /app/article/{type}'
