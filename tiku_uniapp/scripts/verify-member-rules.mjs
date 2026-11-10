/**
 * 模块二十三 · 会员规则静态与边界校验（文档 23.3）
 * 运行：node scripts/verify-member-rules.mjs
 */
import fs from 'fs';
import path from 'path';
import {
	DEFAULT_DAILY_LIMIT,
	VIP_LEVEL_LABEL,
	NORMAL_LEVEL_LABEL,
	resolveUsedToday,
	resolveMemberLevel,
	isQuotaUnlimited,
	canConsumeAiQuota,
	canConsumeOcrQuota,
	calculateVipExpireAt,
	buildActivationQuotaReset,
	formatQuotaDisplay,
	buildQuotaCheckResult,
	formatDateOnly,
	getTodayString,
	resolveDailyLimit,
	isDailyLimitReady
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

// --- 静态引用校验 ---

assertIncludes('member-rules module doc 23.3', 'common/member-rules.js', [
	'文档 23.3',
	'DEFAULT_DAILY_LIMIT',
	'buildActivationQuotaReset',
	'calculateVipExpireAt'
]);
assertIncludes('impl.js uses member-rules', 'common/app-data/impl.js', [
	"from '@/common/member-rules'",
	'buildQuotaCheckResult',
	'resolveMemberQuotaState',
	'buildActivationQuotaReset'
]);
assertIncludes('ai.vue formatQuotaDisplay', 'pages/ai/ai.vue', [
	"from '@/common/member-rules'",
	'formatQuotaDisplay'
]);
assertIncludes('member-activate quota reset', 'common/member-activate.js', [
	'buildActivationQuotaReset',
	'fetchAndMergeUserInfo'
]);
assertIncludes('chat-api exhausted patch', 'common/chat-api.js', [
	'buildAiQuotaExhaustedPatch'
]);
assertIncludes('ocr-result exhausted patch', 'package/question/ocr-result.vue', [
	'buildOcrQuotaExhaustedPatch'
]);
assertIncludes('user-info resolveMemberLevel', 'common/user-info.js', [
	'resolveMemberLevel'
]);

// --- 边界用例：与 MemberRulesSupportTest 对齐 ---

const today = '2026-08-13';
const yesterday = '2026-08-12';

assert('DEFAULT_DAILY_LIMIT is 5 (server sys_config reference)', DEFAULT_DAILY_LIMIT === 5);
assert('resolveDailyLimit unset -> 0', resolveDailyLimit(undefined) === 0);
assert('resolveDailyLimit positive', resolveDailyLimit(5) === 5);
assert('isDailyLimitReady false when unset', isDailyLimitReady(0) === false);
assert('VIP level label', resolveMemberLevel(true) === VIP_LEVEL_LABEL);
assert('normal level label', resolveMemberLevel(false) === NORMAL_LEVEL_LABEL);
assert('VIP quota unlimited', isQuotaUnlimited(true) === true);
assert('free quota limited', isQuotaUnlimited(false) === false);

assert(
	'cross-day ai used -> 0',
	resolveUsedToday(yesterday, 5, today) === 0
);
assert(
	'null quota date -> 0',
	resolveUsedToday(null, 3, today) === 0
);
assert(
	'same-day ai used preserved',
	resolveUsedToday(today, 3, today) === 3
);

assert(
	'canConsumeAi blocked when limit not synced',
	!canConsumeAiQuota({ isVip: false, aiDailyLimit: 0, aiUsedToday: 0, aiQuotaDate: today }, today)
);
assert(
	'canConsumeAi under limit',
	canConsumeAiQuota({ isVip: false, aiDailyLimit: 5, aiUsedToday: 4, aiQuotaDate: today }, today)
);
assert(
	'canConsumeAi exhausted',
	!canConsumeAiQuota({ isVip: false, aiDailyLimit: 5, aiUsedToday: 5, aiQuotaDate: today }, today)
);
assert(
	'VIP canConsumeOcr even high used',
	canConsumeOcrQuota({ isVip: true, ocrDailyLimit: 5, ocrUsedToday: 99, ocrQuotaDate: today }, today)
);

const now = new Date('2026-08-13T10:00:00');
const futureExpire = new Date('2026-08-23T10:00:00');
const newExpireFromNow = calculateVipExpireAt('', 31, now);
assert(
	'expireAt new user from now',
	formatDateOnly(newExpireFromNow) === '2026-09-13'
);
const stackedExpire = calculateVipExpireAt('2026-08-23', 31, now);
assert(
	'expireAt stack from existing',
	formatDateOnly(stackedExpire) === '2026-09-23'
);
const pastExpire = calculateVipExpireAt('2026-08-12', 30, now);
assert(
	'expireAt past renew from now',
	formatDateOnly(pastExpire) === '2026-09-12'
);

const reset = buildActivationQuotaReset(today);
assert(
	'activation reset zeros used',
	reset.aiUsedToday === 0 && reset.ocrUsedToday === 0
);
assert(
	'activation reset sets quota dates',
	reset.aiQuotaDate === today && reset.ocrQuotaDate === today
);

const member = {
	isVip: false,
	aiDailyLimit: 5,
	aiUsedToday: 2,
	aiQuotaDate: today,
	ocrDailyLimit: 5,
	ocrUsedToday: 1,
	ocrQuotaDate: today
};
assert('formatQuotaDisplay ai', formatQuotaDisplay(member, 'ai', today) === '3/5');
assert('formatQuotaDisplay ocr', formatQuotaDisplay(member, 'ocr', today) === '4/5');
assert(
	'formatQuotaDisplay shows dash when limit not synced',
	formatQuotaDisplay({ isVip: false, aiDailyLimit: 0 }, 'ai', today) === '—'
);
assert(
	'buildQuotaCheckResult notReady when limit unset',
	buildQuotaCheckResult({ isVip: false, aiDailyLimit: 0 }, 'ai', today).notReady === true
);
assert(
	'buildQuotaCheckResult ok',
	buildQuotaCheckResult(member, 'ai', today).ok === true
);
assert(
	'buildQuotaCheckResult exhausted',
	buildQuotaCheckResult({
		...member,
		aiUsedToday: 5
	}, 'ai', today).ok === false
);
assert(
	'buildQuotaCheckResult vip unlimited',
	buildQuotaCheckResult({ ...member, isVip: true }, 'ai', today).remaining === '无限'
);

assert('getTodayString format', /^\d{4}-\d{2}-\d{2}$/.test(getTodayString(new Date('2026-08-13T08:00:00'))));

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Member rules verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'member rule checks passed (23.3)');
checks.forEach((c) => console.log('  ✓', c.name));
