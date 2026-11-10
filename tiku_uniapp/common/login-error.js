/**
 * 登录相关 API 错误码（与 server AppError 一致，供 uniapp 登录页展示 Toast / 行内提示）
 */
import { LOGIN_ERROR_MOCK_STORAGE_KEY } from '@/common/storage-keys';

export { LOGIN_ERROR_MOCK_STORAGE_KEY } from '@/common/storage-keys';

export const LOGIN_ERROR_CODES = {
	PHONE_NOT_REGISTER: 350001,
	ACCOUNT_ABNORMAL: 350002,
	PHONE_OR_PASSWORD_ERROR: 350003,
	VERIFY_CODE_ERROR: 350004,
	PHONE_REGISTERED: 350005,
	VERIFY_CODE_SEND_TOO_FREQUENT: 350010,
	VERIFY_CODE_EXPIRED: 350011,
	WECHAT_CODE_INVALID: 350012,
	WECHAT_SIMULATOR_CODE: 350022,
	WECHAT_NOT_CONFIGURED: 350018,
	PROTOCOL_NOT_AGREED: 350019
};

/** 默认文案（服务端未返回 msg 时兜底） */
const DEFAULT_MESSAGES = {
	[LOGIN_ERROR_CODES.PHONE_NOT_REGISTER]: '手机号未注册，请先注册',
	[LOGIN_ERROR_CODES.ACCOUNT_ABNORMAL]: '账号已被封禁，如有疑问请联系客服',
	[LOGIN_ERROR_CODES.PHONE_OR_PASSWORD_ERROR]: '手机号或密码错误',
	[LOGIN_ERROR_CODES.PHONE_REGISTERED]: '手机号已注册，请直接登录',
	[LOGIN_ERROR_CODES.VERIFY_CODE_ERROR]: '验证码不正确',
	[LOGIN_ERROR_CODES.VERIFY_CODE_SEND_TOO_FREQUENT]: '发送过于频繁，请60秒后再试',
	[LOGIN_ERROR_CODES.VERIFY_CODE_EXPIRED]: '验证码已过期，请重新获取',
	[LOGIN_ERROR_CODES.WECHAT_CODE_INVALID]: '微信登录凭证无效或已过期',
	[LOGIN_ERROR_CODES.WECHAT_SIMULATOR_CODE]: '开发者工具模拟器无法完成正式微信登录，请使用真机调试或手机预览',
	[LOGIN_ERROR_CODES.WECHAT_NOT_CONFIGURED]: '微信登录暂未开通，请联系管理员',
	[LOGIN_ERROR_CODES.PROTOCOL_NOT_AGREED]: '请先同意协议',
	NETWORK: '网络异常，请稍后重试',
	DEFAULT: '操作失败，请稍后重试'
};

/**
 * 解析登录 API 失败响应
 * @param {object|null|undefined} res - Result 格式 { code, msg, data }
 * @param {'verify-code'|'sms-login'|'wechat-login'} scene
 * @returns {null|{ type: string, code: number|string, message: string, toast: boolean }}
 */
