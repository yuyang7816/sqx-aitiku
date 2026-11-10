/**
 * 刷题中心 Chip 筛选上下文：主 Tab 与子页（章节/知识点）共用
 */
import {
	PRACTICE_CHIP_ALL_GRADES,
	getUserProfile,
	isPracticeSubjectChip
} from '@/common/app-data';
import { PRACTICE_ACTIVE_CHIP_KEY } from '@/common/storage-keys';

export const normalizePracticeChip = (chip = '') => {
	const value = String(chip || '').trim();
	return value || PRACTICE_CHIP_ALL_GRADES;
};

/** 持久化当前刷题 Chip（进入分包练习页时读取） */
export const setPracticeActiveChip = (chip = '') => {
	uni.setStorageSync(PRACTICE_ACTIVE_CHIP_KEY, normalizePracticeChip(chip));
};

/** 读取当前刷题 Chip */
export const getPracticeActiveChip = () => normalizePracticeChip(uni.getStorageSync(PRACTICE_ACTIVE_CHIP_KEY));

/** 解析路由 query.chip */
export const resolvePracticeChipFromQuery = (query = {}) => {
	const raw = String(query?.chip || '').trim();
	if (!raw) {
		return getPracticeActiveChip();
	}
	try {
		return normalizePracticeChip(decodeURIComponent(raw));
	} catch (e) {
		return normalizePracticeChip(raw);
	}
};

/** 从 Chip 解析学科展示名（非学科 Chip 时返回空字符串） */
export const resolvePracticeSubjectFromChip = (chip = getPracticeActiveChip(), profile = getUserProfile()) => {
	const value = normalizePracticeChip(chip);
	if (!value || value === PRACTICE_CHIP_ALL_GRADES) {
		return '';
	}
	return isPracticeSubjectChip(value, profile) ? value : '';
};

/** 为练习分包 URL 追加 chip 参数（学科/版本筛选延续到子页） */
export const appendPracticeChipQuery = (url = '', chip = getPracticeActiveChip()) => {
	const base = String(url || '').trim();
	if (!base) {
		return base;
	}
	const chipValue = normalizePracticeChip(chip);
	if (chipValue === PRACTICE_CHIP_ALL_GRADES) {
		return base;
	}
	const joiner = base.includes('?') ? '&' : '?';
	return `${base}${joiner}chip=${encodeURIComponent(chipValue)}`;
};
