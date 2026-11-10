<template>
	<view class="app-page practice-page">
		<view class="page-scroll">
			<view class="screen practice-screen">
				<view class="chip-scroll">
					<view class="chip-row">
						<view v-for="item in chips" :key="item" class="chip" :class="{ active: item === activeChip }"
							@tap="selectChip(item)">{{ item }}</view>
					</view>
				</view>

				<view class="section-header practice-section-head">
					<view>
						<text class="section-title">练习入口</text>
						<text class="section-sub">从常用模式开始，题量多的页面优先保证快进快出。</text>
					</view>
				</view>

				<view class="practice-grid">
					<view v-for="item in filteredPracticeCards" :key="`${item.title}-${item.sort}`" class="practice-card"
						:style="{ background: item.bg }" @tap="onPracticeCardTap(item)">
						<view class="practice-card-top">
							<view class="practice-icon">
								<image v-if="item.iconUrl" class="practice-icon-img" :src="item.iconUrl" mode="aspectFit"></image>
								<up-icon v-else :name="item.icon" size="22" :color="item.iconColor"></up-icon>
							</view>
							<view class="practice-arrow">
								<up-icon name="arrow-right" size="14" color="#8ea0b8"></up-icon>
							</view>
						</view>
						<view class="practice-copy">
							<text class="practice-title">{{ item.title }}</text>
							<text class="practice-sub">{{ item.sub }}</text>
						</view>
					</view>
				</view>

				<view class="section-header">
					<view>
						<text class="section-title">热门章节练习</text>
						<text class="section-sub">按章节循序刷题，优先补齐最近易错内容。</text>
					</view>
					<view class="section-badge section-badge--muted">
						<text>{{ filteredChapters.length }} 章</text>
					</view>
				</view>

				<view v-if="chapterLoading" class="chapter-loading">
					<view class="state-mark">
						<up-loading-icon mode="circle" size="20"></up-loading-icon>
					</view>
					<text class="chapter-loading-text">加载章节中...</text>
				</view>
				<view v-else-if="!filteredChapters.length" class="chapter-empty">
					<view class="state-mark state-mark--empty">
						<up-icon name="empty-list" size="34" color="#AEB8C8"></up-icon>
					</view>
					<text>暂无章节，请先在管理端维护或切换筛选条件</text>
				</view>
				<view v-else class="chapter-list">
					<view v-for="item in filteredChapters" :key="item.id || item.title" class="chapter-card card"
						@tap="openChapterPractice(item)">
						<view class="chapter-left">
							<view class="chapter-icon">
								<up-icon name="file-text" size="18" color="#4B7AFE"></up-icon>
							</view>
							<view class="chapter-info">
								<view class="chapter-title">{{ item.title }}</view>
								<view class="chapter-meta">{{ item.meta }}</view>
							</view>
						</view>
						<view class="chapter-arrow">
							<up-icon name="arrow-right" size="14" color="#C0C6D4"></up-icon>
						</view>
					</view>
				</view>

			</view>
		</view>
		<app-tabbar current="practice"></app-tabbar>
	</view>
</template>

