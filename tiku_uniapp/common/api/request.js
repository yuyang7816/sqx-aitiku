import { buildAuthHeader, getToken } from '@/common/auth-token';
import { performLogoutAndRedirect } from '@/common/app-data';
import { beginRequestLoading, endRequestLoading } from '@/common/request-loading';
import { buildApiUrl } from './config';
import { isBusinessSuccess, showBizErrorToast } from './error';
import { CLIENT_TYPE_HEADER, getClientPlatform } from '@/common/platform';

/**
 * 模块二十二 P0#1 · uni.request 核心封装（Token 注入 / 401 / Loading / Toast）
 *
 * 无需携带 Token 的请求白名单（匿名登录 / 验证码，与 server @Login 未标注接口一致）
 * 验收：除下列路径外，已登录态请求均应带 Authorization: Bearer {token}
 */
export const AUTH_REQUEST_WHITELIST = [
	'/app/auth/sms',
	'/app/auth/wechat',
	'/app/auth/mp',
	'/app/auth/phonePasswordLogin',
	'/app/auth/phoneRegister',
	'/app/auth/resetPassword',
	'/app/verifyCode',
	'/app/banner',
	'/app/config/customer',
	'/app/config/wechat',
	'/app/config/info',
	'/app/setup/options',
	'/app/question/chapter/tree',
	'/app/vipPlan',
	'/app/vipRights',
	'/app/homeShortcut',
	'/app/practiceEntry'
];

/** 变更类 HTTP 方法：默认展示 Loading 与失败 Toast */
const MUTATING_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

/** 页面级 GET 拉数时可传 { loading: true }；静默后台请求传 { silent: true } */
export const SILENT_REQUEST_OPTIONS = { loading: false, toast: false };

/** 页面首屏拉数：GET 也展示 Loading */
export const PAGE_LOAD_REQUEST_OPTIONS = { loading: true };

/** 从完整 URL 或相对路径判断是否白名单（兼容 /sqx_fast 前缀） */
export const isAuthWhitelistPath = (url) => {
	if (!url) {
		return false;
	}
	const path = String(url)
		.replace(/^https?:\/\/[^/]+/i, '')
		.replace(/^\/sqx_fast(?=\/|$)/, '')
		.split('?')[0];
	return AUTH_REQUEST_WHITELIST.some((item) => path === item || path.endsWith(item));
};

/** 防止并发 401 重复弹 Toast / reLaunch */
let isHandling401 = false;

/**
 * 判断 HTTP 响应是否为未授权（401）
 * - HTTP statusCode === 401
 * - 或 body Result.code === 401（服务端统一 Result 格式）
 */
export const isUnauthorizedResponse = (result) => {
	if (!result) {
		return false;
	}
	if (result.statusCode === 401) {
		return true;
	}
	const body = result.data;
	return body && typeof body === 'object' && body.code === 401;
};

/**
 * 模块二十四 24.3 · Token 过期 401 全局拦截：清除鉴权态与 token，Toast 并 reLaunch 登录页
 * @param {string} [toastMsg] - Toast 文案，默认使用服务端 msg
 */
export const handleUnauthorized = (toastMsg) => {
	if (isHandling401) {
		return;
	}
	isHandling401 = true;

	performLogoutAndRedirect({
		toastTitle: toastMsg || '登录已失效，请重新登录',
		onComplete: () => {
			isHandling401 = false;
		}
	});
};

/**
 * 解析单次请求交互选项
 * @param {string} method
 * @param {{ loading?: boolean, toast?: boolean, silent?: boolean, loadingText?: string }} [options]
 */
export const resolveRequestOptions = (method, options = {}) => {
	const isMutating = MUTATING_METHODS.has(String(method || '').toUpperCase());
	const silent = options.silent === true;
	return {
		loading: silent ? false : (options.loading ?? isMutating),
		toast: silent ? false : (options.toast ?? isMutating),
		loadingText: options.loadingText || (isMutating ? '提交中...' : '加载中...'),
		skipUnauthorizedRedirect: options.skipUnauthorizedRedirect === true
	};
};

