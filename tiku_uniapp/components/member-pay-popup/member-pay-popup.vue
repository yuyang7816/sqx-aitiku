<template>
	<up-popup
		:show="show"
		mode="bottom"
		round="28"
		:close-on-click-overlay="!paying"
		@close="handleClose"
	>
		<view class="pay-sheet">
			<view class="sheet-handle"></view>
			<view class="sheet-head">
				<view class="sheet-head-copy">
					<text class="sheet-title">选择支付方式</text>
					<text class="sheet-sub">支付安全由平台全程保障</text>
				</view>
				<view class="sheet-close" :class="{ disabled: paying }" @tap="handleClose">
					<up-icon name="close" size="16" color="#667085"></up-icon>
				</view>
			</view>

			<view class="order-card">
				<text class="order-title">AI 智能提分 · {{ planName }}套餐</text>
				<view class="order-row">
					<text class="order-days">有效期共 {{ planDays }} 天</text>
					<view class="order-price-wrap">
						<text class="price-symbol">¥</text>
						<text class="order-price">{{ displayPrice }}</text>
					</view>
				</view>
			</view>

			<view v-if="wechatPayHint" class="pay-hint-banner">
				<text>{{ wechatPayHint }}</text>
			</view>

			<view class="pay-section-head">
				<text class="pay-section-title">支付方式</text>
				<text class="pay-section-tip">请选择一种方式完成付款</text>
			</view>

			<view
				v-if="showWechatPay"
				class="pay-item"
				:class="{ active: payMethod === PAY_METHOD.WECHAT }"
				@tap="selectPayMethod(PAY_METHOD.WECHAT)"
			>
				<view class="pay-left">
					<view class="pay-icon green">
						<up-icon name="weixin-fill" size="20" color="#FFFFFF"></up-icon>
					</view>
					<view class="pay-copy">
						<view class="pay-name-row">
							<text class="pay-name">微信</text>
							<text class="pay-chip recommend">推荐</text>
						</view>
					</view>
				</view>
				<view class="radio" :class="{ on: payMethod === PAY_METHOD.WECHAT }">
					<up-icon v-if="payMethod === PAY_METHOD.WECHAT" name="checkmark" size="12" color="#FFFFFF"></up-icon>
				</view>
			</view>

			<view
				v-if="showAlipayPay"
				class="pay-item"
				:class="{ active: payMethod === PAY_METHOD.ALIPAY }"
				@tap="selectPayMethod(PAY_METHOD.ALIPAY)"
			>
				<view class="pay-left">
					<view class="pay-icon blue">
						<up-icon name="zhifubao-circle-fill" size="22" color="#FFFFFF"></up-icon>
					</view>
					<view class="pay-copy">
						<view class="pay-name-row">
							<text class="pay-name">支付宝</text>
							<text class="pay-chip">常用</text>
						</view>
					</view>
				</view>
				<view class="radio" :class="{ on: payMethod === PAY_METHOD.ALIPAY }">
					<up-icon v-if="payMethod === PAY_METHOD.ALIPAY" name="checkmark" size="12" color="#FFFFFF"></up-icon>
				</view>
			</view>

			<view class="solid-button" :class="{ disabled: paying || !canSubmitPay }" @tap="payNow">
				<text>{{ payButtonText }}</text>
			</view>
		</view>
	</up-popup>
</template>

