/**
 * 首页「在线客服」入口：电话 / 企微 / 在线 IM / 微信客服
 */
import { getCustomerConfig } from '@/common/app-data';
import { KEFU_CHAT_PAGE } from '@/common/kefu-chat-api';

export const CUSTOMER_WECHAT_PAGE = '/package/customer/wechat';

/** 是否配置了可用的客服方式（且入口未关闭） */
export const hasConfiguredCustomerService = (config = getCustomerConfig()) => {
	if (config.enabled === false) {
		return false;
	}
	const phone = String(config.phone || '').trim();
	const weworkUrl = String(config.weworkUrl || '').trim();
	const imEnabled = config.imEnabled === true;
	const wechatId = String(config.wechatId || '').trim();
	const wechatQrUrl = String(config.wechatQrUrl || '').trim();
	return Boolean(phone || weworkUrl || imEnabled || wechatId || wechatQrUrl);
};

/** 构建 ActionSheet 选项 */
export const buildCustomerServiceActions = (config = getCustomerConfig()) => {
	const actions = [];
	const phone = String(config.phone || '').trim();
	const weworkUrl = String(config.weworkUrl || '').trim();
	const imEnabled = config.imEnabled === true;
	const wechatId = String(config.wechatId || '').trim();
	const wechatQrUrl = String(config.wechatQrUrl || '').trim();

	if (imEnabled) {
		actions.push({
			key: 'im',
			label: '在线客服',
			subname: '即时对话'
		});
	}
	if (phone) {
		actions.push({
			key: 'phone',
			label: '电话客服',
			subname: phone
		});
	}
	if (weworkUrl) {
		actions.push({
			key: 'wework',
			label: '企业微信客服',
			subname: weworkUrl
		});
	}
	if (wechatId || wechatQrUrl) {
		actions.push({
			key: 'wechat',
			label: '微信客服',
			subname: wechatId || '扫码添加'
		});
	}
	return actions;
};

const openExternalUrl = (url = '') => {
	const trimmed = String(url || '').trim();
	if (!trimmed) {
		return;
	}

	// #ifdef APP-PLUS
	plus.runtime.openURL(trimmed);
	// #endif
	// #ifdef H5
	window.open(trimmed);
	// #endif
	// #ifdef MP
	uni.setClipboardData({
		data: trimmed,
		success: () => {
			uni.showToast({
				title: '链接已复制，请在浏览器打开',
				icon: 'none'
			});
		}
	});
	// #endif
};

const openPhoneCustomer = (phone) => {
	const phoneNumber = String(phone || '').trim();
	if (!phoneNumber) {
		return;
	}
	uni.makePhoneCall({
		phoneNumber,
		fail: () => {
			uni.showToast({
				title: '无法拨打电话',
				icon: 'none'
			});
		}
	});
};

const openWeworkCustomer = (config = {}) => {
	const url = String(config.weworkUrl || '').trim();
	const appId = String(config.weworkAppId || '').trim();
	if (!url) {
		return;
	}

	// #ifdef MP-WEIXIN
	if (appId && typeof wx !== 'undefined' && wx.openCustomerServiceChat) {
		wx.openCustomerServiceChat({
			extInfo: {
				url
			},
			corpId: appId,
			fail: () => {
				openExternalUrl(url);
			}
		});
		return;
	}
	// #endif

	openExternalUrl(url);
};

const openImCustomer = () => {
	uni.navigateTo({ url: KEFU_CHAT_PAGE });
};

const openWechatCustomer = () => {
	uni.navigateTo({ url: CUSTOMER_WECHAT_PAGE });
};

const invokeCustomerAction = (actionKey, config) => {
	if (actionKey === 'im') {
		openImCustomer();
		return;
	}
	if (actionKey === 'phone') {
		openPhoneCustomer(config.phone);
		return;
	}
	if (actionKey === 'wework') {
		openWeworkCustomer(config);
		return;
	}
	if (actionKey === 'wechat') {
		openWechatCustomer();
	}
};

/**
 * 打开客服：展示所有已配置方式；仅一种时直接跳转
 */
export const openCustomerService = () => {
	const config = getCustomerConfig();

	if (!hasConfiguredCustomerService(config)) {
		uni.showToast({ title: '客服暂未配置', icon: 'none' });
		return;
	}

	const actions = buildCustomerServiceActions(config);
	if (!actions.length) {
		uni.showToast({ title: '客服暂未配置', icon: 'none' });
		return;
	}

	if (actions.length === 1) {
		invokeCustomerAction(actions[0].key, config);
		return;
	}

	uni.showActionSheet({
		itemList: actions.map((item) => item.label),
		success: (res) => {
			const action = actions[res.tapIndex];
			if (action) {
				invokeCustomerAction(action.key, config);
			}
		}
	});
};

export { KEFU_CHAT_PAGE };
