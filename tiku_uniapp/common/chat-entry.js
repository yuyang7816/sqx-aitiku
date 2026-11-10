/**
 * AI 对话页入口参数：解析 URL query、构建跳转链接、生成初始上下文。
 */

const CHAT_PAGE_PATH = '/package/ai/chat';

/** 安全解码 URL 参数（兼容已解码或未编码场景） */
export const safeDecodeQueryParam = (value = '') => {
	const raw = String(value || '').trim();
	if (!raw) {
		return '';
	}
	try {
		return decodeURIComponent(raw.replace(/\+/g, ' '));
	} catch (error) {
		return raw;
	}
};

/** 从 onLoad options 解析对话页入口参数 */
export const parseChatEntryOptions = (options = {}) => ({
	topic: safeDecodeQueryParam(options.topic),
	tip: safeDecodeQueryParam(options.tip)
});

/** 构建跳转 chat 页 URL */
export const buildChatPageUrl = ({ topic = '', tip = '' } = {}) => {
	const query = [];
	const topicText = String(topic || '').trim();
	const tipText = String(tip || '').trim();
	if (topicText) {
		query.push(`topic=${encodeURIComponent(topicText)}`);
	}
	if (tipText) {
		query.push(`tip=${encodeURIComponent(tipText)}`);
	}
	return query.length ? `${CHAT_PAGE_PATH}?${query.join('&')}` : CHAT_PAGE_PATH;
};

/** 错题本选项答案展示：优先按 label 匹配，fallback 原值 */
const formatNotebookOptionAnswer = (value = '', options = []) => {
	const key = String(value || '').trim();
	if (!key) {
		return '—';
	}
	const list = Array.isArray(options) ? options : [];
	const byLabel = list.find(
		(item) => String(item.label || '').trim().toUpperCase() === key.toUpperCase()
	);
	if (byLabel) {
		return `${byLabel.label}. ${byLabel.value}`;
	}
	const byValue = list.find((item) => String(item.value || '').trim() === key);
	if (byValue) {
		return `${byValue.label}. ${byValue.value}`;
	}
	return key;
};

/** 错题本「AI 讲解」入口：生成用户追问 tip（含学科与错误答案） */
export const buildNotebookChatTip = (item = {}) => {
	const subject = String(item.subject || '通用题目').trim() || '通用题目';
	const wrongText = formatNotebookOptionAnswer(item.wrong || item.myAnswer, item.options);
	return `我在 ${subject} 这道题上做错了，错误答案：${wrongText}，请帮我重新讲一遍。`;
};

/** 错题本「AI 讲解」入口：topic 题干 + tip 错题追问文案 */
export const buildNotebookChatEntry = (item = {}) => {
	const topic = String(item.question || item.title || '').trim();
	const tip = buildNotebookChatTip(item);
	return { topic, tip };
};

/** 收藏「AI 讲解」入口：tip 优先 aiSummary，fallback 解析摘要 */
export const buildCollectionChatTip = (item = {}) => {
	const aiSummary = String(item.aiSummary || '').trim();
	const analysis = String(item.analysis || '').trim();
	if (aiSummary && aiSummary !== '暂无 AI 讲解') {
		return aiSummary;
	}
	if (analysis && analysis !== '暂无解析' && analysis !== '暂无详细解析') {
		return analysis;
	}
	return '这道题目暂时没有解析摘要';
};

/** 收藏「AI 讲解」入口：topic 题干 + tip AI 摘要/解析 */
export const buildCollectionChatEntry = (item = {}) => {
	const topic = String(item.title || item.question || '').trim();
	const tip = buildCollectionChatTip(item);
	return { topic, tip };
};

/** 薄弱知识点「AI 讲解」入口：生成用户追问 tip（含薄弱点标题与正确率） */
export const buildWeakPointChatTip = (weakPoint = {}) => {
	const title = String(weakPoint.title || '').trim() || '未命名知识点';
	const percent = String(weakPoint.percent || '').trim()
		|| (weakPoint.correctRate != null ? `${weakPoint.correctRate}%` : '');
	const rateText = percent || '偏低';
	return `我的薄弱知识点是「${title}」，正确率 ${rateText}，请结合我的薄弱点，帮我从基础概念到典型例题重新讲一遍。`;
};

/** 薄弱知识点「AI 讲解」入口：topic 薄弱点标题 + tip 追问文案 */
export const buildWeakPointChatEntry = (weakPoint = {}) => {
	const topic = String(weakPoint.title || '').trim();
	const tip = buildWeakPointChatTip(weakPoint);
	return { topic, tip };
};

/** 月度报告「让 AI 拆计划」入口：生成用户追问 tip（含报告月份标题） */
export const buildReportChatTip = (report = {}) => {
	const monthTitle = String(report.monthTitle || '').trim();
	const prefix = monthTitle ? `我的${monthTitle}` : '我的本月学习报告';
	return `${prefix}，请根据报告里的 AI 学习建议，帮我拆成今晚 30 分钟可执行的复习计划。`;
};

/** 月度报告「让 AI 拆计划」入口：topic 报告标题 + tip 拆计划追问文案 */
export const buildReportChatEntry = (report = {}) => {
	const topic = String(report.monthTitle || '').trim() || '本月学习报告';
	const tip = buildReportChatTip(report);
	return { topic, tip };
};

/** tip 是否为用户发起的追问文案（如错题本、学情报告入口） */
export const isUserIntentTip = (tip = '') => {
	const text = String(tip || '').trim();
	if (!text) {
		return false;
	}
	return /请帮我|我做错|错误答案|帮我重新|帮我拆成|帮我从基础/.test(text);
};

/**
 * 根据 topic/tip 生成初始对话上下文（题目卡片 + 首条用户消息）。
 * 无 topic/tip 时为空白会话（emptyEntry），由用户自行输入后再请求 API。
 */
export const buildChatInitialContext = ({ topic = '', tip = '' } = {}) => {
	const topicText = String(topic || '').trim();
	const tipText = String(tip || '').trim();

	if (!topicText && !tipText) {
		return {
			topic: '',
			tip: '',
			userMessage: '',
			draft: '',
			emptyEntry: true
		};
	}

	if (isUserIntentTip(tipText)) {
		return {
			topic: topicText,
			tip: tipText,
			userMessage: tipText,
			draft: topicText ? '这道题的解题思路能再详细一点吗' : '',
			emptyEntry: false
		};
	}

	const userMessage = topicText
		? `请帮我讲解这道题目：${topicText}`
		: '请帮我讲解一下这道题';

	return {
		topic: topicText,
		tip: tipText,
		userMessage,
		draft: topicText ? '这道题的解题思路能再详细一点吗' : '',
		emptyEntry: false
	};
};

export { CHAT_PAGE_PATH };
