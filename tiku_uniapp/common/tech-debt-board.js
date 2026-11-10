/**
 * 模块二十二 P2#16~24 · 体验增强 tech-debt 看板（唯一权威排期与现状对照）
 * 临时方案须在代码以 TODO(tech-debt:P2#xx) 引用本看板条目 id。
 * 验收：看板含 P2#16~24 共 9 项，可用 npm run audit:tech-debt 校验。
 */

/** 看板版本（排期变更时递增） */
export const TECH_DEBT_BOARD_VERSION = '2026-08-13';

/** 看板列：待排期 → 已排期 → 进行中 → 部分交付 → 已交付 */
export const TECH_DEBT_COLUMNS = {
	backlog: '待排期',
	scheduled: '已排期',
	inProgress: '进行中',
	partial: '部分交付',
	done: '已交付'
};

/**
 * P2 体验增强 tech-debt 条目（文档 22 · P2#16~24）
 * @typedef {Object} TechDebtItem
 * @property {string} id - 如 P2#16
 * @property {string} title - 业务名称
 * @property {string} module - 所属模块
 * @property {keyof TECH_DEBT_COLUMNS} column - 看板列
 * @property {{ phase: string, targetSprint: string, owner?: string, dependsOn?: string[] }} schedule
 * @property {string} currentWorkaround - 当前临时方案
 * @property {string} targetState - 目标完整态
 * @property {string[]} relatedFiles - 关联源码路径（相对 tiku_uniapp/）
 * @property {string} [acceptanceRef] - 关联已完成/待办 taskId
 */

