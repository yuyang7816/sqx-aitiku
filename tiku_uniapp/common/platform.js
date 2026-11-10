/**
 * 多端运行时平台识别（uni-app：小程序 / H5 / H5 微信内置浏览器 / App）
 */

export const CLIENT_PLATFORM = {
	MP_WEIXIN: 'mp-weixin',
	H5_WECHAT: 'h5-wechat',
	H5: 'h5',
	APP_PLUS: 'app-plus',
	MP_ALIPAY: 'mp-alipay'
};

/** 是否 H5 微信内置浏览器 */
export const isWechatBrowser = () => {
	// #ifdef H5
	try {
		return /micromessenger/i.test(window.navigator.userAgent || '');
	} catch (e) {
		return false;
	}
	// #endif
	return false;
};

/** 当前客户端平台标识（与 POST /app/pay clientPlatform 一致） */
export const getClientPlatform = () => {
	// #ifdef MP-WEIXIN
	return CLIENT_PLATFORM.MP_WEIXIN;
	// #endif
	// #ifdef MP-ALIPAY
	return CLIENT_PLATFORM.MP_ALIPAY;
	// #endif
	// #ifdef APP-PLUS
	return CLIENT_PLATFORM.APP_PLUS;
	// #endif
	// #ifdef H5
	return isWechatBrowser() ? CLIENT_PLATFORM.H5_WECHAT : CLIENT_PLATFORM.H5;
	// #endif
	return CLIENT_PLATFORM.H5;
};

/** 注册/登录上报 platform 字段（与服务端 UserEntity.platform 对齐） */
export const getAuthPlatformLabel = () => {
	const platform = getClientPlatform();
	if (platform === CLIENT_PLATFORM.MP_WEIXIN) {
		return 'MA';
	}
	if (platform === CLIENT_PLATFORM.H5_WECHAT) {
		return 'MP';
	}
	if (platform === CLIENT_PLATFORM.APP_PLUS) {
		return 'APP';
	}
	return 'H5';
};

/** 是否支持微信一键登录 */
export const supportsWechatLogin = () => {
	// #ifdef MP-WEIXIN
	return true;
	// #endif
	// #ifdef APP-PLUS
	return true;
	// #endif
	// #ifdef H5
	return isWechatBrowser();
	// #endif
	return false;
};

/** 是否 APP 端 */
export const isAppPlus = () => getClientPlatform() === CLIENT_PLATFORM.APP_PLUS;

/** 是否小程序端 */
export const isMpWeixin = () => getClientPlatform() === CLIENT_PLATFORM.MP_WEIXIN;

/** 是否 H5 微信内置浏览器 */
export const isH5Wechat = () => getClientPlatform() === CLIENT_PLATFORM.H5_WECHAT;

/** 请求头 clientType（用户端固定 1） */
export const CLIENT_TYPE_HEADER = '1';
