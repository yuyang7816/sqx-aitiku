<template>
	<view class="app-page my-page">
		<view class="page-scroll">
			<view class="profile-hero">
				<view class="profile-top">
					<view class="profile-main" @tap="goProfileEdit">
						<profile-avatar :profile="profile" size="md"></profile-avatar>
						<view class="profile-copy">
							<view class="profile-name-row">
								<text class="profile-name">{{ profile.name }}</text>
								<text class="profile-meta">{{ profileMetaLine }}</text>
							</view>
							<view
								v-if="unlockedBadges.length"
								class="profile-badges"
								@tap.stop="goBadgeWall"
							>
								<view
									v-for="item in badgePreviewList"
									:key="item.badgeCode"
									class="badge-chip"
								>
									<view class="badge-chip-icon" :style="{ background: getBadgeBg(item) }">
										<up-icon
											:name="getBadgeIcon(item)"
											size="12"
											:color="getBadgeColor(item)"
										></up-icon>
									</view>
									<text class="badge-chip-name">{{ item.name }}</text>
								</view>
								<text v-if="badgeOverflowCount > 0" class="badge-more">+{{ badgeOverflowCount }}</text>
							</view>
						</view>
						<view class="profile-arrow">
							<up-icon name="arrow-right" size="14" color="#9AA7BC"></up-icon>
						</view>
					</view>

					<view class="profile-accent-row">
						<view class="profile-accent-chip">
							<up-icon name="checkmark-circle-fill" size="12" color="#4E88F6"></up-icon>
							<text>学习档案</text>
						</view>
					</view>

					<view class="stats-card" @tap.stop>
						<view
							v-for="item in stats"
							:key="item.key || item.label"
							class="stat-item"
							:class="{ clickable: !!item.link }"
							@tap="onStatTap(item)"
						>
							<text class="stat-value">{{ statsLoading ? '—' : item.value }}</text>
							<text class="stat-label">{{ item.label }}</text>
						</view>
					</view>
				</view>
				<profile-member-card :member="member" :benefits="vipBenefitPreview"></profile-member-card>
			</view>

			<view class="screen profile-screen">
				<text v-if="statsHint" class="stats-hint">{{ statsHint }}</text>

				<view class="profile-section-head">
					<text class="section-title">常用功能</text>
				</view>

				<view class="menu-card">
					<view v-for="item in menus" :key="item.title" class="menu-item" @tap="go(item.url)">
						<view class="menu-left">
							<view class="menu-icon" :style="{ background: item.bg }">
								<up-icon :name="item.icon" size="18" :color="item.color"></up-icon>
							</view>
							<text class="menu-text">{{ item.title }}</text>
						</view>
						<view class="menu-arrow">
							<up-icon name="arrow-right" size="14" color="#C0C6D4"></up-icon>
						</view>
					</view>
				</view>

			</view>
		</view>
		<app-tabbar current="my"></app-tabbar>
	</view>
</template>

