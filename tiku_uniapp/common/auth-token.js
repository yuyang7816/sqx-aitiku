import { TOKEN_KEY, LEGACY_TOKEN_KEY } from '@/common/storage-keys';
import {
	decryptSensitiveText,
	encryptSensitiveText,
	isEncryptedStorageValue
} from '@/common/storage-crypto';

export { TOKEN_KEY } from '@/common/storage-keys';

const readStoredToken = (key) => {
	const raw = uni.getStorageSync(key);
	if (!raw) {
		return '';
	}
	return decryptSensitiveText(String(raw));
};

/** 读取本地 token（优先 aiQuestionToken，兼容 legacy token） */
export const getToken = () => {
	const token = readStoredToken(TOKEN_KEY);
	if (token) {
		return token;
	}
	const legacy = readStoredToken(LEGACY_TOKEN_KEY);
	if (!legacy) {
		return '';
	}
	// 读到 legacy 明文时立即迁移至加密主键
	saveToken(legacy);
	return legacy;
};

/** 登录成功后持久化 token（AES 加密，Storage 无明文） */
export const saveToken = (token) => {
	const value = token ? String(token).trim() : '';
	if (!value) {
		clearToken();
		return '';
	}
	let stored = value;
	try {
		stored = encryptSensitiveText(value);
	} catch (error) {
		console.warn('[auth-token] token encrypt failed, fallback to plaintext storage', error);
		stored = value;
	}
	uni.setStorageSync(TOKEN_KEY, stored);
	try {
		uni.removeStorageSync(LEGACY_TOKEN_KEY);
	} catch (e) {
		// ignore
	}
	return value;
};

/** 登出或 401 时清除 token */
export const clearToken = () => {
	try {
		uni.removeStorageSync(TOKEN_KEY);
		uni.removeStorageSync(LEGACY_TOKEN_KEY);
	} catch (e) {
		// ignore
	}
};

/** 构建 Authorization 请求头（Bearer JWT） */
export const buildAuthHeader = (token) => {
	if (!token) {
		return {};
	}
	return {
		Authorization: `Bearer ${token}`
	};
};

/** 供迁移脚本判断 token 是否仍为明文 */
export const isPlaintextTokenValue = (value) => {
	if (value === undefined || value === null || value === '') {
		return false;
	}
	return !isEncryptedStorageValue(String(value));
};
