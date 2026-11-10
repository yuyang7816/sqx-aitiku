/**
 * 模块二十 · 深链与 scene/query 解析
 * 支持 inviteCode（邀请绑定，配合 invite-bind）与 poster（report/promo 海报页）直达。
 */
import { POSTER_PAGE, buildReportPosterUrl, PROMO_POSTER_URL } from '@/common/routes';
import { normalizeInviteCode } from '@/common/invite-bind';
import { getToken } from '@/common/auth-token';
import { PENDING_DEEP_LINK_URL_KEY } from '@/common/storage-keys';

/** 海报 scene 取值（与 poster.vue onLoad 一致） */
export const POSTER_SCENE_VALUES = ['report', 'promo'];

/** 深链落地页 route（与 getCurrentPages[].route 一致，无前导 /） */
export const DEEP_LINK_ROUTE_PREFIXES = [
	'package/common/poster'
];

const MONTH_PATTERN = /^\d{4}-\d{2}$/;

/**
 * 是否为深链落地页（路由守卫应放行，避免被 reLaunch 至 Tab/计划页）
 * @param {string} route
 * @returns {boolean}
 */
export const isDeepLinkRoute = (route = '') => {
	const normalized = String(route || '').trim().replace(/^\//, '');
	if (!normalized) {
		return false;
	}
	return DEEP_LINK_ROUTE_PREFIXES.some((prefix) => normalized === prefix);
};

const normalizeMonth = (raw = '') => {
	const key = String(raw || '').trim();
	return MONTH_PATTERN.test(key) ? key : '';
};

const decodeSceneParam = (raw) => {
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
	return text;
};

const buildPosterDeepLinkUrl = (scene = '', month = '') => {
	const normalizedScene = String(scene || '').trim();
	if (!POSTER_SCENE_VALUES.includes(normalizedScene)) {
		return '';
	}
	if (normalizedScene === 'promo') {
		return PROMO_POSTER_URL;
	}
	return buildReportPosterUrl(normalizeMonth(month));
};

/**
 * 从 WeChat scene 或 query 字符串解析 poster=report|promo
 * @param {unknown} raw
 * @returns {{ scene: string, month: string } | null}
 */
export const parsePosterFromSceneString = (raw) => {
	const text = decodeSceneParam(raw);
	if (!text) {
		return null;
	}
	if (POSTER_SCENE_VALUES.includes(text)) {
		return { scene: text, month: '' };
	}
	if (!text.includes('=')) {
		return null;
	}
	const params = new URLSearchParams(text.replace(/\//g, '&'));
	const posterScene = String(params.get('poster') || params.get('scene') || '').trim();
	if (!POSTER_SCENE_VALUES.includes(posterScene)) {
		return null;
	}
	return {
		scene: posterScene,
		month: normalizeMonth(params.get('month'))
	};
};

const parseLaunchQuery = (options = {}) => (
	options.query && typeof options.query === 'object' ? options.query : {}
);

/**
 * 从启动 path + query 解析海报深链（小程序分享卡片直达分包）
 * @param {string} path
 * @param {Record<string, any>} query
 * @returns {string}
 */
const resolvePosterFromLaunchPath = (path = '', query = {}) => {
	const normalizedPath = String(path || '').trim().replace(/^\//, '');
	if (normalizedPath !== 'package/common/poster') {
		return '';
	}
	const scene = String(query.scene || query.poster || 'report').trim();
	const posterScene = POSTER_SCENE_VALUES.includes(scene) ? scene : 'report';
	return buildPosterDeepLinkUrl(posterScene, query.month);
};

/**
 * 解析海报深链 URL（不含 inviteCode 登录页）
 * @param {Record<string, any>} options uni.onLaunch/onShow 参数
 * @returns {string}
 */
export const resolvePosterDeepLinkUrl = (options = {}) => {
	const query = parseLaunchQuery(options);

	const fromPath = resolvePosterFromLaunchPath(options.path, query);
	if (fromPath) {
		return fromPath;
	}

	const posterParam = String(query.poster || '').trim();
	if (POSTER_SCENE_VALUES.includes(posterParam)) {
		return buildPosterDeepLinkUrl(posterParam, query.month);
	}

	const sceneParam = String(query.scene || '').trim();
	if (POSTER_SCENE_VALUES.includes(sceneParam)) {
		return buildPosterDeepLinkUrl(sceneParam, query.month);
	}

	const posterFromScene = parsePosterFromSceneString(query.scene);
	if (posterFromScene) {
		return buildPosterDeepLinkUrl(posterFromScene.scene, posterFromScene.month);
	}

	// #ifdef H5
	if (typeof window !== 'undefined' && window.location?.hash) {
		const hash = window.location.hash.replace(/^#/, '');
		if (hash.startsWith('/package/common/poster')) {
			const queryIndex = hash.indexOf('?');
			const hashQuery = queryIndex >= 0 ? hash.slice(queryIndex + 1) : '';
			const hashParams = new URLSearchParams(hashQuery);
			const h5Scene = String(hashParams.get('scene') || hashParams.get('poster') || 'report').trim();
			const h5PosterScene = POSTER_SCENE_VALUES.includes(h5Scene) ? h5Scene : 'report';
			return buildPosterDeepLinkUrl(h5PosterScene, hashParams.get('month'));
		}
	}
	// #endif

	return '';
};

/**
 * 解析 inviteCode 深链目标（未登录时直达登录页，已登录则不再跳转）
 * @param {Record<string, any>} options
 * @returns {string}
 */
export const resolveInviteDeepLinkUrl = (options = {}) => {
	const query = parseLaunchQuery(options);
	if (query.inviteCode || query.inviterId || query.inviter) {
		return '/package/auth/login';
	}
	const inviteFromScene = normalizeInviteCode(query.scene);
	if (inviteFromScene) {
		return '/package/auth/login';
	}
	return '';
};

/**
 * 解析启动深链 URL（poster 优先，其次 inviteCode 登录页）
 * @param {Record<string, any>} options
 * @returns {string}
 */
export const resolveDeepLinkUrl = (options = {}) => {
	const posterUrl = resolvePosterDeepLinkUrl(options);
	if (posterUrl) {
		return posterUrl;
	}
	return resolveInviteDeepLinkUrl(options);
};

/** @returns {string} */
export const getPendingDeepLinkUrl = () => {
	try {
		return String(uni.getStorageSync(PENDING_DEEP_LINK_URL_KEY) || '').trim();
	} catch (e) {
		return '';
	}
};

/**
 * 写入待跳转深链 URL（闪屏结束后 consume）
 * @param {Record<string, any>} options
 * @returns {boolean}
 */
export const captureDeepLinkFromLaunchOptions = (options = {}) => {
	const posterUrl = resolvePosterDeepLinkUrl(options);
	if (posterUrl) {
		uni.setStorageSync(PENDING_DEEP_LINK_URL_KEY, posterUrl);
		return true;
	}
	const inviteUrl = resolveInviteDeepLinkUrl(options);
	if (inviteUrl && !getToken()) {
		uni.setStorageSync(PENDING_DEEP_LINK_URL_KEY, inviteUrl);
		return true;
	}
	return false;
};

/**
 * 读取并清除待跳转深链 URL
 * @returns {string}
 */
export const consumePendingDeepLinkUrl = () => {
	const url = getPendingDeepLinkUrl();
	if (!url) {
		return '';
	}
	try {
		uni.removeStorageSync(PENDING_DEEP_LINK_URL_KEY);
	} catch (e) {
		// ignore
	}
	return url;
};

/**
 * 闪屏初始化完成后优先跳转深链，否则走 getLaunchRoute
 * @param {() => string} getDefaultLaunchRoute
 * @returns {string}
 */
export const resolveRouteAfterSplash = (getDefaultLaunchRoute) => {
	const pending = consumePendingDeepLinkUrl();
	if (pending) {
		return pending;
	}
	return typeof getDefaultLaunchRoute === 'function' ? getDefaultLaunchRoute() : '';
};

/** 海报页路径常量（供外部引用） */
export const POSTER_DEEP_LINK_PAGE = POSTER_PAGE;
