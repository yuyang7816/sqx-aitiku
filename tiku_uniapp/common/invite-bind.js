/**
 * 模块二十二 P1#15 · 推广邀请码绑定（inviterId / inviteCode）
 * 解析 scene/query 中的邀请码写入 pendingInviterId，注册/首次登录时随 auth 请求提交 superiorInvitationCode。
 * 捕获入口：App.vue onLaunch/onShow（见模块十二 TC-M12-004）。
 * 服务端扫码 scene 即为 inviteCode（如 INV8000），对应 tb_user.superior_invitation_code。
 */
import { getToken } from '@/common/auth-token';
import { PENDING_INVITER_ID_KEY } from '@/common/storage-keys';

export { PENDING_INVITER_ID_KEY } from '@/common/storage-keys';

const INVITE_CODE_PATTERN = /^[A-Za-z0-9]{4,32}$/;

/** 海报 scene 保留字，不可当作邀请码（与 deep-link.js POSTER_SCENE_VALUES 一致） */
const RESERVED_NON_INVITE_SCENES = new Set(['report', 'promo']);

/**
 * 规范化 scene/query 原始值为邀请码
 * @param {unknown} raw
 * @returns {string}
 */
export const normalizeInviteCode = (raw) => {
	if (raw == null) {
		return '';
	}
	let text = String(raw).trim();
	if (!text) {
		return '';
	}
	try {
		text = decodeURIComponent(text).trim();
	} catch (e) {
		// 已是明文
	}
	if (!text) {
		return '';
	}
	if (RESERVED_NON_INVITE_SCENES.has(text)) {
		return '';
	}
	if (text.includes('=')) {
		const params = new URLSearchParams(text.replace(/\//g, '&'));
		const fromKv = params.get('inviteCode')
			|| params.get('inviterId')
			|| params.get('inviter');
		if (fromKv && INVITE_CODE_PATTERN.test(fromKv.trim())) {
			return fromKv.trim();
		}
	}
	if (INVITE_CODE_PATTERN.test(text)) {
		return text;
	}
	return '';
};

/**
 * 从启动参数解析邀请码（scene / query.inviteCode / query.inviterId）
 * @param {Record<string, any>} options uni.onLaunch/onShow/onLoad 参数
 * @returns {string}
 */
export const parseInviteCodeFromLaunchOptions = (options = {}) => {
	const candidates = [];
	const query = options.query && typeof options.query === 'object' ? options.query : {};

	candidates.push(
		query.inviteCode,
		query.inviterId,
		query.inviter,
		query.scene
	);

	// #ifdef MP-WEIXIN
	try {
		const launch = typeof uni.getLaunchOptionsSync === 'function'
			? uni.getLaunchOptionsSync()
			: null;
		const launchQuery = launch?.query && typeof launch.query === 'object' ? launch.query : {};
		candidates.push(
			launchQuery.scene,
			launchQuery.inviteCode,
			launchQuery.inviterId,
			launchQuery.inviter
		);
	} catch (e) {
		// ignore
	}
	// #endif

	for (let i = 0; i < candidates.length; i += 1) {
		const code = normalizeInviteCode(candidates[i]);
		if (code) {
			return code;
		}
	}
	return '';
};

/** @returns {string} */
export const getPendingInviterId = () => {
	try {
		const saved = uni.getStorageSync(PENDING_INVITER_ID_KEY);
		return normalizeInviteCode(saved);
	} catch (e) {
		return '';
	}
};

/**
 * 写入待绑定邀请码（已登录用户忽略，避免覆盖已有上下级）
 * @param {Record<string, any>} options
 * @returns {boolean} 是否写入
 */
export const captureInviteFromLaunchOptions = (options = {}) => {
	if (getToken()) {
		return false;
	}
	const code = parseInviteCodeFromLaunchOptions(options);
	if (!code) {
		return false;
	}
	uni.setStorageSync(PENDING_INVITER_ID_KEY, code);
	return true;
};

/** 登录成功后清除待绑定邀请码 */
export const clearPendingInviterId = () => {
	try {
		uni.removeStorageSync(PENDING_INVITER_ID_KEY);
	} catch (e) {
		// ignore
	}
};

/**
 * 组装 auth 请求体中的上级邀请码字段
 * @returns {{ superiorInvitationCode?: string }}
 */
export const buildAuthInvitePayload = () => {
	const code = getPendingInviterId();
	if (!code) {
		return {};
	}
	return { superiorInvitationCode: code };
};
