/**
 * 模块二十四 · AI 次数用尽弹窗引导会员联调校验（文档 24.3）
 * 运行：node scripts/verify-integration-ai-limit.mjs
 */
import fs from 'fs';
import path from 'path';
import {
	AI_QUOTA_EXHAUSTED_CODE,
	DEFAULT_DAILY_LIMIT,
	buildAiQuotaExhaustedPatch,
	buildQuotaCheckResult,
	getTodayString
} from '../common/member-rules.js';

const root = path.resolve('.');

const read = (rel) => fs.readFileSync(path.join(root, rel), 'utf8');

const checks = [];

const assert = (name, ok, detail = '') => {
	checks.push({ name, ok, detail });
};

const assertIncludes = (name, file, patterns) => {
	const src = read(file);
	const missing = patterns.filter((p) => !src.includes(p));
	if (missing.length) {
		assert(name, false, `missing in ${file}: ${missing.join(', ')}`);
		return;
	}
	assert(name, true, file);
};

// --- 与 impl.js / chat-api.js 对齐的轻量复刻 ---

const MEMBER_CENTER_URL = '/package/member/center';

/** 对齐 showAiQuotaExhaustedModal 弹窗配置 */
const buildAiQuotaExhaustedModalConfig = (member = {}) => {
	const limit = Number(member.aiDailyLimit) || 0;
	const limitText = limit > 0 ? `免费用户每日 ${limit} 次 AI 讲解` : '免费用户每日 AI 讲解次数有限';
	return {
		title: '今日 AI 次数已用尽',
		content: `${limitText}，开通会员后可无限次答疑与搜题。`,
		confirmText: '开通会员',
		cancelText: '知道了',
		memberCenterUrl: MEMBER_CENTER_URL
	};
};

/** 对齐入口页：余量为 0 时不跳转 chat（配额未同步时不拦截） */
const simulateAiEntryGuard = (member = {}, today = getTodayString()) => {
	const quota = buildQuotaCheckResult(member, 'ai', today);
	if (!quota.ok && !quota.notReady) {
		return { navigated: false, showModal: true, quota };
	}
	return { navigated: true, showModal: false, quota };
};

/** 对齐 chat-api 捕获 1050010 后本地 quota 对齐 */
const simulateServerAiQuotaExhausted = (member = {}, today = getTodayString()) => {
	const patch = buildAiQuotaExhaustedPatch(member, today);
	return { ...member, ...patch };
};

// TC-M24-014 · 24.3：AI 次数用尽弹窗引导会员

const today = '2026-08-13';
const exhaustedMember = {
	isVip: false,
	aiDailyLimit: 5,
	aiUsedToday: 5,
	aiQuotaDate: today
};

assert(
	'24.3-1: AI quota exhausted code is 1050010',
	AI_QUOTA_EXHAUSTED_CODE === 1050010
);

assert(
	'24.3-2: checkAiChatQuota returns ok:false when aiUsedToday >= aiDailyLimit',
	(() => {
		const quota = buildQuotaCheckResult(exhaustedMember, 'ai', today);
		return quota.ok === false && quota.remaining === 0;
	})()
);

assert(
	'24.3-3: VIP member never blocked by local AI quota',
	buildQuotaCheckResult({ isVip: true, aiUsedToday: 99 }, 'ai', today).ok === true
);

assert(
	'24.3-4: buildAiQuotaExhaustedPatch aligns used to daily limit',
	(() => {
		const patch = buildAiQuotaExhaustedPatch({ aiDailyLimit: 5 }, today);
		return patch.aiUsedToday === 5 && patch.aiDailyLimit === 5 && patch.aiQuotaDate === today;
	})()
);

assert(
	'24.3-5: modal config title/confirmText and member center URL',
	(() => {
		const modal = buildAiQuotaExhaustedModalConfig(exhaustedMember);
		return modal.title === '今日 AI 次数已用尽'
			&& modal.confirmText === '开通会员'
			&& modal.cancelText === '知道了'
			&& modal.memberCenterUrl === MEMBER_CENTER_URL
			&& modal.content.includes('5 次 AI 讲解');
	})()
);

assert(
	'24.3-6: entry guard blocks chat navigation when quota exhausted',
	(() => {
		const result = simulateAiEntryGuard(exhaustedMember, today);
		return result.showModal === true && result.navigated === false;
	})()
);

assert(
	'24.3-6b: entry guard allows chat when quota not synced',
	(() => {
		const result = simulateAiEntryGuard({ isVip: false, aiDailyLimit: 0 }, today);
		return result.navigated === true && result.showModal === false && result.quota.notReady === true;
	})()
);

assert(
	'24.3-7: server 1050010 sync aligns local member to exhausted',
	(() => {
		const after = simulateServerAiQuotaExhausted({ aiDailyLimit: 5, aiUsedToday: 4 }, today);
		return after.aiUsedToday === 5 && after.aiQuotaDate === today;
	})()
);

// --- 静态链路：入口预检 → showAiQuotaExhaustedModal → 会员中心 ---

assertIncludes('24.3-8: showAiQuotaExhaustedModal guides member center', 'common/app-data/impl.js', [
	'模块二十四 24.3',
	'showAiQuotaExhaustedModal',
	'今日 AI 次数已用尽',
	'confirmText: \'开通会员\'',
	"url: '/package/member/center'"
]);

assertIncludes('24.3-9: chat-api local/server quota exhausted handling', 'common/chat-api.js', [
	'模块二十四 24.3',
	'AI_QUOTA_EXHAUSTED_CODE',
	'assertAiChatQuota',
	'buildAiQuotaExhaustedPatch',
	'syncAiQuotaFromApi'
]);

assertIncludes('24.3-10: chat.vue initial load + follow-up modal on 1050010', 'package/ai/chat.vue', [
	'AI_QUOTA_EXHAUSTED_CODE',
	'showAiQuotaExhaustedModal',
	'loadChatInitialMessages',
	'sendChatFollowUp'
]);

assertIncludes('24.3-11: answer.vue AI explain pre-check', 'package/question/answer.vue', [
	'checkAiChatQuota',
	'showAiQuotaExhaustedModal'
]);

assertIncludes('24.3-12: analysis.vue askAi pre-check', 'package/question/analysis.vue', [
	'checkAiChatQuota',
	'showAiQuotaExhaustedModal'
]);

assertIncludes('24.3-13: notebook.vue askAi pre-check', 'pages/notebook/notebook.vue', [
	'checkAiChatQuota',
	'showAiQuotaExhaustedModal',
	'buildChatPageUrl'
]);

assertIncludes('24.3-14: collection.vue askAi pre-check', 'package/profile/collection.vue', [
	'checkAiChatQuota',
	'showAiQuotaExhaustedModal'
]);

assertIncludes('24.3-15: ocr-result.vue AI explain pre-check', 'package/question/ocr-result.vue', [
	'checkAiChatQuota',
	'showAiQuotaExhaustedModal'
]);

assertIncludes('24.3-16: report/weak-point AI entry pre-check', 'common/app-data/impl.js', [
	'openReportAiPlan',
	'openWeakPointAnalysisChat',
	'checkAiChatQuota',
	'showAiQuotaExhaustedModal'
]);

assertIncludes('24.3-17: AI Tab notice links member center', 'pages/ai/ai.vue', [
	"go('/package/member/center')",
	'开通会员无限次',
	'formatQuotaDisplay'
]);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Integration AI limit verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'integration AI limit checks passed (24.3)');
checks.forEach((c) => console.log('  ✓', c.name));
