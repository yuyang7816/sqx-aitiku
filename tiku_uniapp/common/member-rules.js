/**
 * 模块二十三 · 会员规则（文档 23.3，与 server MemberRulesSupport 对齐）
 * - 免费额度：AI/OCR 各 5 次/日（sys_config 默认）
 * - VIP 无限：isVip 时不扣减
 * - 跨日重置：quotaDate 非今日视为 0 次
 * - expireAt：未过期续费叠加，否则从当前时间起算
 * - 开通重置：当日 ai/ocr used 置 0，quotaDate 置今日
 */

/** 服务端 sys_config 默认每日次数（仅文档/联调参考，前端不当作未同步时的展示值） */
export const DEFAULT_DAILY_LIMIT = 5;

/** 解析每日上限；未从 API 同步时为 0 */
export const resolveDailyLimit = (value) => {
	const n = Number(value);
	return Number.isFinite(n) && n > 0 ? n : 0;
};

/** 配额上限是否已从服务端同步 */
export const isDailyLimitReady = (value) => resolveDailyLimit(value) > 0;

/** VIP 展示等级 */
export const VIP_LEVEL_LABEL = 'AI至尊会员';

/** 非 VIP 展示等级 */
export const NORMAL_LEVEL_LABEL = '普通会员';

/** 服务端 AI 次数用尽业务码 */
export const AI_QUOTA_EXHAUSTED_CODE = 1050010;

/** 服务端 OCR 次数用尽业务码 */
export const OCR_QUOTA_EXHAUSTED_CODE = 1050008;

