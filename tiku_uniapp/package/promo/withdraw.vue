<template>
	<view class="app-page withdraw-page">
		<view class="hero-bg">
			<view class="orb orb-a"></view>
			<view class="orb orb-b"></view>
			<view class="orb orb-c"></view>
		</view>

		<view class="screen withdraw-screen">
			<!-- 余额卡片 -->
			<view class="balance-card">
				<view class="balance-top">
					<view class="balance-icon-wrap">
						<up-icon name="rmb-circle" size="24" color="#FDE68A"></up-icon>
					</view>
					<text class="balance-label">可提现余额（元）</text>
				</view>
				<text class="balance-value">{{ balanceDisplay }}</text>
				<text v-if="balanceHint" class="balance-hint">{{ balanceHint }}</text>
				<view class="records-chip" @tap="goRecords">
					<text>提现记录</text>
					<up-icon name="arrow-right" size="12" color="#6366f1"></up-icon>
				</view>
			</view>

			<!-- 金额输入 -->
			<view class="section-block">
				<text class="section-title">提现金额</text>
				<view class="amount-shell" :class="{ 'has-error amount-error': amountHint }">
					<view class="amount-row">
						<text class="amount-symbol">¥</text>
						<input
							v-model="amount"
							class="amount-input"
							type="digit"
							:placeholder="amountPlaceholder"
							placeholder-class="placeholder"
							:disabled="!rulesFromApi"
						/>
					</view>
					<text v-if="amountHint" class="amount-hint">{{ amountHint }}</text>
					<view v-if="showFeePreview" class="fee-preview">
						<text>手续费 ¥{{ feePreview.fee.toFixed(2) }}</text>
						<text class="fee-dot">·</text>
						<text>预计到账 ¥{{ feePreview.actual.toFixed(2) }}</text>
					</view>
					<text v-else-if="showFeeRateHint" class="fee-hint">手续费率：{{ feeRateLabel }}</text>
					<text
						class="rules-hint"
						:class="{ error: rulesError, loading: !rulesLoaded && !rulesError }"
						@tap="handleRulesHintTap"
					>{{ rulesHintLine }}</text>
				</view>
			</view>

			<!-- 提现方式 -->
			<view class="section-block">
				<text class="section-title">提现方式</text>
				<view class="channel-shell">
					<view
						class="channel-row"
						:class="{ active: accountType === wechatType }"
						@tap="selectAccountType(wechatType)"
					>
						<view class="channel-left">
							<view class="channel-radio" :class="{ checked: accountType === wechatType }"></view>
							<view class="channel-icon wechat">
								<up-icon name="weixin-fill" size="18" color="#fff"></up-icon>
							</view>
							<text class="channel-name">微信</text>
						</view>
						<view class="channel-right" @tap.stop="goBindAccount(wechatType)">
							<text class="bind-status" :class="{ ready: wechatReady }">{{ wechatBindLabel }}</text>
							<up-icon name="arrow-right" size="12" :color="wechatReady ? '#94a3b8' : '#6366f1'"></up-icon>
						</view>
					</view>
					<view class="channel-divider"></view>
					<view
						class="channel-row"
						:class="{ active: accountType === alipayType }"
						@tap="selectAccountType(alipayType)"
					>
						<view class="channel-left">
							<view class="channel-radio" :class="{ checked: accountType === alipayType }"></view>
							<view class="channel-icon alipay">
								<up-icon name="zhifubao-circle-fill" size="18" color="#fff"></up-icon>
							</view>
							<text class="channel-name">支付宝</text>
						</view>
						<view class="channel-right" @tap.stop="goBindAccount(alipayType)">
							<text class="bind-status" :class="{ ready: alipayReady }">{{ alipayBindLabel }}</text>
							<up-icon name="arrow-right" size="12" :color="alipayReady ? '#94a3b8' : '#6366f1'"></up-icon>
						</view>
					</view>
				</view>
			</view>

			<view class="submit-btn" :class="{ disabled: !canApply || submitting }" @tap="apply">
				<text>{{ submitting ? '提交中…' : '申请提现' }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		WITHDRAW_ACCOUNT_TYPE_ALIPAY,
		WITHDRAW_ACCOUNT_TYPE_WECHAT,
		WITHDRAWAL_ACCOUNT_DB_TYPE_ALIPAY,
		WITHDRAWAL_ACCOUNT_DB_TYPE_WECHAT,
		applyWithdraw,
		buildEmptyWithdrawableBalance,
		fetchWithdrawableBalance,
		fetchWithdrawRules,
		fetchWithdrawalAccount,
		isWithdrawAccountReady
	} from '@/common/promo-api';
	import { PROMO_WITHDRAW_ACCOUNT_PAGE, PROMO_WITHDRAW_RECORDS_PAGE } from '@/common/routes';
	import {
		canApplyWithdraw,
		calcWithdrawFeePreview,
		formatFeeRateLabel,
		getWithdrawRules,
		validateWithdrawAmount
	} from '@/common/withdraw-rules';
	import { computed, onMounted, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';

	const wechatType = WITHDRAW_ACCOUNT_TYPE_WECHAT;
	const alipayType = WITHDRAW_ACCOUNT_TYPE_ALIPAY;

	const balance = ref(buildEmptyWithdrawableBalance());
	const balanceHint = ref('');
	const amount = ref('');
	const submitting = ref(false);
	const accountType = ref(wechatType);
	const rulesLoaded = ref(false);
	const rulesFromApi = ref(false);
	const rulesError = ref('');

	const wechatAccount = ref({ accountNumber: '', accountName: '' });
	const alipayAccount = ref({ accountNumber: '', accountName: '' });

	const rules = computed(() => getWithdrawRules());
	const amountPlaceholder = computed(() => (
		rulesFromApi.value
			? `请输入 ${rules.value.stepAmount} 的整数倍`
			: '请先加载提现规则'
	));
	const rulesHintLine = computed(() => {
		if (!rulesLoaded.value) {
			return '提现规则加载中…';
		}
		if (rulesError.value) {
			return `${rulesError.value}，点击重试`;
		}
		if (!rulesFromApi.value) {
			return '提现规则加载失败，请稍后重试';
		}
		const { minAmount, maxAmount, stepAmount } = rules.value;
		return `最低 ¥${minAmount}，单笔上限 ¥${maxAmount}，须为 ${stepAmount} 元整数倍，审核通过后预计 T+1 个工作日到账`;
	});
	const withdrawableAmountNum = computed(() => Number(balance.value.withdrawableAmount) || 0);
	const balanceDisplay = computed(() => balance.value.withdrawableAmount || '0.00');
	const isWechatChannel = computed(() => accountType.value === wechatType);
	const currentAccount = computed(() => (isWechatChannel.value ? wechatAccount.value : alipayAccount.value));
	const currentAccountReady = computed(() => isWithdrawAccountReady(accountType.value, currentAccount.value));
	const wechatReady = computed(() => isWithdrawAccountReady(wechatType, wechatAccount.value));
	const alipayReady = computed(() => isWithdrawAccountReady(alipayType, alipayAccount.value));
	const feeRateLabel = computed(() => formatFeeRateLabel(rules.value.feeRate));

	const maskAccountNumber = (value = '') => {
		const text = String(value || '').trim();
		if (!text || text.length <= 4) {
			return text;
		}
		return `${text.slice(0, 3)}****${text.slice(-2)}`;
	};

	const wechatBindLabel = computed(() => (wechatReady.value ? '已绑定' : '去绑定'));
	const alipayBindLabel = computed(() => {
		if (!alipayReady.value) {
			return '去绑定';
		}
		const name = String(alipayAccount.value.accountName || '').trim();
		const number = maskAccountNumber(alipayAccount.value.accountNumber);
		return name && number ? `${name} · ${number}` : '已绑定';
	});

	const amountValidation = computed(() =>
		validateWithdrawAmount(amount.value, withdrawableAmountNum.value, rules.value)
	);

	const amountHint = computed(() => {
		if (!String(amount.value || '').trim()) {
			return '';
		}
		const result = amountValidation.value;
		return result.valid ? '' : (result.message || '');
	});

	const feePreview = computed(() => calcWithdrawFeePreview(amount.value, rules.value));

	const showFeePreview = computed(() => rulesFromApi.value && feePreview.value.valid);

	const showFeeRateHint = computed(() => rulesFromApi.value && !feePreview.value.valid);

	const canApply = computed(() =>
		rulesFromApi.value
		&& !submitting.value
		&& currentAccountReady.value
		&& canApplyWithdraw(amount.value, withdrawableAmountNum.value, rules.value)
	);

	const selectAccountType = (type) => {
		accountType.value = type;
	};

	const handleRulesHintTap = () => {
		if (rulesError.value) {
			loadRules();
		}
	};

	const loadRules = async () => {
		rulesLoaded.value = false;
		rulesError.value = '';
		const result = await fetchWithdrawRules();
		rulesFromApi.value = Boolean(result.fromApi);
		rulesError.value = result.fromApi ? '' : (result.message || '提现规则加载失败，请稍后重试');
		rulesLoaded.value = true;
	};

	const loadAccounts = async () => {
		const [wechatResult, alipayResult] = await Promise.all([
			fetchWithdrawalAccount(WITHDRAWAL_ACCOUNT_DB_TYPE_WECHAT),
			fetchWithdrawalAccount(WITHDRAWAL_ACCOUNT_DB_TYPE_ALIPAY)
		]);
		wechatAccount.value = wechatResult.account;
		alipayAccount.value = alipayResult.account;
	};

	const loadBalance = async () => {
		const result = await fetchWithdrawableBalance();
		balance.value = result.balance;
		balanceHint.value = result.fromApi ? '' : '余额加载失败，请下拉刷新';
	};

	const goBindAccount = (type = accountType.value) => {
		uni.navigateTo({
			url: `${PROMO_WITHDRAW_ACCOUNT_PAGE}?type=${type}`
		});
	};

	const goRecords = () => {
		uni.navigateTo({ url: PROMO_WITHDRAW_RECORDS_PAGE });
	};

	const apply = async () => {
		if (!rulesFromApi.value) {
			uni.showToast({ title: rulesError.value || '提现规则未加载，请稍后重试', icon: 'none' });
			await loadRules();
			return;
		}
		if (!currentAccountReady.value) {
			uni.showToast({
				title: isWechatChannel.value ? '请先绑定微信收款码' : '请先绑定支付宝账户',
				icon: 'none'
			});
			goBindAccount();
			return;
		}
		const result = amountValidation.value;
		if (!result.valid) {
			uni.showToast({
				title: result.message || '请输入有效提现金额',
				icon: 'none'
			});
			return;
		}
		if (submitting.value) {
			return;
		}

		submitting.value = true;
		try {
			await applyWithdraw({
				amount: result.value,
				accountType: accountType.value
			});
			uni.showToast({ title: '提现申请已提交', icon: 'success' });
			amount.value = '';
			await loadBalance();
		} catch (e) {
			uni.showToast({
				title: e.msg || e.message || '提现申请失败，请稍后重试',
				icon: 'none'
			});
		} finally {
			submitting.value = false;
		}
	};

	onMounted(async () => {
		await Promise.all([loadRules(), loadBalance(), loadAccounts()]);
	});

	onShow(() => {
		loadAccounts();
	});
</script>

<style lang="scss" scoped>
	.withdraw-page {
		background: linear-gradient(180deg, #F3F8FF 0, #F7FAFF 260rpx, #F7FAFE 620rpx, #F7FAFE 100%);
		min-height: 100vh;
		position: relative;
		overflow: hidden;
	}

	.hero-bg {
		position: absolute;
		inset: 0;
		pointer-events: none;
		overflow: hidden;
	}

	.orb {
		position: absolute;
		border-radius: 50%;
		filter: blur(2px);
		opacity: 0.55;
		animation: floatOrb 8s ease-in-out infinite;
	}

	.orb-a {
		width: 280rpx;
		height: 280rpx;
		top: -60rpx;
		right: -40rpx;
		background: radial-gradient(circle, rgba(78, 136, 246, 0.18), rgba(78, 136, 246, 0));
	}

	.orb-b {
		width: 220rpx;
		height: 220rpx;
		top: 120rpx;
		left: -80rpx;
		background: radial-gradient(circle, rgba(191, 221, 255, 0.24), rgba(191, 221, 255, 0));
		animation-delay: -3s;
	}

	.orb-c {
		width: 180rpx;
		height: 180rpx;
		top: 360rpx;
		right: 60rpx;
		background: radial-gradient(circle, rgba(150, 198, 255, 0.22), rgba(150, 198, 255, 0));
		animation-delay: -5s;
	}

	@keyframes floatOrb {
		0%, 100% { transform: translateY(0) scale(1); }
		50% { transform: translateY(18rpx) scale(1.05); }
	}

	.withdraw-screen {
		position: relative;
		z-index: 1;
		padding: 24rpx 28rpx calc(40rpx + env(safe-area-inset-bottom));
	}

	.balance-card {
		padding: 34rpx 32rpx 24rpx;
		border-radius: 32rpx;
		background: linear-gradient(135deg, #4E88F6 0%, #6EA6FF 52%, #7BB2FF 100%);
		box-shadow: 0 18rpx 36rpx rgba(78, 136, 246, 0.18);
		position: relative;
		overflow: hidden;
	}

	.balance-top {
		display: flex;
		align-items: center;
		gap: 14rpx;
	}

	.balance-icon-wrap {
		width: 52rpx;
		height: 52rpx;
		border-radius: 16rpx;
		background: rgba(255, 255, 255, 0.2);
		border: 1rpx solid rgba(255, 255, 255, 0.18);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.balance-label {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.82);
		line-height: 1;
	}

	.balance-value {
		display: block;
		margin-top: 12rpx;
		font-size: 72rpx;
		font-weight: 800;
		color: #fff;
		letter-spacing: -1rpx;
		line-height: 1;
	}

	.balance-hint {
		display: block;
		margin-top: 10rpx;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.72);
	}

	.records-chip {
		position: absolute;
		top: 24rpx;
		right: 28rpx;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.9);
		border: 1rpx solid rgba(255, 255, 255, 0.24);
		display: flex;
		align-items: center;
		gap: 4rpx;
		font-size: 20rpx;
		color: #4E88F6;
		font-weight: 500;
	}

	.section-block {
		margin-top: 28rpx;
	}

	.section-title {
		display: block;
		font-size: 30rpx;
		font-weight: 400;
		color: #1e293b;
		margin-bottom: 16rpx;
	}

	.amount-shell {
		padding: 22rpx 24rpx;
		border-radius: 28rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
		border: 2rpx solid transparent;
		position: relative;
		transition: border-color 0.2s;
	}

	.amount-shell::before {
		content: '';
		position: absolute;
		left: 24rpx;
		right: 24rpx;
		top: 22rpx;
		height: 84rpx;
		border-radius: 22rpx;
		background: linear-gradient(180deg, rgba(243, 247, 255, 0.88) 0%, rgba(255, 255, 255, 0) 100%);
		pointer-events: none;
	}

	.amount-shell.has-error {
		border-color: #fca5a5;
	}

	.amount-row {
		display: flex;
		align-items: center;
		gap: 6rpx;
		position: relative;
		z-index: 1;
		padding-bottom: 12rpx;
		border-bottom: 2rpx solid #EEF3FB;
	}

	.amount-symbol {
		font-size: 40rpx;
		font-weight: 800;
		color: #6366f1;
	}

	.amount-input {
		flex: 1;
		height: 74rpx;
		font-size: 38rpx;
		font-weight: 700;
		color: #0f172a;
	}

	.placeholder {
		color: #cbd5e1;
		font-weight: 400;
		font-size: 28rpx;
	}

	.amount-hint {
		display: block;
		margin-top: 14rpx;
		font-size: 24rpx;
		color: #ef4444;
		position: relative;
		z-index: 1;
	}

	.fee-preview {
		margin-top: 14rpx;
		font-size: 24rpx;
		line-height: 1.5;
		color: #64748b;
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 8rpx;
		position: relative;
		z-index: 1;
	}

	.fee-dot {
		color: #cbd5e1;
	}

	.fee-hint {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		line-height: 1.5;
		color: #64748b;
		position: relative;
		z-index: 1;
	}

	.rules-hint {
		display: block;
		margin-top: 12rpx;
		font-size: 22rpx;
		line-height: 1.5;
		color: #94a3b8;
		position: relative;
		z-index: 1;
	}

	.rules-hint.loading {
		color: #b0bac9;
	}

	.rules-hint.error {
		color: #ef4444;
	}

	.channel-shell {
		border-radius: 28rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
		overflow: hidden;
	}

	.channel-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		padding: 24rpx 28rpx;
		transition: background 0.15s;
	}

	.channel-row.active {
		background: rgba(78, 136, 246, 0.06);
	}

	.channel-divider {
		height: 1rpx;
		margin: 0 28rpx;
		background: #EEF3FB;
	}

	.channel-left {
		display: flex;
		align-items: center;
		gap: 16rpx;
		min-width: 0;
		flex: 1;
	}

	.channel-right {
		display: flex;
		align-items: center;
		gap: 4rpx;
		flex-shrink: 0;
		max-width: 52%;
	}

	.channel-radio {
		width: 32rpx;
		height: 32rpx;
		border-radius: 50%;
		border: 2rpx solid #cbd5e1;
		box-sizing: border-box;
		flex-shrink: 0;
		position: relative;
	}

	.channel-radio.checked {
		border-color: #4E88F6;
	}

	.channel-radio.checked::after {
		content: '';
		position: absolute;
		left: 50%;
		top: 50%;
		width: 16rpx;
		height: 16rpx;
		border-radius: 50%;
		background: #4E88F6;
		transform: translate(-50%, -50%);
	}

	.channel-icon {
		width: 52rpx;
		height: 52rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.channel-icon.wechat { background: linear-gradient(135deg, #22c55e, #16a34a); }
	.channel-icon.alipay { background: linear-gradient(135deg, #3b82f6, #2563eb); }

	.channel-name {
		font-size: 28rpx;
		font-weight: 600;
		color: #1e293b;
	}

	.bind-status {
		font-size: 24rpx;
		color: #6366f1;
		max-width: 100%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.bind-status.ready {
		color: #94a3b8;
	}

	.submit-btn {
		margin-top: 32rpx;
		height: 96rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #4A80EE 0%, #6398F8 100%);
		box-shadow: 0 16rpx 36rpx rgba(78, 136, 246, 0.22);
		border: 1rpx solid rgba(78, 136, 246, 0.08);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 32rpx;
		font-weight: 700;
		color: #fff;
	}

	.submit-btn.disabled {
		background: linear-gradient(135deg, #D6E4FB 0%, #C9DCF9 100%);
		border-color: rgba(154, 183, 233, 0.4);
		box-shadow: none;
		color: rgba(255, 255, 255, 0.88);
	}
</style>
