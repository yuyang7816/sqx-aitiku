/**
 * 微信 OAuth 登录：小程序 uni.login · App uni.login · H5 公众号 OAuth
 */
import { postJson, SILENT_REQUEST_OPTIONS } from '@/common/api';
import { completeAuthSession } from '@/common/auth-api';
import { redirectAfterLogin } from '@/common/app-data';
import { buildAuthInvitePayload } from '@/common/invite-bind';
import { parseLoginApiError } from '@/common/login-error';
import { AGREEMENT_TIP, buildAuthLoginBody } from '@/common/login-rules';
import { fetchWechatPublicConfig } from '@/common/wechat-config';
import {
	getClientPlatform,
	isAppPlus,
	isH5Wechat,
	isMpWeixin,
	CLIENT_PLATFORM
} from '@/common/platform';

const MP_OAUTH_PENDING_KEY = 'aiQuestionMpOAuthPending';
const MP_OAUTH_AGREED_KEY = 'aiQuestionMpOAuthAgreed';
/** 微信开发者工具模拟器固定返回，不能用于正式 jscode2session */
const WECHAT_DEVTOOLS_MOCK_CODE = 'the code is a mock one';
const WECHAT_SIMULATOR_TIP = '开发者工具模拟器无法完成正式微信登录，请使用真机调试或手机预览';

const buildWechatLoginError = (code, message, type = 'toast') => {
	const err = new Error(message);
	err.loginError = {
		type,
		code,
		message,
		toast: true,
		scene: 'wechat-login'
	};
	return err;
};

/** 登录前校验：数据库 sys_user_ma_appid 已配置 */
const ensureWechatMaConfigured = async () => {
	const config = await fetchWechatPublicConfig();
	if (!config.maAppId) {
		throw buildWechatLoginError(350018, '微信登录暂未开通，请联系管理员');
	}
	// #ifdef MP-WEIXIN
	try {
		const runtimeAppId = String(uni.getAccountInfoSync()?.miniProgram?.appId || '').trim();
		if (runtimeAppId && runtimeAppId !== config.maAppId) {
			console.warn('[wechat-oauth] 小程序 AppId 与后台配置不一致', runtimeAppId, config.maAppId);
		}
	} catch (e) {
		// ignore
	}
	// #endif
	return config;
};

const buildAgreementError = () => {
	const err = new Error(AGREEMENT_TIP);
	err.loginError = {
		type: 'agreement-required',
		code: 'AGREEMENT',
		message: AGREEMENT_TIP,
		toast: true,
		scene: 'wechat-login'
	};
	return err;
};

const postWechatAuth = async ({ path, code, channel, agreedProtocol }) => {
	const body = buildAuthLoginBody({
		code,
		channel,
		...buildAuthInvitePayload()
	}, agreedProtocol);
	const res = await postJson(path, body, SILENT_REQUEST_OPTIONS);
	if (res && res.code === 0 && res.data?.token) {
		await completeAuthSession({
			token: res.data.token,
			loginType: channel === 'mp' ? 'wechat-mp' : 'wechat',
			profilePatch: res.data.profile,
			phone: res.data.user?.phone || ''
		});
		const needBindPhone = res.data.needBindPhone === true
			|| (isMpWeixin() && !String(res.data.user?.phone || '').trim());
		if (needBindPhone) {
			return {
				...res.data,
				needBindPhone: true
			};
		}
		redirectAfterLogin();
		return res.data;
	}
	const loginError = parseLoginApiError(res, 'wechat-login');
	if (loginError) {
		const err = new Error(loginError.message);
		err.loginError = loginError;
		throw err;
	}
	throw new Error(res?.msg || '微信登录失败，请稍后重试');
};

const isWechatDevtoolsMockCode = (code) => String(code || '').trim() === WECHAT_DEVTOOLS_MOCK_CODE;

const fetchMiniProgramOrAppCode = () =>
	new Promise((resolve, reject) => {
		uni.login({
			provider: 'weixin',
			success: (res) => {
				const code = String(res?.code || '').trim();
				if (!code) {
					reject(new Error('未获取到微信登录凭证'));
					return;
				}
				if (isWechatDevtoolsMockCode(code)) {
					reject(buildWechatLoginError(350022, WECHAT_SIMULATOR_TIP));
					return;
				}
				resolve(code);
			},
			fail: () => reject(new Error('微信授权失败，请重试'))
		});
	});

/** H5：清理 URL 中的 code/state */
const stripOAuthQueryFromUrl = () => {
	// #ifdef H5
	try {
		const url = new URL(window.location.href);
		url.searchParams.delete('code');
		url.searchParams.delete('state');
		window.history.replaceState({}, '', url.toString());
	} catch (e) {
		// ignore
	}
	// #endif
};

