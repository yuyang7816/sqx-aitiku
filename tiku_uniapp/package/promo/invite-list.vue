<template>
	<view class="app-page invite-list-page">
		<view class="screen invite-screen">
			<view v-if="summaryHint" class="hint-bar"><text>{{ summaryHint }}</text></view>

			<view class="hero-card">
				<view class="hero-glow hero-glow-a"></view>
				<view class="hero-glow hero-glow-b"></view>
				<view class="hero-top">
					<view>
						<text class="hero-eyebrow">推广数据看板</text>
						<text class="hero-title">{{ sceneTitle }}</text>
						<text class="hero-subtitle">{{ sceneSubtitle }}</text>
					</view>
					<view class="hero-tag">
						<up-icon :name="showCommission ? 'rmb-circle' : 'account-fill'" size="14" color="#FFFFFF"></up-icon>
						<text>{{ showCommission ? '分佣视图' : '邀请视图' }}</text>
					</view>
				</view>

				<view class="scene-tabs">
					<view
						class="scene-tab"
						:class="{ on: showCommission }"
						@tap="switchScene('commission')"
					>
						<view class="scene-tab-icon blue">
							<up-icon name="rmb-circle" size="18" :color="showCommission ? '#FFFFFF' : '#386DF5'"></up-icon>
						</view>
						<view class="scene-tab-copy">
							<text class="scene-tab-label">总收益</text>
							<text class="scene-tab-value">¥{{ summary.totalCommission }}</text>
						</view>
					</view>
					<view
						class="scene-tab"
						:class="{ on: !showCommission }"
						@tap="switchScene('invite')"
					>
						<view class="scene-tab-icon purple">
							<up-icon name="account-fill" size="18" :color="!showCommission ? '#FFFFFF' : '#7C5CFF'"></up-icon>
						</view>
						<view class="scene-tab-copy">
							<text class="scene-tab-label">邀请人数</text>
							<text class="scene-tab-value">{{ summary.inviteCount }}<text class="scene-tab-unit">人</text></text>
						</view>
					</view>
				</view>

			</view>

			<view class="section-card">
				<view class="level-pills">
					<text
						class="level-pill"
						:class="{ on: activeLevel === 1 }"
						@tap="switchLevel(1)"
					>一级</text>
					<text
						class="level-pill"
						:class="{ on: activeLevel === 2 }"
						@tap="switchLevel(2)"
					>二级</text>
				</view>

				<view class="list-section">
					<view v-if="loadingMembers" class="list-state">
						<up-loading-icon mode="circle" color="#386df5" size="18"></up-loading-icon>
						<text>加载中...</text>
					</view>

					<view v-else-if="members.length" class="member-list">
						<view
							v-for="(item, index) in members"
							:key="`${scene}-${activeLevel}-${item.name}-${item.time}-${index}`"
							class="member-row"
						>
							<profile-avatar
								:profile="{ name: item.name, avatar: item.avatar }"
								size="sm"
								shape="round"
							></profile-avatar>
							<view class="member-main">
								<view class="member-topline">
									<text class="member-name">{{ item.name }}</text>
									<view v-if="showCommission" class="amount-badge">
										<text>+¥{{ item.commission }}</text>
									</view>
									<text v-else class="member-time-inline">{{ item.time }}</text>
								</view>
								<text v-if="showCommission" class="member-meta">{{ item.time }}</text>
							</view>
						</view>
					</view>

					<view v-else class="list-state list-state--empty">
						<view class="empty-icon">
							<up-icon :name="showCommission ? 'list' : 'account-fill'" size="28" color="#386df5"></up-icon>
						</view>
						<text class="empty-title">{{ emptyText }}</text>
						<text class="empty-sub">{{ emptySubText }}</text>
					</view>
				</view>
			</view>

			<text v-if="membersHint" class="foot-hint">{{ membersHint }}</text>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import ProfileAvatar from '@/components/profile-avatar/profile-avatar.vue';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import {
		buildEmptyPromoSummary,
		fetchPromoMembers,
		fetchPromoSummary
	} from '@/common/promo-api';

	const scene = ref('invite');
	const activeLevel = ref(1);
	const loadingMembers = ref(false);
	const members = ref([]);
	const membersHint = ref('');
	const summaryHint = ref('');
	const summary = ref(buildEmptyPromoSummary());

	const showCommission = computed(() => scene.value === 'commission');
	const sceneTitle = computed(() => (showCommission.value ? '分佣明细' : '邀请人数'));
	const activeLevelLabel = computed(() => (activeLevel.value === 2 ? '二级' : '一级'));
	const sceneSubtitle = computed(() => (
		showCommission.value
			? '按层级查看下级带来的收益记录'
			: '按层级查看你已邀请的成员动态'
	));
	const emptyText = computed(() => {
		const levelText = activeLevelLabel.value;
		return showCommission.value ? `暂无${levelText}分佣记录` : `暂无${levelText}邀请`;
	});
	const emptySubText = computed(() => (
		showCommission.value
			? '下级开通会员后将产生分佣记录'
			: '分享海报邀请好友注册后将显示在这里'
	));

	usePageNavigationTitle(sceneTitle);

	const loadSummary = async () => {
		const summaryResult = await fetchPromoSummary();
		summary.value = summaryResult.summary;
		summaryHint.value = !summaryResult.fromApi ? '概览数据加载失败' : '';
	};

	const loadMembers = async () => {
		loadingMembers.value = true;
		membersHint.value = '';
		try {
			const result = await fetchPromoMembers({
				level: activeLevel.value,
				withCommission: showCommission.value
			});
			members.value = result.list || [];
			membersHint.value = !result.fromApi && result.message ? result.message : '';
		} finally {
			loadingMembers.value = false;
		}
	};

	const switchScene = async (nextScene) => {
		const normalized = String(nextScene || '').trim() === 'commission' ? 'commission' : 'invite';
		if (scene.value === normalized) {
			return;
		}
		scene.value = normalized;
		await loadMembers();
	};

	const switchLevel = async (level) => {
		if (activeLevel.value === level) {
			return;
		}
		activeLevel.value = level;
		await loadMembers();
	};

	onLoad(async (options) => {
		scene.value = String(options?.scene || '').trim() === 'commission' ? 'commission' : 'invite';
		await loadSummary();
		await loadMembers();
	});

	onShow(async () => {
		await loadSummary();
	});
