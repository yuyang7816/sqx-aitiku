/**
 * 海报 canvas 远程图片加载：OSS/CDN 跨域时走 API 同源代理，再 downloadFile + getImageInfo
 */
import { buildApiUrl } from '@/common/api/config';
import config from '@/common/config';

const PROXY_PATH = '/app/common/imageProxy';

const resolveApiOrigin = () => {
	try {
		return new URL(buildApiUrl('/')).origin;
	} catch (e) {
		return '';
	}
};

/** 判断是否为远程 http(s) 图片 */
export const isRemotePosterImageUrl = (url = '') => /^https?:\/\//i.test(String(url || '').trim());

/** 将 OSS/CDN 等跨域图片改写为 API 同源代理地址（canvas 可绘制） */
export const resolvePosterCanvasImageUrl = (url = '') => {
	const normalized = String(url || '').trim();
	if (!normalized) {
		return '';
	}
	if (!isRemotePosterImageUrl(normalized)) {
		if (normalized.startsWith('/')) {
			return `${config.APIHOST}${normalized}`;
		}
		return `${config.APIHOST}/${normalized.replace(/^\/+/, '')}`;
	}

	const apiOrigin = resolveApiOrigin();
	if (apiOrigin) {
		try {
			if (new URL(normalized).origin === apiOrigin) {
				return normalized;
			}
		} catch (e) {
			// ignore invalid url
		}
	}

	return buildApiUrl(`${PROXY_PATH}?url=${encodeURIComponent(normalized)}`);
};

const getImageInfo = (src) => new Promise((resolve, reject) => {
	uni.getImageInfo({
		src,
		success: (res) => resolve({
			path: res.path || src,
			width: Number(res.width) || 0,
			height: Number(res.height) || 0
		}),
		fail: (err) => reject(err || new Error('图片信息读取失败'))
	});
});

const downloadToTempPath = (url) => new Promise((resolve, reject) => {
	uni.downloadFile({
		url,
		success: (res) => {
			const code = Number(res.statusCode) || 0;
			if (code >= 200 && code < 400 && res.tempFilePath) {
				resolve(res.tempFilePath);
				return;
			}
			reject(new Error(`图片下载失败(${code || 'unknown'})`));
		},
		fail: (err) => reject(err || new Error('图片下载失败'))
	});
});

/**
 * 加载海报用图片（返回 canvas 可绘制的本地 path + 宽高）
 * @param {string} url 原始图片地址（OSS/CDN 或本站相对路径）
 */
export const loadPosterImageInfo = async (url = '') => {
	const src = String(url || '').trim();
	if (!src) {
		throw new Error('未配置邀请海报底图');
	}

	const canvasUrl = resolvePosterCanvasImageUrl(src);

	if (isRemotePosterImageUrl(canvasUrl)) {
		const tempPath = await downloadToTempPath(canvasUrl);
		const info = await getImageInfo(tempPath);
		return info;
	}

	return getImageInfo(canvasUrl);
};

/** 加载二维码/小图标，失败时返回空字符串 */
export const loadPosterImagePath = async (url = '', fallbackPath = '') => {
	const src = String(url || '').trim();
	if (!src) {
		return String(fallbackPath || '').trim();
	}
	try {
		const info = await loadPosterImageInfo(src);
		return info.path || src;
	} catch (e) {
		const fallback = String(fallbackPath || '').trim();
		if (!fallback) {
			return '';
		}
		try {
			const info = await getImageInfo(fallback);
			return info.path || fallback;
		} catch (err) {
			return '';
		}
	}
};
