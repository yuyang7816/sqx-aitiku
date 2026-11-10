<template>
	<view class="app-page">
		<view class="page-scroll">
			<!-- 章节列表：无 id 时展示 GET /app/question/chapter/tree -->
			<view v-if="!chapterId" class="screen chapter-list-screen">
				<view class="chapter-hero">
					<view class="chapter-hero-copy">
						<text class="chapter-hero-title">章节练习</text>
						<text class="chapter-hero-sub">按章节循序刷题，优先补齐最近易错内容</text>
					</view>
					<view class="chapter-hero-badge">
						<text class="chapter-hero-badge-value">{{ chapters.length }}</text>
						<text class="chapter-hero-badge-label">章节</text>
					</view>
				</view>
				<view v-if="chapterLoading" class="chapter-loading">
					<up-loading-icon mode="circle" size="20"></up-loading-icon>
					<text class="chapter-loading-text">加载章节中...</text>
				</view>
				<view v-else-if="!chapters.length" class="chapter-empty">
					<up-icon name="empty-list" size="48" color="#C0C6D4"></up-icon>
					<text>暂无章节，请先在管理端维护</text>
				</view>
				<view v-else class="chapter-items">
					<view v-for="item in chapters" :key="item.id" class="chapter-card card" @tap="openChapterDetail(item)">
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

			<!-- 章节详情：带 id 时展示 GET /app/question/list 题目列表 -->
			<view v-else class="screen chapter-screen">
				<view class="chapter-summary card">
					<view class="chapter-summary-copy">
						<text class="chapter-summary-title">{{ activeChapter?.title || routeChapterTitle || '章节题目' }}</text>
						<text class="chapter-summary-sub">共 {{ detailQuestionCount }} 题，可单题预览，也可直接开始整章刷题</text>
					</view>
					<view class="chapter-summary-stat">
						<text class="chapter-summary-stat-value">{{ detailQuestionCount }}</text>
						<text class="chapter-summary-stat-label">题目</text>
					</view>
				</view>
				<view class="meta-row">
					<text class="meta-text">当前排序：{{ sortText }}</text>
					<view class="sort-chip" @tap="sort">
						<up-icon name="reload" size="14" color="#386df5"></up-icon>
						<text>{{ sortText }}</text>
					</view>
				</view>
				<view v-if="questionLoading" class="chapter-loading">
					<up-loading-icon mode="circle" size="20"></up-loading-icon>
					<text class="chapter-loading-text">加载题目中...</text>
				</view>
				<view v-else-if="!sortedQuestions.length" class="chapter-empty">
					<up-icon name="file-text" size="48" color="#C0C6D4"></up-icon>
					<text>暂无题目，请先在管理端维护</text>
				</view>
				<view v-else class="chapter-items">
					<view
						v-for="item in sortedQuestions"
						:key="item.questionId || item.questionCode || item.no"
						class="chapter-item"
						:class="item.type"
						@tap="previewSingleQuestion(item)"
					>
						<view class="item-side">
							<text class="no">NO.{{ item.no }}</text>
							<text class="status">{{ item.status }}</text>
						</view>
						<view class="item-content">
							<view class="question">{{ item.text }}</view>
							<view class="item-footer">
								<text class="item-tip">点击从该题开始刷题</text>
								<up-icon name="arrow-right" size="14" color="#AAB6C8"></up-icon>
							</view>
						</view>
					</view>
				</view>
				<view v-if="hasMoreQuestions && !questionLoading" class="load-more-wrap">
					<view class="load-more-btn" :class="{ disabled: questionLoadingMore }" @tap="loadMoreQuestions">
						<up-loading-icon v-if="questionLoadingMore" mode="circle" size="16"></up-loading-icon>
						<text>{{ questionLoadingMore ? '加载中...' : '加载更多' }}</text>
					</view>
				</view>
			</view>
		</view>
		<view v-if="chapterId" class="chapter-footer">
			<view class="solid-button start-btn" @tap="startChapterPractice">
				<text>开始刷题</text>
			</view>
		</view>
	</view>
</template>

