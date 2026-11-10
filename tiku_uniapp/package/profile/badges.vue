<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen badge-screen">
				<view class="badge-hero">
					<view class="hero-copy">
						<view class="hero-kicker">
							<up-icon name="medal" size="14" color="#7c5cff"></up-icon>
							<text>成长勋章墙</text>
						</view>
						<text class="hero-title">每一次坚持，都会被认真记录</text>
						<text class="hero-subtitle">{{ encouragementText }}</text>
					</view>
					<view class="hero-progress-ring" :style="heroRingStyle">
						<view class="hero-progress-core">
							<text class="hero-progress-value">{{ loading ? '—' : completionRate }}</text>
							<text class="hero-progress-unit">%</text>
						</view>
					</view>
				</view>

				<view class="summary-strip">
					<view class="summary-card summary-card--primary">
						<text class="summary-caption">已点亮</text>
						<text class="summary-value">{{ loading ? '—' : unlockedCount }}</text>
						<text class="summary-note">已完成的关键里程碑</text>
					</view>
					<view class="summary-card summary-card--soft">
						<text class="summary-caption">总进度</text>
						<text class="summary-value muted">{{ loading ? '—' : `${unlockedCount}/${totalCount}` }}</text>
						<text class="summary-note">离下一阶段更近一步</text>
					</view>
				</view>

				<view v-if="nextBadge" class="next-badge-panel">
					<view class="panel-head">
						<view>
							<text class="panel-kicker">下一枚勋章</text>
							<text class="panel-title">{{ nextBadge.name }}</text>
						</view>
						<view class="panel-icon" :style="{ background: getBadgeBg(nextBadge) }">
							<up-icon :name="getBadgeIcon(nextBadge)" size="20" :color="getBadgeColor(nextBadge)"></up-icon>
						</view>
					</view>
					<text class="panel-copy">{{ nextBadge.conditionText }}</text>
					<view class="panel-progress-row">
						<view class="panel-progress-track">
							<view class="panel-progress-fill" :style="{ width: `${nextBadge.progressPercent}%` }"></view>
						</view>
						<text class="panel-progress-text">{{ formatBadgeProgressText(nextBadge) }}</text>
					</view>
				</view>

				<text v-if="loadHint" class="load-hint">{{ loadHint }}</text>

				<view v-if="loading" class="loading-box card">
					<up-loading-icon mode="circle" color="#386df5"></up-loading-icon>
					<text>勋章加载中…</text>
				</view>

				<template v-else>
					<view v-if="unlockedBadges.length" class="section-block">
						<view class="section-head">
							<view>
								<text class="section-title">已获得的勋章</text>
								<text class="section-subtitle">这些里程碑已经被你亲手点亮</text>
							</view>
							<view class="section-badge success">{{ unlockedBadges.length }}</view>
						</view>
						<view class="badge-grid badge-grid--unlocked">
							<view
								v-for="item in unlockedBadges"
								:key="item.badgeCode"
								class="badge-card badge-card--unlocked"
							>
								<view class="badge-glow"></view>
								<view class="badge-icon-wrap badge-icon-wrap--unlocked" :style="{ background: getBadgeBg(item) }">
									<up-icon :name="getBadgeIcon(item)" size="30" :color="getBadgeColor(item)"></up-icon>
								</view>
								<text class="badge-name">{{ item.name }}</text>
								<text class="badge-condition">{{ item.conditionText }}</text>
								<view class="badge-unlocked-tag">
									<up-icon name="checkmark-circle-fill" size="12" color="#16a34a"></up-icon>
									<text>已解锁</text>
								</view>
							</view>
						</view>
					</view>

					<view v-if="lockedBadges.length" class="section-block">
						<view class="section-head">
							<view>
								<text class="section-title">待解锁勋章</text>
								<text class="section-subtitle">继续保持，下一次练习就可能点亮新的成就</text>
							</view>
							<view class="section-badge pending">{{ lockedBadges.length }}</view>
						</view>
						<view class="badge-grid">
							<view
								v-for="item in lockedBadges"
								:key="item.badgeCode"
								class="badge-card badge-card--locked"
							>
								<view class="badge-icon-wrap badge-icon-wrap--locked">
									<up-icon :name="getBadgeIcon(item)" size="28" color="#b0b8c4"></up-icon>
								</view>
								<text class="badge-name">{{ item.name }}</text>
								<text class="badge-condition">{{ item.conditionText }}</text>
								<view class="badge-progress">
									<view class="progress-track">
										<view class="progress-fill" :style="{ width: `${item.progressPercent}%` }"></view>
									</view>
									<text class="progress-text">{{ formatBadgeProgressText(item) }}</text>
								</view>
							</view>
						</view>
					</view>
				</template>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import {
		fetchUserBadges,
		formatBadgeProgressText,
		getBadgeVisual
	} from '@/common/user-badge-api';

	const badges = ref([]);
	const unlockedCount = ref(0);
	const totalCount = ref(8);
	const loading = ref(false);
	const loadHint = ref('');

	const navbarTitle = computed(() => `我的勋章（${loading.value ? '…' : unlockedCount.value}/${totalCount.value}）`);
	usePageNavigationTitle(navbarTitle);

	const completionRate = computed(() => {
		if (!totalCount.value) {
			return 0;
		}
		return Math.round((unlockedCount.value / totalCount.value) * 100);
	});
	const heroRingStyle = computed(() => ({
		background: `conic-gradient(#7c5cff 0deg, #5f8dff ${completionRate.value * 3.6}deg, #e8ecf8 ${completionRate.value * 3.6}deg 360deg)`
	}));
	const unlockedBadges = computed(() => badges.value.filter((item) => item.unlocked));
	const lockedBadges = computed(() => badges.value.filter((item) => !item.unlocked));
	const nextBadge = computed(() => lockedBadges.value.slice().sort((a, b) => b.progressPercent - a.progressPercent)[0] || null);
	const encouragementText = computed(() => {
		if (loading.value) {
			return '正在整理你的成长里程碑';
		}
		if (unlockedCount.value === 0) {
			return '先拿下第一枚勋章，后面的成长会越来越有成就感';
		}
		if (unlockedCount.value === totalCount.value) {
			return '全部勋章已经点亮，这份坚持本身就是最好的奖励';
		}
		if (nextBadge.value) {
			return `再努力一点，就能解锁「${nextBadge.value.name}」`;
		}
		return '每一次练习和坚持，都会慢慢累积成看得见的成长';
	});

	const getBadgeIcon = (item) => getBadgeVisual(item.badgeCode).icon;
	const getBadgeColor = (item) => getBadgeVisual(item.badgeCode).color;
	const getBadgeBg = (item) => (item.unlocked ? getBadgeVisual(item.badgeCode).bg : '#f3f5f8');

	const refreshBadges = async () => {
		loading.value = true;
		loadHint.value = '';
		try {
			const result = await fetchUserBadges();
			badges.value = result.list;
			unlockedCount.value = result.unlockedCount;
			totalCount.value = result.totalCount;
			loadHint.value = !result.fromApi && result.message ? result.message : '';
		} finally {
			loading.value = false;
		}
	};

	onShow(() => {
		refreshBadges();
	});
