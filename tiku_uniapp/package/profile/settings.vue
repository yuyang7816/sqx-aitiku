<template>
	<view class="app-page">
		<view class="screen settings-screen">
			<view class="settings-section">
				<text class="section-title">协议与隐私</text>
				<view class="menu-card">
					<view class="menu-row" @tap="openAgreement">
						<view class="menu-left">
							<view class="menu-icon menu-icon--agreement">
								<up-icon name="file-text" size="18" color="#386DF5"></up-icon>
							</view>
							<text class="menu-title">用户协议</text>
						</view>
						<up-icon name="arrow-right" size="14" color="#AEB8C8"></up-icon>
					</view>
					<view class="menu-row" @tap="openPrivacy">
						<view class="menu-left">
							<view class="menu-icon menu-icon--privacy">
								<up-icon name="lock" size="18" color="#8764E8"></up-icon>
							</view>
							<text class="menu-title">隐私政策</text>
						</view>
						<up-icon name="arrow-right" size="14" color="#AEB8C8"></up-icon>
					</view>
				</view>
			</view>

			<view class="settings-section account-section">
				<text class="section-title">账号操作</text>
				<view class="account-button logout" @tap="logout">
					<up-icon name="close-circle" size="16" color="#FFFFFF"></up-icon>
					<text class="logout-text">退出当前账号</text>
				</view>
				<view
					v-if="deleteStatus.pending"
					class="delete-pending-card"
				>
					<view class="pending-head">
						<up-icon name="clock" size="18" color="#D97706"></up-icon>
						<text class="pending-title">账号注销冷静期中</text>
					</view>
					<text class="pending-copy">
						预计 {{ deleteStatus.effectiveAt || '—' }} 执行注销，剩余约 {{ deleteStatus.remainingDays }} 天。
						冷静期内您的学习数据会完整保留，可随时撤销。
					</text>
					<view class="cancel-delete-button" :class="{ 'is-disabled': isDeletingAccount }" @tap="cancelDelete">
						<text>撤销注销申请</text>
					</view>
				</view>
				<view
					v-else
					class="account-button delete-account"
					:class="{ 'is-disabled': isDeletingAccount }"
					@tap="deleteAccount"
				>
					<up-icon name="trash" size="16" color="#C0392B"></up-icon>
					<text class="delete-account-text">账号注销</text>
				</view>
			</view>
			<text class="version-text">AI 题库 v1.0.0</text>
		</view>
	</view>
</template>

