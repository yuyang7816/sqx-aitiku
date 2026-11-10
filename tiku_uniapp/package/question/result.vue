<template>
	<view class="result-page">
		<view class="page-bg">
			<view class="bg-glow bg-glow-a"></view>
			<view class="bg-glow bg-glow-b"></view>
		</view>

		<view class="result-hero" :class="heroToneClass">
			<view class="hero-badge">
				<up-icon :name="heroIcon" size="32" color="#FFFFFF"></up-icon>
			</view>
			<text class="hero-title">{{ heroTitle }}</text>
			<text class="hero-sub">{{ heroSub }}</text>
			<text v-if="session.practiceTitle && hasSession" class="hero-tag">{{ session.practiceTitle }}</text>
		</view>

		<view class="stats-card card">
			<view class="score-ring">
				<text class="score-value">{{ session.score }}</text>
				<text class="score-unit">分</text>
			</view>
			<view class="stats-grid">
				<view v-for="item in detailStats" :key="item.label" class="stat-item">
					<text class="stat-value" :style="{ color: item.color }">{{ item.value }}</text>
					<text class="stat-label">{{ item.label }}</text>
				</view>
			</view>
		</view>

		<view class="screen">
			<view class="section-head">
				<text class="section-title">{{ reviewTitle }}</text>
				<text v-if="notebookSyncHint" class="section-hint">{{ notebookSyncHint }}</text>
			</view>

			<view v-if="!hasSession" class="empty-card card">
				<view class="empty-icon">
					<up-icon name="empty-page" size="40" color="#94a3b8"></up-icon>
				</view>
				<text class="empty-title">暂无练习结果</text>
				<text class="empty-copy">完成一组章节练习后，这里会展示得分与错题回顾。</text>
			</view>

			<view v-else-if="session.wrongList.length" class="review-block">
				<view class="review-banner">
					<view class="review-banner-icon">
						<up-icon name="close-circle-fill" size="18" color="#ef4444"></up-icon>
					</view>
					<view class="review-banner-copy">
						<text class="review-banner-title">本次 {{ session.wrongList.length }} 道错题已整理</text>
						<text class="review-banner-sub">练习中已逐题展示解析，可在此再次回顾</text>
					</view>
				</view>

				<view class="action-cards">
					<view class="action-card" @tap="openNotebook">
						<view class="action-icon notebook">
							<up-icon name="bookmark-fill" size="18" color="#6366f1"></up-icon>
						</view>
						<view class="action-copy">
							<text class="action-title">进入错题本</text>
							<text class="action-sub">查看完整列表并标记掌握</text>
						</view>
						<up-icon name="arrow-right" size="14" color="#64748b"></up-icon>
					</view>
				</view>

				<view v-if="previewWrongList.length" class="wrong-preview card">
					<text class="preview-label">错题预览</text>
					<view
						v-for="(item, index) in previewWrongList"
						:key="`${item.questionCode || item.questionId || index}-${index}`"
						class="preview-item"
						@tap="openAnalysis(item)"
					>
						<text class="preview-index">{{ formatOrder(index + 1) }}</text>
						<view class="preview-copy">
							<text class="preview-title">{{ item.title || '未命名题目' }}</text>
							<text class="preview-answer">你的答案：{{ item.myAnswer || '未作答' }}</text>
						</view>
						<text class="preview-link">解析</text>
					</view>
				</view>
			</view>

			<view v-else class="empty-card card success">
				<view class="empty-icon success">
					<up-icon name="checkmark-circle-fill" size="40" color="#16a34a"></up-icon>
				</view>
				<text class="empty-title">这组题目全部答对了</text>
				<text class="empty-copy">继续保持这个节奏，再刷一组巩固手感。</text>
			</view>

			<view v-if="hasSession" class="bottom-actions">
				<view
					v-if="session.wrongList.length"
					class="cta-btn cta-primary"
					@tap="redoWrong"
				>
					<up-icon name="reload" size="16" color="#FFFFFF"></up-icon>
					<text>重做错题</text>
				</view>
				<view class="cta-btn" :class="session.wrongList.length ? 'cta-secondary' : 'cta-primary'" @tap="practiceAgain">
					<up-icon name="play-circle-fill" size="16" :color="session.wrongList.length ? '#386df5' : '#FFFFFF'"></up-icon>
					<text>再练一组</text>
				</view>
				<view class="cta-btn cta-ghost" @tap="home">
					<text>返回首页</text>
				</view>
			</view>
			<view v-else class="bottom-actions">
				<view class="cta-btn cta-ghost full" @tap="home">
					<text>返回首页</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		computed,
		ref
	} from 'vue';
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app';
	import {
		canSyncNotebookToApi,
		markNotebookMastered,
		setPracticeSource,
		syncNotebookListFromApi,
		syncWrongNotebook
	} from '@/common/app-data';
	import {
		QUESTION_PRACTICE_SESSION_KEY,
		QUESTION_REVIEW_KEY
	} from '@/common/storage-keys';
	import {
		NOTEBOOK_REDO_TITLE,
		QUESTION_ANALYSIS_PAGE,
		QUESTION_ANSWER_PAGE
	} from '@/common/routes';
	import { switchTabPage } from '@/common/tabbar';
	import { usePageNavigationTitle } from '@/common/navigation-bar';

	/** 空 session，与 answer.vue saveSession 写入结构对齐 */
	const createEmptySession = () => ({
		total: 0,
		correctCount: 0,
		wrongCount: 0,
		score: 0,
		durationSeconds: 0,
		wrongList: [],
		reviewList: [],
		sourceMode: 'all',
		recordCode: '',
		serverSynced: false,
		chapterId: '',
		paperId: '',
		knowledgePoint: '',
		notebookFilterChip: '',
		notebookQuestionKeys: [],
		practiceTitle: '章节练习',
		examMode: false,
		examDurationMinutes: 0
	});

	/** 规范化本地 session，保证与交卷 saveSession 字段一致 */
	const normalizeSession = (raw) => {
		if (!raw || typeof raw !== 'object') {
			return createEmptySession();
		}
		const wrongList = Array.isArray(raw.wrongList) ? raw.wrongList : [];
		const total = Number(raw.total) || 0;
		const correctCount = Number.isFinite(Number(raw.correctCount))
			? Number(raw.correctCount)
			: Math.max(total - wrongList.length, 0);
		const wrongCount = Number.isFinite(Number(raw.wrongCount))
			? Number(raw.wrongCount)
			: wrongList.length;
		const score = Number.isFinite(Number(raw.score))
			? Number(raw.score)
			: (total ? Math.round((correctCount / total) * 100) : 0);
		return {
			total,
			correctCount,
			wrongCount,
			score,
			durationSeconds: Math.max(0, Number(raw.durationSeconds) || 0),
			wrongList,
			reviewList: Array.isArray(raw.reviewList) ? raw.reviewList : [],
			sourceMode: raw.sourceMode || 'all',
			recordCode: String(raw.recordCode || '').trim(),
			serverSynced: Boolean(raw.serverSynced),
			chapterId: String(raw.chapterId || '').trim(),
			paperId: String(raw.paperId || '').trim(),
			knowledgePoint: String(raw.knowledgePoint || '').trim(),
			notebookFilterChip: String(raw.notebookFilterChip || '').trim(),
			notebookQuestionKeys: Array.isArray(raw.notebookQuestionKeys)
				? raw.notebookQuestionKeys.map((key) => String(key || '').trim()).filter(Boolean)
				: [],
			practiceTitle: String(raw.practiceTitle || '').trim() || '章节练习',
			examMode: Boolean(raw.examMode),
			examDurationMinutes: Math.max(0, Number(raw.examDurationMinutes) || 0)
		};
	};

	const session = ref(createEmptySession());
	const hasSession = ref(false);
	/** idle | syncing | synced | local */
	const notebookSyncState = ref('idle');

	const loadSession = () => {
		const saved = uni.getStorageSync(QUESTION_PRACTICE_SESSION_KEY);
		const normalized = normalizeSession(saved);
		hasSession.value = normalized.total > 0;
		session.value = normalized;
	};

	/** 结果页补同步错题本：交卷时服务端未同步或 GET 失败时重试 POST/GET /app/notebook */
	const ensureNotebookSynced = async () => {
		if (!hasSession.value || !session.value.wrongList.length) {
			return;
		}
		if (!canSyncNotebookToApi()) {
			notebookSyncState.value = 'local';
			return;
		}
		if (notebookSyncState.value === 'syncing') {
			return;
		}
		notebookSyncState.value = 'syncing';
		try {
			if (!session.value.serverSynced) {
				const practiceSource = {
					chapterId: session.value.chapterId,
					paperId: session.value.paperId,
					mode: session.value.sourceMode,
					title: session.value.practiceTitle
				};
				const correctList = (session.value.reviewList || []).filter((item) => item.isCorrect);
				await markNotebookMastered(correctList);
				await syncWrongNotebook(session.value.wrongList, practiceSource);
			}
			const synced = await syncNotebookListFromApi();
			notebookSyncState.value = synced.fromApi ? 'synced' : 'local';
		} catch (error) {
			notebookSyncState.value = 'local';
		}
	};

	onLoad(loadSession);
	onShow(async () => {
		loadSession();
		await ensureNotebookSynced();
	});

	const formatDuration = (seconds) => {
		const minute = Math.floor(seconds / 60);
		const second = seconds % 60;
		if (!minute) {
			return `${second}秒`;
		}
		return second ? `${minute}分${second}秒` : `${minute}分钟`;
	};

	const formatOrder = (order) => String(order).padStart(2, '0');

	const detailStats = computed(() => [{
		value: `${session.value.total} 题`,
		label: '答题总数',
		color: '#1e293b'
	}, {
		value: `${session.value.correctCount} 题`,
		label: '答对题数',
		color: '#16a34a'
	}, {
		value: `${session.value.wrongCount} 题`,
		label: '错题数量',
		color: '#ef4444'
	}, {
		value: formatDuration(session.value.durationSeconds),
		label: '答题用时',
		color: '#386df5'
	}]);

	const previewWrongList = computed(() => session.value.wrongList.slice(0, 3));

	const navbarTitle = computed(() => {
		const title = String(session.value.practiceTitle || '').trim();
		return title || '练习结果';
	});
	usePageNavigationTitle(navbarTitle);

	const heroToneClass = computed(() => (
		!hasSession.value ? 'tone-empty' : session.value.wrongCount ? 'tone-warn' : 'tone-success'
	));

	const heroIcon = computed(() => {
		if (!hasSession.value) {
			return 'info-circle-fill';
		}
		return session.value.wrongCount ? 'star-fill' : 'checkmark-circle-fill';
	});

	const heroTitle = computed(() => {
		if (!hasSession.value) {
			return '暂无练习结果';
		}
		return session.value.wrongCount ? '练习完成' : '全对收官';
	});

	const heroSub = computed(() => {
		if (!hasSession.value) {
			return '请先完成一组练习后再查看结果';
		}
		return session.value.wrongCount
			? '错题已整理，趁热回顾更容易提分'
			: '这一组发挥很稳，继续保持';
	});

	const reviewTitle = computed(() => (
		session.value.wrongCount ? `错题回顾 · ${session.value.wrongCount} 题` : '练习反馈'
	));

	const notebookSyncHint = computed(() => {
		if (!hasSession.value || !session.value.wrongList.length) {
			return '';
		}
		if (notebookSyncState.value === 'syncing') {
			return '错题本同步中…';
		}
		if (notebookSyncState.value === 'synced' || session.value.serverSynced) {
			return '已同步至云端错题本';
		}
		if (notebookSyncState.value === 'local') {
			return '已保存至本机错题本';
		}
		return '';
	});

	const openNotebook = () => switchTabPage('notebook');

	const buildPracticeContext = () => ({
		chapterId: session.value.chapterId,
		paperId: session.value.paperId,
		knowledgePoint: session.value.knowledgePoint,
		notebookFilterChip: session.value.notebookFilterChip,
		notebookQuestionKeys: session.value.notebookQuestionKeys,
		title: session.value.practiceTitle || '章节练习'
	});

	const resolvePracticeAgainMode = (context = {}) => {
		if (context.chapterId || context.paperId || context.knowledgePoint) {
			return 'all';
		}
		return session.value.sourceMode || 'all';
	};

	const openAnalysis = (item) => {
		uni.setStorageSync(QUESTION_REVIEW_KEY, item);
		uni.navigateTo({ url: QUESTION_ANALYSIS_PAGE });
	};

	const redoWrong = () => {
		if (!hasSession.value) {
			return;
		}
		setPracticeSource({
			...buildPracticeContext(),
			mode: 'wrong',
			title: NOTEBOOK_REDO_TITLE
		});
		uni.redirectTo({ url: QUESTION_ANSWER_PAGE });
	};

	const practiceAgain = () => {
		if (!hasSession.value) {
			return;
		}
		const context = buildPracticeContext();
		setPracticeSource({
			...context,
			mode: resolvePracticeAgainMode(context),
			title: context.title || '章节练习',
			examMode: Boolean(session.value.examMode),
			examDurationMinutes: session.value.examDurationMinutes || 0
		});
		uni.redirectTo({ url: QUESTION_ANSWER_PAGE });
	};

	const home = () => uni.reLaunch({
		url: '/pages/index/index'
	});
