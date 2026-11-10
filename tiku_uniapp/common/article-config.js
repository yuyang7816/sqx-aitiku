/**
 * 协议与政策：GET /app/article/{type} 一次拉取（与管理端 agreement-config、server AppArticleController 一致）
 */
import { get } from '@/common/api';

/** 文章类型（展示名称，正文仅来自服务端） */
export const ARTICLE_TYPES = [
	{ type: 'agreement', label: '用户协议' },
	{ type: 'privacy', label: '隐私政策' },
	{ type: 'about', label: '关于我们' }
];

const findArticleDef = (type) => ARTICLE_TYPES.find((item) => item.type === type);

/**
 * 按 type 拉取协议/政策正文（GET /app/article/{type}，无需登录）
 * @param {string} type - agreement | privacy | about
 * @returns {Promise<{ title: string, sub: string, content: string }|null>}
 */
export const fetchArticleByType = async (type) => {
	const def = findArticleDef(type);
	if (!def) {
		return null;
	}
	try {
		const res = await get(`/app/article/${encodeURIComponent(type)}`);
		if (res && res.code === 0 && res.data) {
			const data = res.data;
			const title = String(data.title || '').trim();
			const sub = String(data.sub || '').trim();
			const content = String(data.content || '').trim();
			if (!content) {
				return null;
			}
			return {
				title: title || def.label,
				sub,
				content
			};
		}
	} catch (e) {
		// 网络异常
	}
	return null;
};
