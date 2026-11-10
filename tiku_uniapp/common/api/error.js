/**
 * 业务响应与统一错误 Toast（对齐 server Result { code, msg, data }）
 */

/** 业务成功码（与 server 统一 Result 一致） */
export const BUSINESS_SUCCESS_CODE = 0;

/** 默认业务失败文案（服务端未返回 msg 时兜底） */
export const DEFAULT_BIZ_ERROR_MESSAGE = '操作失败，请稍后重试';

/** 判断 Result 是否为业务成功 */
export const isBusinessSuccess = (body) =>
	Boolean(body && typeof body === 'object' && body.code === BUSINESS_SUCCESS_CODE);

/** 解析可展示的业务错误文案 */
export const resolveBizErrorMessage = (body, fallback = DEFAULT_BIZ_ERROR_MESSAGE) => {
	if (!body || typeof body !== 'object') {
		return fallback;
	}
	const msg = body.msg || body.message;
	return msg ? String(msg) : fallback;
};

/** 展示业务错误 Toast */
export const showBizErrorToast = (body, fallback = DEFAULT_BIZ_ERROR_MESSAGE) => {
	uni.showToast({
		title: resolveBizErrorMessage(body, fallback),
		icon: 'none'
	});
};
