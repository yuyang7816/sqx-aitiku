/**
 * 模块二十二 P0#1 · HTTP 请求层（统一 API 层）
 * 封装 uni.request + Token，对接 tiku_server（baseURL、401 拦截、业务错误码 Toast）
 * 全局可用：main.js httpPlugin.$http 与本模块 request 为同一对象
 */
export { API_BASE_URL, buildApiUrl } from './config';
export {
	BUSINESS_SUCCESS_CODE,
	DEFAULT_BIZ_ERROR_MESSAGE,
	isBusinessSuccess,
	resolveBizErrorMessage,
	showBizErrorToast
} from './error';
export {
	AUTH_REQUEST_WHITELIST,
	SILENT_REQUEST_OPTIONS,
	PAGE_LOAD_REQUEST_OPTIONS,
	del,
	get,
	getT,
	handleUnauthorized,
	isAuthWhitelistPath,
	isUnauthorizedResponse,
	patchJson,
	post,
	postJson,
	postT,
	request,
	resolveRequestOptions
} from './request';

export { default } from './request';
