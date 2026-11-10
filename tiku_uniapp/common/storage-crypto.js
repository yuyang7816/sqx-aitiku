/**
 * 模块十九 · 敏感 Storage 字段 AES 加密（文档 19.5）
 * 使用 crypto-js AES，跨 H5 / 小程序 / App 条件编译一致。
 */
import CryptoJS from 'crypto-js';
import { ENCRYPTED_STORAGE_PREFIX } from './storage-keys.js';

/** 应用内固定密钥（防 Storage 明文泄露，非服务端密钥） */
const STORAGE_AES_SECRET = 'aiQuestionUniappStorageKey2026';

/** 小程序 / App 无 Node crypto，需用 wx/crypto API 或 Math.random 填充随机字节 */
const createRandomWordArray = (nBytes = 16) => {
	const words = [];
	const fillFromBytes = (bytes) => {
		for (let i = 0; i < nBytes; i += 4) {
			words.push(
				(((bytes[i] || 0) << 24)
					| ((bytes[i + 1] || 0) << 16)
					| ((bytes[i + 2] || 0) << 8)
					| (bytes[i + 3] || 0)) >>> 0
			);
		}
		return CryptoJS.lib.WordArray.create(words, nBytes);
	};

	try {
		if (typeof wx !== 'undefined' && typeof wx.getRandomValues === 'function') {
			const bytes = new Uint8Array(nBytes);
			wx.getRandomValues(bytes);
			return fillFromBytes(bytes);
		}
	} catch (error) {
		// fall through
	}

	try {
		const cryptoObj = typeof globalThis !== 'undefined' ? globalThis.crypto : null;
		if (cryptoObj && typeof cryptoObj.getRandomValues === 'function') {
			const bytes = new Uint8Array(nBytes);
			cryptoObj.getRandomValues(bytes);
			return fillFromBytes(bytes);
		}
	} catch (error) {
		// fall through
	}

	for (let i = 0; i < nBytes; i += 4) {
		words.push((Math.random() * 0x100000000) >>> 0);
	}
	return CryptoJS.lib.WordArray.create(words, nBytes);
};

/** crypto-js 默认依赖 Node crypto 生成 IV，小程序会抛错导致登录后 saveToken 失败 */
CryptoJS.lib.WordArray.random = createRandomWordArray;

/** 判断 Storage 值是否已加密 */
export const isEncryptedStorageValue = (value) => (
	typeof value === 'string'
	&& value.startsWith(ENCRYPTED_STORAGE_PREFIX)
	&& value.length > ENCRYPTED_STORAGE_PREFIX.length
);

/** 加密敏感明文，写入 Storage 前调用 */
export const encryptSensitiveText = (plainText = '') => {
	const text = String(plainText || '').trim();
	if (!text) {
		return '';
	}
	if (isEncryptedStorageValue(text)) {
		return text;
	}
	const cipher = CryptoJS.AES.encrypt(text, STORAGE_AES_SECRET).toString();
	return `${ENCRYPTED_STORAGE_PREFIX}${cipher}`;
};

/** 解密敏感字段；兼容迁移前明文，解密失败返回空字符串 */
export const decryptSensitiveText = (storedValue = '') => {
	const text = String(storedValue || '');
	if (!text) {
		return '';
	}
	if (!isEncryptedStorageValue(text)) {
		return text;
	}
	try {
		const cipher = text.slice(ENCRYPTED_STORAGE_PREFIX.length);
		const bytes = CryptoJS.AES.decrypt(cipher, STORAGE_AES_SECRET);
		const plain = bytes.toString(CryptoJS.enc.Utf8);
		return plain || '';
	} catch (e) {
		return '';
	}
};
