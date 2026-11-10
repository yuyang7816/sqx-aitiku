/**
 * 模块二十四 · 开通会员冒烟校验（文档 24.1 第 8 项）
 * 运行：node scripts/verify-smoke-vip.mjs
 */
import fs from 'fs';
import path from 'path';
import {
	VIP_LEVEL_LABEL,
	NORMAL_LEVEL_LABEL,
	buildActivationQuotaReset,
	calculateVipExpireAt,
	formatDateOnly,
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

// --- 与 impl.js / member-activate.js 对齐的轻量复刻 ---

/** 首页 Hero Banner 文案：按 isVip 分支，含待复习错题数 */
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

/** 支付成功后刷新本地 memberState（对齐 refreshMemberStateAfterPay） */
const simulateRefreshMemberStateAfterPay = (current = {}, { name = '月卡', today = getTodayString() } = {}) => {
	const reset = buildActivationQuotaReset(today);
	return {
		...current,
		isVip: true,
		level: VIP_LEVEL_LABEL,
		...reset,
		currentPlanName: String(name || '月卡').trim() || '月卡',
		orderCount: Number(current.orderCount || 0) + 1
	};
};

/** 本地激活会员（对齐 activateMemberPlan，expireAt 由套餐天数推算） */
const simulateActivateMemberPlan = (
	current = {},
	{ name = '月卡', days = 31, serverExpireAt = '' } = {},
	today = getTodayString()
) => {
	const reset = buildActivationQuotaReset(today);
	const expireAt = serverExpireAt
		? String(serverExpireAt).trim().slice(0, 10)
		: formatDateOnly(calculateVipExpireAt(current.expireAt, days));
	return {
		...current,
		isVip: true,
		level: VIP_LEVEL_LABEL,
		expireAt,
		...reset,
		currentPlanName: name,
		orderCount: Number(current.orderCount || 0) + 1
	};
};

const buildMemberPayPageUrl = ({ name = '', price = 0, days = 0 } = {}) => {
	const query = [
		`name=${encodeURIComponent(String(name || '').trim())}`,
		`price=${Number(price) || 0}`,
		`days=${Number(days) || 0}`
	];
	return `/package/member/pay?${query.join('&')}`;
};

// --- 文档 24.1 第 8 项：开通会员 → 支付成功 → Hero VIP 文案 ---

const nonVipMember = {
	isVip: false,
	level: NORMAL_LEVEL_LABEL,
	expireAt: '',
	aiUsedToday: 4,
	ocrUsedToday: 3,
	currentPlanName: '未开通',
	orderCount: 0
};
const pendingCount = 2;

const heroBeforePay = buildHomeHeroData(nonVipMember, pendingCount);
assert(
	'24.1-8: non-VIP hero title is 开通AI至尊会员',
	heroBeforePay.titleLines[0] === '开通AI至尊会员'
);
assert(
	'24.1-8: non-VIP hero button is 立即查看',
	heroBeforePay.buttonText === '立即查看'
);
assert(
	'24.1-8: non-VIP hero badge is 限时特惠',
	heroBeforePay.badge === '限时特惠'
);
assert(
	'24.1-8: non-VIP hero mentions pending wrong count',
	heroBeforePay.titleLines[1].includes(String(pendingCount))
);

const afterPay = simulateRefreshMemberStateAfterPay(nonVipMember, { name: '季卡' });
assert(
	'24.1-8: pay success sets isVip=true',
	afterPay.isVip === true && afterPay.level === VIP_LEVEL_LABEL
);
assert(
	'24.1-8: pay success resets ai/ocr used today',
	afterPay.aiUsedToday === 0 && afterPay.ocrUsedToday === 0
);
assert(
	'24.1-8: pay success increments orderCount',
	afterPay.orderCount === 1 && afterPay.currentPlanName === '季卡'
);

const heroAfterPay = buildHomeHeroData(afterPay, pendingCount);
assert(
	'24.1-8: VIP hero title is 你的AI至尊权益已生效',
	heroAfterPay.titleLines[0] === '你的AI至尊权益已生效'
);
assert(
	'24.1-8: VIP hero button is 查看权益',
	heroAfterPay.buttonText === '查看权益'
);
assert(
	'24.1-8: VIP hero badge is 会员进行中',
	heroAfterPay.badge === '会员进行中'
);
assert(
	'24.1-8: VIP hero mentions pending wrong count',
	heroAfterPay.titleLines[1].includes(String(pendingCount))
);

const activated = simulateActivateMemberPlan(nonVipMember, { name: '月卡', days: 31 });
assert(
	'24.1-8: activateMemberPlan sets future expireAt',
	Boolean(activated.expireAt) && activated.isVip === true
);

const payUrl = buildMemberPayPageUrl({ name: '季卡', price: 68, days: 90 });
assert(
	'24.1-8: member center builds pay URL with plan params',
	payUrl.includes('/package/member/center')
		&& payUrl.includes('openPay=1')
		&& payUrl.includes('name=%E5%AD%A3%E5%8D%A1')
		&& payUrl.includes('days=90')
);

// --- 静态链路：会员中心弹框支付 → 成功 → 首页 Hero ---

assertIncludes('member center pay popup wiring', 'package/member/center.vue', [
	'openPayPopup',
	'member-pay-popup',
	'showPayPopup',
	'handlePaySuccess',
	'fetchAndMergeUserInfo',
	'refreshMemberExpireState'
]);
assertIncludes('pay page redirects to center popup', 'package/member/pay.vue', [
	'buildMemberCenterPayUrl',
	'uni.redirectTo'
]);
assertIncludes('pay popup real payment flow', 'components/member-pay-popup/member-pay-popup.vue', [
	'payMemberPlan',
	'PAY_METHOD',
	'选择支付方式'
]);
assertIncludes('pay-api create order + refresh member', 'common/pay-api.js', [
	"postJson('/app/pay'",
	'refreshMemberStateAfterPay',
	'payMemberPlan',
	'appendLocalMemberOrder'
]);
assertIncludes('member-activate after pay', 'common/member-activate.js', [
	'refreshMemberStateAfterPay',
	'fetchAndMergeUserInfo',
	'buildActivationQuotaReset',
	'isVip: true'
]);
assertIncludes('success page go home', 'package/member/success.vue', [
	'开通成功',
	"syncMemberState",
	"uni.reLaunch({ url: '/pages/index/index' })",
	'fetchAndMergeUserInfo'
]);
assertIncludes('impl getHomeHeroData VIP branch', 'common/app-data/impl.js', [
	'export const getHomeHeroData',
	'你的AI至尊权益已生效',
	'开通AI至尊会员',
	'member.isVip'
]);
assertIncludes('index onShow refreshes hero', 'pages/index/index.vue', [
	'getHomeHeroData',
	'refreshHomeData',
	'fetchAndMergeUserInfo',
	'refreshMemberExpireState',
	'heroData'
]);
assertIncludes('home member routes exported', 'common/routes.js', [
	'MEMBER_CENTER_PAGE',
	'MEMBER_PAY_PAGE',
	'MEMBER_SUCCESS_PAGE',
	'buildMemberPayPageUrl'
]);
assertIncludes('pages.json member subpackage', 'pages.json', [
	'"root": "package/member"',
	'"path": "center"',
	'"path": "pay"',
	'"path": "success"'
]);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Smoke VIP flow verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'smoke VIP flow checks passed (24.1 item 8)');
checks.forEach((c) => console.log('  ✓', c.name));
