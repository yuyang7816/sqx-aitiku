/**
 * 学习提醒订阅消息授权（设置页 pushEnabled 开关）
 * 微信小程序：开启时拉取 GET /app/subscribe/templates 后调用 uni.requestSubscribeMessage
 * H5 等端仅保存本地偏好
 */
import { get, post } from '@/common/api';

const hasAcceptedSubscribe = (result = {}, tmplIds = []) =>
	tmplIds.some((id) => result[id] === 'accept');

/** 从服务端 SubscribeTemplateVO 提取已配置的非空模板 ID */
export const collectSubscribeTemplateIds = (data = {}) =>
	[
		data.monthlyReportTemplateId,
		data.vipExpireTemplateId,
		data.wrongReviewTemplateId
	]
		.map((id) => String(id || '').trim())
		.filter(Boolean);

/**
 * 拉取订阅消息模板 ID（无需登录）
 * @returns {Promise<string[]>}
 */
export const fetchSubscribeTemplateIds = async () => {
	try {
		const res = await get('/app/subscribe/templates');
		if (res && res.code === 0 && res.data) {
			return collectSubscribeTemplateIds(res.data);
		}
	} catch (e) {
		// 静默回退：模板未配置时仍允许保存本地开关偏好
	}
	return [];
};

/**
 * 请求学习提醒相关订阅消息授权
 * @returns {Promise<boolean>} 是否允许开启 pushEnabled（非微信端或无模板配置时视为允许）
 */
export const requestStudyReminderSubscribe = async () => {
	// #ifdef MP-WEIXIN
	const tmplIds = await fetchSubscribeTemplateIds();
	if (!tmplIds.length) {
		// 模板 ID 待管理端系统配置 wx_ma_subscribe_* 后生效；先允许保存开关偏好
		return true;
	}
	return new Promise((resolve) => {
		uni.requestSubscribeMessage({
			tmplIds,
			success: (res) => {
				resolve(hasAcceptedSubscribe(res, tmplIds));
			},
			fail: () => {
				resolve(false);
			}
		});
	});
	// #endif
	// #ifndef MP-WEIXIN
	return true;
	// #endif
};

/** 将授权成功的模板 ID 映射为服务端场景码 */
export const mapAcceptedSubscribeScenes = (result = {}, tmplIds = []) => {
	const scenes = [];
	if (result[tmplIds[0]] === 'accept') {
		scenes.push('monthly_report');
	}
	if (result[tmplIds[1]] === 'accept') {
		scenes.push('vip_expire');
	}
	if (result[tmplIds[2]] === 'accept') {
		scenes.push('wrong_review');
	}
	return scenes.filter(Boolean);
};

/**
 * 登记订阅消息偏好到服务端
 * @returns {Promise<boolean>}
 */
export const registerSubscribePreferences = async ({ pushEnabled = true, acceptedScenes = [] } = {}) => {
	try {
		const res = await post('/app/subscribe/register', {
			pushEnabled: Boolean(pushEnabled),
			acceptedScenes: Array.isArray(acceptedScenes) ? acceptedScenes : []
		});
		return Boolean(res && res.code === 0);
	} catch (e) {
		return false;
	}
};
