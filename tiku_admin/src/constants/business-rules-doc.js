/**
 * 模块二十三 · 业务规则文档（23.1～23.5）
 * 供管理端运营/客服查阅，与 uniapp、server 实现对齐
 */

/** 文档版本与更新说明 */
export const BUSINESS_RULES_DOC_META = {
  module: '模块二十三',
  title: '用户端业务规则',
  audience: '运营 / 客服',
  updatedAt: '2026-08-13',
  remark:
    '以下规则已在 uniapp 用户端与 server 服务端落地；用户咨询时请按本节说明答复，勿自行编造规则。',
}

/**
 * @typedef {{ id: string, title: string, summary: string, rules: object[], faq: object[], relatedPages?: string[] }} BusinessRuleSection
 */

/** @type {BusinessRuleSection[]} */
export const BUSINESS_RULES_SECTIONS = [
  {
    id: '23.1',
    title: '登录规则',
    summary: '用户登录须同意协议；完善年级学科前不可进首页；退出不清错题本等学习数据。',
    rules: [
      {
        key: '23.1-1',
        name: '协议必选',
        userBehavior: '登录页须勾选「用户协议与隐私政策」后才能点登录。',
        platformRule:
          '短信/微信登录请求体 agreedProtocol 须为 true；未勾选时前端 Toast「请先同意协议」，服务端返回错误码 350019。',
        csTip: '用户反馈「点登录没反应」→ 先确认是否勾选协议复选框。',
      },
      {
        key: '23.1-2',
        name: 'profile.ready 门禁',
        userBehavior: '新用户或未完成学习计划的账号，登录后进入「学习计划」页，不可直接进首页刷题。',
        platformRule:
          '登录响应含 profile.ready；false 时 uniapp 路由守卫跳转计划页；完善年级+学科后 ready=true 方可进首页。',
        csTip: '用户说「登录后进不了首页」→ 引导在计划页选择年级、学科并保存。',
      },
      {
        key: '23.1-3',
        name: '退出不清学习数据',
        userBehavior: '退出登录后，错题本、收藏、练习记录等仍保留；同一账号再次登录可恢复。',
        platformRule:
          '退出仅清除 token 等鉴权信息，不删除服务端按 userId 持久化的学习数据；也不清除本地 LEARNING_STORAGE_KEYS。',
        csTip: '用户担心「退出会丢错题」→ 告知退出不影响云端数据，换设备登录同一账号即可同步。',
      },
    ],
    faq: [
      {
        q: '用户换手机号登录，错题还在吗？',
        a: '错题本绑定 userId。换号等于新账号，旧账号数据不自动迁移；需原账号登录或联系运营处理账号合并（若有流程）。',
      },
      {
        q: '微信登录也要勾选协议吗？',
        a: '是，与短信登录一致，agreedProtocol 必选。',
      },
    ],
    relatedPages: ['协议 CMS（/business/agreement-config）', 'C端用户（/users）'],
  },
  {
    id: '23.2',
    title: '练习规则',
    summary: '做题须先选题；交卷前未答题会被引导补答；答对掌握、答错累计次数。',
    rules: [
      {
        key: '23.2-1',
        name: '必须选题才能下一题',
        userBehavior: '非最后一题时，未选择答案点「下一题」会提示「请先选择答案」，题号不变。',
        platformRule: 'canProceedToNextQuestion：非末题且无当前题答案则拦截。',
        csTip: '用户说「下一题按钮无效」→ 确认当前题是否已选 A/B/C/D。',
      },
      {
        key: '23.2-2',
        name: '末题漏答跳转首道未答题',
        userBehavior: '最后一题点「完成练习」时，若还有其他题未答，自动跳转到第一道未作答题并展开答题卡。',
        platformRule: 'resolveCompletePracticeAction：存在未答题且非当前题时 action=jump-unanswered。',
        csTip: '用户说「交卷被弹回去」→ 说明系统要求全部作答后才能交卷，属正常规则。',
      },
      {
        key: '23.2-3',
        name: '答对自动掌握',
        userBehavior: '练习中答对的题目，错题本中同题会自动标记为「已掌握」。',
        platformRule: '交卷后 markNotebookMastered 处理答对条目。',
        csTip: '用户问「为什么错题变已掌握」→ 说明在该题练习中已答对，系统自动掌握。',
      },
      {
        key: '23.2-4',
        name: '答错/未答累计 wrongCount',
        userBehavior: '答错或未作答的题目写入错题本；同一题再次答错时 wrongCount 加 1。',
        platformRule: 'syncWrongNotebook 对错题/未答题入库并递增 wrongCount。',
        csTip: 'wrongCount 反映累计错误次数，用于「高频错题」筛选（见 23.5）。',
      },
    ],
    faq: [
      {
        q: '网络不好交卷失败，错题会丢吗？',
        a: '本地有 fallback：答对/答错仍会尝试同步错题本；建议用户网络恢复后重试或查看错题本列表。',
      },
    ],
    relatedPages: ['题目管理（/business/questions）', '章节题库（/business/chapter-tree）'],
  },
  {
    id: '23.3',
    title: '会员规则',
    summary: '非会员 AI/OCR 各 5 次/日；VIP 无限；续费叠加到期日；开通当日重置次数。',
    rules: [
      {
        key: '23.3-1',
        name: '免费额度',
        userBehavior: '普通会员 AI 对话、拍照搜题各 5 次/日；跨日自动重置；用尽后弹 VIP 引导。',
        platformRule:
          'sys_config ai_daily_limit / ocr_daily_limit 默认 5；tb_user 记录 ai_used_today、ocr_used_today 与 quota_date；用尽错误码 AI 1050010、OCR 1050008。',
        csTip: '用户说「今天次数没了」→ 确认是否 VIP；非 VIP 告知次日 0 点重置，或引导开通 VIP。',
      },
      {
        key: '23.3-2',
        name: 'VIP 无限',
        userBehavior: '有效 VIP 在 AI Tab 显示「当前AI权益：无限次」，不扣减每日次数。',
        platformRule: 'is_vip=1 且未过期时 isQuotaUnlimited=true，不递增 used_today；展示等级「AI至尊会员」。',
        csTip: 'VIP 仍显示有限次数 → 查 vipExpireAt 是否已过期，或让用户重新登录刷新会员态。',
      },
      {
        key: '23.3-3',
        name: 'expireAt 计算',
        userBehavior: '开通或续费 VIP 后，到期日在「我的」页展示；未过期续费在原到期日上叠加套餐天数。',
        platformRule: 'MemberRulesSupport.calculateVipExpireAt：未过期续费 expireAt+planDays，否则 now+planDays。',
        csTip: '用户质疑到期日 → 管理端可查用户 VIP 到期字段，对照支付时间与套餐天数说明。',
      },
      {
        key: '23.3-4',
        name: '开通重置次数',
        userBehavior: '支付成功开通/续费 VIP 后，当日 AI/OCR 已用次数重置为 0。',
        platformRule: '支付回调 VipPayNotifyServiceImpl.activateVip 将 ai/ocr_used_today 置 0，quota_date 置今日。',
        csTip: '用户刚开通仍提示次数用尽 → 建议退出重新登录或下拉刷新会员信息。',
      },
    ],
    faq: [
      {
        q: 'VIP 过期后会怎样？',
        a: '到期后降级为普通会员，恢复每日 5 次限制；权益对比见 VIP 权益配置页。',
      },
      {
        q: '次数配置能改吗？',
        a: '管理端「AI 配置 / OCR 配置 / 会员配置」可调整 sys_config 限额，保存后 C 端读取新值。',
      },
    ],
    relatedPages: [
      '会员配置（/business/vip-config）',
      'VIP套餐（/business/vip-plans）',
      'VIP权益（/business/vip-rights）',
      'AI 配置（/business/ai-config）',
      'OCR 配置（/business/ocr-config）',
    ],
  },
  {
    id: '23.4',
    title: '推广提现规则',
    summary: '提现金额 50 元整数倍、单笔 50～5000 元；渠道为微信零钱，T+1 到账文案。',
    rules: [
      {
        key: '23.4-1',
        name: '金额整数倍与区间',
        userBehavior:
          '推广中心提现页：输入 49 元按钮禁用并提示最低 50 元；5001 元提示超限；须为 50 的整数倍（50、100、150…）。',
        platformRule:
          'validateWithdrawAmount：WITHDRAW_MIN=50、MAX=5000、STEP=50；不得超过可提现余额 withdrawableBalance。',
        csTip: '用户无法提现 → 核对金额是否为 50 倍数、是否在 50～5000、余额是否足够。',
      },
      {
        key: '23.4-2',
        name: '微信零钱 T+1',
        userBehavior: '提现渠道展示「微信零钱」，说明 T+1 到账（下一个工作日）。',
        platformRule: 'uniapp withdraw.vue 文案；管理端审核通过后按渠道打款，拒绝则余额退回。',
        csTip: '用户问「多久到账」→ 审核通过后 T+1 微信零钱；可在「提现审核」查状态。',
      },
      {
        key: '23.4-3',
        name: '实名与审核',
        userBehavior: '未实名不可申请提现；提交后进入待审核，通过/拒绝由管理端操作。',
        platformRule: 'server 校验 real_name_verified；拒绝后钱包余额自动退回（见推广佣金规则页说明）。',
        csTip: '提现失败提示实名 → 引导用户在 uniapp 完成实名绑定后再申请。',
      },
    ],
    faq: [
      {
        q: '手续费怎么算？',
        a: '实际到账 = 申请金额 × (1 - 手续费率)，费率在「推广佣金规则」配置；差额记 serviceFee。',
      },
      {
        q: '审核拒绝钱去哪了？',
        a: '申请时已扣减可提现余额；拒绝后自动退回，可在用户钱包/流水核对。',
      },
    ],
    relatedPages: [
      '推广佣金规则（/business/promo-config）',
      '提现审核（/business/promo-withdraw）',
      '用户钱包（/business/wallets）',
    ],
  },
  {
    id: '23.5',
    title: '错题本规则',
    summary: 'wrongCount≥2 为高频错题；已掌握不参与错题练习；清空只删当前筛选结果。',
    rules: [
      {
        key: '23.5-1',
        name: '高频错题',
        userBehavior: '错题本「高频错题」Chip 仅显示 wrongCount≥2 且未掌握的题目。',
        platformRule: 'isNotebookHighFreqItem：未掌握且 wrongCount >= NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT(2)。',
        csTip: '用户问「为什么没有高频」→ 需同一题至少错 2 次且未标记已掌握。',
      },
      {
        key: '23.5-2',
        name: '已掌握排除 wrong 模式',
        userBehavior: '标记「已掌握」的题不会出现在错题重做（wrong 模式）题目列表中。',
        platformRule: 'filterNotebookForWrongMode 排除 mastered=true；练习答对也会自动掌握（见 23.2-3）。',
        csTip: '用户找不到某道错题练习 → 可能已掌握，可在「已掌握」筛选或全部列表查看。',
      },
      {
        key: '23.5-3',
        name: '清空仅当前筛选',
        userBehavior: '点击「清空」只删除当前 Chip 筛选命中的错题，其他筛选/未展示条目保留。',
        platformRule: 'resolveClearNotebookFilteredList 按当前筛选业务键删除，不全量清空错题本。',
        csTip: '用户误操作清空 → 说明只清了当前 Tab/筛选，切换「全部」可确认是否还有剩余错题。',
      },
    ],
    faq: [
      {
        q: '错题本和退出登录有关系吗？',
        a: '无。退出不清学习数据（23.1-3），错题本按 userId 持久化。',
      },
    ],
    relatedPages: ['C端用户（/users）'],
  },
]