<script setup>	import { computed, ref } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import { getUserProfile } from '@/common/app-data';
	import { fetchChapterTree, findChapterById, formatChapterPracticeNavbarTitle, resolveChapterIdForPracticeChip, startChapterPracticeAnswer } from '@/common/chapter-tree';
	import {
		getPracticeActiveChip,
		resolvePracticeChipFromQuery,
		resolvePracticeSubjectFromChip,
		setPracticeActiveChip
	} from '@/common/practice-chip-context';
	import {
		CHAPTER_SORT_DEFAULT,
		fetchQuestionList,
		getChapterSortLabel,
		sortChapterQuestions,
		toggleChapterSortMode
	} from '@/common/question-list';

	const profile = ref(getUserProfile());
	const activeChip = ref(getPracticeActiveChip());
	const chapterId = ref('');
	const routeChapterTitle = ref('');
	const chapters = ref([]);
	const chapterLoading = ref(false);
	const activeChapter = ref(null);
	const questions = ref([]);
	const questionTotal = ref(0);
	const questionLoading = ref(false);
	const questionLoadingMore = ref(false);
	const hasMoreQuestions = ref(false);
	const questionPage = ref(1);
	const sortMode = ref(CHAPTER_SORT_DEFAULT);
	let skipNextShowQuestionRefresh = false;
	let skipNextShowChapterRefresh = false;

	const navbarTitle = computed(() => {
		if (!chapterId.value) {
			return '章节练习';
		}
		const rawTitle = activeChapter.value?.title || routeChapterTitle.value;
		return formatChapterPracticeNavbarTitle(rawTitle);
	});
	usePageNavigationTitle(navbarTitle);

	const sortText = computed(() => getChapterSortLabel(sortMode.value));
	const sortedQuestions = computed(() => sortChapterQuestions(questions.value, sortMode.value));
	const detailQuestionCount = computed(() => questionTotal.value || activeChapter.value?.questionCount || questions.value.length);

	const refreshChapterList = async () => {
		chapterLoading.value = true;
		try {
			const result = await fetchChapterTree(profile.value);
			chapters.value = result.list;
		} finally {
			chapterLoading.value = false;
		}
	};

	const resolveActiveChapter = async (id) => {
		if (!id) {
			activeChapter.value = null;
			return;
		}
		const cached = findChapterById(chapters.value, id);
		if (cached) {
			activeChapter.value = cached;
			if (cached.title) {
				routeChapterTitle.value = cached.title;
			}
			return;
		}
		const result = await fetchChapterTree(profile.value);
		chapters.value = result.list;
		activeChapter.value = findChapterById(result.list, id);
		if (activeChapter.value?.title) {
			routeChapterTitle.value = activeChapter.value.title;
		}
	};

	const refreshQuestionList = async (reset = true) => {
		if (!chapterId.value) {
			questions.value = [];
			questionTotal.value = 0;
			hasMoreQuestions.value = false;
			return;
		}
		if (reset) {
			questionPage.value = 1;
			questionLoading.value = true;
		}
		try {
			const resolvedChapterId = await resolveChapterIdForPracticeChip(
				chapterId.value,
				activeChip.value,
				profile.value
			);
			const result = await fetchQuestionList(resolvedChapterId, {
				page: questionPage.value
			});
			if (reset) {
				questions.value = result.list;
			} else {
				questions.value = [...questions.value, ...result.list];
			}
			questionTotal.value = result.total || questions.value.length;
			hasMoreQuestions.value = result.hasMore;
		} finally {
			questionLoading.value = false;
			questionLoadingMore.value = false;
		}
	};

	const loadMoreQuestions = async () => {
		if (!hasMoreQuestions.value || questionLoadingMore.value || questionLoading.value) {
			return;
		}
		questionLoadingMore.value = true;
		questionPage.value += 1;
		await refreshQuestionList(false);
	};

	const startChapterAnswer = async ({ id = '', title = '', questionId = '', subject = '' } = {}) => {
		await startChapterPracticeAnswer({
			chapterId: id || chapterId.value,
			title: title || activeChapter.value?.title || routeChapterTitle.value || '章节练习',
			subject: subject || activeChapter.value?.subject || '',
			activeChip: activeChip.value,
			profile: profile.value,
			questionId
		});
	};

	const startChapterPractice = () => {
		if (!chapterId.value) {
			return;
		}
		startChapterAnswer({
			id: chapterId.value,
			title: activeChapter.value?.title || routeChapterTitle.value || '章节练习'
		});
	};

	const previewSingleQuestion = (item) => {
		const questionId = item?.questionId ?? item?.questionCode ?? '';
		if (!questionId) {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		startChapterAnswer({ questionId: String(questionId) });
	};

	const openChapterDetail = (item) => {
		startChapterAnswer({
			id: item?.id,
			title: item?.title,
			subject: item?.subject
		});
	};

	const sort = () => {
		sortMode.value = toggleChapterSortMode(sortMode.value);
		uni.showToast({
			title: sortMode.value === CHAPTER_SORT_DEFAULT ? '已切回顺序排列' : '已切换为错题优先',
			icon: 'none'
		});
	};

	const parseRouteTitle = (raw = '') => {
		const value = String(raw || '').trim();
		if (!value) {
			return '';
		}
		try {
			return decodeURIComponent(value);
		} catch (e) {
			return value;
		}
	};

	onLoad(async (query) => {
		skipNextShowQuestionRefresh = true;
		skipNextShowChapterRefresh = true;
		profile.value = getUserProfile();
		activeChip.value = resolvePracticeChipFromQuery(query);
		setPracticeActiveChip(activeChip.value);
		chapterId.value = String(query?.id || '').trim();
		routeChapterTitle.value = parseRouteTitle(query?.title);
		if (chapterId.value) {
			await resolveActiveChapter(chapterId.value);
			await refreshQuestionList(true);
			return;
		}
		await refreshChapterList();
	});

	onShow(async () => {
		profile.value = getUserProfile();
		if (!chapterId.value) {
			if (skipNextShowChapterRefresh) {
				skipNextShowChapterRefresh = false;
				return;
			}
			await refreshChapterList();
			return;
		}
		if (skipNextShowQuestionRefresh) {
			skipNextShowQuestionRefresh = false;
			return;
		}
		await refreshQuestionList(true);
	});
</script>

<style lang="scss" scoped>
	.app-page {
		background: linear-gradient(180deg, #F3F8FF 0, #F7FAFF 260rpx, #F7FAFE 620rpx, #F7FAFE 100%);
	}

	.chapter-list-screen,
	.chapter-screen {
		padding-top: 12rpx;
		padding-bottom: calc(144rpx + env(safe-area-inset-bottom));
	}

	.chapter-list-screen {
		padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	}

	.chapter-hero,
	.chapter-summary {
		display: flex;
		align-items: stretch;
		justify-content: space-between;
		gap: 20rpx;
		padding: 28rpx;
		border-radius: 30rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 250, 255, 0.96) 100%);
		border: 1rpx solid rgba(121, 159, 229, 0.1);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
	}

	.chapter-hero-copy,
	.chapter-summary-copy {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		justify-content: center;
	}

	.chapter-hero-title,
	.chapter-summary-title {
		display: block;
		font-size: 34rpx;
		font-weight: 600;
		color: #253143;
		line-height: 1.25;
	}

	.chapter-hero-sub,
	.chapter-summary-sub {
		display: block;
		margin-top: 10rpx;
		font-size: 24rpx;
		color: #8C98AA;
		line-height: 1.5;
	}

	.chapter-hero-badge,
	.chapter-summary-stat {
		min-width: 120rpx;
		padding: 18rpx 16rpx;
		border-radius: 24rpx;
		background: linear-gradient(180deg, #EEF5FF 0%, #F7FAFF 100%);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		align-self: center;
	}

	.chapter-hero-badge-value,
	.chapter-summary-stat-value {
		font-size: 40rpx;
		font-weight: 800;
		color: #386df5;
		line-height: 1;
	}

	.chapter-hero-badge-label,
	.chapter-summary-stat-label {
		margin-top: 10rpx;
		font-size: 22rpx;
		color: #93A1B5;
	}

	.section-sub {
		display: none;
	}

	.chapter-loading,
	.chapter-empty {
		margin-top: 20rpx;
		padding: 72rpx 24rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		font-size: 26rpx;
		color: #97a0af;
		border-radius: 28rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, rgba(248, 251, 255, 0.92) 100%);
		border: 1rpx solid rgba(121, 159, 229, 0.08);
	}

	.chapter-card {
		padding: 26rpx 24rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		min-height: 120rpx;
		border-radius: 28rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		border: 1rpx solid rgba(121, 159, 229, 0.1);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
		transition: transform 0.15s ease;
	}

	.chapter-card:active {
		transform: scale(0.99);
	}

	.chapter-left {
		display: flex;
		align-items: center;
		gap: 18rpx;
		min-width: 0;
	}

	.chapter-icon {
		width: 72rpx;
		height: 72rpx;
		border-radius: 22rpx;
		background: linear-gradient(180deg, #EDF4FF 0%, #E4EEFF 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.chapter-info {
		display: flex;
		flex-direction: column;
		min-width: 0;
	}

	.chapter-title {
		font-size: 30rpx;
		font-weight: 600;
		color: #253143;
		line-height: 1.25;
	}

	.chapter-meta {
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #95A1B3;
	}

	.chapter-arrow {
		width: 44rpx;
		height: 44rpx;
		border-radius: 999rpx;
		background: #F2F6FD;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.meta-row {
		margin-top: 18rpx;
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: 16rpx;
		font-size: 24rpx;
		color: #7f8898;
	}

	.meta-text {
		font-size: 24rpx;
		color: #7D8A9E;
	}

	.sort-chip {
		padding: 12rpx 16rpx;
		border-radius: 999rpx;
		background: #EFF5FF;
		display: flex;
		align-items: center;
		gap: 8rpx;
		font-size: 24rpx;
		color: #386df5;
		font-weight: 500;
		flex-shrink: 0;
	}

	.chapter-items {
		margin-top: 20rpx;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.chapter-item {
		padding: 24rpx;
		border-radius: 28rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		display: flex;
		align-items: stretch;
		gap: 18rpx;
		border: 1rpx solid rgba(121, 159, 229, 0.1);
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08);
		transition: transform 0.15s ease;
	}

	.chapter-item:active {
		transform: scale(0.99);
	}

	.chapter-item.right {
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08), inset 6rpx 0 0 #22C55E;
	}

	.chapter-item.wrong {
		box-shadow: 0 14rpx 30rpx rgba(74, 108, 173, 0.08), inset 6rpx 0 0 #FF6B6B;
	}

	.item-side {
		width: 116rpx;
		flex-shrink: 0;
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		justify-content: space-between;
		gap: 18rpx;
	}

	.item-content {
		flex: 1;
		min-width: 0;
	}

	.no {
		font-size: 22rpx;
		font-weight: 600;
		color: #9AA6B7;
	}

	.question {
		font-size: 30rpx;
		font-weight: 500;
		line-height: 1.5;
		color: #253143;
	}

	.item-footer {
		margin-top: 16rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	.item-tip {
		font-size: 22rpx;
		color: #97A2B2;
	}

	.status {
		padding: 8rpx 14rpx;
		border-radius: 14rpx;
		font-size: 22rpx;
		background: #F3F6FB;
		color: #96A1B1;
	}

	.chapter-footer {
		padding: 18rpx 32rpx calc(18rpx + env(safe-area-inset-bottom));
		background: rgba(247, 250, 255, 0.96);
		backdrop-filter: blur(8px);
	}

	.start-btn {
		height: 92rpx;
		border-radius: 28rpx;
		background: linear-gradient(135deg, #4A80EE 0%, #6398F8 100%);
		box-shadow: 0 14rpx 30rpx rgba(78, 136, 246, 0.18);
	}

	.load-more-wrap {
		margin-top: 8rpx;
		padding-bottom: 12rpx;
		display: flex;
		justify-content: center;
	}

	.load-more-btn {
		min-width: 220rpx;
		padding: 18rpx 30rpx;
		border-radius: 999rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.96) 100%);
		border: 1rpx solid rgba(121, 159, 229, 0.1);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		font-size: 24rpx;
		color: #386df5;
		font-weight: 500;
		box-shadow: 0 10rpx 22rpx rgba(74, 108, 173, 0.08);
	}

	.load-more-btn.disabled {
		opacity: 0.7;
	}
</style>
