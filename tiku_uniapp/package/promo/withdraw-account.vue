<template>
	<view class="app-page account-page">
		<view class="hero-bg">
			<view class="orb orb-a"></view>
			<view class="orb orb-b"></view>
		</view>

		<view class="screen account-screen">
			<view class="tab-slider">
				<view
					class="tab-item"
					:class="{ active: accountType === wechatType }"
					@tap="selectAccountType(wechatType)"
				>
					<up-icon name="weixin-fill" size="16" :color="accountType === wechatType ? '#22c55e' : '#94a3b8'"></up-icon>
					<text>微信</text>
				</view>
				<view
					class="tab-item"
					:class="{ active: accountType === alipayType }"
					@tap="selectAccountType(alipayType)"
				>
					<up-icon name="zhifubao-circle-fill" size="16" :color="accountType === alipayType ? '#3b82f6' : '#94a3b8'"></up-icon>
					<text>支付宝</text>
				</view>
				<view class="tab-indicator" :class="isWechatChannel ? 'left' : 'right'"></view>
			</view>

			<view v-if="isWechatChannel" class="form-panel">
				<view class="panel-head">
					<view class="panel-title-row">
						<text class="panel-title">微信收款码</text>
						<view class="status-pill" :class="{ ready: wechatReady }">
							<text>{{ wechatReady ? '已绑定 ✓' : '待绑定' }}</text>
						</view>
					</view>
					<text class="panel-desc">上传你的微信收款码，审核通过后打款到此账户</text>
				</view>

				<view class="qr-stage">
					<view v-if="wechatQrPreview" class="qr-box filled" @tap="chooseWechatQr">
						<image :src="wechatQrPreview" mode="aspectFill" class="qr-image"></image>
						<view class="qr-mask">
							<up-icon name="camera-fill" size="22" color="#fff"></up-icon>
							<text>更换收款码</text>
						</view>
					</view>
					<view v-else class="qr-box empty" @tap="chooseWechatQr">
						<view class="qr-ring"></view>
						<up-icon name="camera-fill" size="32" color="#6366f1"></up-icon>
						<text class="qr-tip">点击上传收款码</text>
						<text class="qr-sub">支持相册或拍照</text>
					</view>
				</view>

				<view
					class="save-btn"
					:class="{ disabled: savingWechat || !wechatQrUrl }"
					@tap="saveWechatAccount"
				>
					<text>{{ savingWechat ? '保存中…' : (wechatReady ? '更新收款码' : '保存收款码') }}</text>
				</view>
			</view>

			<view v-else class="form-panel">
				<view class="panel-head">
					<view class="panel-title-row">
						<text class="panel-title">支付宝账户</text>
						<view class="status-pill" :class="{ ready: alipayReady }">
							<text>{{ alipayReady ? '已绑定 ✓' : '待绑定' }}</text>
						</view>
					</view>
					<text class="panel-desc">请填写与支付宝实名一致的账号和姓名</text>
				</view>

				<view class="field-card">
					<text class="field-label">支付宝账号</text>
					<input
						v-model="alipayAccount"
						class="field-input"
						placeholder="手机号 / 邮箱"
						placeholder-class="placeholder"
					/>
				</view>
				<view class="field-card">
					<text class="field-label">收款姓名</text>
					<input
						v-model="alipayName"
						class="field-input"
						placeholder="与支付宝实名一致"
						placeholder-class="placeholder"
					/>
				</view>

				<view
					class="save-btn"
					:class="{ disabled: savingAlipay || !alipayAccount.trim() || !alipayName.trim() }"
					@tap="saveAlipayAccount"
				>
					<text>{{ savingAlipay ? '保存中…' : (alipayReady ? '更新账户' : '保存账户') }}</text>
				</view>
			</view>

			<view class="safe-note">
				<up-icon name="lock-fill" size="14" color="#94a3b8"></up-icon>
				<text>账户信息仅用于提现，不会对外展示</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { resolveUploadedFileUrl, uploadLocalImage } from '@/common/file-upload';
	import {
		WITHDRAW_ACCOUNT_TYPE_ALIPAY,
		WITHDRAW_ACCOUNT_TYPE_WECHAT,
		WITHDRAWAL_ACCOUNT_DB_TYPE_ALIPAY,
		WITHDRAWAL_ACCOUNT_DB_TYPE_WECHAT,
		fetchWithdrawalAccount,
		isAlipayWithdrawAccountReady,
		isWechatWithdrawAccountReady,
		saveWithdrawalAccount
	} from '@/common/promo-api';
	import { computed, ref } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';

	const wechatType = WITHDRAW_ACCOUNT_TYPE_WECHAT;
	const alipayType = WITHDRAW_ACCOUNT_TYPE_ALIPAY;

	const accountType = ref(wechatType);
	const wechatAccount = ref({ accountNumber: '', accountName: '' });
	const alipayAccount = ref('');
	const alipayName = ref('');
	const wechatQrUrl = ref('');
	const savingWechat = ref(false);
	const savingAlipay = ref(false);

	const isWechatChannel = computed(() => accountType.value === wechatType);
	const wechatReady = computed(() => isWechatWithdrawAccountReady(wechatAccount.value));
	const alipayReady = computed(() => isAlipayWithdrawAccountReady({
		accountNumber: alipayAccount.value,
		accountName: alipayName.value
	}));
	const wechatQrPreview = computed(() => resolveUploadedFileUrl(wechatQrUrl.value || wechatAccount.value.accountNumber));

	const selectAccountType = (type) => {
		accountType.value = type;
	};

	const loadAccounts = async () => {
		const [wechatResult, alipayResult] = await Promise.all([
			fetchWithdrawalAccount(WITHDRAWAL_ACCOUNT_DB_TYPE_WECHAT),
			fetchWithdrawalAccount(WITHDRAWAL_ACCOUNT_DB_TYPE_ALIPAY)
		]);
		wechatAccount.value = wechatResult.account;
		wechatQrUrl.value = wechatResult.account.accountNumber || '';
		alipayAccount.value = alipayResult.account.accountNumber || '';
		alipayName.value = alipayResult.account.accountName || '';
	};

	const chooseWechatQr = () => {
		uni.chooseImage({
			count: 1,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: async (res) => {
				const filePath = res.tempFilePaths && res.tempFilePaths[0];
				if (!filePath) {
					return;
				}
				// 先用本地临时路径预览，避免等待上传完成前空白
				wechatQrUrl.value = filePath;
				try {
					uni.showLoading({ title: '上传中', mask: true });
					const url = await uploadLocalImage(filePath);
					wechatQrUrl.value = url;
				} catch (e) {
					uni.showToast({
						title: e.msg || e.message || '收款码上传失败',
						icon: 'none'
					});
				} finally {
					uni.hideLoading();
				}
			}
		});
	};

	const saveWechatAccount = async () => {
		if (savingWechat.value || !wechatQrUrl.value) {
			if (!wechatQrUrl.value) {
				uni.showToast({ title: '请先上传微信收款码', icon: 'none' });
			}
			return;
		}
		savingWechat.value = true;
		try {
			await saveWithdrawalAccount({
				type: WITHDRAWAL_ACCOUNT_DB_TYPE_WECHAT,
				accountName: '微信收款码',
				accountNumber: wechatQrUrl.value
			});
			wechatAccount.value = {
				...wechatAccount.value,
				accountNumber: wechatQrUrl.value,
				accountName: '微信收款码'
			};
			uni.showToast({ title: '微信收款码已保存', icon: 'success' });
		} catch (e) {
			uni.showToast({
				title: e.msg || e.message || '保存失败',
				icon: 'none'
			});
		} finally {
			savingWechat.value = false;
		}
	};

	const saveAlipayAccount = async () => {
		const accountNumber = String(alipayAccount.value || '').trim();
		const accountName = String(alipayName.value || '').trim();
		if (!accountNumber || !accountName) {
			uni.showToast({ title: '请填写支付宝账号和收款姓名', icon: 'none' });
			return;
		}
		if (savingAlipay.value) {
			return;
		}
		savingAlipay.value = true;
		try {
			await saveWithdrawalAccount({
				type: WITHDRAWAL_ACCOUNT_DB_TYPE_ALIPAY,
				accountName,
				accountNumber
			});
			alipayAccount.value = accountNumber;
			alipayName.value = accountName;
			uni.showToast({ title: '支付宝账户已保存', icon: 'success' });
		} catch (e) {
			uni.showToast({
				title: e.msg || e.message || '保存失败',
				icon: 'none'
			});
		} finally {
			savingAlipay.value = false;
		}
	};

	onLoad((options) => {
		const type = Number(options?.type);
		if (type === alipayType) {
			accountType.value = alipayType;
		}
		loadAccounts();
	});
