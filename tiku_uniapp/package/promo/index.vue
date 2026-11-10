<template>
	<view class="app-page promo-page">
		<view class="screen promo-screen">
			<view v-if="loading" class="state-panel">
				<up-loading-icon mode="circle" color="#386df5"></up-loading-icon>
				<text class="state-text">正在加载推广数据</text>
			</view>

			<view v-else-if="loadFailed" class="state-panel">
				<text class="state-text">加载失败，请检查网络后重试</text>
				<text class="link-text" @tap="reloadPage">重新加载</text>
			</view>

			<template v-else>
				<view v-if="partialHint" class="hint-bar"><text>{{ partialHint }}</text></view>

				<view class="hero-card">
					<view class="hero-orb orb-a"></view>
					<view class="hero-orb orb-b"></view>
					<text class="hero-watermark">推广</text>
					<view class="hero-inner">
						<view class="hero-badge">
							<up-icon name="share-fill" size="12" color="#DDEBFF"></up-icon>
							<text>我的推广收益</text>
						</view>
						<text class="hero-label">累计收益（元）</text>
						<text class="hero-amount">¥{{ summary.totalCommission }}</text>
						<view class="hero-metrics">
							<view class="metric-box">
								<view class="metric-icon">
									<up-icon name="calendar" size="14" color="#FFFFFF"></up-icon>
								</view>
								<view class="metric-copy">
									<text class="metric-label">今日收益</text>
									<text class="metric-value">¥{{ summary.todayCommission }}</text>
								</view>
							</view>
							<view class="metric-divider"></view>
							<view class="metric-box">
								<view class="metric-icon">
									<up-icon name="rmb-circle" size="14" color="#FFFFFF"></up-icon>
								</view>
								<view class="metric-copy">
									<text class="metric-label">可提现</text>
									<text class="metric-value">¥{{ withdrawableAmount }}</text>
								</view>
							</view>
						</view>
					</view>
				</view>

				<view class="section-head">
					<view class="section-accent"></view>
					<text class="section-title">常用功能</text>
				</view>

				<view class="menu-card">
					<view class="menu-row" @tap="showDetail">
						<view class="menu-icon blue">
							<up-icon name="file-text" size="18" color="#386df5"></up-icon>
						</view>
						<view class="menu-body">
							<text class="menu-label">收益明细</text>
							<text class="menu-desc">待结算 ¥{{ pendingAmount }}</text>
						</view>
					</view>
					<view class="menu-row highlight" @tap="showWithdraw">
						<view class="menu-icon green">
							<up-icon name="rmb-circle" size="18" color="#16a34a"></up-icon>
						</view>
						<view class="menu-body">
							<text class="menu-label">立即提现</text>
							<text class="menu-desc">可提现 ¥{{ withdrawableAmount }}</text>
						</view>
					</view>
					<view class="menu-row" @tap="showWithdrawRecords">
						<view class="menu-icon teal">
							<up-icon name="clock" size="18" color="#0891b2"></up-icon>
						</view>
						<view class="menu-body">
							<text class="menu-label">提现明细</text>
							<text class="menu-desc">查看提现申请与审核进度</text>
						</view>
					</view>
					<view class="menu-row" @tap="showCommissionList">
						<view class="menu-icon orange">
							<up-icon name="list" size="18" color="#ea580c"></up-icon>
						</view>
						<view class="menu-body">
							<text class="menu-label">分佣明细</text>
							<text class="menu-desc">查看下级分佣记录</text>
						</view>
					</view>
					<view class="menu-row" @tap="showInviteList">
						<view class="menu-icon purple">
							<up-icon name="account-fill" size="18" color="#7c5cff"></up-icon>
						</view>
						<view class="menu-body">
							<text class="menu-label">邀请人数</text>
							<text class="menu-desc">已邀请 {{ summary.inviteCount }} 人</text>
						</view>
					</view>
				</view>

				<view class="poster-btn" @tap="poster">
					<view class="poster-btn-glow"></view>
					<up-icon name="photo" size="18" color="#FFFFFF"></up-icon>
					<text>生成我的专属推广海报</text>
				</view>
			</template>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import {
		buildEmptyPromoSummary,
		fetchPromoIndexData
	} from '@/common/promo-api';
	import {
		buildPromoInviteListUrl,
		PROMO_DETAIL_PAGE,
		PROMO_POSTER_URL,
		PROMO_WITHDRAW_PAGE,
		PROMO_WITHDRAW_RECORDS_PAGE
	} from '@/common/routes';

	const loading = ref(true);
	const loadFailed = ref(false);
	const partialHint = ref('');
	const summary = ref(buildEmptyPromoSummary());
	const withdrawableAmount = ref('0.00');
	const pendingAmount = ref('0.00');

	const go = (url) => uni.navigateTo({ url });
	const poster = () => go(PROMO_POSTER_URL);
	const showDetail = () => go(PROMO_DETAIL_PAGE);
	const showWithdraw = () => go(PROMO_WITHDRAW_PAGE);
	const showWithdrawRecords = () => go(PROMO_WITHDRAW_RECORDS_PAGE);
	const showCommissionList = () => go(buildPromoInviteListUrl('commission'));
	const showInviteList = () => go(buildPromoInviteListUrl('invite'));

	const applyIndexData = (result) => {
		summary.value = result.summary;
		withdrawableAmount.value = result.withdrawableAmount;
		pendingAmount.value = result.pendingAmount;
		partialHint.value = result.errors.length
			? `${result.errors.join('、')}加载失败，请稍后重试`
			: '';
	};

	const reloadPage = async () => {
		loading.value = true;
		loadFailed.value = false;
		partialHint.value = '';
		try {
			const result = await fetchPromoIndexData();
			if (!result.fromApi && result.errors.length >= 3) {
				loadFailed.value = true;
				return;
			}
			applyIndexData(result);
		} catch (e) {
			loadFailed.value = true;
		} finally {
			loading.value = false;
		}
	};

	onShow(() => {
		reloadPage();
	});