<script setup>
	import AppTabbar from '@/components/app-tabbar/app-tabbar.vue';
	import {
		computed,
		ref
	} from 'vue';
	import {
		onShow
	} from '@dcloudio/uni-app';
	import {
		PRACTICE_CHIP_ALL_GRADES,
		getPracticeChips,
		getPracticeFilteredCards,
		getPracticeFilteredChapters,
		getUserProfile
	} from '@/common/app-data';
	import {
		fetchChapterTree,
		startChapterPracticeAnswer
	} from '@/common/chapter-tree';
	import {
		fetchPracticePaperIds
	} from '@/common/paper-list';
	import {
		fetchPracticeEntries,
		mergePracticeEntryPaperIds
	} from '@/common/practice-entry-api';
	import {
		CHAPTER_PRACTICE_PAGE,
		KNOWLEDGE_PRACTICE_PAGE,
		buildKnowledgePracticeUrl
	} from '@/common/practice-config';
	import {
		appendPracticeChipQuery,
		setPracticeActiveChip
	} from '@/common/practice-chip-context';
	import {
		useTabPageState
	} from '@/common/use-tab-page-state';

	const activeChip = ref(PRACTICE_CHIP_ALL_GRADES);
	const {
		restoreScrollAfterReady
	} = useTabPageState('practice', {
		activeChip,
		deferScrollRestore: true
	});
	const profile = ref(getUserProfile());
	/** 练习模式卡片（配置走 API，专项/真题 paperId 动态注入） */
	const practiceCardsList = ref([]);
	/** 章节列表由 fetchChapterTree 拉取 API，避免 mock 闪屏 */
	const chapters = ref([]);
	const chapterLoading = ref(false);

	const refreshPracticeCards = async () => {
		const [{
			list
		}, {
			special,
			exam
		}] = await Promise.all([
			fetchPracticeEntries(),
			fetchPracticePaperIds()
		]);
		practiceCardsList.value = mergePracticeEntryPaperIds(list, {
			special,
			exam
		});
	};

	const chips = computed(() => getPracticeChips(profile.value));

	const refreshChapters = async () => {
		chapterLoading.value = true;
		try {
			const result = await fetchChapterTree(profile.value, PRACTICE_CHIP_ALL_GRADES);
			chapters.value = result.list;
		} finally {
			chapterLoading.value = false;
			restoreScrollAfterReady();
		}
	};

	onShow(() => {
		profile.value = getUserProfile();
		if (!chips.value.includes(activeChip.value)) {
			activeChip.value = PRACTICE_CHIP_ALL_GRADES;
		}
		setPracticeActiveChip(activeChip.value);
		refreshPracticeCards();
		refreshChapters();
	});

	const filteredPracticeCards = computed(() => getPracticeFilteredCards(activeChip.value, practiceCardsList.value,
		profile.value));
	const filteredChapters = computed(() => getPracticeFilteredChapters(activeChip.value, chapters.value, profile.value));

	const selectChip = (chip) => {
		activeChip.value = chip;
		setPracticeActiveChip(chip);
	};

	const openChapterPractice = (chapter) => {
		startChapterPracticeAnswer({
			chapterId: chapter?.id,
			title: chapter?.title,
			subject: chapter?.subject,
			activeChip: activeChip.value,
			profile: profile.value
		});
	};

	const openKnowledgePractice = () => {
		uni.navigateTo({
			url: buildKnowledgePracticeUrl(activeChip.value)
		});
	};

	const onPracticeCardTap = (item) => {
		const url = String(item?.url || '').trim();
		if (!url) {
			return;
		}
		if (url === CHAPTER_PRACTICE_PAGE) {
			uni.navigateTo({
				url: appendPracticeChipQuery(CHAPTER_PRACTICE_PAGE, activeChip.value)
			});
			return;
		}
		if (url === KNOWLEDGE_PRACTICE_PAGE) {
			openKnowledgePractice();
			return;
		}
		go(appendPracticeChipQuery(url, activeChip.value));
	};

	const go = (url) => {
		const target = String(url || '').trim();
		if (!target) {
			return;
		}
		if (target.startsWith('/pages/')) {
			uni.reLaunch({
				url: target
			});
			return;
		}
		uni.navigateTo({
			url: target
		});
	};
</script>

