/**
 * @deprecated 请优先从 @/common/api 导入；本文件保留兼容 re-export
 */
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
} from './api';

/** legacy 默认导出，与 httpPlugin.$http 一致 */
export { default } from './api';