/** H5 微信内置浏览器：读取 URL code */
export const readMpOAuthCodeFromUrl = () => {
	// #ifdef H5
	try {
		const params = new URLSearchParams(window.location.search);
		return String(params.get('code') || '').trim();
	} catch (e) {
		return '';
	}
	// #endif
	return '';
};

/** H5 公众号 OAuth 跳转 */
const redirectToMpOAuth = async () => {
	// #ifdef H5
	const config = await fetchWechatPublicConfig();
	if (!config.mpLoginEnabled) {
		throw new Error('公众号登录未开启');
	}
	if (!config.mpAppId) {
		throw new Error('公众号登录未配置，请联系管理员');
	}
	const redirectUri = encodeURIComponent(window.location.href.split('#')[0]);
	const state = `mp_${Date.now()}`;
	uni.setStorageSync(MP_OAUTH_PENDING_KEY, state);
	const oauthUrl = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${config.mpAppId}`
		+ `&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_userinfo&state=${state}#wechat_redirect`;
	window.location.href = oauthUrl;
	// #endif
};

/**
 * 页面 onLoad/onShow：若 URL 带公众号 code 且此前已同意协议，则自动完成登录
 */
export const tryConsumeMpOAuthCodeFromUrl = async () => {
	if (!isH5Wechat()) {
		return false;
	}
	const config = await fetchWechatPublicConfig();
	if (!config.mpLoginEnabled) {
		return false;
	}
	const code = readMpOAuthCodeFromUrl();
	if (!code) {
		return false;
	}
	const agreed = uni.getStorageSync(MP_OAUTH_AGREED_KEY) === true;
	if (!agreed) {
		return false;
	}
	await postWechatAuth({
		path: '/app/auth/mp',
		code,
		channel: 'mp',
		agreedProtocol: true
	});
	uni.removeStorageSync(MP_OAUTH_AGREED_KEY);
	uni.removeStorageSync(MP_OAUTH_PENDING_KEY);
	stripOAuthQueryFromUrl();
	return true;
};

/**
 * 微信 OAuth 完整流程
 * @param {{ agreedProtocol?: boolean }} [options]
 */
export const startWechatOAuth = async ({ agreedProtocol = false } = {}) => {
	if (agreedProtocol !== true) {
		throw buildAgreementError();
	}

	if (isMpWeixin()) {
		await ensureWechatMaConfigured();
		const code = await fetchMiniProgramOrAppCode();
		return postWechatAuth({
			path: '/app/auth/wechat',
			code,
			channel: 'ma',
			agreedProtocol: true
		});
	}

	if (isAppPlus()) {
		await ensureWechatMaConfigured();
		const code = await fetchMiniProgramOrAppCode();
		return postWechatAuth({
			path: '/app/auth/wechat',
			code,
			channel: 'app',
			agreedProtocol: true
		});
	}

	if (isH5Wechat()) {
		const config = await fetchWechatPublicConfig();
		if (!config.mpLoginEnabled) {
			throw buildWechatLoginError(350023, '公众号登录未开启');
		}
		const code = readMpOAuthCodeFromUrl();
		if (code) {
			const data = await postWechatAuth({
				path: '/app/auth/mp',
				code,
				channel: 'mp',
				agreedProtocol: true
			});
			stripOAuthQueryFromUrl();
			return data;
		}
		uni.setStorageSync(MP_OAUTH_AGREED_KEY, true);
		await redirectToMpOAuth();
		return null;
	}

	throw new Error('当前环境不支持微信一键登录');
};

/** 当前平台是否展示微信登录按钮（同步粗筛，最终以 resolveWechatLoginVisible 为准） */
export const shouldShowWechatLoginButton = () => {
	const platform = getClientPlatform();
	return platform === CLIENT_PLATFORM.MP_WEIXIN
		|| platform === CLIENT_PLATFORM.APP_PLUS
		|| platform === CLIENT_PLATFORM.H5_WECHAT;
};

/** 结合后台配置判断是否展示微信/公众号登录入口 */
export const resolveWechatLoginVisible = (config = {}) => {
	const platform = getClientPlatform();
	if (platform === CLIENT_PLATFORM.MP_WEIXIN) {
		return Boolean(String(config.maAppId || '').trim());
	}
	if (platform === CLIENT_PLATFORM.APP_PLUS) {
		return Boolean(String(config.maAppId || '').trim());
	}
	if (platform === CLIENT_PLATFORM.H5_WECHAT) {
		return config.mpLoginEnabled === true && Boolean(String(config.mpAppId || '').trim());
	}
	return false;
};