</script>

<style lang="scss" scoped>
	.result-page {
		position: relative;
		min-height: 100vh;
		background: #f3f6fc;
		padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	}

	.page-bg {
		position: absolute;
		inset: 0;
		pointer-events: none;
		overflow: hidden;
	}

	.bg-glow {
		position: absolute;
		border-radius: 50%;
		filter: blur(90rpx);
		opacity: 0.45;
	}

	.bg-glow-a {
		width: 420rpx;
		height: 420rpx;
		top: -100rpx;
		right: -80rpx;
		background: rgba(56, 109, 245, 0.2);
	}

	.bg-glow-b {
		width: 340rpx;
		height: 340rpx;
		left: -80rpx;
		bottom: 160rpx;
		background: rgba(34, 197, 94, 0.12);
	}

	.result-hero {
		position: relative;
		z-index: 1;
		padding: calc(24rpx + env(safe-area-inset-top)) 32rpx 120rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		overflow: hidden;
	}

	.result-hero.tone-success {
		background: linear-gradient(180deg, #16a34a 0%, #22c55e 52%, #4ade80 100%);
	}

	.result-hero.tone-warn {
		background: linear-gradient(180deg, #386df5 0%, #4f46e5 52%, #6366f1 100%);
	}

	.result-hero.tone-empty {
		background: linear-gradient(180deg, #64748b 0%, #94a3b8 100%);
	}

	.hero-badge {
		width: 96rpx;
		height: 96rpx;
		border-radius: 28rpx;
		background: rgba(255, 255, 255, 0.2);
		border: 2rpx solid rgba(255, 255, 255, 0.24);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 16rpx 32rpx rgba(15, 23, 42, 0.12);
	}

	.hero-title {
		margin-top: 20rpx;
		font-size: 44rpx;
		font-weight: 800;
		color: #fff;
		line-height: 1.15;
	}

	.hero-sub {
		margin-top: 10rpx;
		font-size: 24rpx;
		line-height: 1.55;
		color: rgba(255, 255, 255, 0.88);
		text-align: center;
		max-width: 520rpx;
	}

	.hero-tag {
		margin-top: 16rpx;
		padding: 8rpx 18rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.18);
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.92);
	}

	.stats-card {
		position: relative;
		z-index: 2;
		margin: -88rpx 24rpx 0;
		padding: 28rpx 24rpx;
		border-radius: 32rpx;
		display: flex;
		align-items: center;
		gap: 24rpx;
	}

	.score-ring {
		width: 148rpx;
		height: 148rpx;
		border-radius: 50%;
		background: linear-gradient(145deg, #eef4ff 0%, #f8fbff 100%);
		border: 6rpx solid rgba(56, 109, 245, 0.14);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.score-value {
		font-size: 48rpx;
		font-weight: 800;
		line-height: 1;
		color: #386df5;
	}

	.score-unit {
		margin-top: 4rpx;
		font-size: 22rpx;
		color: #64748b;
	}

	.stats-grid {
		flex: 1;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 16rpx 12rpx;
	}

	.stat-item {
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}

	.stat-value {
		font-size: 28rpx;
		font-weight: 800;
		line-height: 1.2;
	}

	.stat-label {
		font-size: 20rpx;
		color: #94a3b8;
	}

	.screen {
		position: relative;
		z-index: 1;
		padding: 28rpx 24rpx 0;
	}

	.section-head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: 16rpx;
	}

	.section-title {
		font-size: 32rpx;
		font-weight: 800;
		color: #182230;
	}

	.section-hint {
		font-size: 20rpx;
		color: #64748b;
	}

	.review-block {
		margin-top: 20rpx;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.review-banner {
		padding: 22rpx 24rpx;
		border-radius: 24rpx;
		background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
		border: 2rpx solid rgba(251, 191, 36, 0.24);
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.review-banner-icon {
		width: 52rpx;
		height: 52rpx;
		border-radius: 16rpx;
		background: rgba(239, 68, 68, 0.12);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.review-banner-copy {
		flex: 1;
		min-width: 0;
	}

	.review-banner-title {
		display: block;
		font-size: 26rpx;
		font-weight: 800;
		color: #9a3412;
	}

	.review-banner-sub {
		display: block;
		margin-top: 4rpx;
		font-size: 22rpx;
		color: #c2410c;
	}

	.action-cards {
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.action-card {
		padding: 22rpx 24rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.96);
		border: 2rpx solid rgba(226, 232, 240, 0.92);
		box-shadow: 0 12rpx 28rpx rgba(36, 48, 65, 0.05);
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.action-card.primary {
		background: linear-gradient(135deg, #f8fbff 0%, #eef4ff 100%);
		border-color: rgba(56, 109, 245, 0.18);
	}

	.action-icon {
		width: 52rpx;
		height: 52rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.action-icon.analysis {
		background: rgba(56, 109, 245, 0.12);
	}

	.action-icon.notebook {
		background: rgba(99, 102, 241, 0.12);
	}

	.action-copy {
		flex: 1;
		min-width: 0;
	}

	.action-title {
		display: block;
		font-size: 28rpx;
		font-weight: 800;
		color: #1e293b;
	}

	.action-sub {
		display: block;
		margin-top: 4rpx;
		font-size: 22rpx;
		color: #64748b;
	}

	.wrong-preview {
		padding: 24rpx;
	}

	.preview-label {
		display: block;
		font-size: 22rpx;
		font-weight: 700;
		color: #94a3b8;
		margin-bottom: 16rpx;
	}

	.preview-item {
		padding: 18rpx 0;
		border-top: 2rpx solid rgba(226, 232, 240, 0.92);
		display: flex;
		align-items: flex-start;
		gap: 14rpx;
	}

	.preview-item:first-of-type {
		border-top: none;
		padding-top: 0;
	}

	.preview-index {
		width: 44rpx;
		height: 44rpx;
		border-radius: 14rpx;
		background: rgba(239, 68, 68, 0.12);
		color: #ef4444;
		font-size: 20rpx;
		font-weight: 800;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.preview-copy {
		flex: 1;
		min-width: 0;
	}

	.preview-title {
		display: block;
		font-size: 26rpx;
		font-weight: 700;
		line-height: 1.5;
		color: #1e293b;
	}

	.preview-answer {
		display: block;
		margin-top: 6rpx;
		font-size: 22rpx;
		color: #94a3b8;
	}

	.preview-link {
		flex-shrink: 0;
		font-size: 22rpx;
		font-weight: 700;
		color: #386df5;
		padding-top: 8rpx;
	}

	.empty-card {
		margin-top: 20rpx;
		padding: 48rpx 32rpx;
		border-radius: 28rpx;
		text-align: center;
	}

	.empty-card.success {
		background: linear-gradient(180deg, #f0fdf4 0%, #ecfdf5 100%);
		border: 2rpx solid rgba(34, 197, 94, 0.12);
	}

	.empty-icon {
		width: 96rpx;
		height: 96rpx;
		border-radius: 28rpx;
		background: #f1f5f9;
		display: flex;
		align-items: center;
		justify-content: center;
		margin: 0 auto 20rpx;
	}

	.empty-icon.success {
		background: rgba(34, 197, 94, 0.12);
	}

	.empty-title {
		font-size: 32rpx;
		font-weight: 800;
		color: #1e293b;
	}

	.empty-copy {
		margin-top: 12rpx;
		display: block;
		font-size: 24rpx;
		line-height: 1.7;
		color: #64748b;
	}

	.bottom-actions {
		margin-top: 32rpx;
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.cta-btn {
		height: 92rpx;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		font-size: 28rpx;
		font-weight: 800;
	}

	.cta-btn.full {
		width: 100%;
	}

	.cta-primary {
		background: linear-gradient(135deg, #386df5 0%, #4f46e5 100%);
		color: #fff;
		box-shadow: 0 16rpx 28rpx rgba(56, 109, 245, 0.24);
	}

	.cta-secondary {
		background: #fff;
		color: #386df5;
		border: 2rpx solid rgba(56, 109, 245, 0.2);
	}

	.cta-ghost {
		background: #f8fafc;
		color: #64748b;
		border: 2rpx solid rgba(226, 232, 240, 0.92);
	}

	@media screen and (max-width: 375px) {
		.stats-card {
			margin-left: 18rpx;
			margin-right: 18rpx;
			flex-direction: column;
		}

		.score-ring {
			width: 128rpx;
			height: 128rpx;
		}

		.screen {
			padding-left: 18rpx;
			padding-right: 18rpx;
		}
	}

	/* #ifdef MP-WEIXIN */
	.stats-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
		margin: -8rpx -6rpx 0;
	}

	.stat-item {
		width: calc(50% - 12rpx);
		margin: 8rpx 6rpx 0;
	}

	.review-block,
	.action-cards,
	.bottom-actions {
		gap: 16rpx;
	}
	/* #endif */
</style>
