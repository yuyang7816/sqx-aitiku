/**
 * 多端真实支付：POST /app/pay → 平台调起 uni.requestPayment / JSAPI / MWEB
 */
import { postJson } from '@/common/api';
import { refreshMemberStateAfterPay } from '@/common/member-activate';
import { appendLocalMemberOrder } from '@/common/vip-order-api';
import { MEMBER_ORDER_PAY_STATUS } from '@/common/member-constants';
import { assertWechatPayAllowed, PAY_METHOD } from '@/common/member-pay-method';
import { getClientPlatform } from '@/common/platform';
import { invokePlatformPayment } from '@/common/wechat-pay';

const assertPayCreateSuccess = (res, fallbackMsg) => {
	if (res && res.code === 0 && res.data) {
		return res.data;
	}
	const err = new Error((res && res.msg) || fallbackMsg);
	err.msg = (res && res.msg) || fallbackMsg;
	if (res && res.code != null) {
		err.code = res.code;
	}
	throw err;
};

/**
 * 创建会员支付订单（需登录）
 * @param {{ planName: string, payMethod: 'wechat'|'alipay', clientPlatform?: string }} payload
 */
export const createMemberPayOrder = async ({ planName, payMethod, clientPlatform }) => {
	const res = await postJson('/app/pay', {
		planName: String(planName || '').trim(),
		payMethod: String(payMethod || 'wechat').trim(),
		clientPlatform: clientPlatform || getClientPlatform()
	});
	return assertPayCreateSuccess(res, '创建支付订单失败');
};

/** 解析支付失败/取消文案 */
export const resolvePayErrorMessage = (err, fallback = '支付失败，请稍后重试') => {
	if (!err) {
		return fallback;
	}
	const errMsg = String(err.errMsg || err.message || err.msg || '').trim();
	if (/cancel|取消/i.test(errMsg)) {
		return '已取消支付';
	}
	return err.msg || err.message || errMsg || fallback;
};

/**
 * 完整支付流程：POST /app/pay → 多端调起支付 → 刷新会员态
 */
export const payMemberPlan = async ({ planName, payMethod, planDays, silent = false } = {}) => {
	const normalizedPlan = String(planName || '').trim();
	if (!normalizedPlan) {
		const message = '套餐信息无效，请返回重选';
		if (!silent) {
			uni.showToast({ title: message, icon: 'none' });
		}
		return { ok: false, message };
	}

	const method = String(payMethod || PAY_METHOD.WECHAT).trim();

	if (method === PAY_METHOD.WECHAT) {
		const wechatCheck = assertWechatPayAllowed();
		if (!wechatCheck.ok) {
			const message = wechatCheck.message || '当前环境不支持微信支付';
			if (!silent) {
				uni.showToast({ title: message, icon: 'none' });
			}
			return { ok: false, message };
		}
	}

	try {
		const order = await createMemberPayOrder({
			planName: normalizedPlan,
			payMethod: method
		});
		const prepay = order.prepay || {};
		const payResult = await invokePlatformPayment(method, prepay);

		if (payResult?.redirected) {
			return { ok: true, redirected: true, order };
		}

		await refreshMemberStateAfterPay({
			name: order.planName || normalizedPlan,
			days: Number(order.planDays) || Number(planDays) || 31
		});
		appendLocalMemberOrder({
			orderNo: order.orderNo,
			planName: order.planName || normalizedPlan,
			planDays: Number(order.planDays) || Number(planDays) || 31,
			amount: order.amount,
			payChannel: method,
			payStatus: MEMBER_ORDER_PAY_STATUS.PAID
		});
		return { ok: true, order };
	} catch (e) {
		const message = resolvePayErrorMessage(e);
		if (!silent) {
			uni.showToast({ title: message, icon: 'none' });
		}
		return { ok: false, message };
	}
};