<script setup>
	import { reactive, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import {
		performLogoutAndRedirect
	} from '@/common/app-data';
	import {
		confirmAndCancelDeleteAccount,
		confirmAndDeleteAccount,
		fetchDeleteAccountStatus
	} from '@/common/user-delete-account';
	import { AGREEMENT_PAGE, PRIVACY_PAGE } from '@/common/routes';
	/** 注销相关请求进行中，防止重复点击 */
	const isDeletingAccount = ref(false);
	const deleteStatus = reactive({
		pending: false,
		effectiveAt: '',
		remainingDays: 0,
		coolingDays: 7
	});

	const refreshDeleteStatus = async () => {
		const result = await fetchDeleteAccountStatus();
		if (!result.success || !result.data) {
			deleteStatus.pending = false;
			deleteStatus.effectiveAt = '';
			deleteStatus.remainingDays = 0;
			return;
		}
		deleteStatus.pending = result.data.pending;
		deleteStatus.effectiveAt = result.data.effectiveAt;
		deleteStatus.remainingDays = result.data.remainingDays;
		deleteStatus.coolingDays = result.data.coolingDays;
	};

	onShow(() => {
		refreshDeleteStatus();
	});

	const openArticle = (url) => {
		uni.navigateTo({ url });
	};

	const openAgreement = () => openArticle(AGREEMENT_PAGE);

	const openPrivacy = () => openArticle(PRIVACY_PAGE);

	/** 退出当前账号：同我的页 logoutUser，清鉴权保留学习数据后跳转登录页 */
	const logout = () => {
		performLogoutAndRedirect();
	};

	/** 账号注销：二次确认 → POST /app/user/delete → 进入冷静期 */
	const deleteAccount = async () => {
		if (isDeletingAccount.value || deleteStatus.pending) {
			return;
		}
		isDeletingAccount.value = true;
		try {
			const data = await confirmAndDeleteAccount();
			if (data && data.pending) {
				deleteStatus.pending = true;
				deleteStatus.effectiveAt = data.effectiveAt;
				deleteStatus.remainingDays = data.remainingDays;
				deleteStatus.coolingDays = data.coolingDays;
			}
		} finally {
			isDeletingAccount.value = false;
		}
	};

	/** 撤销注销申请 */
	const cancelDelete = async () => {
		if (isDeletingAccount.value || !deleteStatus.pending) {
			return;
		}
		isDeletingAccount.value = true;
		try {
			const data = await confirmAndCancelDeleteAccount();
			if (data && !data.pending) {
				deleteStatus.pending = false;
				deleteStatus.effectiveAt = '';
				deleteStatus.remainingDays = 0;
			}
		} finally {
			isDeletingAccount.value = false;
		}
	};
</script>

<style lang="scss" scoped>
	.settings-screen {
		padding-top: 32rpx;
		background: #F6F8FC;
	}

	.settings-section + .settings-section {
		margin-top: 52rpx;
	}

	.section-title {
		display: block;
		font-size: 32rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #243041;
	}

	.menu-card {
		margin-top: 18rpx;
		padding: 0 24rpx;
		border-radius: 22rpx;
		background: #FFFFFF;
		border: 1rpx solid #EDF1F6;
		box-shadow: 0 10rpx 26rpx rgba(36, 48, 65, 0.04);
	}

	.menu-row {
		min-height: 108rpx;
		padding: 22rpx 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 20rpx;
	}

	.menu-row:not(:last-child) {
		border-bottom: 1rpx solid #EEF1F5;
	}

	.menu-row {
		transition: opacity 0.2s ease;
	}

	.menu-row:active {
		background: #FAFBFD;
	}

	.menu-left {
		display: flex;
		align-items: center;
		gap: 18rpx;
	}

	.menu-icon {
		width: 60rpx;
		height: 60rpx;
		border-radius: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.menu-icon--agreement {
		background: #EEF4FF;
	}

	.menu-icon--privacy {
		background: #F4F0FF;
	}

	.menu-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #253143;
	}

	.account-button {
		height: 88rpx;
		border-radius: 18rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		font-size: 28rpx;
		font-weight: 700;
	}

	.account-button:active,
	.cancel-delete-button:active {
		opacity: 0.72;
	}

	.logout {
		margin-top: 18rpx;
		background: #386DF5;
		border: 1rpx solid #386DF5;
		box-shadow: 0 10rpx 22rpx rgba(56, 109, 245, 0.16);
	}

	.logout-text {
		color: #FFFFFF;
		font-weight: 700;
	}

	.delete-account {
		margin-top: 44rpx;
		display: flex;
		background: #FFF5F3;
		border: 1rpx solid #F5D0D0;
		box-shadow: none;
	}

	.delete-account.is-disabled {
		opacity: 0.5;
		pointer-events: none;
	}

	.delete-account-text {
		color: #c0392b;
		font-weight: 700;
	}

	.delete-pending-card {
		margin-top: 44rpx;
		padding: 24rpx;
		border-radius: 18rpx;
		background: #FFF9ED;
		border: 1rpx solid #F7DDA5;
	}

	.pending-head {
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	.pending-title {
		font-size: 28rpx;
		font-weight: 800;
		color: #b45309;
	}

	.pending-copy {
		margin-top: 16rpx;
		display: block;
		font-size: 22rpx;
		line-height: 1.7;
		color: #92400e;
	}

	.cancel-delete-button {
		margin-top: 20rpx;
		height: 80rpx;
		border-radius: 16rpx;
		background: #FFFDF8;
		border: 1rpx solid #E7BA55;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-weight: 700;
		color: #B45309;
	}

	.cancel-delete-button.is-disabled {
		opacity: 0.5;
		pointer-events: none;
	}

	.version-text {
		margin-top: 56rpx;
		display: block;
		text-align: center;
		font-size: var(--text-sm);
		color: #b3bcc9;
	}

	/* #ifdef MP-WEIXIN */
	.menu-icon {
		border-radius: 24rpx;
	}

	.menu-title,
	.pending-title {
		font-size: 30rpx;
	}

	.pending-copy,
	.version-text {
		font-size: 22rpx;
	}

	.cancel-delete-button {
		font-size: 28rpx;
	}
	/* #endif */
</style>
