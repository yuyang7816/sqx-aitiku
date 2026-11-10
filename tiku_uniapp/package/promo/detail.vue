<template>
	<view class="app-page detail-page">
		<view class="screen detail-screen">
			<view class="summary-card card">
				<view class="summary-item">
					<view class="summary-icon blue">
						<up-icon name="rmb-circle" size="20" color="#386df5"></up-icon>
					</view>
					<text class="summary-label">累计收益</text>
					<text class="summary-value">¥{{ detail.totalAmount }}</text>
				</view>
				<view class="summary-divider"></view>
				<view class="summary-item">
					<view class="summary-icon green">
						<up-icon name="clock" size="20" color="#16a34a"></up-icon>
					</view>
					<text class="summary-label">待结算</text>
					<text class="summary-value green">¥{{ detail.pendingAmount }}</text>
				</view>
			</view>

			<view class="section-head">
				<text class="section-title record-title-section">收益记录</text>
				<text v-if="recordsHint" class="section-hint">{{ recordsHint }}</text>
			</view>
			<view v-if="detail.records.length" class="record-list card">
				<view v-for="item in detail.records" :key="item.id" class="record-row">
					<view class="record-left">
						<view class="record-icon">
							<up-icon name="account-fill" size="18" color="#386df5"></up-icon>
						</view>
						<view class="record-copy">
							<text class="record-title">{{ item.title }}</text>
							<text class="record-time">{{ item.time }}</text>
						</view>
					</view>
					<view class="record-right">
						<text class="record-amount">+{{ item.amount }}</text>
						<text class="record-status" :class="item.status === '已到账' ? 'settled' : ''">{{ item.status }}</text>
					</view>
				</view>
			</view>
			<view v-else class="record-empty card">
				<text class="record-empty-text">暂无收益记录</text>
				<text class="record-empty-sub">邀请好友开通会员后，推广收益将显示在这里</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { onMounted, ref } from 'vue';
	import { buildEmptyPromoIncomeDetail, fetchPromoIncomeDetail } from '@/common/promo-api';

	/** 收益明细（累计/待结算 + 流水），优先 GET /app/userWalletTransaction */
	const detail = ref(buildEmptyPromoIncomeDetail());
	const recordsHint = ref('');

	onMounted(async () => {
		const result = await fetchPromoIncomeDetail({ page: 1, limit: 10 });
		detail.value = result.detail;
		recordsHint.value = !result.fromApi && result.message ? result.message : '';
	});
</script>

<style lang="scss" scoped>
	.detail-page {
		background: linear-gradient(180deg, #F3F8FF 0, #F7FAFF 260rpx, #F7FAFE 620rpx, #F7FAFE 100%);
	}

	.detail-screen {
		padding-top: 12rpx;
	}

	.summary-card {
		margin-top: 16rpx;
		padding: 24rpx 28rpx;
		display: flex;
		align-items: center;
		position: relative;
		overflow: hidden;
		background: linear-gradient(180deg, #F9FBFF 0%, #FFFFFF 42%, #F4F8FF 100%);
		border: 1rpx solid rgba(121, 159, 229, 0.12);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08), inset 0 1rpx 0 rgba(255, 255, 255, 0.88);
	}

	.summary-card::before {
		content: '';
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		height: 46%;
		background: linear-gradient(180deg, rgba(233, 242, 255, 0.7) 0%, rgba(255, 255, 255, 0) 100%);
		pointer-events: none;
	}

	.summary-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		position: relative;
		z-index: 1;
		padding: 2rpx 0;
	}

	.summary-icon {
		width: 72rpx;
		height: 72rpx;
		border-radius: var(--radius-lg);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 8rpx;
		border: 1rpx solid rgba(255, 255, 255, 0.72);
		box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.72);
	}

	.summary-icon.blue {
		background: linear-gradient(180deg, rgba(235, 242, 255, 0.96) 0%, rgba(226, 236, 255, 0.92) 100%);
	}

	.summary-icon.green {
		background: linear-gradient(180deg, rgba(233, 250, 239, 0.98) 0%, rgba(224, 246, 232, 0.94) 100%);
	}

	.summary-divider {
		width: 2rpx;
		height: 80rpx;
		background: linear-gradient(180deg, rgba(229, 235, 244, 0) 0%, rgba(229, 235, 244, 0.92) 22%, rgba(229, 235, 244, 0.92) 78%, rgba(229, 235, 244, 0) 100%);
		position: relative;
		z-index: 1;
	}

	.summary-label {
		display: block;
		font-size: var(--text-base);
		color: #97a0af;
	}

	.summary-value {
		margin-top: 6rpx;
		display: block;
		font-size: 44rpx;
		font-weight: 800;
		color: var(--text-main);
		letter-spacing: 0;
		text-shadow: 0 1rpx 0 rgba(255, 255, 255, 0.45);
	}

	.summary-value.green {
		color: #14a05a;
	}

	.section-head {
		margin-top: 28rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.record-title-section {
		display: block;
		font-size: 30rpx;
		font-weight: 400;
		color: #253143;
	}

	.section-hint {
		flex: 1;
		text-align: right;
		font-size: 24rpx;
		color: #f59e0b;
	}

	.record-list {
		margin-top: 12rpx;
		padding: 0 28rpx;
		border: 1rpx solid rgba(121, 159, 229, 0.1);
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
	}

	.record-row {
		padding: 24rpx 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 18rpx;
	}

	.record-row:not(:last-child) {
		border-bottom: 2rpx solid #f2f4f8;
	}

	.record-left {
		display: flex;
		align-items: center;
		gap: 18rpx;
	}

	.record-icon {
		width: 72rpx;
		height: 72rpx;
		border-radius: var(--radius-lg);
		background: var(--primary-light);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.record-copy {
		display: flex;
		flex-direction: column;
	}

	.record-title {
		display: block;
		font-size: 30rpx;
		font-weight: 400;
		color: #253143;
	}

	.record-time,
	.record-status {
		margin-top: 8rpx;
		display: block;
		font-size: 22rpx;
		color: #A1AABA;
	}

	.record-status.settled {
		color: #67B282;
	}

	.record-right {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 8rpx;
		text-align: right;
	}

	.record-amount {
		display: block;
		font-size: 32rpx;
		font-weight: 800;
		color: #18995a;
	}

	.record-empty {
		margin-top: 16rpx;
		padding: 48rpx 32rpx;
		text-align: center;
		border: 1rpx solid rgba(121, 159, 229, 0.1);
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
	}

	.record-empty-text {
		display: block;
		font-size: 30rpx;
		font-weight: 400;
		color: #253143;
	}

	.record-empty-sub {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #97a0af;
		line-height: 1.5;
	}
</style>
