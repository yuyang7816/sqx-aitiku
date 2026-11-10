/**
 * 模块二十二 P0#6 · OCR 搜题服务
 * POST /app/ocr/upload + POST /app/ocr/search，失败返回空结果
 */
import config from '@/common/config';
import { buildAuthHeader, getToken } from '@/common/auth-token';
import {
	buildApiUrl,
	handleUnauthorized,
	isUnauthorizedResponse,
	postJson,
	SILENT_REQUEST_OPTIONS
} from '@/common/api';

/** 服务端 OcrError.QUOTA_EXCEEDED 业务码 */
export const OCR_QUOTA_EXCEEDED_CODE = 1050008;

const assertApiSuccess = (res, fallbackMsg) => {
	if (res && res.code === 0) {
		return res.data;
	}
	const err = new Error((res && res.msg) || fallbackMsg);
	err.msg = (res && res.msg) || fallbackMsg;
	if (res && res.code != null) {
		err.code = res.code;
	}
	throw err;
};

/** 将服务端 AppOcrMatchItemVO 规范为 ocr-result 页展示结构 */
export const normalizeOcrMatchItem = (item = {}) => ({
	questionId: item.questionId,
	questionCode: item.questionCode || '',
	title: item.title || '',
	answer: item.answer || '',
	analysis: item.analysis || '',
	tips: Array.isArray(item.tips) ? item.tips : [],
	similarity: item.similarity
});

const isRemoteUrl = (path = '') => /^https?:\/\//i.test(String(path).trim());

/** 将服务端返回的相对/绝对路径转为可展示的图片地址 */
export const resolveOcrImageUrl = (url = '') => {
	const normalized = String(url || '').trim();
	if (!normalized) {
		return '';
	}
	if (isRemoteUrl(normalized)) {
		return normalized;
	}
	if (normalized.startsWith('/')) {
		return `${config.APIHOST}${normalized}`;
	}
	return `${config.APIHOST}/${normalized}`;
};

/**
 * 上传本地拍照/相册图片（POST /app/ocr/upload）
 * @param {string} filePath - uni.chooseImage 返回的临时路径
 * @returns {Promise<string>} 可访问 imageUrl
 */
export const uploadOcrImage = (filePath) => {
	const normalizedPath = String(filePath || '').trim();
	if (!normalizedPath) {
		const err = new Error('未获取到图片路径');
		err.msg = '未获取到图片路径';
		return Promise.reject(err);
	}

	return new Promise((resolve, reject) => {
		const token = getToken();
		const authHeaders = token ? buildAuthHeader(token) : {};

		uni.uploadFile({
			url: buildApiUrl('/app/ocr/upload'),
			filePath: normalizedPath,
			name: 'file',
			header: {
				...authHeaders,
				...(token ? { token } : {})
			},
			success: (result) => {
				let body = result.data;
				if (typeof body === 'string') {
					try {
						body = JSON.parse(body);
					} catch (e) {
						if (isUnauthorizedResponse(result)) {
							handleUnauthorized();
							reject({ code: 401, msg: '登录已失效，请重新登录' });
							return;
						}
						reject({ msg: '图片上传响应解析失败' });
						return;
					}
				}

				if (isUnauthorizedResponse({ statusCode: result.statusCode, data: body })) {
					handleUnauthorized(body && body.msg);
					reject({ code: 401, msg: (body && body.msg) || '登录已失效，请重新登录' });
					return;
				}

				try {
					const data = assertApiSuccess(body, '图片上传失败');
					const imageUrl = data.url || data.path || '';
					if (!imageUrl) {
						reject({ msg: '未获取到图片访问地址' });
						return;
					}
					resolve(imageUrl);
				} catch (e) {
					reject(e);
				}
			},
			fail: (error) => {
				reject(error);
			}
		});
	});
};

/**
 * OCR 搜题（POST /app/ocr/search）
 * @param {{ text?: string, imageUrl?: string }} params
 */
export const searchOcr = async ({ text = '', imageUrl = '' } = {}) => {
	const payload = {};
	const normalizedText = String(text || '').trim();
	const normalizedImageUrl = String(imageUrl || '').trim();

	if (normalizedText) {
		payload.text = normalizedText;
	}
	if (normalizedImageUrl) {
		payload.imageUrl = normalizedImageUrl;
	}
	if (!payload.text && !payload.imageUrl) {
		const err = new Error('请提供题干文本或图片');
		err.msg = '请提供题干文本或图片';
		throw err;
	}

	const res = await postJson('/app/ocr/search', payload, SILENT_REQUEST_OPTIONS);
	const data = assertApiSuccess(res, 'OCR 搜题失败');
	const matches = Array.isArray(data.matches) ? data.matches.map(normalizeOcrMatchItem) : [];

	return {
		recognizedText: data.recognizedText || normalizedText,
		matches,
		fromApi: true,
		quota: data.quota || null
	};
};

/** API 失败时的空 OCR 结果 */
export const getFallbackOcrResult = (text = '') => ({
	recognizedText: String(text || '').trim(),
	matches: [],
	fromApi: false
});

/**
 * 完整识别流程：本地图片先上传，再调用搜题 API
 * @param {{ text?: string, imagePath?: string }} params
 */
export const runOcrRecognition = async ({ text = '', imagePath = '' } = {}) => {
	const normalizedText = String(text || '').trim();
	const normalizedImagePath = String(imagePath || '').trim();

	let imageUrl = '';
	if (normalizedImagePath) {
		imageUrl = isRemoteUrl(normalizedImagePath)
			? normalizedImagePath
			: await uploadOcrImage(normalizedImagePath);
	}

	const searchResult = await searchOcr({
		text: normalizedText,
		imageUrl
	});

	return {
		...searchResult,
		imageUrl
	};
};
