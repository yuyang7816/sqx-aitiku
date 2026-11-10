/**
 * 模块二十三 · 推广提现规则静态与边界校验（文档 23.4）
 * 运行：node scripts/verify-promo-rules.mjs
 */
import fs from 'fs';
import path from 'path';
import {
	WITHDRAW_MIN_AMOUNT,
	WITHDRAW_MAX_AMOUNT,
	WITHDRAW_AMOUNT_STEP,
	validateWithdrawAmount,
	canApplyWithdraw
} from '../common/withdraw-rules.js';

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

assertIncludes('withdraw-rules module doc 23.4', 'common/withdraw-rules.js', [
	'文档 23.4',
	'WITHDRAW_MIN_AMOUNT',
	'WITHDRAW_MAX_AMOUNT',
	'validateWithdrawAmount'
]);
assertIncludes('withdraw.vue imports withdraw-rules', 'package/promo/withdraw.vue', [
	"from '@/common/withdraw-rules'",
	'validateWithdrawAmount',
	'canApplyWithdraw',
	'WITHDRAW_ACCOUNT_TYPE_WECHAT'
]);
assertIncludes('withdraw.vue wechat T+1 copy', 'package/promo/withdraw.vue', [
	'提现到微信零钱',
	'预计 T+1 个工作日到账',
	'withdrawMin',
	'withdrawMax'
]);
assertIncludes('promo-api applyWithdraw guard', 'common/promo-api.js', [
	"from '@/common/withdraw-rules'",
	'validateWithdrawAmount',
	"postJson('/app/withdrawRecord'"
]);

// --- 边界用例：文档 23.4 金额规则 ---

const balance = 10000;

assert('constants: min 50 max 5000 step 50', (
	WITHDRAW_MIN_AMOUNT === 50
	&& WITHDRAW_MAX_AMOUNT === 5000
	&& WITHDRAW_AMOUNT_STEP === 50
));

assert('reject 49: below min', !validateWithdrawAmount(49, balance).valid);
assert(
	'reject 49: reason below_min',
	validateWithdrawAmount(49, balance).reason === 'below_min'
);

assert('reject 5001: above max', !validateWithdrawAmount(5001, balance).valid);
assert(
	'reject 5001: reason above_max',
	validateWithdrawAmount(5001, balance).reason === 'above_max'
);

assert('accept 50: min boundary', validateWithdrawAmount(50, balance).valid);
assert('accept 5000: max boundary', validateWithdrawAmount(5000, balance).valid);
assert('accept 100: step multiple', validateWithdrawAmount(100, balance).valid);

assert('reject 51: not step', !validateWithdrawAmount(51, balance).valid);
assert(
	'reject 51: reason not_step',
	validateWithdrawAmount(51, balance).reason === 'not_step'
);

assert('reject 150: exceed balance', !validateWithdrawAmount(150, 100).valid);
assert(
	'reject exceed balance message',
	validateWithdrawAmount(150, 100).message === '提现金额不能超过可提现余额'
);

assert('canApply 49 disabled', !canApplyWithdraw(49, balance));
assert('canApply 5001 disabled', !canApplyWithdraw(5001, balance));
assert('canApply 100 enabled', canApplyWithdraw(100, balance));

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Promo withdraw rules verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'promo withdraw rule checks passed (23.4)');
checks.forEach((c) => console.log('  ✓', c.name));