</script>

<style lang="scss" scoped>
	.invite-list-page {
		min-height: 100vh;
		background: linear-gradient(180deg, #eef4ff 0%, #f4f7fd 260rpx, #f7f9fc 100%);
	}

	.invite-screen {
		padding-top: 12rpx;
		padding-bottom: 48rpx;
	}

	.hint-bar {
		margin-bottom: 16rpx;
		padding: 18rpx 24rpx;
		border-radius: 20rpx;
		background: linear-gradient(180deg, #fffbeb 0%, #fff7e8 100%);
		border: 1rpx solid rgba(245, 158, 11, 0.18);
		box-shadow: 0 8rpx 20rpx rgba(245, 158, 11, 0.08);
	}

	.hint-bar text {
		font-size: 24rpx;
		color: #b45309;
		line-height: 1.5;
	}

	.hero-card {
		position: relative;
		padding: 30rpx;
		border-radius: 36rpx;
		overflow: hidden;
		background: linear-gradient(135deg, #1f4fd6 0%, #4676f3 50%, #6d92ff 100%);
		box-shadow: 0 24rpx 48rpx rgba(44, 74, 148, 0.22);
	}

	.hero-glow {
		position: absolute;
		border-radius: 50%;
		pointer-events: none;
	}

	.hero-glow-a {
		top: -96rpx;
		right: -80rpx;
		width: 280rpx;
		height: 280rpx;
		background: rgba(255, 255, 255, 0.12);
	}

	.hero-glow-b {
		left: -100rpx;
		bottom: -140rpx;
		width: 320rpx;
		height: 320rpx;
		background: rgba(197, 221, 255, 0.16);
	}

	.hero-top,
	.scene-tabs {
		position: relative;
		z-index: 1;
	}

	.hero-top {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 20rpx;
	}

	.hero-eyebrow {
		display: block;
		font-size: 22rpx;
		color: rgba(231, 240, 255, 0.92);
	}

	.hero-title {
		display: block;
		margin-top: 10rpx;
		font-size: 44rpx;
		font-weight: 800;
		line-height: 1.15;
		color: #ffffff;
	}

	.hero-subtitle {
		display: block;
		margin-top: 12rpx;
		max-width: 480rpx;
		font-size: 24rpx;
		line-height: 1.55;
		color: rgba(226, 236, 255, 0.9);
	}

	.hero-tag {
		flex-shrink: 0;
		height: 52rpx;
		padding: 0 18rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.16);
		border: 1rpx solid rgba(255, 255, 255, 0.18);
		display: flex;
		align-items: center;
		gap: 8rpx;
	}

	.hero-tag text {
		font-size: 22rpx;
		color: #ffffff;
	}

	.scene-tabs {
		margin-top: 28rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 16rpx;
	}

	.scene-tab {
		padding: 20rpx;
		border-radius: 26rpx;
		background: rgba(255, 255, 255, 0.12);
		border: 1rpx solid rgba(255, 255, 255, 0.14);
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.scene-tab.on {
		background: rgba(255, 255, 255, 0.22);
		box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.24);
	}

	.scene-tab-icon {
		width: 72rpx;
		height: 72rpx;
		border-radius: 22rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		background: rgba(255, 255, 255, 0.18);
	}

	.scene-tab.on .scene-tab-icon {
		background: rgba(255, 255, 255, 0.18);
	}

	.scene-tab-icon.blue {
		box-shadow: inset 0 0 0 1rpx rgba(56, 109, 245, 0.08);
	}

	.scene-tab-icon.purple {
		box-shadow: inset 0 0 0 1rpx rgba(124, 92, 255, 0.08);
	}

	.scene-tab-copy {
		flex: 1;
		min-width: 0;
	}

	.scene-tab-label {
		display: block;
		font-size: 22rpx;
		color: rgba(232, 239, 255, 0.86);
	}

	.scene-tab-value {
		display: block;
		margin-top: 8rpx;
		font-size: 34rpx;
		font-weight: 800;
		line-height: 1.15;
		color: #ffffff;
	}

	.scene-tab-unit {
		margin-left: 4rpx;
		font-size: 24rpx;
		font-weight: 600;
	}

	.section-card {
		margin-top: 24rpx;
		padding: 0;
	}

	.level-pills {
		margin-top: 4rpx;
		display: flex;
		width: 100%;
		padding: 6rpx;
		border-radius: 999rpx;
		background: #f3f7ff;
		border: 1rpx solid rgba(121, 159, 229, 0.14);
		gap: 6rpx;
	}

	.level-pill {
		flex: 1;
		height: 60rpx;
		padding: 0 24rpx;
		border-radius: 999rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		color: #7e8ca1;
	}

	.level-pill.on {
		background: linear-gradient(135deg, #386df5 0%, #5b8df7 100%);
		color: #ffffff;
		font-weight: 600;
		box-shadow: 0 8rpx 16rpx rgba(56, 109, 245, 0.22);
	}

	.list-section {
		margin-top: 20rpx;
		border-radius: 28rpx;
		overflow: hidden;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 250, 255, 0.96) 100%);
		border: 1rpx solid rgba(121, 159, 229, 0.1);
	}

	.member-list {
		padding: 12rpx 0 12rpx;
	}

	.member-row {
		padding: 24rpx;
		display: flex;
		align-items: center;
		gap: 18rpx;
	}

	.member-row + .member-row {
		border-top: 1rpx solid #edf2f8;
	}

	.member-main {
		flex: 1;
		min-width: 0;
	}

	.member-topline {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.member-name {
		flex: 1;
		min-width: 0;
		font-size: 28rpx;
		font-weight: 700;
		color: #22324a;
	}

	.member-meta {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		line-height: 1.5;
		color: #9aa7ba;
	}

	.member-time-inline {
		flex-shrink: 0;
		font-size: 22rpx;
		line-height: 1.5;
		color: #9aa7ba;
		text-align: right;
	}

	.amount-badge {
		flex-shrink: 0;
	}

	.amount-badge text {
		font-size: 26rpx;
		font-weight: 700;
		color: #15803d;
	}

	.list-state {
		padding: 56rpx 32rpx 48rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 12rpx;
		text-align: center;
		color: #8b95a8;
		font-size: 24rpx;
	}

	.list-state--empty {
		padding-top: 56rpx;
		padding-bottom: 64rpx;
	}

	.empty-icon {
		width: 108rpx;
		height: 108rpx;
		border-radius: 32rpx;
		background: linear-gradient(180deg, #ebf2ff 0%, #deeaff 100%);
		border: 1rpx solid rgba(56, 109, 245, 0.12);
		box-shadow: 0 10rpx 24rpx rgba(56, 109, 245, 0.12);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 8rpx;
	}

	.empty-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #22324a;
	}

	.empty-sub {
		font-size: 24rpx;
		line-height: 1.55;
		color: #8b95a8;
		max-width: 520rpx;
	}

	.foot-hint {
		display: block;
		margin-top: 20rpx;
		text-align: center;
		font-size: 22rpx;
		color: #9aa7ba;
	}

	/* #ifdef MP-WEIXIN */
	.scene-tabs {
		display: flex;
		flex-wrap: wrap;
	}

	.scene-tabs .scene-tab {
		flex: 0 0 calc(50% - 8rpx);
	}
	/* #endif */
</style>
