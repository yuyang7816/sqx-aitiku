<template>
	<view class="app-page home-page">
		<app-network-status />
		<view class="page-scroll">
			<view class="home-screen">
				<!-- 沉浸式顶栏 -->
				<view class="hero-top">
					<view class="hero-bg"></view>
					<view class="hero-inner">
						<view class="hero-row">
							<view class="hero-user">
								<text class="hero-greet">Hi，{{ profile.name || '同学' }}</text>
								<text class="hero-meta">{{ profileMetaLine || '今天也要加油刷题哦' }}</text>
							</view>
							<view class="hero-bell" @tap="go('/package/common/notice')">
								<up-icon name="bell-fill" size="22" color="#FFFFFF"></up-icon>
								<view v-if="noticeCount > 0" class="hero-badge">{{ noticeBadgeText }}</view>
							</view>
						</view>
						<view class="search-bar" @tap="openSearch">
							<view class="search-icon-wrap">
								<up-icon name="search" size="18" color="#5B4CFF"></up-icon>
							</view>
							<text class="search-hint">{{ searchPlaceholder }}</text>
							<view class="search-go">
								<up-icon name="arrow-right" size="14" color="#4E88F6"></up-icon>
							</view>
						</view>
					</view>
				</view>

				<view class="home-content">
					<!-- 会员卡片 -->
					<view class="vip-banner" :class="{ 'is-vip': member?.isVip }" @tap="go(MEMBER_CENTER_PAGE)">
						<text class="vip-bg-word">VIP</text>
						<view class="vip-left">
							<view class="vip-chip">
								<text>{{ vipChipText }}</text>
							</view>
							<view class="vip-title-row">
								<text class="vip-headline">{{ vipReminderTitle }}</text>
								<text v-if="member?.isVip" class="vip-state-tag">已开通</text>
							</view>
							<view class="vip-benefits">
								<view class="vip-benefit" v-for="item in vipBenefitList" :key="item">
									<up-icon name="checkmark-circle-fill" size="12" color="#4E88F6"></up-icon>
									<text>{{ item }}</text>
								</view>
							</view>
						</view>
						<view class="vip-btn">
							<text>{{ vipButtonText }}</text>
							<up-icon name="arrow-right" size="14" color="#fff"></up-icon>
						</view>
					</view>

					<!-- 快捷入口 -->
					<view class="quick-panel">
						<view class="quick-row">
							<view
								v-for="item in quickActions"
								:key="`${item.title}-${item.sort}`"
								class="quick-item"
								@tap="onQuickActionTap(item)"
							>
								<view class="quick-icon" :style="{ background: item.bg }">
									<image
										v-if="item.iconUrl"
										class="quick-icon-img"
										:src="item.iconUrl"
										mode="aspectFit"
									></image>
									<up-icon v-else :name="item.icon" size="22" :color="item.color"></up-icon>
									<view v-if="item.action === 'customer-service' && kefuBadgeText" class="quick-badge">{{ kefuBadgeText }}</view>
								</view>
								<text class="quick-label">{{ item.title }}</text>
							</view>
						</view>
					</view>

					<!-- 今日学习状态 -->
					<view class="section-block">
						<view class="sec-head section-top">
							<text class="sec-title">今日学习状态</text>
							<text class="sec-link" @tap.stop="openStudyCard">去练习 ›</text>
						</view>
						<view class="study-panel" @tap="openStudyCard">
							<view class="study-stats-row">
								<view
									v-for="(item, index) in studyCardStats"
									:key="item.label"
									class="study-stat"
									:class="`stat-tone-${index}`"
								>
									<text class="stat-val">{{ item.value }}</text>
									<text class="stat-lbl">{{ item.label }}</text>
								</view>
							</view>
						</view>
					</view>

					<!-- 为你推荐 -->
					<view class="section-block section-block-last">
						<view class="sec-head section-top">
							<text class="sec-title">为你推荐</text>
						</view>
						<view class="recommend-list">
							<view
								v-for="(item, index) in recommendList"
								:key="item.title"
								class="recommend-card"
								:class="`rec-tone-${index}`"
								@tap="openRecommend(item)"
							>
								<view class="rec-icon" :style="{ background: item.iconBg }">
									<up-icon :name="item.icon" size="26" :color="item.iconColor"></up-icon>
								</view>
								<view class="rec-body">
									<view class="rec-top">
										<text class="rec-title">{{ item.title }}</text>
										<text v-if="item.tag" class="rec-tag">{{ item.tag }}</text>
									</view>
									<text class="rec-meta">{{ item.meta }}</text>
								</view>
								<view class="rec-go">
									<up-icon name="arrow-right" size="16" color="#4E88F6"></up-icon>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
		<app-tabbar current="home" :badge-count="noticeCount"></app-tabbar>
	</view>