<script setup>
	import { computed, ref, watch } from 'vue';
	import { payMemberPlan } from '@/common/pay-api';
	import {
		PAY_METHOD,
		assertWechatPayAllowed,
		formatPayPrice,
		getDefaultPayMethod,
		getWechatPayUnavailableReason,
		hasAnyPayMethod,
		isAlipayPayVisible,
		isWechatPayVisible,
		normalizePayMethod
	} from '@/common/member-pay-method';
	import { fetchWechatPublicConfig } from '@/common/wechat-config';
	import { isH5Wechat } from '@/common/platform';
	import { isMpLoginEnabled } from '@/common/wechat-config';

	const props = defineProps({
		show: {
			type: Boolean,
			default: false
		},
		plan: {
			type: Object,
			default: () => ({ name: '', price: 0, days: 0 })
		}
	});

	const emit = defineEmits(['update:show', 'close', 'success']);

	const paying = ref(false);
	const payMethod = ref(getDefaultPayMethod());

	const planName = computed(() => String(props.plan?.name || '').trim());
	const planDays = computed(() => Number(props.plan?.days) || 0);
	const displayPrice = computed(() => formatPayPrice(props.plan?.price));
	const showWechatPay = computed(() => isWechatPayVisible());
	const showAlipayPay = computed(() => isAlipayPayVisible());
	const canSubmitPay = computed(() => hasAnyPayMethod());
	const wechatPayHint = computed(() => {
		if (showWechatPay.value || !isH5Wechat()) {
			return '';
		}
		if (!isMpLoginEnabled()) {
			return '';
		}
		return getWechatPayUnavailableReason();
	});
	const payButtonText = computed(() => {
		if (paying.value) {
			return '支付处理中...';
		}
		if (!canSubmitPay.value) {
			return '暂无可用支付方式';
		}
		return `立即支付 ¥${displayPrice.value}`;
	});

	const syncPayMethod = () => {
		payMethod.value = normalizePayMethod(payMethod.value);
	};

	watch(
		() => props.show,
		async (visible) => {
			if (visible) {
				await fetchWechatPublicConfig();
				syncPayMethod();
			}
		}
	);

	watch([showWechatPay, showAlipayPay], () => {
		if (props.show) {
			syncPayMethod();
		}
	});

	const selectPayMethod = (method) => {
		if (paying.value) {
			return;
		}
		if (method === PAY_METHOD.WECHAT && !isWechatPayVisible()) {
			uni.showToast({
				title: getWechatPayUnavailableReason() || '当前环境不支持微信支付',
				icon: 'none'
			});
			return;
		}
		if (method !== PAY_METHOD.WECHAT && method !== PAY_METHOD.ALIPAY) {
			return;
		}
		if (method === PAY_METHOD.ALIPAY && !isAlipayPayVisible()) {
			return;
		}
		payMethod.value = method;
	};

	const handleClose = () => {
		if (paying.value) {
			return;
		}
		emit('update:show', false);
		emit('close');
	};

	const payNow = async () => {
		if (paying.value || !planName.value) {
			if (!planName.value) {
				uni.showToast({ title: '请选择会员套餐', icon: 'none' });
			}
			return;
		}
		if (!canSubmitPay.value) {
			uni.showToast({ title: '暂无可用支付方式', icon: 'none' });
			return;
		}
		if (payMethod.value === PAY_METHOD.WECHAT) {
			const wechatCheck = assertWechatPayAllowed();
			if (!wechatCheck.ok) {
				uni.showToast({
					title: wechatCheck.message || '当前环境不支持微信支付',
					icon: 'none'
				});
				return;
			}
		}
		paying.value = true;
		try {
			const result = await payMemberPlan({
				planName: planName.value,
				payMethod: payMethod.value,
				planDays: planDays.value,
				silent: true
			});
			if (result.ok) {
				if (result.redirected) {
					emit('update:show', false);
					return;
				}
				emit('update:show', false);
				emit('success', {
					planName: planName.value,
					planDays: planDays.value,
					order: result.order
				});
				return;
			}
			uni.showToast({
				title: result.message || '支付失败，请稍后重试',
				icon: 'none'
			});
		} finally {
			paying.value = false;
		}
	};
</script>

