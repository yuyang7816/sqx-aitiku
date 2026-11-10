/**
 * 多端微信支付调起：小程序/App uni.requestPayment · H5 微信 JSAPI · H5 MWEB 跳转
 */

const invokeWeixinJsBridgePay = (params = {}) =>
	new Promise((resolve, reject) => {
		const onBridgeReady = () => {
			// eslint-disable-next-line no-undef
			WeixinJSBridge.invoke('getBrandWCPayRequest', {
				appId: params.appId || params.appid || '',
				timeStamp: String(params.timeStamp || params.timestamp || ''),
				nonceStr: params.nonceStr || '',
				package: params.package || params.packageValue || '',
				signType: params.signType || 'MD5',
				paySign: params.paySign || ''
			}, (res) => {
				if (res.err_msg === 'get_brand_wcpay_request:ok') {
					resolve(res);
					return;
				}
				if (res.err_msg === 'get_brand_wcpay_request:cancel') {
					reject(new Error('已取消支付'));
					return;
				}
				reject(new Error(res.err_msg || '微信支付失败'));
			});
		};

		// #ifdef H5
		if (typeof WeixinJSBridge === 'undefined') {
			document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
		} else {
			onBridgeReady();
		}
		// #endif
		// #ifndef H5
		reject(new Error('当前环境不支持 JSAPI 支付'));
		// #endif
	});

/** H5 MWEB 支付：跳转 mweb_url */
export const redirectWechatH5Pay = (prepay = {}) => {
	const url = String(prepay.mweb_url || prepay.mwebUrl || '').trim();
	if (!url) {
		throw new Error('微信支付链接无效');
	}
	// #ifdef H5
	window.location.href = url;
	// #endif
};

/** H5 支付宝 WAP：跳转 form/html 或 orderInfo URL */
export const redirectAlipayH5Pay = (prepay = {}) => {
	const orderInfo = String(prepay.orderInfo || prepay.orderStr || prepay.body || '').trim();
	if (!orderInfo) {
		throw new Error('支付宝支付参数无效');
	}
	// #ifdef H5
	if (/^https?:\/\//i.test(orderInfo)) {
		window.location.href = orderInfo;
		return;
	}
	const container = document.createElement('div');
	container.innerHTML = orderInfo;
	document.body.appendChild(container);
	const form = container.querySelector('form');
	if (form) {
		form.submit();
		return;
	}
	throw new Error('支付宝支付表单无效');
	// #endif
};

/**
 * 按 prepay 结构调起真实支付
 * @param {'wechat'|'alipay'} payMethod
 * @param {object} prepay
 */
export const invokePlatformPayment = async (payMethod, prepay = {}) => {
	const method = String(payMethod || 'wechat').trim();

	if (method === 'wechat') {
		const mwebUrl = prepay.mweb_url || prepay.mwebUrl;
		if (mwebUrl) {
			redirectWechatH5Pay(prepay);
			return { redirected: true };
		}

		// #ifdef H5
		const { isWechatBrowser } = await import('@/common/platform');
		if (isWechatBrowser()) {
			await invokeWeixinJsBridgePay(prepay);
			return { ok: true };
		}
		throw new Error('请在微信内打开或使用其他支付方式');
		// #endif

		const payOptions = {
			provider: 'wxpay',
			timeStamp: String(prepay.timeStamp || prepay.timestamp || ''),
			nonceStr: String(prepay.nonceStr || ''),
			package: String(prepay.package || prepay.packageValue || ''),
			signType: String(prepay.signType || 'MD5'),
			paySign: String(prepay.paySign || '')
		};
		if (!payOptions.timeStamp || !payOptions.package || !payOptions.paySign) {
			throw new Error('微信支付参数不完整');
		}
		return new Promise((resolve, reject) => {
			uni.requestPayment({
				...payOptions,
				success: resolve,
				fail: reject
			});
		});
	}

	if (method === 'alipay') {
		const orderInfo = prepay.orderInfo || prepay.orderStr || '';
		// #ifdef H5
		redirectAlipayH5Pay(prepay);
		return { redirected: true };
		// #endif
		// #ifndef H5
		if (!orderInfo) {
			throw new Error('支付宝支付参数不完整');
		}
		return new Promise((resolve, reject) => {
			uni.requestPayment({
				provider: 'alipay',
				orderInfo: String(orderInfo),
				success: resolve,
				fail: reject
			});
		});
		// #endif
	}

	throw new Error('不支持的支付方式');
};
