import { buildApiUrl } from '@/common/api/config';

/** 判断 icon 字段是否为上传图片（兼容旧版 uView 图标名） */
export const isUploadedIcon = (icon = '') => {
	const value = String(icon || '').trim();
	if (!value) {
		return false;
	}
	if (/^https?:\/\//i.test(value)) {
		return true;
	}
	if (value.includes('/')) {
		return true;
	}
	return /\.(png|jpe?g|gif|webp|svg)(\?|$)/i.test(value);
};

/** 拼接上传图片完整地址 */
export const resolveMediaUrl = (url = '') => {
	const value = String(url || '').trim();
	if (!value) {
		return '';
	}
	return buildApiUrl(value);
};

/** 卡片背景：新版纯色 / 旧版 gradient 均兼容 */
export const resolveCardBg = (bg = '') => {
	const value = String(bg || '').trim();
	if (!value) {
		return '';
	}
	if (value.includes('gradient')) {
		return value;
	}
	return value;
};
