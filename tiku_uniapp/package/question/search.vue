<template>
	<view class="app-page find-page">
		<view class="screen">
			<view class="top-card">
				<view class="search-row">
					<view class="search-field">
						<up-icon name="search" size="18" color="#9AA7BC"></up-icon>
						<input
							v-model="keyword"
							class="search-input"
							type="text"
							confirm-type="search"
							:placeholder="searchPlaceholder"
							@confirm="runSearch"
						/>
					</view>
					<view class="search-btn" @tap="runSearch">搜索</view>
				</view>

				<view class="action-row">
					<view class="action-btn primary" @tap="openPhotoSearch">
						<up-icon name="camera-fill" size="18" color="#FFFFFF"></up-icon>
						<text>拍照搜题</text>
					</view>
					<view class="action-btn" @tap="openPasteSearch">
						<up-icon name="edit-pen-fill" size="18" color="#4E88F6"></up-icon>
						<text>粘贴题干</text>
					</view>
				</view>

				<view class="tab-row">
					<view
						v-for="tab in tabs"
						:key="tab.key"
						class="tab-item"
						:class="{ active: activeTab === tab.key }"
						@tap="switchTab(tab.key)"
					>
						<text>{{ tab.label }}</text>
					</view>
				</view>
			</view>

			<view v-if="showSuggestArea" class="suggest-block">
				<view class="suggest-head">
					<text class="suggest-label">试试这些</text>
					<text class="suggest-link" @tap="goChapterPractice">按章节找题</text>
				</view>
				<view class="suggest-list">
					<text
						v-for="item in suggestKeywords"
						:key="item"
						class="suggest-chip"
						@tap="applyHotKeyword(item)"
					>{{ item }}</text>
				</view>
			</view>

			<view class="content-area">
				<view v-if="results.length" class="result-summary">
					<text>{{ resultSummaryText }}</text>
				</view>

				<view v-if="loading" class="state-box">
					<up-loading-icon mode="circle" size="22" color="#4E88F6"></up-loading-icon>
					<text class="state-title">搜索中...</text>
				</view>

				<view v-else-if="searched && !results.length" class="state-box">
					<up-icon name="search" size="36" color="#C5D0E0"></up-icon>
					<text class="state-title">{{ emptyTitle }}</text>
					<text class="state-copy">{{ emptyCopy }}</text>
					<view v-if="activeTab === 'knowledge'" class="state-actions">
						<text class="state-link primary" @tap="openPhotoSearch">拍照搜题</text>
						<text class="state-link" @tap="goChapterPractice">按章节找题</text>
					</view>
				</view>

				<view v-else-if="results.length" class="result-list">
					<view
						v-for="item in results"
						:key="item.id || item.questionId || item.questionCode || item.title"
						class="result-item"
						@tap="openResult(item)"
					>
						<view class="result-main">
							<text v-if="item.subject || item.subtitle" class="result-tag">{{ item.subject || item.subtitle }}</text>
							<text class="result-title">{{ item.title }}</text>
							<text v-if="item.meta || item.chapterTitle" class="result-meta">
								{{ item.meta || item.chapterTitle }}
								<text v-if="item.difficulty"> · {{ item.difficulty }}</text>
							</text>
						</view>
						<up-icon name="arrow-right" size="14" color="#C0C6D4"></up-icon>
					</view>
				</view>

				<view v-else-if="!showSuggestArea" class="state-box">
					<up-icon name="file-text" size="36" color="#C5D0E0"></up-icon>
					<text class="state-title">{{ idleTitle }}</text>
					<text class="state-copy">{{ idleCopy }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import { storeToRefs } from 'pinia';
	import { getWeakPointRecommendations, setQuestionReviewItem } from '@/common/app-data';
	import { openOcrImagePicker } from '@/common/ocr-camera';
	import {
		buildSuggestKeywords,
		FIND_HUB_TABS,
		queryLocalFindHubList
	} from '@/common/question-find-hub';
	import { searchQuestions } from '@/common/question-search';
	import {
		CHAPTER_PRACTICE_PAGE,
		OCR_MANUAL_SEARCH_URL,
		OCR_RESULT_PAGE,
		QUESTION_ANALYSIS_PAGE
	} from '@/common/routes';
	import { useUserStore } from '@/stores/user';

	const tabs = FIND_HUB_TABS;
	const keyword = ref('');
	const results = ref([]);
	const loading = ref(false);
	const searched = ref(false);
	const activeTab = ref('knowledge');
	const suggestKeywords = ref([]);

	const userStore = useUserStore();
	const { profile } = storeToRefs(userStore);

	const searchPlaceholder = computed(() => (
		activeTab.value === 'knowledge'
			? '搜知识点，如：一元一次方程'
			: '输入关键词筛选'
	));

	const showSuggestArea = computed(() => (
		activeTab.value === 'knowledge'
		&& !searched.value
		&& !loading.value
		&& suggestKeywords.value.length > 0
	));

	const resultSummaryText = computed(() => {
		if (activeTab.value === 'knowledge') {
			return `共 ${results.value.length} 道相关题`;
		}
		return `共 ${results.value.length} 条记录`;
	});

	const emptyTitle = computed(() => (
		activeTab.value === 'knowledge' ? '没找到相关题目' : '暂无匹配内容'
	));

	const emptyCopy = computed(() => {
		if (activeTab.value === 'knowledge') {
			return '换更短的知识点词，或直接拍照搜题';
		}
		if (activeTab.value === 'notebook') {
			return '先去刷题，错题会自动收录到这里';
		}
		return '收藏和搜题记录会出现在这里';
	});

	const idleTitle = computed(() => (
		activeTab.value === 'knowledge' ? '输入知识点开始搜索' : '输入关键词筛选'
	));

	const idleCopy = computed(() => (
		activeTab.value === 'knowledge'
			? '不必记住整道题，2～8 字即可'
			: '留空并搜索可浏览最近记录'
	));

	const refreshSuggestKeywords = async () => {
		let weakPoints = [];
		try {
			const list = await getWeakPointRecommendations();
			weakPoints = Array.isArray(list) ? list.slice(0, 3) : [];
		} catch (e) {
			weakPoints = [];
		}
		suggestKeywords.value = buildSuggestKeywords(profile.value, weakPoints);
	};

	const refreshLocalTabResults = () => {
		results.value = queryLocalFindHubList(activeTab.value, keyword.value, 20);
		searched.value = true;
	};

	onLoad((options) => {
		const initial = String(options?.keyword || options?.q || '').trim();
		if (initial) {
			keyword.value = initial;
			runSearch();
		}
	});

	onShow(async () => {
		await refreshSuggestKeywords();
		if (activeTab.value !== 'knowledge' && !searched.value) {
			refreshLocalTabResults();
		}
	});

	const switchTab = (tabKey) => {
		if (activeTab.value === tabKey) {
			return;
		}
		activeTab.value = tabKey;
		searched.value = false;
		results.value = [];
		if (tabKey !== 'knowledge') {
			refreshLocalTabResults();
		}
	};

	const runSearch = async () => {
		if (activeTab.value !== 'knowledge') {
			refreshLocalTabResults();
			return;
		}
		const key = String(keyword.value || '').trim();
		if (!key) {
			uni.showToast({ title: '请输入知识点', icon: 'none' });
			return;
		}
		loading.value = true;
		searched.value = true;
		try {
			const result = await searchQuestions({ keyword: key, page: 1, size: 20 });
			results.value = result.list;
		} finally {
			loading.value = false;
		}
	};

	const applyHotKeyword = (word) => {
		keyword.value = String(word || '').trim();
		activeTab.value = 'knowledge';
		runSearch();
	};

	const openPhotoSearch = () => {
		openOcrImagePicker();
	};

	const openPasteSearch = () => {
		const topic = String(keyword.value || '').trim();
		const url = topic
			? `${OCR_MANUAL_SEARCH_URL}&topic=${encodeURIComponent(topic)}`
			: OCR_MANUAL_SEARCH_URL;
		uni.navigateTo({ url });
	};

	const goChapterPractice = () => {
		uni.navigateTo({ url: CHAPTER_PRACTICE_PAGE });
	};

	const openKnowledgeQuestion = (item) => {
		setQuestionReviewItem({
			title: item.title,
			questionCode: item.questionCode,
			questionId: item.questionId,
			subject: item.subject,
			questionType: item.questionType,
			difficulty: item.difficulty,
			analysis: '请在练习或搜题结果中查看完整解析。',
			rightAnswer: '',
			tags: item.chapterTitle ? [item.chapterTitle] : []
		});
		uni.navigateTo({ url: QUESTION_ANALYSIS_PAGE });
	};

	const openNotebookItem = (item) => {
		setQuestionReviewItem({
			title: item.question,
			myAnswer: item.wrong,
			rightAnswer: item.right,
			analysis: item.analysis || '暂无解析',
			tags: item.subject ? [item.subject] : [],
			aiSummary: item.aiSummary || ''
		});
		uni.navigateTo({ url: QUESTION_ANALYSIS_PAGE });
	};

	const openCollectionItem = (item) => {
		setQuestionReviewItem({
			title: item.title,
			analysis: item.analysis || '暂无解析',
			rightAnswer: item.rightAnswer || item.right || '',
			tags: item.subject ? [item.subject] : [],
			aiSummary: item.aiSummary || ''
		});
		uni.navigateTo({ url: QUESTION_ANALYSIS_PAGE });
	};

	const openRecordItem = (item) => {
		const query = [];
		const topicText = String(item.title || '').trim();
		if (topicText) {
			query.push(`topic=${encodeURIComponent(topicText)}`);
		}
		const imageUrl = String(item.imageUrl || '').trim();
		if (imageUrl) {
			query.push(`imageUrl=${encodeURIComponent(imageUrl)}`);
		}
		const url = query.length ? `${OCR_RESULT_PAGE}?${query.join('&')}` : OCR_RESULT_PAGE;
		uni.navigateTo({ url });
	};

	const openResult = (item) => {
		if (item.type === 'notebook') {
			openNotebookItem(item.raw || item);
			return;
		}
		if (item.type === 'collection') {
			openCollectionItem(item.raw || item);
			return;
		}
		if (item.type === 'records') {
			openRecordItem(item.raw || item);
			return;
		}
		openKnowledgeQuestion(item);
	};
</script>

<style lang="scss" scoped>
	.find-page {
		background: #F4F7FB;
		min-height: 100vh;
	}

	.screen {
		padding: 20rpx 24rpx 40rpx;
	}

	.top-card {
		padding: 20rpx;
		border-radius: 28rpx;
		background: #FFFFFF;
		box-shadow: 0 10rpx 30rpx rgba(45, 86, 156, 0.06);
	}

	.search-row {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.search-field {
		flex: 1;
		height: 76rpx;
		padding: 0 20rpx;
		border-radius: 20rpx;
		background: #F5F7FA;
		display: flex;
		align-items: center;
		gap: 12rpx;
		min-width: 0;
	}

	.search-input {
		flex: 1;
		min-width: 0;
		font-size: 28rpx;
		color: #22324B;
	}

	.search-btn {
		height: 76rpx;
		padding: 0 28rpx;
		border-radius: 20rpx;
		background: #4E88F6;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-weight: 600;
		color: #FFFFFF;
		flex-shrink: 0;
	}

	.action-row {
		margin-top: 16rpx;
		display: flex;
		gap: 12rpx;
	}

	.action-btn {
		flex: 1;
		height: 72rpx;
		border-radius: 18rpx;
		background: #F5F8FF;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 26rpx;
		font-weight: 600;
		color: #4E88F6;
	}

	.action-btn.primary {
		background: linear-gradient(135deg, #4E88F6 0%, #6AA0FB 100%);
		color: #FFFFFF;
	}

	.tab-row {
		margin-top: 18rpx;
		display: flex;
		border-top: 1rpx solid #EEF1F5;
		padding-top: 16rpx;
	}

	.tab-item {
		flex: 1;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		color: #8A96A8;
		position: relative;
	}

	.tab-item.active {
		color: #22324B;
		font-weight: 700;
	}

	.tab-item.active::after {
		content: '';
		position: absolute;
		left: 50%;
		bottom: 0;
		width: 40rpx;
		height: 6rpx;
		margin-left: -20rpx;
		border-radius: 999rpx;
		background: #4E88F6;
	}

	.suggest-block {
		margin-top: 20rpx;
		padding: 0 4rpx;
	}

	.suggest-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 12rpx;
	}

	.suggest-label {
		font-size: 24rpx;
		font-weight: 600;
		color: #6B7A90;
	}

	.suggest-link {
		font-size: 24rpx;
		color: #4E88F6;
	}

	.suggest-list {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
	}

	.suggest-chip {
		padding: 12rpx 22rpx;
		border-radius: 999rpx;
		background: #FFFFFF;
		font-size: 24rpx;
		color: #4A5870;
		box-shadow: 0 4rpx 16rpx rgba(45, 86, 156, 0.05);
	}

	.content-area {
		margin-top: 20rpx;
	}

	.result-summary {
		padding: 0 8rpx 12rpx;
		font-size: 24rpx;
		color: #8A96A8;
	}

	.state-box {
		padding: 80rpx 24rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 14rpx;
		text-align: center;
		border-radius: 28rpx;
		background: #FFFFFF;
	}

	.state-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #22324B;
	}

	.state-copy {
		font-size: 24rpx;
		line-height: 1.6;
		color: #8A96A8;
		max-width: 480rpx;
	}

	.state-actions {
		margin-top: 8rpx;
		display: flex;
		gap: 24rpx;
	}

	.state-link {
		font-size: 26rpx;
		color: #4E88F6;
	}

	.state-link.primary {
		font-weight: 600;
	}

	.result-list {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.result-item {
		padding: 24rpx;
		border-radius: 24rpx;
		background: #FFFFFF;
		display: flex;
		align-items: center;
		gap: 16rpx;
		box-shadow: 0 6rpx 20rpx rgba(45, 86, 156, 0.04);
	}

	.result-main {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.result-tag {
		align-self: flex-start;
		padding: 4rpx 12rpx;
		border-radius: 999rpx;
		background: #EEF4FF;
		font-size: 20rpx;
		color: #4E88F6;
	}

	.result-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #22324B;
		line-height: 1.5;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	.result-meta {
		font-size: 22rpx;
		color: #9AA7BC;
	}
</style>