</script>

<style lang="scss" scoped>
	.promo-page {
		min-height: 100vh;
		background: linear-gradient(180deg, #E8F0FF 0%, #F3F8FF 320rpx, #F7FAFE 100%);
	}

	.promo-screen {
		padding-top: 12rpx;
		padding-bottom: 48rpx;
	}

	.state-panel {
		margin-top: 24rpx;
		padding: 72rpx 40rpx;
		border-radius: 32rpx;
		background: #FFFFFF;
		border: 1rpx solid rgba(121, 159, 229, 0.12);
		box-shadow: 0 16rpx 36rpx rgba(74, 108, 173, 0.08);
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
	}

	.state-text {
		font-size: 26rpx;
		color: #8B95A8;
	}

	.link-text {
		font-size: 26rpx;
		color: #386DF5;
		font-weight: 600;
	}

	.hint-bar {
		margin-bottom: 16rpx;
		padding: 18rpx 24rpx;
		border-radius: 20rpx;
		background: linear-gradient(180deg, #FFFBEB 0%, #FFF7E8 100%);
		border: 1rpx solid rgba(245, 158, 11, 0.18);
		box-shadow: 0 8rpx 20rpx rgba(245, 158, 11, 0.08);
	}

	.hint-bar text {
		font-size: 24rpx;
		color: #B45309;
		line-height: 1.5;
	}

	.hero-card {
		position: relative;
		border-radius: 32rpx;
		overflow: hidden;
		box-shadow: 0 22rpx 46rpx rgba(43, 78, 151, 0.22);
	}

	.hero-orb {
		position: absolute;
		border-radius: 50%;
		pointer-events: none;
	}

	.orb-a {
		width: 240rpx;
		height: 240rpx;
		right: -50rpx;
		top: -70rpx;
		background: rgba(255, 255, 255, 0.14);
	}

	.orb-b {
		width: 280rpx;
		height: 280rpx;
		left: -80rpx;
		bottom: -120rpx;
		background: rgba(190, 222, 255, 0.16);
	}

	.hero-watermark {
		position: absolute;
		right: 24rpx;
		top: 50%;
		transform: translateY(-50%);
		font-size: 140rpx;
		font-weight: 900;
		line-height: 1;
		color: rgba(255, 255, 255, 0.06);
		-webkit-text-stroke: 2rpx rgba(255, 255, 255, 0.12);
		pointer-events: none;
		z-index: 0;
	}

	.hero-inner {
		position: relative;
		z-index: 1;
		padding: 32rpx 30rpx 28rpx;
		background: linear-gradient(155deg, #2E63D6 0%, #3569DD 30%, #4576E3 68%, #5D8CED 100%);
		color: #fff;
	}

	.hero-badge {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 8rpx 18rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.14);
		border: 1rpx solid rgba(255, 255, 255, 0.18);
	}

	.hero-badge text {
		font-size: 22rpx;
		color: #DDEBFF;
		font-weight: 600;
	}

	.hero-label {
		display: block;
		margin-top: 24rpx;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.78);
	}

	.hero-amount {
		display: block;
		margin-top: 12rpx;
		font-size: 72rpx;
		font-weight: 800;
		line-height: 1.05;
		letter-spacing: -2rpx;
		text-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.12);
	}

	.hero-metrics {
		margin-top: 28rpx;
		padding: 20rpx 24rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.12);
		border: 1rpx solid rgba(255, 255, 255, 0.16);
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.metric-box {
		flex: 1;
		display: flex;
		align-items: center;
		gap: 14rpx;
	}

	.metric-icon {
		width: 52rpx;
		height: 52rpx;
		border-radius: 16rpx;
		background: rgba(255, 255, 255, 0.14);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.metric-copy {
		flex: 1;
		min-width: 0;
	}

	.metric-divider {
		width: 2rpx;
		height: 56rpx;
		background: rgba(255, 255, 255, 0.2);
		flex-shrink: 0;
	}

	.metric-label {
		display: block;
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.72);
	}

	.metric-value {
		display: block;
		margin-top: 4rpx;
		font-size: 30rpx;
		font-weight: 700;
	}

	.section-head {
		margin-top: 32rpx;
		margin-bottom: 16rpx;
		display: flex;
		align-items: center;
		gap: 12rpx;
		padding: 0 4rpx;
	}

	.section-accent {
		width: 8rpx;
		height: 28rpx;
		border-radius: 999rpx;
		background: linear-gradient(180deg, #5F95F8 0%, #386DF5 100%);
		box-shadow: 0 4rpx 12rpx rgba(56, 109, 245, 0.28);
		flex-shrink: 0;
	}

	.section-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #22324A;
	}

	.menu-card {
		border-radius: 28rpx;
		overflow: hidden;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		border: 1rpx solid rgba(125, 166, 226, 0.14);
		box-shadow: 0 16rpx 36rpx rgba(79, 121, 198, 0.09);
	}

	.menu-row {
		padding: 24rpx 28rpx;
		display: flex;
		align-items: center;
		gap: 20rpx;
		transition: background 0.2s ease;
	}

	.menu-row:active {
		background: #F8FBFF;
	}

	.menu-row:nth-child(odd) {
		background: rgba(248, 251, 255, 0.5);
	}

	.menu-row.highlight {
		background: linear-gradient(90deg, rgba(234, 248, 239, 0.72) 0%, rgba(255, 255, 255, 0) 100%);
	}

	.menu-row + .menu-row {
		border-top: 2rpx solid #F1F5FA;
	}

	.menu-icon {
		width: 72rpx;
		height: 72rpx;
		border-radius: 22rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		border: 1rpx solid rgba(255, 255, 255, 0.72);
		box-shadow: 0 8rpx 18rpx rgba(56, 109, 245, 0.08);
	}

	.menu-icon.blue {
		background: linear-gradient(180deg, #EBF2FF 0%, #DEEAFF 100%);
	}

	.menu-icon.green {
		background: linear-gradient(180deg, #EAF8EF 0%, #DFF5E7 100%);
		box-shadow: 0 8rpx 18rpx rgba(22, 163, 74, 0.08);
	}

	.menu-icon.teal {
		background: linear-gradient(180deg, #E6FAFA 0%, #D7F5F5 100%);
		box-shadow: 0 8rpx 18rpx rgba(8, 145, 178, 0.08);
	}

	.menu-icon.orange {
		background: linear-gradient(180deg, #FFF4EB 0%, #FFE8D6 100%);
		box-shadow: 0 8rpx 18rpx rgba(234, 88, 12, 0.08);
	}

	.menu-icon.purple {
		background: linear-gradient(180deg, #F0EBFF 0%, #E8E0FF 100%);
		box-shadow: 0 8rpx 18rpx rgba(124, 92, 255, 0.08);
	}

	.menu-body {
		flex: 1;
		min-width: 0;
	}

	.menu-label {
		display: block;
		font-size: 28rpx;
		font-weight: 600;
		color: #22324A;
	}

	.menu-desc {
		display: block;
		margin-top: 6rpx;
		font-size: 24rpx;
		color: #9AA7BA;
	}

	.poster-btn {
		position: relative;
		margin-top: 36rpx;
		height: 100rpx;
		border-radius: 28rpx;
		background: linear-gradient(135deg, #386DF5 0%, #5B8DF7 55%, #6EA0FF 100%);
		box-shadow: 0 16rpx 32rpx rgba(56, 109, 245, 0.32);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		overflow: hidden;
	}

	.poster-btn-glow {
		position: absolute;
		left: 20%;
		top: -40rpx;
		width: 200rpx;
		height: 80rpx;
		background: rgba(255, 255, 255, 0.18);
		border-radius: 50%;
		filter: blur(8rpx);
		pointer-events: none;
	}

	.poster-btn:active {
		opacity: 0.92;
		transform: scale(0.99);
	}

	.poster-btn text {
		position: relative;
		z-index: 1;
		font-size: 30rpx;
		font-weight: 700;
		color: #fff;
	}
</style>