export const parseLoginApiError = (res, scene = 'sms-login') => {
	if (!res || res.code === 0) {
		return null;
	}
	const code = res.code;
	const message = res.msg || DEFAULT_MESSAGES[code] || DEFAULT_MESSAGES.DEFAULT;

	if (code === LOGIN_ERROR_CODES.PHONE_OR_PASSWORD_ERROR || code === LOGIN_ERROR_CODES.PHONE_NOT_REGISTER) {
		return {
			type: 'inline-account',
			code,
			message,
			toast: false,
			scene
		};
	}

	if (code === LOGIN_ERROR_CODES.PHONE_REGISTERED) {
		return {
			type: 'toast',
			code,
			message: DEFAULT_MESSAGES[LOGIN_ERROR_CODES.PHONE_REGISTERED],
			toast: true,
			scene
		};
	}

	if (code === LOGIN_ERROR_CODES.VERIFY_CODE_ERROR || code === LOGIN_ERROR_CODES.VERIFY_CODE_EXPIRED) {
		return {
			type: 'inline-code',
			code,
			message,
			toast: false,
			scene
		};
	}

	if (code === LOGIN_ERROR_CODES.ACCOUNT_ABNORMAL) {
		return {
			type: 'account-banned',
			code,
			message: DEFAULT_MESSAGES[LOGIN_ERROR_CODES.ACCOUNT_ABNORMAL],
			toast: true,
			scene
		};
	}

	if (code === LOGIN_ERROR_CODES.PROTOCOL_NOT_AGREED) {
		return {
			type: 'agreement-required',
			code,
			message: DEFAULT_MESSAGES[LOGIN_ERROR_CODES.PROTOCOL_NOT_AGREED],
			toast: true,
			scene
		};
	}

	if (code === LOGIN_ERROR_CODES.VERIFY_CODE_SEND_TOO_FREQUENT) {
		return {
			type: 'toast',
			code,
			message,
			toast: true,
			scene
		};
	}

	return {
		type: 'toast',
		code,
		message,
		toast: true,
		scene
	};
};

/** 网络 / 请求层异常 */
export const parseNetworkError = () => ({
	type: 'toast',
	code: 'NETWORK',
	message: DEFAULT_MESSAGES.NETWORK,
	toast: true
});

/**
 * 将错误反馈应用到登录页 UI
 * @param {ReturnType<typeof parseLoginApiError>} errorInfo
 * @param {{ setCodeHint?: (msg: string) => void, setAccountAlert?: (msg: string) => void, showToast?: (msg: string) => void }} handlers
 */
export const applyLoginErrorFeedback = (errorInfo, handlers = {}) => {
	if (!errorInfo) {
		return;
	}
	const { setCodeHint, setAccountAlert, showToast } = handlers;

	if (errorInfo.type === 'inline-code') {
		setCodeHint?.(errorInfo.message);
	}
	if (errorInfo.type === 'inline-account') {
		setAccountAlert?.(errorInfo.message);
	}
	if (errorInfo.type === 'account-banned') {
		setAccountAlert?.(errorInfo.message);
	}
	if (errorInfo.toast) {
		showToast?.(errorInfo.message);
	}
};

/**
 * 构造 Mock 错误响应（验收各错误码 UI 反馈）
 * @param {number} errorCode
 */
export const createMockLoginApiResponse = (errorCode) => ({
	code: errorCode,
	msg: DEFAULT_MESSAGES[errorCode] || DEFAULT_MESSAGES.DEFAULT,
	data: null
});

/**
 * 读取当前场景的 Mock 错误码（H5 URL 参数优先，其次 Storage）
 * H5 URL：?mockVerifyCodeError=350010&mockSmsLoginError=350004&mockWechatLoginError=350002
 * @param {'verify-code'|'sms-login'|'wechat-login'} scene
 */
export const getLoginErrorMockCode = (scene) => {
	const sceneKeyMap = {
		'verify-code': 'verifyCode',
		'sms-login': 'smsLogin',
		'wechat-login': 'wechatLogin'
	};
	const storageKey = sceneKeyMap[scene];
	if (!storageKey) {
		return null;
	}

	// #ifdef H5
	try {
		if (typeof window !== 'undefined' && window.location?.search) {
			const params = new URLSearchParams(window.location.search);
			const urlKeyMap = {
				verifyCode: 'mockVerifyCodeError',
				smsLogin: 'mockSmsLoginError',
				wechatLogin: 'mockWechatLoginError'
			};
			const raw = params.get(urlKeyMap[storageKey]);
			if (raw) {
				const code = Number(raw);
				if (!Number.isNaN(code)) {
					return code;
				}
			}
		}
	} catch (e) {
		// ignore
	}
	// #endif

	try {
		const stored = uni.getStorageSync(LOGIN_ERROR_MOCK_STORAGE_KEY);
		if (stored && typeof stored === 'object' && stored[storageKey] != null) {
			const code = Number(stored[storageKey]);
			return Number.isNaN(code) ? null : code;
		}
	} catch (e) {
		// ignore
	}

	return null;
};
