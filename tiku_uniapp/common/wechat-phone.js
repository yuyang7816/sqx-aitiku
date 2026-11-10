/**
 * 小程序官方 getPhoneNumber 绑定手机号（须先完成微信登录拿到 JWT）
 */
import { postJson, SILENT_REQUEST_OPTIONS } from '@/common/api';
import { completeAuthSession } from '@/common/auth-api';
import { getAuthState } from '@/common/app-data';
import { getToken } from '@/common/auth-token';
import { isMpWeixin } from '@/common/platform';

/** 小程序已登录但未绑定手机号 */
export const needsWechatPhoneBind = () => {
	if (!isMpWeixin() || !getToken()) {
		return false;
	}
	const auth = getAuthState();
	return auth.loggedIn === true && !String(auth.phone || '').trim();
};

/**
 * 使用 getPhoneNumber 回调 code 调用服务端微信官方 API 换号并合并账号
 * @param {string} phoneCode - event.detail.code
 */
export const bindWechatPhoneByCode = async (phoneCode = '') => {
	const code = String(phoneCode || '').trim();
	if (!code) {
		const err = new Error('需要授权手机号才能继续');
		err.msg = '需要授权手机号才能继续';
		throw err;
	}
	const res = await postJson('/app/auth/wechat/bindPhone', { phoneCode: code }, SILENT_REQUEST_OPTIONS);
	if (res && res.code === 0 && res.data?.token) {
		await completeAuthSession({
			token: res.data.token,
			phone: res.data.user?.phone || '',
			loginType: 'wechat-mp',
			profilePatch: res.data.profile
		});
		return res.data;
	}
	const err = new Error((res && res.msg) || '绑定手机号失败，请重试');
	err.msg = (res && res.msg) || '绑定手机号失败，请重试';
	if (res && res.code != null) {
		err.code = res.code;
	}
	throw err;
};

/** 解析 getPhoneNumber 事件是否成功返回 code */
export const extractWechatPhoneCode = (event = {}) => {
	if (!event || event.detail?.errMsg !== 'getPhoneNumber:ok') {
		return '';
	}
	return String(event.detail?.code || '').trim();
};
