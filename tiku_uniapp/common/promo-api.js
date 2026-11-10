/**
 * 推广收益概览：GET /app/referral/userCommission
 * 推广邀请码：GET /app/referral/inviteCode
 * 推广下级成员：GET /app/referral/members
 * 可提现余额：GET /app/userWallet
 * 收益明细：GET /app/userWalletTransaction
 * 提现申请：POST /app/withdrawRecord
 * 提现记录列表：POST /app/withdrawRecord/page
 */
import { get, postJson, PAGE_LOAD_REQUEST_OPTIONS } from '@/common/api';
import {
	getWithdrawRules,
	isWithdrawRulesReady,
	resetWithdrawRules,
	setWithdrawRules,
	validateWithdrawAmount
} from '@/common/withdraw-rules';

/** 金额展示（保留两位小数，元） */
export const formatCommissionAmount = (amount) => {
	const num = Number(amount);
	if (!Number.isFinite(num)) {
		return '0.00';
	}
	return num.toFixed(2);
};

/** 将服务端 AppUserCommissionSummaryVO 规范为推广主页展示字段 */
export const normalizePromoSummary = (data = {}) => ({
	totalCommission: formatCommissionAmount(data.totalCommission),
	todayCommission: formatCommissionAmount(data.todayCommission),
	inviteCount: Number(data.inviteCount) || 0
});

/** 空收益概览 */
export const buildEmptyPromoSummary = () => normalizePromoSummary({});

/** 将服务端 AppReferralInviteCodeVO 规范为海报/分享展示字段 */
export const normalizeInviteCode = (data = {}) => ({
	inviteCode: String(data.inviteCode || '').trim(),
	qrCodeUrl: String(data.qrCodeUrl || '').trim(),
	posterImageUrl: String(data.posterImageUrl || '').trim(),
	inviteText: String(data.inviteText || '').trim()
});

/**
 * 拉取推广邀请码与小程序码（需登录）
 * @returns {Promise<{ invite: { inviteCode: string, qrCodeUrl: string }, fromApi: boolean }>}
 */
