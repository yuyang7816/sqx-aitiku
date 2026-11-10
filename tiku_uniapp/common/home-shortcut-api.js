/**
 * 首页快捷入口：GET /app/homeShortcut
 */
import { get } from '@/common/api';

import { resolveCardBg, resolveMediaUrl, isUploadedIcon } from '@/common/card-display';

export const normalizeHomeShortcutItem = (item = {}) => {
	const icon = item.icon || '';
	const uploaded = isUploadedIcon(icon);
	return {
		title: item.title || '',
		icon,
		iconUrl: uploaded ? resolveMediaUrl(icon) : '',
		color: item.color || '#4E88F6',
		bg: resolveCardBg(item.bg || ''),
		url: item.url || '',
		action: item.action || '',
		sort: Number(item.sort) || 0
	};
};

/**
 * 拉取启用的首页快捷入口
 * @returns {Promise<{ list: object[], fromApi: boolean }>}
 */
export const fetchHomeShortcuts = async () => {
	try {
		const res = await get('/app/homeShortcut');
		if (res && res.code === 0 && Array.isArray(res.data)) {
			return {
				list: res.data.map(normalizeHomeShortcutItem).filter((item) => item.title),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由全局拦截处理
	}

	return { list: [], fromApi: false };
};
