/**
 * 模块二十四 · 提现 49 元边界校验（文档 24.2）
 * 运行：node scripts/verify-edge-withdraw.mjs
 */
import fs from 'fs';
import path from 'path';
import {
	WITHDRAW_MIN_AMOUNT,
	calcWithdrawFeePreview,
	formatFeeRateLabel,
	setWithdrawRules,
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

/** 与 withdraw.vue computed 对齐的行内提示 */
const resolveAmountHint = (amount, balance) => {
	if (!String(amount || '').trim()) {
		return '';
	}
	const result = validateWithdrawAmount(amount, balance);
	return result.valid ? '' : (result.message || '');
};

/** 与 withdraw.vue computed 对齐的按钮可用态 */
const resolveCanApply = (amount, balance, submitting = false) =>
	!submitting && canApplyWithdraw(amount, balance);

setWithdrawRules({ minAmount: 50, maxAmount: 5000, stepAmount: 50, feeRate: 0.02 });

// --- 静态引用 ---

assertIncludes('withdraw-rules min amount guard', 'common/withdraw-rules.js', [
	'WITHDRAW_MIN_AMOUNT',
	'validateWithdrawAmount',
	'canApplyWithdraw',
	'below_min',
	'提现金额最低'
]);
assertIncludes('withdraw.vue validation wiring', 'package/promo/withdraw.vue', [
	"from '@/common/withdraw-rules'",
	'validateWithdrawAmount',
	'canApplyWithdraw',
	'amountValidation',
	'amountHint',
	'canApply'
]);
assertIncludes('withdraw.vue disabled button UI', 'package/promo/withdraw.vue', [
	':class="{ disabled: !canApply || submitting }"',
	'amount-error',
	'amount-hint',
	'fee-preview',
	'showFeePreview',
	'feeRateLabel',
	'申请提现'
]);
assertIncludes('withdraw.vue apply guard before submit', 'package/promo/withdraw.vue', [
	'if (!result.valid)',
	'请输入有效提现金额'
]);
assertIncludes('promo-api applyWithdraw guard', 'common/promo-api.js', [
	"from '@/common/withdraw-rules'",
	'validateWithdrawAmount',
	"postJson('/app/withdrawRecord'"
]);

// --- 边界用例：文档 24.2 提现 49 元 ---

const balance = 10000;

assert(
	'24.2: reject 49 below_min',
	!validateWithdrawAmount(49, balance).valid
);
assert(
	'24.2: 49 reason below_min',
	validateWithdrawAmount(49, balance).reason === 'below_min'
);
assert(
	'24.2: 49 inline hint 提现金额最低 50 元',
	validateWithdrawAmount(49, balance).message === '提现金额最低 50 元'
);
assert(
	'24.2: canApplyWithdraw(49) disabled',
	!canApplyWithdraw(49, balance)
);
assert(
	'24.2: resolveCanApply(49) disabled',
	!resolveCanApply('49', balance)
);
assert(
	'24.2: resolveAmountHint(49) shows min copy',
	resolveAmountHint('49', balance) === '提现金额最低 50 元'
);
assert(
	'24.2: empty amount no inline hint',
	resolveAmountHint('', balance) === ''
);
assert(
	'24.2: min boundary 50 still valid',
	validateWithdrawAmount(50, balance).valid
);
assert(
	'24.2: min boundary 50 enables apply',
	resolveCanApply('50', balance)
);
assert(
	'24.2: submitting blocks apply even for valid amount',
	!resolveCanApply('100', balance, true)
);
assert(
	'24.2: WITHDRAW_MIN_AMOUNT is 50',
	WITHDRAW_MIN_AMOUNT === 50
);

const feePreview49 = calcWithdrawFeePreview('49');
assert(
	'24.2: invalid amount 49 still shows fee preview',
	feePreview49.valid && feePreview49.fee === 0.98 && feePreview49.actual === 48.02
);
assert(
	'24.2: fee rate label for 2%',
	formatFeeRateLabel(0.02) === '2%'
);

// --- 汇总 ---

const failed = checks.filter((item) => !item.ok);
console.log(`\n模块二十四 · 提现 49 元边界校验：${checks.length - failed.length}/${checks.length} 通过\n`);
checks.forEach((item) => {
	console.log(`${item.ok ? '✓' : '✗'} ${item.name}${item.detail ? ` — ${item.detail}` : ''}`);
});

if (failed.length) {
	process.exit(1);
}

console.log('\nOK: 文档 24.2 提现输入 49 元 → 按钮禁用 校验通过\n');