export const fetchInviteCode = async () => {
	try {
		const res = await get('/app/referral/inviteCode');
		if (res && res.code === 0 && res.data) {
			return {
				invite: normalizeInviteCode(res.data),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理
	}
	return {
		invite: { inviteCode: '', qrCodeUrl: '', posterImageUrl: '', inviteText: '' },
		fromApi: false
	};
};

/** 将服务端 AppUserWalletBalanceVO 规范为提现页可提现余额（元，两位小数） */
export const normalizeWithdrawableBalance = (data = {}) => ({
	withdrawableAmount: formatCommissionAmount(data.withdrawableAmount)
});

/** 空可提现余额 */
export const buildEmptyWithdrawableBalance = () => normalizeWithdrawableBalance({});

/** 提现渠道：微信（withdraw_record.account_type） */
export const WITHDRAW_ACCOUNT_TYPE_WECHAT = 1;

/** 提现渠道：支付宝（withdraw_record.account_type） */
export const WITHDRAW_ACCOUNT_TYPE_ALIPAY = 2;

/** 提现账户表 type：微信（withdrawal_account.type） */
export const WITHDRAWAL_ACCOUNT_DB_TYPE_WECHAT = 2;

/** 提现账户表 type：支付宝（withdrawal_account.type） */
export const WITHDRAWAL_ACCOUNT_DB_TYPE_ALIPAY = 1;

/** 将服务端 WithdrawalAccount 规范为绑定页字段 */
export const normalizeWithdrawalAccount = (data = {}) => ({
	id: data.id,
	type: Number(data.type) || 0,
	accountName: String(data.accountName || '').trim(),
	accountNumber: String(data.accountNumber || '').trim()
});

export const isWechatWithdrawAccountReady = (account = {}) => Boolean(String(account.accountNumber || '').trim());

export const isAlipayWithdrawAccountReady = (account = {}) => {
	const accountNumber = String(account.accountNumber || '').trim();
	const accountName = String(account.accountName || '').trim();
	return Boolean(accountNumber && accountName);
};

export const isWithdrawAccountReady = (accountType, account = {}) => {
	const type = Number(accountType);
	if (type === WITHDRAW_ACCOUNT_TYPE_ALIPAY) {
		return isAlipayWithdrawAccountReady(account);
	}
	return isWechatWithdrawAccountReady(account);
};

/**
 * 查询已绑定的提现账户（withdrawal_account.type：1 支付宝 2 微信）
 */
export const fetchWithdrawalAccount = async (dbType) => {
	try {
		const res = await get('/app/withdrawalAccount/getUserWithdrawalAccount', { type: dbType }, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0) {
			return {
				account: normalizeWithdrawalAccount(res.data || { type: dbType }),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理
	}
	return {
		account: normalizeWithdrawalAccount({ type: dbType }),
		fromApi: false
	};
};

/**
 * 保存/更新提现账户绑定
 * @param {{ type: number, accountName?: string, accountNumber: string }} payload
 */
export const saveWithdrawalAccount = async (payload = {}) => {
	const res = await postJson('/app/withdrawalAccount/save', {
		type: Number(payload.type),
		accountName: String(payload.accountName || '').trim(),
		accountNumber: String(payload.accountNumber || '').trim()
	}, { toast: false });
	if (res && res.code === 0) {
		return true;
	}
	const err = new Error((res && res.msg) || '保存提现账户失败');
	err.msg = (res && res.msg) || '保存提现账户失败';
	throw err;
};

/** 提现记录状态（与 server WithdrawChannelConstants 一致） */
export const WITHDRAW_RECORD_STATUS = {
	PENDING: 1,
	SUCCESS: 2,
	REJECTED: 3
};

/** 提现记录状态筛选项（业务字段：审核状态） */
export const WITHDRAW_RECORD_STATUS_FILTERS = [
	{ label: '全部', value: '' },
	{ label: '待审核', value: WITHDRAW_RECORD_STATUS.PENDING },
	{ label: '打款成功', value: WITHDRAW_RECORD_STATUS.SUCCESS },
	{ label: '已拒绝', value: WITHDRAW_RECORD_STATUS.REJECTED }
];

/** 提现渠道展示文案 */
export const mapWithdrawAccountTypeLabel = (accountType) => {
	const type = Number(accountType);
	if (type === WITHDRAW_ACCOUNT_TYPE_WECHAT) {
		return '微信';
	}
	if (type === WITHDRAW_ACCOUNT_TYPE_ALIPAY) {
		return '支付宝';
	}
	return '—';
};

/** 提现审核状态展示文案 */
export const mapWithdrawRecordStatusLabel = (status) => {
	const code = Number(status);
	if (code === WITHDRAW_RECORD_STATUS.PENDING) {
		return '待审核';
	}
	if (code === WITHDRAW_RECORD_STATUS.SUCCESS) {
		return '打款成功';
	}
	if (code === WITHDRAW_RECORD_STATUS.REJECTED) {
		return '已拒绝';
	}
	return '未知';
};

/** 将服务端 WithdrawRecordVo 规范为提现记录列表字段 */
export const normalizeWithdrawRecord = (item = {}) => ({
	id: item.id != null ? item.id : `${item.createTime || ''}-${item.amount || ''}`,
	amount: formatCommissionAmount(item.amount),
	actualAmount: formatCommissionAmount(item.actualAmount),
	serviceFee: formatCommissionAmount(item.serviceFee),
	accountType: Number(item.accountType) || WITHDRAW_ACCOUNT_TYPE_WECHAT,
	accountTypeLabel: mapWithdrawAccountTypeLabel(item.accountType),
	status: Number(item.status) || WITHDRAW_RECORD_STATUS.PENDING,
	statusLabel: mapWithdrawRecordStatusLabel(item.status),
	reason: String(item.reason || '').trim(),
	createTime: String(item.createTime || '').trim() || '—',
	withdrawTime: String(item.withdrawTime || '').trim()
});

/** 空提现记录 */
export const buildEmptyWithdrawRecords = () => [];

const assertWithdrawApplySuccess = (res, fallbackMsg) => {
	if (res && res.code === 0 && res.data) {
		return res.data;
	}
	const err = new Error((res && res.msg) || fallbackMsg);
	err.msg = (res && res.msg) || fallbackMsg;
	if (res && res.code != null) {
		err.code = res.code;
	}
	throw err;
};

/**
 * 申请提现（需登录）
 * @param {{ amount: number, accountType?: number }} payload
 * @returns {Promise<{ recordId: number, amount: number, status: number, statusName?: string }>}
 */
export const applyWithdraw = async ({ amount, accountType = WITHDRAW_ACCOUNT_TYPE_WECHAT } = {}) => {
	const validation = validateWithdrawAmount(amount, Infinity, getWithdrawRules());
	if (!validation.valid) {
		const err = new Error(validation.message || '请输入有效提现金额');
		err.msg = validation.message || '请输入有效提现金额';
		throw err;
	}
	const res = await postJson('/app/withdrawRecord', {
		amount: validation.value,
		accountType: Number(accountType) || WITHDRAW_ACCOUNT_TYPE_WECHAT
	}, { toast: false });
	return assertWithdrawApplySuccess(res, '提现申请失败，请稍后重试');
};

/** 将服务端提现规则规范为前端校验字段 */
export const normalizeWithdrawRules = (data = {}) => ({
	minAmount: Number(data.minAmount) || 50,
	maxAmount: Number(data.maxAmount) || 5000,
	stepAmount: Number(data.stepAmount) || Number(data.minAmount) || 50,
	feeRate: Number(data.feeRate) || 0
});

/**
 * 拉取提现规则（最低/最高/手续费）；失败时不写入本地默认，须重试
 */
export const fetchWithdrawRules = async () => {
	try {
		const res = await get('/app/withdrawRecord/rules', undefined, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && res.data) {
			const rules = normalizeWithdrawRules(res.data);
			const applied = setWithdrawRules(rules);
			if (isWithdrawRulesReady()) {
				return { rules: applied, fromApi: true };
			}
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理
	}
	resetWithdrawRules();
	return {
		rules: getWithdrawRules(),
		fromApi: false,
		message: '提现规则加载失败，请稍后重试'
	};
};

/**
	 * 拉取可提现余额；接口失败时返回空余额
 * @returns {Promise<{ balance: { withdrawableAmount: string }, fromApi: boolean }>}
 */
export const fetchWithdrawableBalance = async () => {
	try {
		const res = await get('/app/userWallet', undefined, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && res.data) {
			return {
				balance: normalizeWithdrawableBalance(res.data),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理；其余场景回退 mock
	}

	return {
		balance: buildEmptyWithdrawableBalance(),
		fromApi: false
	};
};

/**
 * 推广主页一次性拉取概览、余额、待结算（均走真实接口）
 * @returns {Promise<{ summary, withdrawableAmount, pendingAmount, fromApi: boolean, errors: string[] }>}
 */
export const fetchPromoIndexData = async () => {
	const [
		summaryResult,
		balanceResult,
		incomeResult
	] = await Promise.all([
		fetchPromoSummary(),
		fetchWithdrawableBalance(),
		fetchPromoIncomeDetail({ page: 1, limit: 5 })
	]);

	const errors = [];
	if (!summaryResult.fromApi) {
		errors.push('收益概览');
	}
	if (!balanceResult.fromApi) {
		errors.push('可提现余额');
	}
	if (!incomeResult.fromApi) {
		errors.push('待结算金额');
	}

	return {
		summary: summaryResult.summary,
		withdrawableAmount: balanceResult.balance.withdrawableAmount,
		pendingAmount: incomeResult.detail.pendingAmount,
		fromApi: errors.length === 0,
		errors
	};
};

/**
	 * 拉取推广收益概览；接口失败时返回空概览
 * @returns {Promise<{ summary: { totalCommission: string, todayCommission: string, inviteCount: number }, fromApi: boolean }>}
 */
export const fetchPromoSummary = async () => {
	try {
		const res = await get('/app/referral/userCommission', undefined, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && res.data) {
			return {
				summary: normalizePromoSummary(res.data),
				fromApi: true
			};
		}
	} catch (e) {
		// 网络异常时静默回退 mock
	}

	return {
		summary: buildEmptyPromoSummary(),
		fromApi: false
	};
};

/** 将服务端 AppReferralMemberVO 规范为推广页下级列表字段 */
export const normalizePromoMember = (item = {}) => ({
	name: String(item.name || '').trim() || '—',
	avatar: String(item.avatar || '').trim(),
	time: String(item.time || '').trim() || '—',
	status: String(item.status || '').trim() || '未付费',
	commission: formatCommissionAmount(item.commission)
});

/** 空下级成员 */
export const buildEmptyPromoMembers = () => [];

/**
 * 拉取推广下级成员列表；接口失败时返回空列表
 * @param {{ level?: 1|2, withCommission?: boolean }} [params]
 * @returns {Promise<{ list: Array, fromApi: boolean, message?: string }>}
 */
export const fetchPromoMembers = async (params = {}) => {
	const level = Number(params.level) === 2 ? 2 : 1;
	const withCommission = params.withCommission ? 1 : 0;

	try {
		const res = await get('/app/referral/members', { level, withCommission }, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && Array.isArray(res.data)) {
			return {
				list: res.data.map(normalizePromoMember),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理；其余场景回退 mock
	}

	return {
		list: buildEmptyPromoMembers(),
		fromApi: false,
		message: '下级成员加载失败'
	};
};

/** 将服务端 AppUserWalletTransactionItemVO 规范为收益明细列表字段 */
export const normalizePromoIncomeRecord = (item = {}) => ({
	id: item.id != null ? item.id : `${item.title || ''}-${item.time || ''}`,
	title: String(item.title || '').trim() || '—',
	time: String(item.time || '').trim() || '—',
	amount: formatCommissionAmount(item.amount),
	status: String(item.status || '').trim() || '待结算'
});

/** 将服务端 AppUserWalletTransactionDetailVO 规范为收益明细页展示结构 */
export const normalizePromoIncomeDetail = (data = {}) => {
	const pageData = data.records || {};
	const rawList = Array.isArray(pageData.records) ? pageData.records : [];
	return {
		totalAmount: formatCommissionAmount(data.totalAmount),
		pendingAmount: formatCommissionAmount(data.pendingAmount),
		records: rawList.map(normalizePromoIncomeRecord)
	};
};

/** 空收益明细 */
export const buildEmptyPromoIncomeDetail = () => normalizePromoIncomeDetail({});

/**
	 * 拉取收益明细（累计/待结算 + 流水分页）；接口失败时返回空数据
 * @param {{ page?: number, limit?: number }} [params]
 * @returns {Promise<{ detail: { totalAmount: string, pendingAmount: string, records: Array }, fromApi: boolean, message?: string }>}
 */
export const fetchPromoIncomeDetail = async (params = {}) => {
	const page = Number(params.page) > 0 ? Number(params.page) : 1;
	const limit = Number(params.limit) > 0 ? Number(params.limit) : 10;

	try {
		const res = await get('/app/userWalletTransaction', { page, limit }, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && res.data) {
			return {
				detail: normalizePromoIncomeDetail(res.data),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理；其余场景回退 mock
	}

	return {
		detail: buildEmptyPromoIncomeDetail(),
		fromApi: false,
		message: '收益明细加载失败'
	};
};

/**
	 * 拉取提现记录分页列表；接口失败时返回空列表
 * @param {{ page?: number, limit?: number }} [params]
 * @returns {Promise<{ list: Array, total: number, current: number, pages: number, fromApi: boolean, message?: string }>}
 */
export const fetchWithdrawRecords = async (params = {}) => {
	const page = Number(params.page) > 0 ? Number(params.page) : 1;
	const limit = Number(params.limit) > 0 ? Number(params.limit) : 10;

	try {
		const res = await postJson('/app/withdrawRecord/page', { page, limit }, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && res.data) {
			const pageData = res.data;
			const rawList = Array.isArray(pageData.records) ? pageData.records : [];
			return {
				list: rawList.map(normalizeWithdrawRecord),
				total: Number(pageData.total) || rawList.length,
				current: Number(pageData.current) || page,
				pages: Number(pageData.pages) || 1,
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理；其余场景回退 mock
	}

	const fallbackList = buildEmptyWithdrawRecords();
	return {
		list: fallbackList,
		total: fallbackList.length,
		current: 1,
		pages: 1,
		fromApi: false,
		message: '提现记录加载失败'
	};
};