/** @type {TechDebtItem[]} */
export const TECH_DEBT_ITEMS = [
	{
		id: 'P2#16',
		title: '试卷中心/模考',
		module: '刷题',
		column: 'done',
		schedule: {
			phase: '模块四 · 试卷中心独立页',
			targetSprint: '2026-Q3-S1',
			owner: 'uniapp',
			dependsOn: ['GET /app/question/paper/list 已就绪（common/paper-list.js）']
		},
		currentWorkaround: '—',
		targetState: '独立试卷列表/模考入口页，按 type=special|exam 展示题集并进入答题',
		relatedFiles: ['common/routes.js', 'common/paper-list.js', 'package/practice/paper-center.vue', 'pages/index/index.vue'],
		acceptanceRef: 'm04-f09-shortcut-paper-uniapp'
	},
	{
		id: 'P2#17',
		title: '学习日历打卡',
		module: '首页',
		column: 'done',
		schedule: {
			phase: '模块四 · 学习日历独立页',
			targetSprint: '2026-Q3-S2',
			owner: 'uniapp+server',
			dependsOn: ['打卡记录 API、月历 UI 分包页']
		},
		currentWorkaround: '—',
		targetState: '专用学习日历页：打卡状态、连续天数、日程提醒联动',
		relatedFiles: ['common/study-calendar.js', 'package/analysis/calendar.vue', 'pages/index/index.vue'],
		acceptanceRef: 'm04-f13-shortcut-calendar-uniapp'
	},
	{
		id: 'P2#18',
		title: '关键词搜索',
		module: '首页',
		column: 'done',
		schedule: {
			phase: '模块七 · 首页题库搜索',
			targetSprint: '2026-Q3-S2',
			owner: 'uniapp+server',
			dependsOn: ['GET /app/search/questions 或等价题库检索 API']
		},
		currentWorkaround: '—',
		targetState: '首页搜索直达关键词结果列表（题目/知识点/试卷），支持历史记录云端同步',
		relatedFiles: ['pages/index/index.vue', 'common/question-search.js', 'package/question/search.vue'],
		acceptanceRef: 'm07-fxx-keyword-search-uniapp'
	},
	{
		id: 'P2#19',
		title: '海报真实生成',
		module: '分享',
		column: 'done',
		schedule: {
			phase: '模块十二/学情 · 海报服务端渲染或稳定小程序码',
			targetSprint: '2026-Q3-S3',
			owner: 'uniapp+server',
			dependsOn: ['wx 小程序码接口、报告/推广数据全量来自 API']
		},
		currentWorkaround: '—',
		targetState: '报告与推广海报数据 100% 来自服务端，小程序码/分享图稳定可追踪',
		relatedFiles: ['common/report-poster.js', 'common/promo-poster.js', 'package/common/poster.vue'],
		acceptanceRef: 'm12-fxx-poster-uniapp'
	},
	{
		id: 'P2#20',
		title: '视频解析页',
		module: '会员权益',
		column: 'done',
		schedule: {
			phase: '模块十一 · 会员视频讲解',
			targetSprint: '2026-Q3-S3',
			owner: 'uniapp+server',
			dependsOn: ['题目讲解视频 CDN/API，按 questionCode 下发']
		},
		currentWorkaround: '—',
		targetState: '会员题目解析页「视频讲解」对接服务端视频 URL，无 mock 回退',
		relatedFiles: ['common/video-analysis.js', 'common/question-video-api.js', 'package/question/video-analysis.vue'],
		acceptanceRef: 'm11-fxx-video-analysis-uniapp'
	},
	{
		id: 'P2#21',
		title: '多题型支持',
		module: '答题',
		column: 'done',
		schedule: {
			phase: '模块六 · 多题型扩展',
			targetSprint: '2026-Q2（已交付）',
			owner: 'uniapp',
			dependsOn: []
		},
		currentWorkaround: '—',
		targetState: '答题页按题型切换单选/多选/填空/判断，统一判分与交卷',
		relatedFiles: ['common/question-answer.js', 'package/question/answer.vue'],
		acceptanceRef: 'm06-f18-multi-type-uniapp'
	},
	{
		id: 'P2#22',
		title: '订阅消息推送',
		module: '设置',
		column: 'done',
		schedule: {
			phase: '模块十四 · 订阅消息全链路',
			targetSprint: '2026-Q3-S4',
			owner: 'admin+uniapp+server',
			dependsOn: ['管理端配置 wx_ma_subscribe_* 模板 ID', '服务端定时推送任务']
		},
		currentWorkaround: '—',
		targetState: '模板 ID 管理端可配，用户授权后可收到月报/VIP到期/错题复习推送',
		relatedFiles: ['common/subscribe-message.js', 'package/profile/settings.vue'],
		acceptanceRef: 'm14-fxx-subscribe-uniapp'
	},
	{
		id: 'P2#23',
		title: '勋章系统',
		module: '我的',
		column: 'done',
		schedule: {
			phase: '模块十三 · 勋章展示',
			targetSprint: '2026-Q2（已交付）',
			owner: 'uniapp+server',
			dependsOn: []
		},
		currentWorkaround: '—',
		targetState: '我的页统计与勋章墙对接 GET /app/user/stats、GET /app/user/badges',
		relatedFiles: ['common/user-badge-api.js', 'common/user-stats.js', 'pages/my/my.vue'],
		acceptanceRef: 'm13-f21-badge-uniapp'
	},
	{
		id: 'P2#24',
		title: '学习统计真实化',
		module: '学情/我的',
		column: 'done',
		schedule: {
			phase: '模块十三 · 全站统计口径统一',
			targetSprint: '2026-Q3-S4',
			owner: 'uniapp+server',
			dependsOn: ['首页三栏与学情概览全部走服务端统计，移除 mock/homeStudyStats']
		},
		currentWorkaround: '—',
		targetState: '首页/学情/我的统计字段与服务端一致，无硬编码 mock 统计',
		relatedFiles: ['common/user-stats.js', 'common/analysis-today.js', 'common/analysis-overview.js', 'common/app-data/impl.js', 'pages/index/index.vue'],
		acceptanceRef: 'm13-f03-stats-uniapp'
	}
];

/** P2 条目 id 清单（验收：P2#16~24） */
export const TECH_DEBT_P2_IDS = TECH_DEBT_ITEMS.map((item) => item.id);

/** 按 id 查条目 */
export const getTechDebtItem = (id = '') =>
	TECH_DEBT_ITEMS.find((item) => item.id === String(id).trim()) || null;

/** 按看板列分组 */
export const listTechDebtByColumn = (column = '') => {
	const key = String(column).trim();
	if (!key) {
		return [...TECH_DEBT_ITEMS];
	}
	return TECH_DEBT_ITEMS.filter((item) => item.column === key);
};

/** 看板摘要（排期一览） */
export const getTechDebtBoardSummary = () => {
	const byColumn = {};
	Object.keys(TECH_DEBT_COLUMNS).forEach((key) => {
		byColumn[key] = listTechDebtByColumn(key).map((item) => ({
			id: item.id,
			title: item.title,
			targetSprint: item.schedule.targetSprint
		}));
	});
	return {
		version: TECH_DEBT_BOARD_VERSION,
		total: TECH_DEBT_ITEMS.length,
		p2Range: 'P2#16~P2#24',
		byColumn
	};
};
