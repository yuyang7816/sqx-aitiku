/**
 * 帮助与反馈 FAQ：GET /app/help/faq/list（无数据时返回空列表，由运营在管理端维护）
 */
import { get, PAGE_LOAD_REQUEST_OPTIONS } from '@/common/api';

/** 工单反馈分类（表单选项，非业务 mock 数据） */
export const feedbackCategories = [
	{ value: 'feature', label: '功能问题' },
	{ value: 'account', label: '账号与会员' },
	{ value: 'search', label: '搜题与答疑' },
	{ value: 'other', label: '其他建议' }
];

export const feedbackStatusMap = {
	pending: '待处理',
	processing: '处理中',
	replied: '已回复'
};

/** 将服务端 FAQ 项规范为 help-feedback 页结构 */
export const normalizeHelpFaqItem = (item = {}, index = 0) => ({
	id: String(item.id || item.code || `faq-${index + 1}`).trim(),
	question: String(item.question || item.title || '').trim(),
	answer: String(item.answer || item.content || '').trim()
});

/**
 * 拉取常见问题列表
 * @returns {Promise<{ list: Array<{ id: string, question: string, answer: string }>, fromApi: boolean }>}
 */
export const fetchHelpFaqList = async () => {
	try {
		const res = await get('/app/help/faq/list', undefined, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && Array.isArray(res.data)) {
			const list = res.data
				.map(normalizeHelpFaqItem)
				.filter((item) => item.question && item.answer);
			return { list, fromApi: true };
		}
	} catch (e) {
		// 接口未上线或网络异常
	}

	return { list: [], fromApi: false };
};