</script>

<style lang="scss" scoped>
	.badge-screen {
		padding-top: 16rpx;
	}

	.badge-hero {
		padding: 40rpx 34rpx;
		border-radius: 36rpx;
		background:
			radial-gradient(circle at top right, rgba(130, 101, 255, 0.22) 0, rgba(130, 101, 255, 0) 34%),
			linear-gradient(135deg, #ffffff 0%, #f5f7ff 44%, #fff8ef 100%);
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 24rpx;
		box-shadow: 0 18rpx 40rpx rgba(79, 104, 173, 0.08);
	}

	.hero-copy {
		flex: 1;
		min-width: 0;
	}

	.hero-kicker {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 10rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(124, 92, 255, 0.1);
		font-size: 22rpx;
		font-weight: 700;
		color: #6d4cff;
	}

	.hero-title {
		display: block;
		margin-top: 18rpx;
		font-size: 42rpx;
		line-height: 1.35;
		font-weight: 800;
		color: #14213d;
	}

	.hero-subtitle {
		display: block;
		margin-top: 12rpx;
		font-size: 25rpx;
		line-height: 1.65;
		color: #627089;
	}

	.hero-progress-ring {
		width: 154rpx;
		height: 154rpx;
		border-radius: 50%;
		padding: 12rpx;
		flex-shrink: 0;
	}

	.hero-progress-core {
		width: 100%;
		height: 100%;
		border-radius: 50%;
		background: #ffffff;
		display: flex;
		align-items: baseline;
		justify-content: center;
		gap: 4rpx;
		box-shadow: inset 0 0 0 2rpx rgba(124, 92, 255, 0.06);
	}

	.hero-progress-value {
		font-size: 42rpx;
		font-weight: 800;
		color: #1f2a44;
	}

	.hero-progress-unit {
		font-size: 22rpx;
		font-weight: 700;
		color: #7c88a0;
	}

	.summary-strip {
		margin-top: 22rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 18rpx;
	}

	.summary-card {
		padding: 28rpx 30rpx;
		border-radius: 28rpx;
		background: #ffffff;
		box-shadow: 0 14rpx 34rpx rgba(79, 104, 173, 0.06);
	}

	.summary-card--primary {
		background: linear-gradient(135deg, #ffffff 0%, #f6f4ff 100%);
	}

	.summary-card--soft {
		background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
	}

	.summary-caption {
		display: block;
		font-size: 22rpx;
		font-weight: 700;
		color: #7e8aa3;
	}

	.summary-value {
		display: block;
		margin-top: 14rpx;
		font-size: 52rpx;
		font-weight: 800;
		color: var(--text-main);
	}

	.summary-value.muted {
		color: #5d6777;
	}

	.summary-note {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		line-height: 1.6;
		color: #8a96a8;
	}

	.next-badge-panel {
		margin-top: 22rpx;
		padding: 30rpx 32rpx;
		border-radius: 30rpx;
		background: linear-gradient(135deg, #ffffff 0%, #f8fbff 52%, #f3efff 100%);
		box-shadow: 0 16rpx 38rpx rgba(79, 104, 173, 0.07);
	}

	.panel-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 18rpx;
	}

	.panel-kicker {
		display: block;
		font-size: 22rpx;
		font-weight: 700;
		color: #7c5cff;
	}

	.panel-title {
		display: block;
		margin-top: 10rpx;
		font-size: 34rpx;
		font-weight: 800;
		color: #15233f;
	}

	.panel-icon {
		width: 84rpx;
		height: 84rpx;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.panel-copy {
		display: block;
		margin-top: 16rpx;
		font-size: 24rpx;
		line-height: 1.7;
		color: #66758d;
	}

	.panel-progress-row {
		margin-top: 18rpx;
	}

	.panel-progress-track {
		height: 12rpx;
		border-radius: 999rpx;
		background: #e8edf7;
		overflow: hidden;
	}

	.panel-progress-fill {
		height: 100%;
		border-radius: 999rpx;
		background: linear-gradient(90deg, #7c5cff 0%, #5f8dff 100%);
	}

	.panel-progress-text {
		display: block;
		margin-top: 12rpx;
		font-size: 22rpx;
		font-weight: 600;
		color: #7f8ca4;
	}

	.load-hint {
		display: block;
		margin-top: 16rpx;
		padding: 0 8rpx;
		font-size: var(--text-sm);
		color: #b0b8c4;
		text-align: center;
	}

	.loading-box {
		margin-top: 24rpx;
		padding: 80rpx 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		color: #8a96a8;
		font-size: var(--text-base);
	}

	.section-block {
		margin-top: 30rpx;
	}

	.section-head {
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		gap: 16rpx;
	}

	.section-title {
		display: block;
		font-size: 30rpx;
		font-weight: 800;
		color: #17233c;
	}

	.section-subtitle {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		line-height: 1.6;
		color: #7e8aa3;
	}

	.section-badge {
		padding: 10rpx 18rpx;
		border-radius: 999rpx;
		font-size: 22rpx;
		font-weight: 700;
	}

	.section-badge.success {
		background: #ebfbf2;
		color: #16a34a;
	}

	.section-badge.pending {
		background: #f0f4ff;
		color: #4d67d9;
	}

	.badge-grid {
		margin-top: 24rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 20rpx;
	}

	.badge-grid--unlocked {
		gap: 22rpx;
	}

	.badge-card {
		position: relative;
		padding: 30rpx 24rpx 28rpx;
		border-radius: 28rpx;
		background: #ffffff;
		box-shadow: 0 14rpx 34rpx rgba(79, 104, 173, 0.06);
		display: flex;
		flex-direction: column;
		align-items: center;
		text-align: center;
		min-height: 320rpx;
		overflow: hidden;
	}

	.badge-card--unlocked {
		min-height: 292rpx;
		background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
	}

	.badge-card--locked {
		border: 2rpx solid #edf1f7;
	}

	.badge-glow {
		position: absolute;
		top: -28rpx;
		right: -20rpx;
		width: 110rpx;
		height: 110rpx;
		border-radius: 50%;
		background: rgba(124, 92, 255, 0.08);
		filter: blur(8rpx);
	}

	.badge-icon-wrap {
		width: 104rpx;
		height: 104rpx;
		border-radius: 30rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: inset 0 0 0 2rpx rgba(255, 255, 255, 0.35);
	}

	.badge-icon-wrap--unlocked {
		box-shadow: 0 14rpx 28rpx rgba(124, 92, 255, 0.12);
	}

	.badge-icon-wrap--locked {
		background: #f4f6fa;
	}

	.badge-name {
		margin-top: 20rpx;
		font-size: 30rpx;
		font-weight: 700;
		color: var(--text-main);
	}

	.badge-condition {
		margin-top: 12rpx;
		font-size: 23rpx;
		line-height: 1.6;
		color: #8a96a8;
		min-height: 74rpx;
	}

	.badge-progress {
		width: 100%;
		margin-top: auto;
	}

	.progress-track {
		height: 10rpx;
		border-radius: var(--radius-full);
		background: #edf0f5;
		overflow: hidden;
	}

	.progress-fill {
		height: 100%;
		border-radius: var(--radius-full);
		background: linear-gradient(90deg, #386df5, #5b46e8);
	}

	.progress-text {
		display: block;
		margin-top: 12rpx;
		font-size: 22rpx;
		color: #b0b8c4;
	}

	.badge-unlocked-tag {
		margin-top: auto;
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 10rpx 18rpx;
		border-radius: var(--radius-full);
		background: #e8fbee;
		color: #16a34a;
		font-size: 22rpx;
		font-weight: 600;
	}

	/* #ifdef MP-WEIXIN */
	.summary-strip,
	.badge-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
		margin-left: -10rpx;
		margin-right: -10rpx;
	}

	.summary-card,
	.badge-card {
		width: calc(50% - 20rpx);
		margin: 0 10rpx 20rpx;
	}

	.summary-value,
	.badge-name {
		color: #243041;
	}

	.load-hint {
		font-size: 22rpx;
	}

	.loading-box {
		font-size: 24rpx;
	}

	.badge-glow {
		filter: none;
	}

	.progress-track,
	.progress-fill,
	.badge-unlocked-tag {
		border-radius: 999rpx;
	}

	.hero-progress-core {
		align-items: center;
	}

	.hero-progress-value,
	.hero-progress-unit {
		line-height: 1;
	}
	/* #endif */
</style>
