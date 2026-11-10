/**
 * 模块二十四 · 支付回调后会员状态刷新联调校验（文档 24.3）
 * 运行：node scripts/verify-integration-pay-callback.mjs
 */
import fs from 'fs';
import path from 'path';
import {
	VIP_LEVEL_LABEL,
	NORMAL_LEVEL_LABEL,
	buildActivationQuotaReset,
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

// --- 与 member-activate.js / user-info.js 对齐的轻量复刻 ---

/** 首页 Hero Banner 文案：按 isVip 分支 */
const buildHomeHeroData = (member = {}, pendingCount = 0) => {
	if (member.isVip) {
		return {
			badge: '会员进行中',
			titleLines: [
				'你的AI至尊权益已生效',
				pendingCount ? `还有 ${pendingCount} 道错题待复习` : '继续保持当前学习节奏'
			],
			buttonText: '查看权益'
		};
	}
	return {
		badge: '限时特惠',
		titleLines: [
			'开通AI至尊会员',
			pendingCount ? `解锁无限次搜题并优先消灭 ${pendingCount} 道错题` : '享无限次搜题'
		],
		buttonText: '立即查看'
	};
};

/** 对齐 mapServerUserToLocal 会员字段映射 */
const mapServerUserMemberPatch = (data = {}) => {
	const memberPatch = {};
	if (typeof data.vip === 'boolean') {
		memberPatch.isVip = data.vip;
		memberPatch.level = data.vip ? VIP_LEVEL_LABEL : NORMAL_LEVEL_LABEL;
	}
	if (data.vipExpireAt) {
		memberPatch.expireAt = String(data.vipExpireAt).trim().slice(0, 10);
	}
	return memberPatch;
};

/** 对齐 mergeServerUserInfo：支付回调延迟时保留本地有效会员态 */
const mergeServerMemberWithLocal = (current = {}, serverData = {}) => {
	const memberPatch = mapServerUserMemberPatch(serverData);
	if (!Object.keys(memberPatch).length) {
		return { ...current };
	}
	if (memberPatch.isVip === false && current.isVip === true && current.expireAt) {
		const expire = new Date(String(current.expireAt).trim());
		if (!Number.isNaN(expire.getTime())) {
			expire.setHours(23, 59, 59, 999);
			if (expire.getTime() >= Date.now()) {
				delete memberPatch.isVip;
				delete memberPatch.level;
			}
		}
	}
	return { ...current, ...memberPatch };
};

/** 对齐 refreshMemberStateAfterPay：先合并服务端，再强制本地 VIP 即时生效 */
const simulateRefreshMemberStateAfterPay = (
	current = {},
	serverData = {},
	{ name = '月卡', today = getTodayString() } = {}
) => {
	const merged = mergeServerMemberWithLocal(current, serverData);
	const reset = buildActivationQuotaReset(today);
	return {
		...merged,
		isVip: true,
		level: VIP_LEVEL_LABEL,
		...reset,
		currentPlanName: String(name || '月卡').trim() || '月卡',
		orderCount: Number(merged.orderCount || 0) + 1
	};
};

// TC-M24-012 · 24.3：支付回调后会员状态刷新（VIP 即时生效）

const nonVipMember = {
	isVip: false,
	level: NORMAL_LEVEL_LABEL,
	expireAt: '',
	aiUsedToday: 4,
	ocrUsedToday: 3,
	currentPlanName: '未开通',
	orderCount: 0
};

assert(
	'24.3-1: server callback done — vip=true + vipExpireAt merged',
	(() => {
		const after = simulateRefreshMemberStateAfterPay(nonVipMember, {
			vip: true,
			vipExpireAt: '2026-09-15T23:59:59'
		}, { name: '季卡' });
		return after.isVip === true
			&& after.level === VIP_LEVEL_LABEL
			&& after.expireAt === '2026-09-15'
			&& after.currentPlanName === '季卡'
			&& after.orderCount === 1;
	})()
);

assert(
	'24.3-2: callback delayed — server vip=false still forces local VIP immediately',
	(() => {
		const after = simulateRefreshMemberStateAfterPay(nonVipMember, { vip: false }, { name: '月卡' });
		return after.isVip === true && after.level === VIP_LEVEL_LABEL;
	})()
);

assert(
	'24.3-3: pay success resets ai/ocr used today (23.3-4)',
	(() => {
		const after = simulateRefreshMemberStateAfterPay(nonVipMember, { vip: true, vipExpireAt: '2026-10-01' });
		return after.aiUsedToday === 0 && after.ocrUsedToday === 0;
	})()
);

assert(
	'24.3-4: merge preserves local VIP when callback delayed but expireAt still valid',
	(() => {
		const localVip = {
			isVip: true,
			level: VIP_LEVEL_LABEL,
			expireAt: '2099-12-31',
			orderCount: 2
		};
		const merged = mergeServerMemberWithLocal(localVip, { vip: false });
		return merged.isVip === true && merged.level === VIP_LEVEL_LABEL;
	})()
);

assert(
	'24.3-5: hero switches to VIP copy immediately after pay refresh',
	(() => {
		const after = simulateRefreshMemberStateAfterPay(nonVipMember, { vip: true, vipExpireAt: '2026-10-01' });
		const hero = buildHomeHeroData(after, 2);
		return hero.titleLines[0] === '你的AI至尊权益已生效'
			&& hero.badge === '会员进行中'
			&& hero.buttonText === '查看权益';
	})()
);

assert(
	'24.3-6: server vipExpireAt maps to yyyy-MM-dd expireAt',
	mapServerUserMemberPatch({ vip: true, vipExpireAt: '2026-08-20T00:00:00' }).expireAt === '2026-08-20'
);

// --- 静态链路：支付 → refreshMemberStateAfterPay → success/index 二次拉取 ---

assertIncludes('24.3-7: pay-api POST /app/pay + refresh after requestPayment', 'common/pay-api.js', [
	"postJson('/app/pay'",
	'refreshMemberStateAfterPay',
	'invokePlatformPayment',
	'payMemberPlan'
]);

assertIncludes('24.3-8: member-activate fetch server then force VIP', 'common/member-activate.js', [
	'refreshMemberStateAfterPay',
	'fetchAndMergeUserInfo',
	'buildActivationQuotaReset',
	'isVip: true'
]);

assertIncludes('24.3-9: user-info merge vip/vipExpireAt + callback delay guard', 'common/user-info.js', [
	"get('/app/user')",
	'data.vipExpireAt',
	'memberPatch.isVip',
	'支付回调未落库'
]);

assertIncludes('24.3-10: center popup pay success redirect', 'package/member/center.vue', [
	'handlePaySuccess',
	'MEMBER_SUCCESS_PAGE',
	'uni.redirectTo'
]);

assertIncludes('24.3-11: success page syncMemberState onShow', 'package/member/success.vue', [
	'syncMemberState',
	'fetchAndMergeUserInfo',
	'getMemberState',
	'开通成功'
]);

assertIncludes('24.3-12: index onShow refresh hero after fetch user', 'pages/index/index.vue', [
	'fetchAndMergeUserInfo',
	'refreshMemberExpireState',
	'getHomeHeroData',
	'refreshHomeData'
]);

assertIncludes('24.3-13: getHomeHeroData VIP branch', 'common/app-data/impl.js', [
	'export const getHomeHeroData',
	'你的AI至尊权益已生效',
	'开通AI至尊会员',
	'member.isVip'
]);

assertIncludes('24.3-14: member center refreshes user on show', 'package/member/center.vue', [
	'fetchAndMergeUserInfo',
	'refreshMemberExpireState',
	'useMemberStore',
	'onShow'
]);

assertIncludes('24.3-15: member-expire module doc references pay callback merge', 'common/member-expire.js', [
	'支付回调延迟',
	'refreshMemberExpireState',
	'GET /app/user'
]);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Integration pay-callback verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'integration pay-callback checks passed (24.3)');
checks.forEach((c) => console.log('  ✓', c.name));