export const getTodayString = (date = new Date()) => (
	`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
);

const parseDate = (value) => {
	if (!value) {
		return null;
	}
	const normalized = new Date(String(value).replace(/-/g, '/'));
	return Number.isNaN(normalized.getTime()) ? null : normalized;
};

/** yyyy-MM-dd 格式化（expireAt 展示与 Storage） */
export const formatDateOnly = (date = new Date()) => getTodayString(date);

/**
 * 跨日重置：配额日期不是 today 则视为 0 次（规则 23.3-1）
 */
export const resolveUsedToday = (quotaDate, usedToday, today = getTodayString()) => {
	const quotaDay = String(quotaDate || '').trim().slice(0, 10);
	if (!today || !quotaDay || quotaDay !== today) {
		return 0;
	}
	return Number(usedToday) || 0;
};

/** 规则 23.3-2：VIP AI/OCR 无限次 */
export const isQuotaUnlimited = (isVip) => isVip === true;

/** 规则 23.3-2：会员等级文案 */
export const resolveMemberLevel = (isVip) => (
	isVip ? VIP_LEVEL_LABEL : NORMAL_LEVEL_LABEL
);

export const resolveAiUsedToday = (member = {}, today = getTodayString()) => (
	resolveUsedToday(member.aiQuotaDate, member.aiUsedToday, today)
);

export const resolveOcrUsedToday = (member = {}, today = getTodayString()) => (
	resolveUsedToday(member.ocrQuotaDate, member.ocrUsedToday, today)
);

/**
 * 读取会员态时应用跨日有效已用次数（不修改 quotaDate，首次本地扣减再写入）
 */
export const resolveMemberQuotaState = (state = {}, today = getTodayString()) => ({
	...state,
	aiUsedToday: resolveAiUsedToday(state, today),
	ocrUsedToday: resolveOcrUsedToday(state, today)
});

/** 非会员是否仍可消耗 AI 配额 */
export const canConsumeAiQuota = (member = {}, today = getTodayString()) => {
	if (isQuotaUnlimited(member.isVip)) {
		return true;
	}
	const dailyLimit = resolveDailyLimit(member.aiDailyLimit);
	if (!dailyLimit) {
		return false;
	}
	return resolveAiUsedToday(member, today) < dailyLimit;
};

/** 非会员是否仍可消耗 OCR 配额 */
export const canConsumeOcrQuota = (member = {}, today = getTodayString()) => {
	if (isQuotaUnlimited(member.isVip)) {
		return true;
	}
	const dailyLimit = resolveDailyLimit(member.ocrDailyLimit);
	if (!dailyLimit) {
		return false;
	}
	return resolveOcrUsedToday(member, today) < dailyLimit;
};

/**
 * 规则 23.3-3：开通/续费 expireAt
 * @param {string} currentExpireAt - 当前 expireAt yyyy-MM-dd
 * @param {number} planDays - 套餐天数
 * @param {Date} [now]
 */
export const calculateVipExpireAt = (currentExpireAt = '', planDays = 31, now = new Date()) => {
	const days = Math.max(Number(planDays) || 0, 0);
	const baseExpire = parseDate(String(currentExpireAt || '').trim());
	const startAt = (baseExpire && baseExpire.getTime() > now.getTime()) ? baseExpire : now;
	const result = new Date(startAt.getTime());
	result.setDate(result.getDate() + days);
	return result;
};

/** 规则 23.3-4：开通/续费当日重置 AI/OCR 已用次数 */
export const buildActivationQuotaReset = (today = getTodayString()) => ({
	aiUsedToday: 0,
	ocrUsedToday: 0,
	aiQuotaDate: today,
	ocrQuotaDate: today
});

/** 非 VIP 剩余次数；VIP 返回「无限」 */
export const getRemainingAiTimesFromMember = (member = {}, today = getTodayString()) => {
	if (isQuotaUnlimited(member.isVip)) {
		return '无限';
	}
	const dailyLimit = resolveDailyLimit(member.aiDailyLimit);
	if (!dailyLimit) {
		return 0;
	}
	return Math.max(dailyLimit - resolveAiUsedToday(member, today), 0);
};

export const getRemainingOcrTimesFromMember = (member = {}, today = getTodayString()) => {
	if (isQuotaUnlimited(member.isVip)) {
		return '无限';
	}
	const dailyLimit = resolveDailyLimit(member.ocrDailyLimit);
	if (!dailyLimit) {
		return 0;
	}
	return Math.max(dailyLimit - resolveOcrUsedToday(member, today), 0);
};

/** AI Tab 等非 VIP 展示：剩余/上限 */
export const formatQuotaDisplay = (member = {}, type = 'ai', today = getTodayString()) => {
	if (isQuotaUnlimited(member.isVip)) {
		return '无限';
	}
	const dailyLimit = type === 'ocr'
		? resolveDailyLimit(member.ocrDailyLimit)
		: resolveDailyLimit(member.aiDailyLimit);
	if (!dailyLimit) {
		return '—';
	}
	const usedToday = type === 'ocr'
		? resolveOcrUsedToday(member, today)
		: resolveAiUsedToday(member, today);
	const remaining = Math.max(dailyLimit - usedToday, 0);
	return `${remaining}/${dailyLimit}`;
};

/** checkAiChatQuota / checkOcrSearchQuota 统一返回结构 */
export const buildQuotaCheckResult = (member = {}, type = 'ai', today = getTodayString()) => {
	if (isQuotaUnlimited(member.isVip)) {
		return { ok: true, remaining: '无限' };
	}
	const dailyLimit = type === 'ocr'
		? resolveDailyLimit(member.ocrDailyLimit)
		: resolveDailyLimit(member.aiDailyLimit);
	if (!dailyLimit) {
		return { ok: false, remaining: 0, notReady: true };
	}
	const remaining = type === 'ocr'
		? getRemainingOcrTimesFromMember(member, today)
		: getRemainingAiTimesFromMember(member, today);
	if (remaining <= 0) {
		return { ok: false, remaining: 0 };
	}
	return { ok: true, remaining };
};

/** 本地扣减 AI 一次（离线 fallback；在线以 POST /app/ai/chat quota 为准） */
export const buildAiConsumePatch = (member = {}, today = getTodayString()) => {
	const effectiveUsed = resolveAiUsedToday(member, today);
	const dailyLimit = resolveDailyLimit(member.aiDailyLimit);
	const nextUsed = effectiveUsed + 1;
	return {
		aiUsedToday: nextUsed,
		aiQuotaDate: today,
		remaining: dailyLimit ? Math.max(dailyLimit - nextUsed, 0) : 0
	};
};

/** 服务端 quota 快照合并补丁 */
export const buildAiQuotaSyncPatch = (quota = {}, member = {}, today = getTodayString()) => {
	const patch = {};
	if (quota.aiDailyLimit != null) {
		patch.aiDailyLimit = resolveDailyLimit(quota.aiDailyLimit);
	}
	if (quota.aiUsedToday != null) {
		patch.aiUsedToday = Number(quota.aiUsedToday) || 0;
		patch.aiQuotaDate = today;
	}
	if (quota.aiUnlimited === true) {
		patch.isVip = true;
		patch.level = VIP_LEVEL_LABEL;
	}
	return patch;
};

export const buildOcrQuotaSyncPatch = (quota = {}, member = {}, today = getTodayString()) => {
	const patch = {};
	if (quota.ocrDailyLimit != null) {
		patch.ocrDailyLimit = resolveDailyLimit(quota.ocrDailyLimit);
	}
	if (quota.ocrUsedToday != null) {
		patch.ocrUsedToday = Number(quota.ocrUsedToday) || 0;
		patch.ocrQuotaDate = today;
	}
	if (quota.ocrUnlimited === true) {
		patch.isVip = true;
		patch.level = VIP_LEVEL_LABEL;
	}
	return patch;
};

/** 次数用尽时本地对齐为「已用=上限」 */
export const buildAiQuotaExhaustedPatch = (member = {}, today = getTodayString()) => {
	const dailyLimit = resolveDailyLimit(member.aiDailyLimit);
	const patch = { aiQuotaDate: today };
	if (dailyLimit) {
		patch.aiDailyLimit = dailyLimit;
		patch.aiUsedToday = dailyLimit;
	} else {
		patch.aiUsedToday = Math.max(resolveAiUsedToday(member, today), 1);
	}
	return patch;
};

export const buildOcrQuotaExhaustedPatch = (member = {}, today = getTodayString()) => {
	const dailyLimit = resolveDailyLimit(member.ocrDailyLimit);
	const patch = { ocrQuotaDate: today };
	if (dailyLimit) {
		patch.ocrDailyLimit = dailyLimit;
		patch.ocrUsedToday = dailyLimit;
	} else {
		patch.ocrUsedToday = Math.max(resolveOcrUsedToday(member, today), 1);
	}
	return patch;
};
