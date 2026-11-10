/**
 * 微信公开配置：GET /app/config/wechat（公众号 AppId、登录开关等）
 */
import { get } from '@/common/api';

let cachedConfig = null;

const normalizeWechatPublicConfig = (data = {}) => ({
	mpAppId: String(data.mpAppId || '').trim(),
	maAppId: String(data.maAppId || '').trim(),
	mpLoginEnabled: data.mpLoginEnabled === true
});

/** 清除缓存（配置变更后可选调用） */
export const clearWechatPublicConfigCache = () => {
	cachedConfig = null;
};

export const fetchWechatPublicConfig = async () => {
	if (cachedConfig) {
		return cachedConfig;
	}
	try {
		const res = await get('/app/config/wechat');
		if (res && res.code === 0 && res.data) {
			cachedConfig = normalizeWechatPublicConfig(res.data);
			return cachedConfig;
		}
	} catch (e) {
		// ignore
	}
	cachedConfig = normalizeWechatPublicConfig({});
	return cachedConfig;
};

/** 公众号 OAuth 登录是否已开启（默认 false） */
export const isMpLoginEnabled = () => Boolean(cachedConfig?.mpLoginEnabled);