<style lang="scss" scoped>
	.practice-page {
		background: linear-gradient(180deg, #F3F8FF 0, #F7FAFF 260rpx, #F7FAFE 620rpx, #F7FAFE 100%);
	}

	.practice-screen {
		padding: 18rpx 24rpx 36rpx;
		background: transparent;
		box-sizing: border-box;
	}

	.chip-scroll {
		margin-top: 8rpx;
		overflow-x: auto;
		overflow-y: hidden;
		-webkit-overflow-scrolling: touch;
		scrollbar-width: none;
	}

	.chip-scroll::-webkit-scrollbar {
		display: none;
	}

	.chip-row {
		display: flex;
		flex-wrap: nowrap;
		gap: 14rpx;
		width: max-content;
		min-width: 100%;
	}

	.chip {
		padding: 14rpx 26rpx;
		border-radius: 999rpx;
		background: #f6f7fb;
		font-size: 24rpx;
		font-weight: 600;
		color: #5f6979;
		transition: all 0.2s ease;
		white-space: nowrap;
		flex-shrink: 0;
	}

	.chip.active {
		background: #386df5;
		color: #fff;
		box-shadow: 0 8rpx 18rpx rgba(56, 109, 245, 0.16);
	}

	.practice-section-head {
		margin-top: 22rpx;
	}

	.practice-grid {
		margin-top: 24rpx;
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 16rpx;
	}

	.practice-card {
		padding: 22rpx 20rpx;
		border-radius: 22rpx;
		min-height: 208rpx;
		box-shadow: 0 10rpx 22rpx rgba(32, 50, 86, 0.04);
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		transition: transform 0.15s ease, box-shadow 0.15s ease;
	}

	.practice-card:active {
		transform: scale(0.98);
		box-shadow: 0 12rpx 24rpx rgba(32, 50, 86, 0.06);
	}

	.practice-card-top {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 12rpx;
	}

	.practice-icon {
		width: 76rpx;
		height: 76rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.85);
		box-shadow: 0 8rpx 18rpx rgba(255, 255, 255, 0.3);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.practice-icon-img {
		width: 44rpx;
		height: 44rpx;
	}

	.practice-arrow {
		width: 36rpx;
		height: 36rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.practice-copy {
		margin-top: 18rpx;
	}

	.practice-title {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		color: #253143;
		line-height: 1.35;
		text-align: left;
	}

	.practice-sub {
		display: block;
		margin-top: 10rpx;
		font-size: 20rpx;
		color: #97a0af;
		line-height: 1.55;
		text-align: left;
	}

	.section-header {
		margin-top: 40rpx;
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.section-badge {
		display: inline-flex;
		align-items: center;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(237, 244, 255, 0.84);
		font-size: 17rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #386df5;
		flex-shrink: 0;
	}

	.section-badge--muted {
		background: rgba(243, 247, 252, 0.96);
		color: #73829a;
	}

	.chapter-loading,
	.chapter-empty {
		margin-top: 24rpx;
		padding: 50rpx 24rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.95);
		box-shadow: 0 8rpx 22rpx rgba(32, 49, 84, 0.025);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		font-size: 24rpx;
		color: #97a0af;
	}

	.state-mark {
		width: 72rpx;
		height: 72rpx;
		border-radius: 22rpx;
		background: rgba(237, 244, 255, 0.84);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.state-mark--empty {
		background: rgba(244, 247, 252, 0.96);
	}

	.chapter-list {
		margin-top: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.chapter-card {
		padding: 24rpx 22rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		min-height: 108rpx;
		transition: transform 0.15s ease;
		box-shadow: 0 8rpx 22rpx rgba(32, 49, 84, 0.025);
	}

	.chapter-card:active {
		transform: scale(0.99);
	}

	.chapter-left {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.chapter-icon {
		width: 64rpx;
		height: 64rpx;
		border-radius: 24rpx;
		background: #eef4ff;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.chapter-info {
		display: flex;
		flex-direction: column;
	}

	.chapter-title {
		font-size: 26rpx;
		font-weight: 700;
		color: #243041;
		line-height: 1.35;
	}

	.chapter-meta {
		margin-top: 8rpx;
		font-size: 18rpx;
		color: #97a0af;
		line-height: 1.45;
	}

	.chapter-arrow {
		width: 40rpx;
		height: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	@media screen and (max-width: 420px) {
		.practice-screen {
			padding: 16rpx 20rpx 28rpx;
		}

		.section-header {
			flex-direction: column;
		}

		.section-badge {
			align-self: flex-start;
		}
	}

	/* #ifdef MP-WEIXIN */
	.chip-row {
		display: inline-flex;
		width: auto;
		min-width: 100%;
	}

	.practice-grid {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
	}

	.practice-card {
		width: 48.5%;
		margin: 0 0 16rpx;
	}
	/* #endif */
</style>
