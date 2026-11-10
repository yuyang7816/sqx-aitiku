/** 会员订单支付状态（与 tb_vip_order.pay_status 一致） */
export const MEMBER_ORDER_PAY_STATUS = {
	PENDING: 0,
	PAID: 1,
	CLOSED: 2
};

/** 支付状态文案 */
export const MEMBER_ORDER_STATUS_LABELS = {
	[MEMBER_ORDER_PAY_STATUS.PENDING]: '待支付',
	[MEMBER_ORDER_PAY_STATUS.PAID]: '已支付',
	[MEMBER_ORDER_PAY_STATUS.CLOSED]: '已关闭'
};

/** 支付渠道文案 */
export const MEMBER_ORDER_CHANNEL_LABELS = {
	wechat: '微信支付',
	alipay: '支付宝'
};