const showNetworkErrorToast = (message) => {
	uni.showToast({
		title: message || '网络异常，请稍后重试',
		icon: 'none'
	});
};

/**
 * 统一处理 Result 业务错误码 Toast（401 单独走登出流程）
 * @param {object|null} body
 * @param {{ toast?: boolean }} requestOptions
 */
const handleBusinessErrorFeedback = (body, requestOptions) => {
	if (!body || typeof body !== 'object' || typeof body.code !== 'number') {
		return;
	}
	if (body.code === 401 || isBusinessSuccess(body)) {
		return;
	}
	if (requestOptions.toast) {
		showBizErrorToast(body);
	}
};

const sendRequest = (url, data, method, header, attachToken = true, options = {}) => {
	const requestOptions = resolveRequestOptions(method, options);
	const shouldShowLoading = requestOptions.loading;

	if (shouldShowLoading) {
		beginRequestLoading(requestOptions.loadingText);
	}

	return new Promise((succ, error) => {
		const shouldAttachToken = attachToken && !isAuthWhitelistPath(url);
		const token = shouldAttachToken ? getToken() : '';
		const authHeaders = shouldAttachToken && token ? buildAuthHeader(token) : {};

		uni.request({
			url,
			data,
			method,
			header: {
				'content-type': header,
				clientType: CLIENT_TYPE_HEADER,
				clientPlatform: getClientPlatform(),
				...authHeaders,
				// legacy 请求头，服务端仍兼容
				...(shouldAttachToken && token ? {
					token
				} : {})
			},
			success: (result) => {
				if (isUnauthorizedResponse(result)) {
					const msg = result.data && result.data.msg;
					if (!requestOptions.skipUnauthorizedRedirect) {
						handleUnauthorized(msg);
					}
					error({
						code: 401,
						msg: msg || '暂未登录或token已经过期',
						data: null
					});
					return;
				}

				const body = result.data;
				handleBusinessErrorFeedback(body, requestOptions);
				succ(body);
			},
			fail: (e) => {
				if (requestOptions.toast) {
					showNetworkErrorToast(e?.errMsg || e?.msg);
				}
				error(e);
			},
			complete: () => {
				if (shouldShowLoading) {
					endRequestLoading();
				}
			}
		});
	});
};

const createRequestMethod = (method, defaultHeader) => (url, data, third, fourth) => {
	let header = defaultHeader;
	let options = {};

	if (typeof third === 'string') {
		header = third;
		options = fourth || {};
	} else if (third && typeof third === 'object') {
		options = third;
	}

	const fullUrl = buildApiUrl(url);
	return sendRequest(fullUrl, data, method, header, true, options);
};

/** POST 表单请求 */
export const post = createRequestMethod('POST', 'application/x-www-form-urlencoded');

/** POST 表单请求（legacy 别名） */
export const postT = createRequestMethod('POST', 'application/x-www-form-urlencoded');

/** POST JSON 请求 */
export const postJson = createRequestMethod('POST', 'application/json');

/** GET 请求（legacy 别名） */
export const getT = createRequestMethod('GET', 'application/x-www-form-urlencoded');

/** GET 请求 */
export const get = createRequestMethod('GET', 'application/x-www-form-urlencoded');

/** DELETE 请求（query 参数走 data，与 GET 一致） */
export const del = createRequestMethod('DELETE', 'application/x-www-form-urlencoded');

/** PATCH JSON 请求（错题本掌握态等） */
export const patchJson = createRequestMethod('PATCH', 'application/json');

/** 统一 request 入口（供 $http 与模块 API 复用） */
export const request = {
	get,
	getT,
	post,
	postT,
	postJson,
	del,
	patchJson
};

export default request;
