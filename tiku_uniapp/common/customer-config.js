/**
 * App 启动拉取 GET /app/config/customer，合并至 aiQuestionCustomerConfig
 */
import { getCustomerConfig, saveCustomerConfig } from '@/common/app-data';
import { get } from '@/common/api';

/**
 * 将服务端 CustomerConfigVO 映射为本地 Storage 结构
 * @param {object} data - CustomerConfigVO
 */
export const mapServerCustomerToLocal = (data = {}) => {
	const patch = {};

	if (typeof data.enabled === 'boolean') {
		patch.enabled = data.enabled;
	}
	if (data.type != null) {
		patch.type = data.type;
	}
	if (data.phone != null) {
		patch.phone = data.phone;
	}
	if (data.weworkUrl != null) {
		patch.weworkUrl = data.weworkUrl;
	}
	if (data.weworkAppId != null) {
		patch.weworkAppId = data.weworkAppId;
	}
	if (typeof data.imEnabled === 'boolean') {
		patch.imEnabled = data.imEnabled;
	}
	if (data.imWelcome != null) {
		patch.imWelcome = data.imWelcome;
	}
	if (data.wechatId != null) {
		patch.wechatId = data.wechatId;
	}
	if (data.wechatQrUrl != null) {
		patch.wechatQrUrl = data.wechatQrUrl;
	}

	return patch;
};

/** 合并服务端客服配置到 aiQuestionCustomerConfig */
export const mergeServerCustomerConfig = (data = {}) => {
	const patch = mapServerCustomerToLocal(data);
	if (!Object.keys(patch).length) {
		return getCustomerConfig();
	}
	return saveCustomerConfig(patch);
};

/**
 * 拉取 GET /app/config/customer 并写入本地 Storage（无需登录）
 * @returns {Promise<object|null>} 成功返回服务端 data，失败返回 null
 */
export const fetchAndMergeCustomerConfig = async () => {
	try {
		const res = await get('/app/config/customer');
		if (res && res.code === 0 && res.data) {
			mergeServerCustomerConfig(res.data);
			return res.data;
		}
		return null;
	} catch (e) {
		return null;
	}
};