</script>

<style lang="scss" scoped>
	.account-page {
		background: linear-gradient(180deg, #F3F8FF 0, #F7FAFF 260rpx, #F7FAFE 620rpx, #F7FAFE 100%);
		min-height: 100vh;
		position: relative;
		overflow: hidden;
	}

	.hero-bg {
		position: absolute;
		inset: 0;
		pointer-events: none;
	}

	.orb {
		position: absolute;
		border-radius: 50%;
		opacity: 0.5;
		animation: drift 9s ease-in-out infinite;
	}

	.orb-a {
		width: 260rpx;
		height: 260rpx;
		top: -50rpx;
		left: -60rpx;
		background: radial-gradient(circle, rgba(78, 136, 246, 0.18), transparent);
	}

	.orb-b {
		width: 200rpx;
		height: 200rpx;
		top: 180rpx;
		right: -40rpx;
		background: radial-gradient(circle, rgba(191, 221, 255, 0.24), transparent);
		animation-delay: -4s;
	}

	@keyframes drift {
		0%, 100% { transform: translate(0, 0); }
		50% { transform: translate(0, 16rpx); }
	}

	.account-screen {
		position: relative;
		z-index: 1;
		padding: 24rpx 28rpx calc(40rpx + env(safe-area-inset-bottom));
	}

	.tab-slider {
		position: relative;
		display: flex;
		padding: 8rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.92);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
	}

	.tab-item {
		flex: 1;
		height: 76rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 28rpx;
		font-weight: 400;
		color: #64748b;
		position: relative;
		z-index: 1;
	}

	.tab-item.active {
		color: #1e293b;
	}

	.tab-indicator {
		position: absolute;
		top: 8rpx;
		width: calc(50% - 8rpx);
		height: 76rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #eef4ff, #f5f9ff);
		box-shadow: 0 4rpx 16rpx rgba(78, 136, 246, 0.12);
		transition: left 0.25s ease;
	}

	.tab-indicator.left { left: 8rpx; }
	.tab-indicator.right { left: calc(50%); }

	.form-panel {
		margin-top: 28rpx;
		padding: 32rpx 28rpx;
		border-radius: 32rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		border: 1rpx solid rgba(121, 159, 229, 0.12);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
	}

	.panel-head {
		margin-bottom: 28rpx;
	}

	.panel-title-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.panel-title {
		font-size: 34rpx;
		font-weight: 400;
		color: #1e293b;
	}

	.status-pill {
		padding: 8rpx 18rpx;
		border-radius: 999rpx;
		background: #fef3c7;
		font-size: 22rpx;
		color: #d97706;
		font-weight: 600;
	}

	.status-pill.ready {
		background: #dcfce7;
		color: #16a34a;
	}

	.panel-desc {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #94a3b8;
		line-height: 1.6;
	}

	.qr-stage {
		display: flex;
		justify-content: center;
		padding: 12rpx 0 8rpx;
	}

	.qr-box {
		width: 320rpx;
		height: 320rpx;
		border-radius: 32rpx;
		overflow: hidden;
		position: relative;
	}

	.qr-box.empty {
		border: 3rpx dashed #c7d2fe;
		background: linear-gradient(180deg, #f7fbff, #f8fafc);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
	}

	.qr-ring {
		position: absolute;
		inset: 16rpx;
		border-radius: 24rpx;
		border: 2rpx solid rgba(78, 136, 246, 0.15);
		animation: pulseRing 2.4s ease-in-out infinite;
	}

	@keyframes pulseRing {
		0%, 100% { opacity: 0.4; transform: scale(1); }
		50% { opacity: 1; transform: scale(1.02); }
	}

	.qr-tip {
		font-size: 28rpx;
		font-weight: 400;
		color: #4E88F6;
	}

	.qr-sub {
		font-size: 22rpx;
		color: #94a3b8;
	}

	.qr-box.filled .qr-image {
		width: 100%;
		height: 100%;
	}

	.qr-mask {
		position: absolute;
		inset: 0;
		background: rgba(15, 23, 42, 0.42);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		opacity: 0;
		transition: opacity 0.2s;
		font-size: 24rpx;
		color: #fff;
		font-weight: 600;
	}

	.qr-box.filled:active .qr-mask {
		opacity: 1;
	}

	.field-card {
		margin-top: 20rpx;
		padding: 22rpx 24rpx;
		border-radius: 20rpx;
		background: #f8fbff;
		border: 1rpx solid rgba(121, 159, 229, 0.12);
	}

	.field-label {
		display: block;
		font-size: 22rpx;
		color: #64748b;
		margin-bottom: 10rpx;
	}

	.field-input {
		width: 100%;
		height: 72rpx;
		font-size: 30rpx;
		font-weight: 400;
		color: #0f172a;
	}

	.placeholder {
		color: #cbd5e1;
		font-weight: 500;
		font-size: 28rpx;
	}

	.save-btn {
		margin-top: 36rpx;
		height: 92rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #4E88F6, #6EA6FF);
		box-shadow: 0 14rpx 32rpx rgba(78, 136, 246, 0.22);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 30rpx;
		font-weight: 400;
		color: #fff;
	}

	.save-btn.disabled {
		opacity: 0.45;
		box-shadow: none;
	}

	.safe-note {
		margin-top: 28rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 22rpx;
		color: #94a3b8;
	}
</style>
