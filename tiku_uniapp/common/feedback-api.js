/**
 * 帮助与反馈：POST /app/feedback/submit（仅提交，不维护本地工单列表）
 */
import { postJson } from '@/common/api';

const assertApiSuccess = (res, fallbackMsg) => {
	if (res && res.code === 0) {
		return res.data;
	}
	const err = new Error((res && res.msg) || fallbackMsg);
	err.msg = (res && res.msg) || fallbackMsg;
	throw err;
};

/**
 * 提交反馈
 * @param {{ category: string, content: string, contactPhone?: string, images?: string[] }} payload
 */
export const submitFeedbackTicket = async (payload = {}) => {
	const category = String(payload.category || '').trim();
	const content = String(payload.content || '').trim();
	const contactPhone = String(payload.contactPhone || '').trim();
	const images = Array.isArray(payload.images) ? payload.images.filter(Boolean) : [];

	if (!category) {
		throw new Error('请选择反馈类型');
	}
	if (content.length < 10) {
		throw new Error('问题描述至少 10 个字');
	}
	if (contactPhone && !/^1\d{10}$/.test(contactPhone)) {
		throw new Error('请输入正确的手机号');
	}

	const res = await postJson('/app/feedback/submit', {
		category,
		content,
		contactPhone: contactPhone || undefined,
		images: images.length ? images : undefined
	});
	return assertApiSuccess(res, '提交反馈失败');
};