<style lang="scss" scoped>
	.pay-sheet {
		padding: 18rpx 28rpx calc(36rpx + env(safe-area-inset-bottom));
		background: linear-gradient(180deg, #f8fbff 0%, #ffffff 120rpx);
		border-radius: 32rpx 32rpx 0 0;
		overflow: hidden;
	}

	.sheet-handle {
		width: 84rpx;
		height: 8rpx;
		margin: 0 auto 22rpx;
		border-radius: 999rpx;
		background: #d5ddeb;
	}

	.sheet-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 20rpx;
		padding-bottom: 24rpx;
		border-bottom: 2rpx solid rgba(227, 233, 243, 0.72);
	}

	.sheet-head-copy {
		min-width: 0;
	}

	.sheet-title {
		display: block;
		font-size: 36rpx;
		font-weight: 700;
		color: #22324b;
		line-height: 1.25;
	}

	.sheet-sub {
		display: block;
		margin-top: 10rpx;
		font-size: 22rpx;
		color: #97a0af;
		line-height: 1.45;
	}

	.sheet-close {
		width: 56rpx;
		height: 56rpx;
		margin-top: 4rpx;
		border-radius: 50%;
		background: rgba(243, 246, 251, 0.96);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		border: 2rpx solid rgba(225, 231, 240, 0.8);
	}

	.sheet-close.disabled {
		opacity: 0.45;
	}

	.order-card {
		margin-top: 24rpx;
		padding: 24rpx 26rpx;
		border-radius: 24rpx;
		background: linear-gradient(135deg, #edf4ff 0%, #dfe9ff 52%, #f6f9ff 100%);
		border: 2rpx solid #d8e4ff;
		box-shadow: 0 14rpx 34rpx rgba(65, 104, 189, 0.1);
	}

	.order-title {
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		color: #22324b;
		line-height: 1.35;
	}

	.order-row {
		margin-top: 12rpx;
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		gap: 20rpx;
	}

	.order-days {
		font-size: 24rpx;
		color: #7f8898;
		line-height: 1.5;
		padding-bottom: 2rpx;
	}

	.order-price-wrap {
		display: flex;
		align-items: baseline;
		gap: 6rpx;
		flex-shrink: 0;
	}

	.price-symbol {
		font-size: 28rpx;
		font-weight: 700;
		color: #386df5;
	}

	.order-price {
		font-size: 52rpx;
		font-weight: 800;
		color: #386df5;
	}

	.pay-hint-banner {
		margin-top: 22rpx;
		padding: 18rpx 20rpx;
		border-radius: 16rpx;
		background: rgba(255, 247, 237, 0.96);
		font-size: 22rpx;
		line-height: 1.55;
		color: #b45309;
	}

	.pay-section-head {
		margin-top: 24rpx;
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		gap: 6rpx;
	}

	.pay-section-title {
		font-size: 28rpx;
		font-weight: 700;
		color: #22324b;
		line-height: 1.3;
	}

	.pay-section-tip {
		font-size: 22rpx;
		color: #97a0af;
		line-height: 1.45;
	}

	.pay-item {
		margin-top: 16rpx;
		padding: 24rpx 28rpx;
		min-height: 108rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.98);
		border: 2rpx solid #eef1f5;
		display: flex;
		justify-content: space-between;
		align-items: center;
		box-shadow: 0 8rpx 20rpx rgba(31, 45, 61, 0.04);
		transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
	}

	.pay-item.active {
		background: linear-gradient(180deg, #f4f8ff 0%, #eef4ff 100%);
		border-color: #386df5;
		box-shadow: 0 12rpx 28rpx rgba(56, 109, 245, 0.14);
	}

	.pay-item.active .pay-name {
		color: #386df5;
	}

	.pay-item.active .pay-chip {
		background: rgba(56, 109, 245, 0.1);
		color: #386df5;
	}

	.pay-left {
		display: flex;
		align-items: center;
		gap: 18rpx;
		min-width: 0;
	}

	.pay-icon {
		width: 72rpx;
		height: 72rpx;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.pay-icon.green {
		background: #22c55e;
	}

	.pay-icon.blue {
		background: linear-gradient(135deg, #2f86ff 0%, #1677ff 100%);
	}

	.pay-copy {
		display: flex;
		flex-direction: column;
		min-width: 0;
	}

	.pay-name-row {
		display: flex;
		align-items: center;
		gap: 10rpx;
		flex-wrap: wrap;
	}

	.pay-name {
		font-size: 30rpx;
		font-weight: 700;
		color: #22324b;
		line-height: 1.3;
	}

	.pay-chip {
		display: inline-flex;
		align-items: center;
		height: 34rpx;
		padding: 0 12rpx;
		border-radius: 999rpx;
		background: #f3f5f8;
		color: #6b7280;
		font-size: 18rpx;
		font-weight: 700;
	}

	.pay-chip.recommend {
		background: rgba(34, 197, 94, 0.12);
		color: #16a34a;
	}

	.radio {
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		border: 2rpx solid #d9dfeb;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.radio.on {
		background: #386df5;
		border-color: #386df5;
		box-shadow: 0 0 0 8rpx rgba(56, 109, 245, 0.12);
	}

	.solid-button {
		margin-top: 28rpx;
		height: 96rpx;
		border-radius: 24rpx;
		background: linear-gradient(135deg, #2f6bf3 0%, #5586ff 100%);
		box-shadow: 0 16rpx 32rpx rgba(56, 109, 245, 0.26);
		font-weight: 800;
	}

	.solid-button.disabled {
		opacity: 0.55;
		box-shadow: none;
	}

</style>
