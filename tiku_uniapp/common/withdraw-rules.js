/**
 * 推广提现规则（与 server sys_withdraw_* 对齐，仅 API 成功后生效）
 */

/** 未加载规则时的占位结构（不可用于提交） */
export const EMPTY_WITHDRAW_RULES = {
	minAmount: 0,
	maxAmount: 0,
	stepAmount: 0,
	feeRate: 0
};

let activeRules = null;

export const isWithdrawRulesReady = () => activeRules !== null;

export const getWithdrawRules = () => (activeRules ? { ...activeRules } : { ...EMPTY_WITHDRAW_RULES });

export const setWithdrawRules = (rules = {}) => {
	const minAmount = parsePositiveNumber(rules.minAmount, 0);
	const maxAmount = parsePositiveNumber(rules.maxAmount, 0);
	const stepAmount = parsePositiveNumber(rules.stepAmount, minAmount);
	const feeRate = parseFeeRate(rules.feeRate);
	if (!minAmount || !maxAmount) {
		activeRules = null;
		return getWithdrawRules();
	}
	activeRules = {
		minAmount,
		maxAmount: Math.max(maxAmount, minAmount),
		stepAmount: stepAmount || minAmount,
		feeRate
	};
	return getWithdrawRules();
};

export const resetWithdrawRules = () => {
	activeRules = null;
	return getWithdrawRules();
};

/** @deprecated 请使用 getWithdrawRules().minAmount */
export const WITHDRAW_MIN_AMOUNT = 50;

/** @deprecated 请使用 getWithdrawRules().maxAmount */
export const WITHDRAW_MAX_AMOUNT = 5000;

/** @deprecated 请使用 getWithdrawRules().stepAmount */
export const WITHDRAW_AMOUNT_STEP = 50;

function parsePositiveNumber(value, fallback) {
	const num = Number(value);
	if (!Number.isFinite(num) || num <= 0) {
		return fallback;
	}
	return num;
}

function parseFeeRate(value) {
	const num = Number(value);
	if (!Number.isFinite(num) || num < 0) {
		return 0;
	}
	return num;
}

export const parseWithdrawAmount = (raw) => {
	const text = String(raw ?? '').trim();
	if (!text) {
		return null;
	}
	const value = Number(text);
	if (!Number.isFinite(value) || value <= 0) {
		return null;
	}
	return value;
};

export const formatFeeRateLabel = (feeRate = getWithdrawRules().feeRate) => {
	const rate = Number(feeRate) || 0;
	if (rate <= 0) {
		return '免手续费';
	}
	const percent = rate * 100;
	const text = Number.isInteger(percent) ? String(percent) : percent.toFixed(2).replace(/\.?0+$/, '');
	return `${text}%`;
};

export const calcWithdrawFeePreview = (amount, rules = getWithdrawRules()) => {
	const value = parseWithdrawAmount(amount);
	if (value === null || !isWithdrawRulesReady()) {
		return { fee: 0, actual: 0, valid: false };
	}
	const rate = Number(rules.feeRate) || 0;
	const fee = Math.round(value * rate * 100) / 100;
	const actual = Math.round((value - fee) * 100) / 100;
	return { fee, actual, valid: true };
};

export const validateWithdrawAmount = (raw, withdrawableBalance = Infinity, rules = getWithdrawRules()) => {
	if (!isWithdrawRulesReady()) {
		return {
			valid: false,
			reason: 'rules_not_loaded',
			message: '提现规则加载中，请稍后重试'
		};
	}

	const value = parseWithdrawAmount(raw);
	const minAmount = Number(rules.minAmount) || 0;
	const maxAmount = Number(rules.maxAmount) || 0;
	const stepAmount = Number(rules.stepAmount) || minAmount;

	if (value === null) {
		return { valid: false, reason: 'empty' };
	}

	if (value < minAmount) {
		return {
			valid: false,
			reason: 'below_min',
			message: `提现金额最低 ${minAmount} 元`
		};
	}

	if (value > maxAmount) {
		return {
			valid: false,
			reason: 'above_max',
			message: `单笔最高提现 ${maxAmount} 元`
		};
	}

	const cents = Math.round(value * 100);
	if (stepAmount > 0 && cents % Math.round(stepAmount * 100) !== 0) {
		return {
			valid: false,
			reason: 'not_step',
			message: `提现金额需为 ${stepAmount} 元的整数倍`
		};
	}

	const balance = Number(withdrawableBalance);
	const maxBalance = Number.isFinite(balance) ? balance : Infinity;
	if (value > maxBalance) {
		return {
			valid: false,
			reason: 'exceed_balance',
			message: '提现金额不能超过可提现余额'
		};
	}

	return { valid: true, value };
};

export const canApplyWithdraw = (raw, withdrawableBalance = Infinity, rules = getWithdrawRules()) =>
	validateWithdrawAmount(raw, withdrawableBalance, rules).valid;

export const buildQuickWithdrawAmounts = (balance = 0, rules = getWithdrawRules()) => {
	if (!isWithdrawRulesReady()) {
		return [];
	}
	const minAmount = Number(rules.minAmount) || 0;
	const maxAmount = Number(rules.maxAmount) || 0;
	const stepAmount = Number(rules.stepAmount) || minAmount;
	if (!minAmount || !maxAmount) {
		return [];
	}
	const available = Number(balance) || 0;
	const capped = Math.min(available, maxAmount);
	const presets = [minAmount, minAmount * 2, minAmount * 4, capped]
		.map((item) => Math.floor(item / stepAmount) * stepAmount)
		.filter((item) => item >= minAmount && item <= capped);
	return [...new Set(presets)].slice(0, 4);
};