</template>

<script setup>
	import AppNetworkStatus from '@/components/app-network-status/app-network-status.vue';
	import AppTabbar from '@/components/app-tabbar/app-tabbar.vue';
	import { computed, ref } from 'vue';
	import { storeToRefs } from 'pinia';
	import { onShow, onUnload } from '@dcloudio/uni-app';
	import { fetchTodayAnalysis } from '@/common/analysis-today';
	import { fetchOverviewAnalysis } from '@/common/analysis-overview';
	import {
		formatProfileMetaLine,
		getHomeRecommendList,
		getHomeStudyStats,
		getNotebookMasteryRateText,
		getNotebookPendingCount,
		refreshMemberExpireState,
		setPracticeSource,
		syncNotebookListFromApi
	} from '@/common/app-data';
	import {
		fetchNoticeUnreadCount,
		formatNoticeBadge,
		getNoticeUnreadCountSync,
		NOTICE_READ_STATE_EVENT
	} from '@/common/messages';
	import {
		CHAPTER_PRACTICE_PAGE,
		HOME_SEARCH_PLACEHOLDER,
		MEMBER_CENTER_PAGE,
		QUESTION_ANSWER_PAGE,
		QUESTION_SEARCH_PAGE,
		STUDY_CARD_WRONG_TITLE
	} from '@/common/routes';
	import { fetchHomeShortcuts } from '@/common/home-shortcut-api';
	import { fetchAndMergeUserInfo } from '@/common/user-info';
	import { openCustomerService } from '@/common/customer-service';
	import { fetchKefuUnreadCount, formatKefuBadge } from '@/common/kefu-chat-api';
	import { openOcrImagePicker } from '@/common/ocr-camera';
	import { useTabPageState } from '@/common/use-tab-page-state';
	import { useUserStore } from '@/stores/user';
	import { useMemberStore } from '@/stores/member';
	import { useNotebookStore } from '@/stores/notebook';

	useTabPageState('home');

	const userStore = useUserStore();
	const memberStore = useMemberStore();
	const notebookStore = useNotebookStore();
	const { profile } = storeToRefs(userStore);
	const { member } = storeToRefs(memberStore);
	const { list: notebookList } = storeToRefs(notebookStore);

	const quickActions = ref([]);
	const searchPlaceholder = HOME_SEARCH_PLACEHOLDER;
	const studyStats = ref(getHomeStudyStats());
	const recommendList = ref(getHomeRecommendList());
	const noticeCount = ref(getNoticeUnreadCountSync());
	const kefuUnreadCount = ref(0);
	const todayAnalysis = ref({ fromApi: false });
	const overviewAnalysis = ref({ fromApi: false });

	const syncNoticeCountFromStorage = () => {
		noticeCount.value = getNoticeUnreadCountSync();
	};

	const refreshKefuUnreadCount = async () => {
		kefuUnreadCount.value = await fetchKefuUnreadCount();
	};

	const refreshNoticeCount = async () => {
		noticeCount.value = await fetchNoticeUnreadCount();
	};

	const onNoticeReadStateChanged = () => {
		syncNoticeCountFromStorage();
	};

	uni.$on(NOTICE_READ_STATE_EVENT, onNoticeReadStateChanged);
	onUnload(() => {
		uni.$off(NOTICE_READ_STATE_EVENT, onNoticeReadStateChanged);
	});

	const refreshHomeData = () => {
		studyStats.value = getHomeStudyStats(todayAnalysis.value, overviewAnalysis.value);
		recommendList.value = getHomeRecommendList();
	};

	const refreshTodayAnalysis = async () => {
		todayAnalysis.value = await fetchTodayAnalysis();
	};

	const refreshOverviewAnalysis = async () => {
		overviewAnalysis.value = await fetchOverviewAnalysis();
	};

	const refreshHomeShortcuts = async () => {
		const { list } = await fetchHomeShortcuts();
		quickActions.value = list;
	};

	onShow(async () => {
		refreshMemberExpireState();
		refreshHomeData();
		syncNoticeCountFromStorage();
		refreshNoticeCount();
		refreshKefuUnreadCount();
		refreshHomeShortcuts();
		await fetchAndMergeUserInfo();
		try {
			await syncNotebookListFromApi();
		} catch (e) {
			// 网络异常时首页错题统计保持为空，待下次 onShow 重试
		}
		await Promise.all([refreshTodayAnalysis(), refreshOverviewAnalysis()]);
		refreshHomeData();
	});

	const noticeBadgeText = computed(() => formatNoticeBadge(noticeCount.value));
	const kefuBadgeText = computed(() => formatKefuBadge(kefuUnreadCount.value));
	const profileMetaLine = computed(() => formatProfileMetaLine(profile.value));
	const masteryRate = computed(() => getNotebookMasteryRateText(notebookList.value));
	const vipChipText = computed(() => (member.value?.isVip ? '权益生效中' : '开通提醒'));
	const vipReminderTitle = computed(() => {
		if (member.value?.isVip) {
			return member.value?.level || 'VIP会员';
		}
		return '开通 VIP 会员';
	});
	const vipBenefitList = computed(() => {
		if (member.value?.isVip) {
			const expireAt = String(member.value?.expireAt || '').trim();
			return ['有效期内可用', expireAt ? `有效期至 ${expireAt}` : '会员权益已生效'];
		}
		return ['升级后可享无限次 AI 搜题'];
	});
	const vipButtonText = computed(() => (member.value?.isVip ? '立即续费' : '立即开通'));

	const studyCardStats = computed(() => {
		const pendingCount = getNotebookPendingCount(notebookList.value);
		const durationStat = studyStats.value[0] || { value: '—', label: '建议时长' };
		const questionStat = studyStats.value[1] || { value: `${pendingCount} 道`, label: '待复习错题' };
		const masteryStat = studyStats.value[2] || { value: masteryRate.value, label: '当前掌握率' };
		return [durationStat, questionStat, masteryStat];
	});

	const openStudyCard = () => {
		setPracticeSource({ mode: 'wrong', title: STUDY_CARD_WRONG_TITLE });
		go(QUESTION_ANSWER_PAGE);
	};

	const openRecommend = (item) => {
		if (item?.action === 'chapter-practice') {
			go(CHAPTER_PRACTICE_PAGE);
			return;
		}
		setPracticeSource({ mode: 'wrong', title: item?.title || STUDY_CARD_WRONG_TITLE });
		go(QUESTION_ANSWER_PAGE);
	};

	const openSearch = () => go(QUESTION_SEARCH_PAGE);

	const go = (url) => {
		if (url.startsWith('/pages/')) {
			uni.reLaunch({ url });
			return;
		}
		uni.navigateTo({ url });
	};

	const onQuickActionTap = (item) => {
		if (item.action === 'customer-service') {
			openCustomerService();
			return;
		}
		if (item.action === 'ocr-camera') {
			openOcrImagePicker();
			return;
		}
		go(item.url);
	};
