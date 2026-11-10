import appConfig from '@/common/config';

/** 服务端 API 根路径（含 /sqx_fast 前缀，与 server 端口 9135 一致） */
export const API_BASE_URL = appConfig.APIHOST;

/**
 * 拼接完整请求 URL
 * @param {string} path - 相对路径，如 /app/banner
 */
export const buildApiUrl = (path = '') => {
	const normalized = String(path || '').trim();
	if (!normalized) {
		return API_BASE_URL;
	}
	if (/^https?:\/\//i.test(normalized)) {
		return normalized;
	}
	return `${API_BASE_URL}${normalized.startsWith('/') ? normalized : `/${normalized}`}`;
};
