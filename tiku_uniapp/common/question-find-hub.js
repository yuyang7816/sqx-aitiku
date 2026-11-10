/**
 * 找题枢纽：本地错题/收藏/记录筛选；推荐词来自学情薄弱点 API
 */
import {
	getCollectionList,
	getNotebookList,
	getSearchRecords
} from '@/common/app-data';

export const FIND_HUB_TABS = [
	{ key: 'knowledge', label: '知识点' },
	{ key: 'notebook', label: '错题' },
	{ key: 'history', label: '历史' }
];

const normalizeText = (value = '') => String(value || '').trim().toLowerCase();

const matchKeyword = (item, keyword, fields = []) => {
	const key = normalizeText(keyword);
	if (!key) {
		return true;
	}
	return fields.some((field) => normalizeText(item?.[field]).includes(key));
};

/** 筛选错题本 */
export const filterNotebookByKeyword = (keyword = '', sourceList = getNotebookList()) => {
	const list = Array.isArray(sourceList) ? sourceList : [];
	return list.filter((item) => matchKeyword(item, keyword, ['question', 'subject']));
};

/** 筛选收藏 */
export const filterCollectionByKeyword = (keyword = '', sourceList = getCollectionList()) => {
	const list = Array.isArray(sourceList) ? sourceList : [];
	return list.filter((item) => matchKeyword(item, keyword, ['title', 'subject']));
};

/** 筛选搜题记录 */
export const filterSearchRecordsByKeyword = (keyword = '', sourceList = getSearchRecords()) => {
	const list = Array.isArray(sourceList) ? sourceList : [];
	return list.filter((item) => matchKeyword(item, keyword, ['title', 'source']));
};

/** 由学情薄弱点生成推荐搜索词，最多 5 个 */
export const buildSuggestKeywords = (_profile = {}, weakPoints = []) => {
	const words = [];
	(Array.isArray(weakPoints) ? weakPoints : []).forEach((item) => {
		const title = String(item?.title || '').trim();
		if (title && !words.includes(title)) {
			words.push(title);
		}
	});
	return words.slice(0, 5);
};

/** 本地 Tab 列表（无关键词时返回最近 N 条） */
export const queryLocalFindHubList = (tabKey = 'notebook', keyword = '', limit = 20) => {
	const key = String(keyword || '').trim();
	if (tabKey === 'notebook') {
		const list = filterNotebookByKeyword(key);
		return list.slice(0, limit).map((item) => ({
			id: item.id || item.question,
			title: item.question,
			subtitle: item.subject || '错题本',
			meta: item.mastered ? '已掌握' : '待复习',
			raw: item,
			type: 'notebook'
		}));
	}
	if (tabKey === 'history') {
		const records = filterSearchRecordsByKeyword(key).map((item) => ({
			id: `record-${item.title}-${item.time}`,
			title: item.title,
			subtitle: item.source || '搜题记录',
			meta: item.time || '',
			raw: item,
			type: 'records'
		}));
		const collections = filterCollectionByKeyword(key).map((item) => ({
			id: item.id || item.title,
			title: item.title,
			subtitle: item.subject || '收藏',
			meta: '',
			raw: item,
			type: 'collection'
		}));
		return [...records, ...collections].slice(0, limit);
	}
	return [];
};