</script>

<style lang="scss" scoped>
	$purple: #5B4CFF;
	$purple-dark: #3D2FE0;
	$purple-light: #EEEAFF;
	$orange: #FF6B35;
	$teal: #00C896;
	$gold: #FFB800;
	$bg: #F6F8FC;
	$surface: #FFFFFF;
	$surface-soft: #FBFBFE;
	$ink: #1C1B33;
	$ink-muted: #6B6A8A;
	$ink-light: #9897B5;
	$page-x: 24rpx;
	$r: 24rpx;

	.home-page {
		background: linear-gradient(180deg, #F3F8FF 0, #F7FAFF 260rpx, $bg 620rpx, $bg 100%);
	}

	.home-screen {
		min-height: 100vh;
		padding: 0 0 calc(132rpx + env(safe-area-inset-bottom));
	}

	.home-content {
		position: relative;
		z-index: 2;
		margin-top: -74rpx;
	}

	// ── 顶栏 ──
	.hero-top {
		position: relative;
		padding: calc(env(safe-area-inset-top) + 22rpx) $page-x 96rpx;
		overflow: hidden;
	}

	.hero-bg {
		position: absolute;
		inset: 0;
		background: linear-gradient(160deg, #679AF7 0%, #79ABFA 34%, #9BC5FD 72%, #C3DEFF 100%);
		border-radius: 0 0 48rpx 48rpx;
		box-shadow: inset 0 -34rpx 64rpx rgba(255, 255, 255, 0.18);

		&::after {
			content: '';
			position: absolute;
			right: -24rpx;
			top: 28rpx;
			width: 240rpx;
			height: 240rpx;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.22);
		}

		&::before {
			content: '';
			position: absolute;
			left: -52rpx;
			bottom: -112rpx;
			width: 260rpx;
			height: 260rpx;
			border-radius: 50%;
			background: rgba(225, 241, 255, 0.52);
		}
	}

	.hero-inner {
		position: relative;
		z-index: 1;
	}

	.hero-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 24rpx;
	}

	.hero-user {
		flex: 1;
		min-width: 0;
	}

	.hero-greet {
		display: block;
		font-size: 42rpx;
		font-weight: 800;
		color: #fff;
		line-height: 1.2;
	}

	.hero-meta {
		display: block;
		margin-top: 10rpx;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.88);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.hero-bell {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.18);
		backdrop-filter: blur(10rpx);
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		flex-shrink: 0;
		margin-left: 20rpx;
	}

	.hero-badge {
		position: absolute;
		top: 0;
		right: 0;
		min-width: 28rpx;
		height: 28rpx;
		padding: 0 6rpx;
		border-radius: 999rpx;
		background: $orange;
		color: #fff;
		font-size: 16rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.search-bar {
		display: flex;
		align-items: center;
		gap: 14rpx;
		height: 74rpx;
		padding: 0 10rpx 0 20rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.92);
		box-shadow: 0 14rpx 28rpx rgba(56, 104, 184, 0.1);
	}

	.search-icon-wrap {
		flex-shrink: 0;
	}

	.search-hint {
		flex: 1;
		font-size: 25rpx;
		color: $ink-light;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.search-go {
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		background: rgba(78, 136, 246, 0.12);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	// ── 会员 ──
	.vip-banner {
		position: relative;
		margin: 0 $page-x;
		padding: 16rpx 20rpx;
		border-radius: $r;
		background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
		overflow: hidden;
		box-shadow: 0 12rpx 28rpx rgba(45, 86, 156, 0.08);
		border: 1rpx solid rgba(102, 144, 214, 0.12);
		display: flex;
		align-items: center;
		gap: 16rpx;

		&.is-vip {
			background: linear-gradient(135deg, #ffffff 0%, #f4f9ff 100%);
		}

		&::after {
			content: '';
			position: absolute;
			right: -36rpx;
			bottom: -48rpx;
			width: 220rpx;
			height: 220rpx;
			border-radius: 50%;
			background: rgba(190, 222, 255, 0.26);
		}

		&::before {
			content: '';
			position: absolute;
			left: -40rpx;
			top: -40rpx;
			width: 140rpx;
			height: 140rpx;
			border-radius: 50%;
			background: rgba(103, 154, 247, 0.08);
		}
	}

	.vip-deco {
		display: none;
	}

	.vip-left {
		position: relative;
		z-index: 1;
		flex: 1;
		min-width: 0;
	}

	.vip-bg-word {
		position: absolute;
		right: 172rpx;
		top: 50%;
		transform: translateY(-50%);
		font-size: 144rpx;
		font-weight: 900;
		font-style: italic;
		line-height: 1;
		letter-spacing: 4rpx;
		color: rgba(78, 136, 246, 0.05);
		-webkit-text-stroke: 2rpx rgba(78, 136, 246, 0.2);
		text-shadow: 0 8rpx 20rpx rgba(78, 136, 246, 0.06);
		pointer-events: none;
		z-index: 0;
	}

	.vip-chip {
		display: inline-flex;
		align-items: center;
		padding: 6rpx 12rpx;
		border-radius: 999rpx;
		background: rgba(78, 136, 246, 0.08);
		font-size: 17rpx;
		font-weight: 600;
		color: #4E88F6;
	}

	.vip-headline {
		display: block;
		font-size: 22rpx;
		font-weight: 600;
		color: $ink;
		line-height: 1.3;
		flex: 1;
		position: relative;
		z-index: 1;
	}

	.vip-title-row {
		margin-top: 8rpx;
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	.vip-state-tag {
		flex-shrink: 0;
		padding: 3rpx 10rpx;
		border-radius: 999rpx;
		background: rgba(78, 136, 246, 0.1);
		font-size: 16rpx;
		font-weight: 600;
		color: #4E88F6;
	}

	.vip-benefits {
		margin-top: 10rpx;
		display: flex;
		flex-wrap: wrap;
		gap: 8rpx 10rpx;
	}

	.vip-benefit {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 5rpx 10rpx;
		border-radius: 999rpx;
		background: #f1f6ff;
		font-size: 17rpx;
		color: #355886;
		border: 1rpx solid rgba(102, 144, 214, 0.1);
	}

	.vip-btn {
		height: 46rpx;
		padding: 0 16rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #4e88f6 0%, #6aa0fb 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 18rpx;
		font-weight: 600;
		color: #fff;
		flex-shrink: 0;
		box-shadow: 0 10rpx 20rpx rgba(78, 136, 246, 0.2);
	}

	.section-block {
		margin-top: 26rpx;
	}

	.section-block-last {
		padding-bottom: 8rpx;
	}

	.section-top {
		margin-bottom: 14rpx;
	}

	.compact-head {
		padding-bottom: 0;
	}

	.quick-panel {
		margin: 14rpx $page-x 0;
		padding: 18rpx 16rpx 10rpx;
		border-radius: $r;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 10rpx 24rpx rgba(45, 86, 156, 0.06);
	}

	// ── 快捷入口 ──
	.quick-row {
		display: grid;
		grid-template-columns: repeat(4, minmax(0, 1fr));
		gap: 18rpx 8rpx;
	}

	.quick-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 10rpx;
		min-width: 0;
	}

	.quick-icon {
		position: relative;
		width: 98rpx;
		height: 98rpx;
		border-radius: 28rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 12rpx 22rpx rgba(69, 110, 175, 0.1), inset 0 1rpx 0 rgba(255, 255, 255, 0.48);
		transform: translateY(0);
	}

	.quick-icon-img {
		width: 52rpx;
		height: 52rpx;
	}

	.quick-badge {
		position: absolute;
		top: -6rpx;
		right: -6rpx;
		min-width: 30rpx;
		height: 30rpx;
		padding: 0 6rpx;
		border-radius: 999rpx;
		background: $orange;
		color: #fff;
		font-size: 16rpx;
		font-weight: 700;
		line-height: 30rpx;
		text-align: center;
		border: 2rpx solid $bg;
	}

	.quick-label {
		font-size: 23rpx;
		font-weight: 400;
		color: $ink;
		text-align: center;
		line-height: 1.3;
	}

	// ── 区块标题 ──
	.sec-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 $page-x;
	}

	.sec-title {
		font-size: 28rpx;
		font-weight: 500;
		color: $ink;
	}

	.sec-link {
		font-size: 24rpx;
		font-weight: 500;
		color: #4E88F6;
	}

	// ── 学习状态 ──
	.study-panel {
		margin: 0 $page-x;
		padding: 18rpx;
		border-radius: $r;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 10rpx 24rpx rgba(45, 86, 156, 0.06);
	}

	.study-stats-row {
		display: flex;
		gap: 14rpx;
	}

	.study-stat {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		padding: 22rpx 12rpx;
		border-radius: 18rpx;
		background: #F7FAFE;
		min-width: 0;
	}

	.stat-tone-0 {
		border-top: 4rpx solid #4E88F6;
		background: linear-gradient(180deg, #F5F9FF 0%, #FFFFFF 100%);
	}

	.stat-tone-1 {
		border-top: 4rpx solid #7AAEF5;
		background: linear-gradient(180deg, #F6FAFF 0%, #FFFFFF 100%);
	}

	.stat-tone-2 {
		border-top: 4rpx solid #9BC2FF;
		background: linear-gradient(180deg, #F8FBFF 0%, #FFFFFF 100%);
	}

	.stat-val {
		font-size: 32rpx;
		font-weight: 800;
		color: $ink;
		line-height: 1.15;
		text-align: center;
	}

	.stat-lbl {
		font-size: 21rpx;
		color: $ink-muted;
		text-align: center;
		line-height: 1.35;
	}

	// ── 推荐 ──
	.recommend-list {
		display: flex;
		flex-direction: column;
		gap: 14rpx;
		padding: 0 $page-x 8rpx;
	}

	.recommend-card {
		display: flex;
		align-items: center;
		gap: 18rpx;
		padding: 18rpx;
		border-radius: $r;
		background: $surface;
		box-shadow: 0 10rpx 24rpx rgba(45, 86, 156, 0.06);
		border: 1rpx solid rgba(102, 144, 214, 0.08);
	}

	.rec-tone-0 {
		background: linear-gradient(135deg, #FFFFFF 0%, #FAFCFF 100%);
	}

	.rec-tone-1 {
		background: linear-gradient(135deg, #FFFFFF 0%, #F9FBFF 100%);
	}

	.rec-tone-2 {
		background: linear-gradient(135deg, #FFFFFF 0%, #FBFCFE 100%);
	}

	.rec-icon {
		width: 88rpx;
		height: 88rpx;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.45);
	}

	.rec-body {
		flex: 1;
		min-width: 0;
	}

	.rec-top {
		display: flex;
		align-items: center;
		gap: 8rpx;
		flex-wrap: wrap;
	}

	.rec-title {
		font-size: 28rpx;
		font-weight: 700;
		color: $ink;
		line-height: 1.3;
	}

	.rec-tag {
		padding: 4rpx 12rpx;
		border-radius: 999rpx;
		background: $purple-light;
		color: $purple;
		font-size: 19rpx;
		font-weight: 600;
	}

	.rec-meta {
		display: block;
		margin-top: 8rpx;
		font-size: 23rpx;
		color: $ink-muted;
		line-height: 1.45;
	}

	.rec-go {
		width: 52rpx;
		height: 52rpx;
		border-radius: 50%;
		background: #EFF5FF;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		border: 1rpx solid rgba(102, 144, 214, 0.12);
	}

	.rec-tone-0 .rec-go {
		background: #EAF2FF;
	}

	.rec-tone-2 .rec-go {
		background: #EAF2FF;
	}

	/* #ifdef MP-WEIXIN */
	.hero-bell {
		background: rgba(255, 255, 255, 0.28);
	}

	.quick-row {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
	}

	.quick-item {
		width: 25%;
		padding: 0 4rpx;
		margin-bottom: 18rpx;
	}
	/* #endif */
</style>