<script setup>
	import AppTabbar from '@/components/app-tabbar/app-tabbar.vue';
	import ProfileAvatar from '@/components/profile-avatar/profile-avatar.vue';
	import ProfileMemberCard from '@/components/profile-member-card/profile-member-card.vue';
	import { computed, ref } from 'vue';
	import { storeToRefs } from 'pinia';
	import { onShow } from '@dcloudio/uni-app';
	import { formatMyProfileMetaLine, refreshMemberExpireState } from '@/common/app-data';
	import { BADGE_WALL_PAGE, PROFILE_EDIT_PAGE } from '@/common/routes';
	import { myMenus } from '@/common/profile-config';
	import { fetchAndMergeUserInfo } from '@/common/user-info';
	import { buildEmptyUserStats, fetchUserStats } from '@/common/user-stats';
	import {
		BADGE_PREVIEW_LIMIT,
		fetchUserBadges,
		filterUnlockedBadges,
		getBadgeVisual
	} from '@/common/user-badge-api';
	import { buildVipBenefitPreview, fetchMemberRightsCompare } from '@/common/vip-rights-api';
	import { useTabPageState } from '@/common/use-tab-page-state';
	import { useUserStore } from '@/stores/user';
	import { useMemberStore } from '@/stores/member';

	useTabPageState('my');

	const userStore = useUserStore();
	const memberStore = useMemberStore();
	const { profile } = storeToRefs(userStore);
	const { member } = storeToRefs(memberStore);

	/** 个人中心统计三栏，优先 GET /app/user/stats */
	const stats = ref(buildEmptyUserStats());
	const statsLoading = ref(false);
	const statsHint = ref('');
	const unlockedBadges = ref([]);
	const badgeLoading = ref(false);
	const vipBenefitPreview = ref([]);
	const menus = computed(() => [...myMenus, {
		title: '设置中心',
		icon: 'setting',
		color: '#4E88F6',
		bg: '#EEF4FF',
		url: '/package/profile/settings'
	}]);
	/** Pinia 已同步 Storage；此处仅触发会员到期校验 */
	const refreshProfileHeader = () => {
		refreshMemberExpireState();
	};

	/** 刷新统计三栏（交卷后返回本页 onShow 会重新拉取，做题数同步增加） */
	const refreshUserStats = async () => {
		statsLoading.value = true;
		try {
			const result = await fetchUserStats();
			stats.value = result.items;
			statsHint.value = !result.fromApi && result.message ? result.message : '';
		} finally {
			statsLoading.value = false;
		}
	};

	/** 刷新已解锁勋章（展示在用户名下方，激励用户） */
	const refreshUnlockedBadges = async () => {
		badgeLoading.value = true;
		try {
			const result = await fetchUserBadges();
			unlockedBadges.value = filterUnlockedBadges(result.list);
		} finally {
			badgeLoading.value = false;
		}
	};

	onShow(async () => {
		refreshProfileHeader();
		await Promise.all([
			fetchAndMergeUserInfo().then(() => refreshProfileHeader()),
			refreshUserStats(),
			refreshUnlockedBadges(),
			fetchMemberRightsCompare().then(({ list }) => {
				vipBenefitPreview.value = buildVipBenefitPreview(list, 2);
			})
		]);
	});

	const profileMetaLine = computed(() => formatMyProfileMetaLine(profile.value));
	const badgePreviewList = computed(() => unlockedBadges.value.slice(0, BADGE_PREVIEW_LIMIT));
	const badgeOverflowCount = computed(() => Math.max(0, unlockedBadges.value.length - BADGE_PREVIEW_LIMIT));
	const getBadgeIcon = (item) => getBadgeVisual(item.badgeCode).icon;
	const getBadgeColor = (item) => getBadgeVisual(item.badgeCode).color;
	const getBadgeBg = (item) => getBadgeVisual(item.badgeCode).bg;

	const goBadgeWall = () => {
		uni.navigateTo({ url: BADGE_WALL_PAGE });
	};

	/** 点击顶部用户头图区进入头像昵称编辑 */
	const goProfileEdit = () => {
		uni.navigateTo({ url: PROFILE_EDIT_PAGE });
	};

	/** 点击统计栏：获得勋章进入勋章墙 */
	const onStatTap = (item) => {
		const url = item && item.link
			? item.link
			: (item && item.key === 'badgeCount' ? BADGE_WALL_PAGE : '');
		if (!url) {
			return;
		}
		go(url);
	};

	const go = (url) => {
		if (url.startsWith('/pages/')) {
			uni.reLaunch({
				url
			});
			return;
		}
		uni.navigateTo({
			url
		});
	};
</script>

