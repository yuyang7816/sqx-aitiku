/**
 * 会员支付：支付方式枚举与多端可见性（POST /app/pay + 真实 uni.requestPayment）
 *
 * 微信支付规则：
 * - 微信小程序 / App：可用微信登录后直接微信支付
 * - H5 微信内置浏览器：须使用公众号 OAuth 登录（loginType=wechat-mp）才可用 JSAPI 支付
 * - 普通 H5 浏览器：不提供微信支付（服务端走 MWEB，体验与 openId 均不匹配公众号场景）
 */
import { getAuthState } from '@/common/app-data';
import { isMpLoginEnabled } from '@/common/wechat-config';
import { CLIENT_PLATFORM, getClientPlatform, isH5Wechat } from '@/common/platform';

export const PAY_METHOD = {
	WECHAT: 'wechat',
	ALIPAY: 'alipay'
};

/** 公众号 OAuth 登录类型（与 wechat-oauth.js completeAuthSession 一致） */
export const LOGIN_TYPE_WECHAT_MP = 'wechat-mp';

/** 是否公众号授权登录（H5 微信 JSAPI 支付前置条件） */
export const isMpWechatLogin = () => {
	const auth = getAuthState();
	return String(auth.loginType || '').trim() === LOGIN_TYPE_WECHAT_MP;
};

/** H5 微信内置浏览器内是否满足 JSAPI 支付条件 */
export const isH5WechatJsapiPayEligible = () => (
	isH5Wechat() && isMpLoginEnabled() && isMpWechatLogin()
);

/** 当前环境是否展示微信支付选项 */
export const isWechatPayVisible = () => {
	const platform = getClientPlatform();
	if (platform === CLIENT_PLATFORM.MP_ALIPAY) {
		return false;
	}
	if (platform === CLIENT_PLATFORM.MP_WEIXIN || platform === CLIENT_PLATFORM.APP_PLUS) {
		return true;
	}
	if (platform === CLIENT_PLATFORM.H5_WECHAT) {
		if (!isMpLoginEnabled()) {
			return false;
		}
		return isMpWechatLogin();
	}
	return false;
};

export const isAlipayPayVisible = () => {
	const platform = getClientPlatform();
	return platform === CLIENT_PLATFORM.APP_PLUS
		|| platform === CLIENT_PLATFORM.H5
		|| platform === CLIENT_PLATFORM.H5_WECHAT
		|| platform === CLIENT_PLATFORM.MP_ALIPAY;
};

/** 是否存在任一可用支付方式 */
export const hasAnyPayMethod = () => isWechatPayVisible() || isAlipayPayVisible();

/** 微信支付不可用时的说明文案 */
export const getWechatPayUnavailableReason = () => {
	if (isWechatPayVisible()) {
		return '';
	}
	const platform = getClientPlatform();
	if (platform === CLIENT_PLATFORM.H5_WECHAT) {
		if (!isMpLoginEnabled()) {
			return '';
		}
		if (!isMpWechatLogin()) {
			return '微信支付需使用公众号授权登录。请退出账号后，使用「微信一键登录」重新登录。';
		}
	}
	if (platform === CLIENT_PLATFORM.H5) {
		return '请在微信内打开，并使用公众号授权登录后使用微信支付。';
	}
	return '当前环境不支持微信支付';
};

/** 提交前校验微信支付是否允许 */
export const assertWechatPayAllowed = () => {
	if (isWechatPayVisible()) {
		return { ok: true };
	}
	return {
		ok: false,
		message: getWechatPayUnavailableReason() || '当前环境不支持微信支付'
	};
};

/** 解析当前环境下应默认选中的支付方式 */
export const resolveDefaultPayMethod = () => {
	if (isWechatPayVisible()) {
		return PAY_METHOD.WECHAT;
	}
	if (isAlipayPayVisible()) {
		return PAY_METHOD.ALIPAY;
	}
	return PAY_METHOD.WECHAT;
};

/** 默认选中支付方式 */
export const getDefaultPayMethod = () => {
	// #ifdef MP-ALIPAY
	return PAY_METHOD.ALIPAY;
	// #endif
	return resolveDefaultPayMethod();
};

/** 校正已选支付方式（弹框打开或环境变化时） */
export const normalizePayMethod = (method = '') => {
	const selected = String(method || '').trim();
	if (selected === PAY_METHOD.WECHAT && isWechatPayVisible()) {
		return PAY_METHOD.WECHAT;
	}
	if (selected === PAY_METHOD.ALIPAY && isAlipayPayVisible()) {
		return PAY_METHOD.ALIPAY;
	}
	return getDefaultPayMethod();
};

export const formatPayPrice = (price) => {
	const num = Number(price);
	return Number.isFinite(num) ? num.toFixed(2) : '0.00';
};
