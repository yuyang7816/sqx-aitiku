/**
 * 通用文件上传：POST /file/upload，返回可访问 url（头像等场景复用）
 */
import config from '@/common/config';
import { buildAuthHeader, getToken } from '@/common/auth-token';
import { handleUnauthorized, isUnauthorizedResponse } from '@/common/api';

const isRemoteUrl = (path = '') => /^https?:\/\//i.test(String(path).trim());

/** 将服务端返回的相对/绝对路径转为可展示/提交的完整地址 */
export const resolveUploadedFileUrl = (url = '') => {
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
 * 上传本地图片（POST /file/upload）
 * @param {string} filePath - uni.chooseImage / up-upload 返回的临时路径
 * @returns {Promise<string>} 可访问图片 URL
 */
export const uploadLocalImage = (filePath) => {
	const normalizedPath = String(filePath || '').trim();
	if (!normalizedPath) {
		const err = new Error('未获取到图片路径');
		err.msg = '未获取到图片路径';
		return Promise.reject(err);
	}
	if (isRemoteUrl(normalizedPath)) {
		return Promise.resolve(normalizedPath);
	}

	return new Promise((resolve, reject) => {
		const token = getToken();
		const authHeaders = token ? buildAuthHeader(token) : {};

		uni.uploadFile({
			url: `${config.APIHOST}/file/upload`,
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
							reject({ msg: '登录已失效，请重新登录' });
							return;
						}
						reject({ msg: '图片上传响应解析失败' });
						return;
					}
				}

				if (isUnauthorizedResponse({ statusCode: result.statusCode, data: body })) {
					handleUnauthorized(body && body.msg);
					reject({ msg: (body && body.msg) || '登录已失效，请重新登录' });
					return;
				}

				if (body && body.code === 0 && body.data) {
					const url = body.data.url || body.data.path || body.data.relativePath || '';
					if (!url) {
						reject({ msg: '图片上传成功但未返回地址' });
						return;
					}
					resolve(resolveUploadedFileUrl(url));
					return;
				}

				reject({ msg: (body && body.msg) || '图片上传失败' });
			},
			fail: () => {
				reject({ msg: '图片上传失败，请检查网络' });
			}
		});
	});
};