<style lang="scss" scoped>
	.my-page {
		position: relative;
		background:
			radial-gradient(circle at top right, rgba(96, 159, 255, 0.28), transparent 26%),
			radial-gradient(circle at left 18%, rgba(191, 221, 255, 0.28), transparent 24%),
			radial-gradient(circle at 50% 62%, rgba(222, 236, 255, 0.42), transparent 34%),
			linear-gradient(180deg, #EAF3FF 0, #F1F7FF 220rpx, #F4F8FE 620rpx, #E8EEF8 100%);
	}

	.my-page::before {
		content: '';
		position: absolute;
		left: -120rpx;
		top: 520rpx;
		width: 360rpx;
		height: 360rpx;
		border-radius: 50%;
		background: rgba(196, 220, 255, 0.22);
		filter: blur(10rpx);
		pointer-events: none;
	}

	.my-page::after {
		content: '';
		position: absolute;
		right: -90rpx;
		bottom: 220rpx;
		width: 280rpx;
		height: 280rpx;
		border-radius: 50%;
		background: rgba(180, 208, 248, 0.2);
		filter: blur(8rpx);
		pointer-events: none;
	}

	/* #ifdef MP-WEIXIN */
	.my-page::before,
	.my-page::after {
		filter: none;
	}
	/* #endif */

	.profile-hero {
		padding: calc(env(safe-area-inset-top) + 20rpx) 24rpx 0;
	}

	.profile-top {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
		padding: 24rpx 22rpx 18rpx;
		border-radius: 32rpx;
		background:
			radial-gradient(circle at top right, rgba(101, 164, 255, 0.26), transparent 32%),
			radial-gradient(circle at left center, rgba(182, 216, 255, 0.4), transparent 28%),
			linear-gradient(180deg, rgba(236, 245, 255, 0.98) 0%, rgba(226, 239, 255, 0.96) 100%);
		border: 1rpx solid rgba(118, 164, 233, 0.24);
		box-shadow: 0 18rpx 38rpx rgba(79, 121, 198, 0.12);
		position: relative;
		overflow: hidden;
	}

	.profile-top::after {
		content: '';
		position: absolute;
		right: -42rpx;
		top: -42rpx;
		width: 180rpx;
		height: 180rpx;
		border-radius: 50%;
		background: rgba(78, 136, 246, 0.05);
		pointer-events: none;
	}

	.profile-main {
		display: flex;
		align-items: center;
		position: relative;
		z-index: 1;
	}

	.profile-accent-row {
		display: flex;
		align-items: center;
		position: relative;
		z-index: 1;
		padding-left: 2rpx;
	}

	.profile-accent-chip {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 5rpx 10rpx;
		border-radius: 999rpx;
		background: rgba(78, 136, 246, 0.08);
		color: #4E88F6;
		font-size: 16rpx;
		font-weight: 600;
	}

	.profile-copy {
		flex: 1;
		margin-left: 18rpx;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	.profile-name-row {
		display: flex;
		align-items: baseline;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	.profile-name {
		font-size: 36rpx;
		font-weight: 700;
		color: #22324A;
	}

	.profile-meta {
		font-size: 24rpx;
		color: #7E8CA0;
	}

	.profile-badges {
		margin-top: 10rpx;
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8rpx;
	}

	.badge-chip {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 4rpx 10rpx 4rpx 4rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.72);
		border: 1rpx solid rgba(118, 164, 233, 0.18);
		max-width: 100%;
	}

	.badge-chip-icon {
		width: 28rpx;
		height: 28rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.badge-chip-name {
		font-size: 18rpx;
		font-weight: 600;
		color: #4A5870;
		max-width: 140rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.badge-more {
		font-size: 18rpx;
		font-weight: 600;
		color: #4E88F6;
		padding: 4rpx 8rpx;
	}

	.profile-arrow {
		width: 44rpx;
		height: 44rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.profile-screen {
		position: relative;
		z-index: 1;
		padding-top: 6rpx;
		padding-bottom: 36rpx;
	}

	.stats-card {
		// padding: 18rpx 0 16rpx;
		display: flex;
		border-radius: 30rpx;
	}

	.stat-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;
		position: relative;
		text-align: center;
	}

	.stat-item.clickable:active {
		opacity: 0.72;
	}

	.stat-item:not(:last-child)::after {
		content: '';
		position: absolute;
		top: 6rpx;
		right: 0;
		width: 2rpx;
		height: 58rpx;
		background: #EAF0F7;
	}

	.stat-value {
		font-size: 40rpx;
		font-weight: 700;
		color: #22324A;
	}

	.stat-label {
		font-size: 22rpx;
		color: #9AA8BA;
	}

	.stats-hint {
		display: block;
		margin-top: 12rpx;
		padding: 0 8rpx;
		font-size: 22rpx;
		color: #9BA7B9;
		text-align: center;
	}

	.profile-section-head {
		margin-top: 22rpx;
		margin-bottom: 8rpx;
		padding: 0 4rpx;
	}

	.profile-section-head .section-title {
		display: inline-flex;
		align-items: center;
		gap: 12rpx;
		font-size: 30rpx;
		font-weight: 700;
	}

	.profile-section-head .section-title::before {
		content: '';
		width: 8rpx;
		height: 22rpx;
		border-radius: 999rpx;
		background: linear-gradient(180deg, #5F95F8 0%, #7BA9FF 100%);
		box-shadow: 0 4rpx 12rpx rgba(95, 149, 248, 0.26);
		flex-shrink: 0;
	}

	.menu-card {
		margin-top: 0;
		padding: 4rpx 0;
		border-radius: 30rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		border: 1rpx solid rgba(125, 166, 226, 0.14);
		box-shadow: 0 16rpx 36rpx rgba(79, 121, 198, 0.09);
		overflow: hidden;
	}

	.menu-item {
		padding: 24rpx 28rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		transition: background 0.2s ease;
	}

	.menu-item:active {
		background: #F8FBFF;
	}

	.menu-item:nth-child(odd) {
		background: rgba(248, 251, 255, 0.34);
	}

	.menu-item:not(:last-child) {
		border-bottom: 2rpx solid #F1F5FA;
	}

	.menu-left {
		display: flex;
		align-items: center;
		gap: 18rpx;
	}

	.menu-icon {
		width: 64rpx;
		height: 64rpx;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: inset 0 0 0 2rpx rgba(255, 255, 255, 0.4);
	}

	.menu-text {
		font-size: 26rpx;
		font-weight: 400;
		color: #22324A;
	}

	.menu-arrow {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	@media screen and (max-width: 420px) {
		.profile-hero {
			padding-left: 32rpx;
			padding-right: 32rpx;
		}

		.profile-top {
			padding: 20rpx 18rpx 16rpx;
			gap: 14rpx;
		}

		.profile-main {
			align-items: flex-start;
		}

		.profile-name {
			font-size: 34rpx;
		}

		.menu-item {
			padding-left: 24rpx;
			padding-right: 24rpx;
			padding-top: 22rpx;
			padding-bottom: 22rpx;
		}
	}
</style>
