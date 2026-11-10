/**
 * 账号密码登录 / 注册 / 忘记密码 / 登录态写入
 */
import { applyPostLoginProfile, getAuthState, loginUser, redirectAfterLogin } from '@/common/app-data';
import { saveToken } from '@/common/auth-token';
import { postJson, SILENT_REQUEST_OPTIONS } from '@/common/api';
import { buildAuthInvitePayload, clearPendingInviterId } from '@/common/invite-bind';
import { buildAuthLoginBody } from '@/common/login-rules';
import { parseLoginApiError, parseNetworkError } from '@/common/login-error';
import { syncUserInfoAfterLogin } from '@/common/user-info';
import { getAuthPlatformLabel } from '@/common/platform';

const assertAuthSuccess = (res, fallbackMsg) => {
	if (res && res.code === 0 && res.data?.token) {
		return res.data;
	}
	const loginError = parseLoginApiError(res, 'account-login');
	if (loginError) {
		const err = new Error(loginError.message);
		err.loginError = loginError;
		throw err;
	}
	const err = new Error((res && res.msg) || fallbackMsg);
	err.msg = (res && res.msg) || fallbackMsg;
	throw err;
};

/**
 * 登录成功后写入 token + 本地鉴权 + 拉取 profile
 */
export const completeAuthSession = async ({
	token,
	phone = '',
	loginType = 'account',
	profilePatch = null
} = {}) => {
	if (!token) {
		throw new Error('登录凭证无效');
	}
	const prevAuth = getAuthState();
	saveToken(token);
	loginUser({
		phone: phone || prevAuth.phone || '',
		loginType
	});
	clearPendingInviterId();
	if (profilePatch && typeof profilePatch.ready === 'boolean') {
		applyPostLoginProfile({
			profilePatch,
			identityKey: phone || prevAuth.phone,
			prevIdentityKey: prevAuth.phone
		});
	}
	await syncUserInfoAfterLogin({
		profilePatch,
		identityKey: phone || prevAuth.phone,
		prevIdentityKey: prevAuth.phone
	});
};

/** 发送短信验证码 */
export const sendAuthVerifyCode = async (phone) => {
	const res = await postJson('/app/verifyCode', {
		phone: String(phone || '').trim()
	}, SILENT_REQUEST_OPTIONS);
	if (res && res.code === 0) {
		return res;
	}
	const loginError = parseLoginApiError(res, 'verify-code');
	if (loginError) {
		const err = new Error(loginError.message);
		err.loginError = loginError;
		throw err;
	}
	throw parseNetworkError();
};

/** 手机号 + 密码登录 POST /app/auth/phonePasswordLogin */
export const loginByPhonePassword = async ({
	phoneNo = '',
	password = '',
	agreedProtocol = true
} = {}) => {
	const res = await postJson('/app/auth/phonePasswordLogin', buildAuthLoginBody({
		phoneNo: String(phoneNo || '').trim(),
		password: String(password || ''),
		...buildAuthInvitePayload()
	}, agreedProtocol), SILENT_REQUEST_OPTIONS);
	const data = assertAuthSuccess(res, '账号或密码错误');
	await completeAuthSession({
		token: data.token,
		phone: phoneNo,
		loginType: 'account',
		profilePatch: data.profile
	});
	return data;
};

/** 手机号注册 POST /app/auth/phoneRegister */
export const registerByPhone = async ({
	phoneNo = '',
	code = '',
	password = '',
	username = ''
} = {}) => {
	const res = await postJson('/app/auth/phoneRegister', {
		phoneNo: String(phoneNo || '').trim(),
		code: String(code || '').trim(),
		password: String(password || ''),
		username: String(username || '').trim() || String(phoneNo || '').trim(),
		platform: getAuthPlatformLabel(),
		...buildAuthInvitePayload()
	}, SILENT_REQUEST_OPTIONS);
	const data = assertAuthSuccess(res, '注册失败，请稍后重试');
	await completeAuthSession({
		token: data.token,
		phone: phoneNo,
		loginType: 'account',
		profilePatch: data.profile
	});
	return data;
};

/** 忘记密码 POST /app/auth/resetPassword */
export const resetPasswordByPhone = async ({
	phone = '',
	code = '',
	password = ''
} = {}) => {
	const res = await postJson('/app/auth/resetPassword', {
		phone: String(phone || '').trim(),
		code: String(code || '').trim(),
		password: String(password || '')
	}, SILENT_REQUEST_OPTIONS);
	if (res && res.code === 0) {
		return res;
	}
	const loginError = parseLoginApiError(res, 'reset-password');
	if (loginError) {
		const err = new Error(loginError.message);
		err.loginError = loginError;
		throw err;
	}
	const err = new Error((res && res.msg) || '重置密码失败');
	err.msg = (res && res.msg) || '重置密码失败';
	throw err;
};

/** 登录/注册成功后跳转 */
export const finishAuthNavigation = () => {
	redirectAfterLogin();
};
