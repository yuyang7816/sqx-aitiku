/**
 * 刷题中心练习入口：GET /app/practiceEntry
 */
import { get } from '@/common/api';
import { PAPER_TYPE_EXAM, PAPER_TYPE_SPECIAL } from '@/common/paper-list';
import { buildPaperCenterUrl } from '@/common/practice-config';

export const PRACTICE_ENTRY_TYPE_SPECIAL = 'special';
export const PRACTICE_ENTRY_TYPE_EXAM = 'exam';

import { resolveCardBg, resolveMediaUrl, isUploadedIcon } from '@/common/card-display';

export const normalizePracticeEntryItem = (item = {}) => {
	const icon = item.icon || '';
	const uploaded = isUploadedIcon(icon);
	return {
		title: item.title || '',
		sub: item.sub || '',
		icon,
		iconUrl: uploaded ? resolveMediaUrl(icon) : '',
		iconColor: item.iconColor || '#4B7AFE',
		bg: resolveCardBg(item.bg || ''),
		url: item.url || '',
		entryType: item.entryType || '',
		sort: Number(item.sort) || 0,
		paperId: ''
	};
};

/**
 * 拉取启用的练习入口卡片
 * @returns {Promise<{ list: object[], fromApi: boolean }>}
 */
export const fetchPracticeEntries = async () => {
	try {
		const res = await get('/app/practiceEntry');
		if (res && res.code === 0 && Array.isArray(res.data)) {
			return {
				list: res.data.map(normalizePracticeEntryItem).filter((item) => item.title),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由全局拦截处理
	}

	return { list: [], fromApi: false };
};

/**
 * 为专项/真题卡片注入 paperId，并补全默认跳转 URL
 * @param {object[]} entries
 * @param {{ special?: string, exam?: string }} paperIds
 */
export const mergePracticeEntryPaperIds = (entries = [], { special = '', exam = '' } = {}) => {
	return (Array.isArray(entries) ? entries : []).map((item) => {
		if (item.entryType === PRACTICE_ENTRY_TYPE_SPECIAL) {
			return {
				...item,
				paperId: special,
				url: item.url || buildPaperCenterUrl(PAPER_TYPE_SPECIAL)
			};
		}
		if (item.entryType === PRACTICE_ENTRY_TYPE_EXAM) {
			return {
				...item,
				paperId: exam,
				url: item.url || buildPaperCenterUrl(PAPER_TYPE_EXAM)
			};
		}
		return item;
	});
};
