<template>
	<view class="app-page question-page">
		<view v-if="showProgressBar" class="question-toolbar">
			<view class="progress-wrap">
				<view class="progress-track">
					<view class="progress-bar" :style="{ width: progressWidth }"></view>
				</view>
				<text
					v-if="showExamTimer"
					class="exam-timer"
					:class="{ 'exam-timer--warn': examTimerWarn }"
				>{{ examCountdownText }}</text>
			</view>
			<view class="icon-shell" @tap.stop="openAnswerSheet">
				<up-icon name="grid" size="18" color="#7C8798"></up-icon>
			</view>
		</view>
		<view class="screen question-screen">
			<view v-if="questionLoading" class="question-loading">
				<up-loading-icon mode="circle" size="22"></up-loading-icon>
				<text class="question-loading-text">加载题目中...</text>
			</view>
			<view v-else-if="!questionList.length" class="question-empty">
				<up-icon name="empty-page" size="64" color="#C0C6D4"></up-icon>
				<text class="question-empty-text">暂无题目</text>
				<view class="question-empty-btn" @tap="back">返回</view>
			</view>
			<template v-else>
			<view class="question-stage">
				<view class="question-stage-head">
					<text class="question-progress-badge">{{ progressText }}</text>
					<view class="label-row">
						<text class="meta-tag type-tag">{{ questionTypeLabel }}</text>
						<text class="meta-tag difficulty-tag" :class="`difficulty-tag--${difficultyTagClass}`">难度 {{ difficultyLabel }}</text>
					</view>
					<view class="question-meta-row">
						<text class="question-meta">第 {{ currentQuestionNo }} / {{ totalQuestionCount }} 题</text>
						<text class="question-meta">已答 {{ answeredCount }} 题</text>
						<text class="question-meta">未答 {{ remainingCount }} 题</text>
					</view>
				</view>
				<view class="question-stage-body" :class="{ 'review-locked': showReview }">
					<view class="question-title">{{ currentQuestion.title }}</view>
					<!-- 单选/判断：互斥仅一项选中 -->
					<question-option-single
						v-if="currentInputMode === 'single' && currentQuestion.options.length"
						:model-value="currentAnswer"
						:options="currentQuestion.options"
						@update:model-value="updateAnswer"
					/>
					<!-- 多选：可多选，答案以逗号分隔 label 存储 -->
					<question-option-multi
						v-else-if="currentInputMode === 'multi' && currentQuestion.options.length"
						:model-value="currentAnswer"
						:options="currentQuestion.options"
						@update:model-value="updateAnswer"
					/>
					<!-- 填空：文本输入 -->
					<question-fill-blank
						v-else-if="currentInputMode === 'fill'"
						:model-value="currentAnswer"
						@update:model-value="updateAnswer"
					/>
					<question-analysis-panel
						v-if="showReview"
						:item="currentReviewItem"
						embedded
						:player-id="`practiceVideo-${currentIndex}`"
					/>
				</view>
			</view>
			</template>
		</view>
		<view v-if="!questionLoading && questionList.length" class="question-footer">
			<view class="footer-action muted" :class="{ collected: isCollected }" @tap="toggleCollect">
				<up-icon :name="isCollected ? 'star-fill' : 'star'" size="18" :color="isCollected ? '#FF9A48' : '#9DA6B3'"></up-icon>
				<text>{{ isCollected ? '已收藏' : '收藏' }}</text>
			</view>
			<view class="footer-action neutral" :class="{ disabled: isFirstQuestion }" @tap="prevQuestion">
				<text>上一题</text>
			</view>
			<view class="footer-action green" @tap="openAiChat">
				<up-icon name="chat" size="18" color="#1CAF55"></up-icon>
				<text>AI讲解</text>
			</view>
			<view class="footer-action primary" :class="{ disabled: submitting }" @tap="handlePrimaryAction">
				<text>{{ submitting ? '交卷中...' : primaryActionLabel }}</text>
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
		clearPracticeSource,
		checkAiChatQuota,
		getPracticeSource,
		hasCollectionItem,
		markNotebookMastered,
		resolveCollectionSubject,
		showAiQuotaExhaustedModal,
		syncChapterQuestionAnswer,
		syncCollectionListFromApi,
		syncNotebookListFromApi,
		syncWrongNotebook,
		toggleCollectionItem
	} from '@/common/app-data';
	import {
		QUESTION_PRACTICE_SESSION_KEY,
		QUESTION_REVIEW_KEY
	} from '@/common/storage-keys';
	import { fetchPracticeQuestionsBySource } from '@/common/question-list';
	import { getDifficultyTagClass, resolveDifficultyLabel, resolveQuestionTypeLabel } from '@/common/question-labels';
	import {
		hasQuestionAnswer,
		isQuestionAnswerCorrect,
		normalizeOptionLabel,
		resolveQuestionInputMode
	} from '@/common/question-answer';
	import {
		EXAM_TIMER_WARN_SECONDS,
		formatExamCountdown,
		resolveExamDurationMinutes,
		resolveExamDurationSeconds
	} from '@/common/exam-timer';
	import { submitChapterPractice } from '@/common/practice-progress';
	import { recordLocalStudyActivity } from '@/common/study-calendar';
	import {
		applyDraftToAnswerState,
		buildDraftPayload,
		clearPracticeDraft,
		fetchPracticeDraft,
		revertPracticeDraft,
		saveLocalPracticeDraft,
		savePracticeDraft
	} from '@/common/practice-draft';
	import { onLoad, onHide, onUnload, onShow } from '@dcloudio/uni-app';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import { playAnswerTapSound } from '@/common/sound-feedback';
	import { QUESTION_ANSWER_SHEET_PAGE, QUESTION_RESULT_PAGE } from '@/common/routes';
	import {
		clearAnswerSheetState,
		consumeAnswerSheetReturn,
		pushAnswerSheetState
	} from '@/common/answer-sheet-bridge';
	import {
		applyLocalNotebookAfterSubmit,
		buildUnansweredCompleteModalContent,
		resolveCompletePracticeAction
	} from '@/common/practice-rules';
	import {
		buildReviewItemFromQuestion,
		isInstantReviewEnabled,
		resolvePracticePrimaryActionLabel
	} from '@/common/practice-review';
	const practiceSource = ref(getPracticeSource());
	const questionList = ref([]);
	const questionLoading = ref(false);

	const currentIndex = ref(0);
	const selectedMap = ref({});
	const collectedMap = ref({});
	const collectLoading = ref(false);
	const startTime = ref(Date.now());
	/** 进入答题页时的暂存快照，直接退出时回滚 */
	const entryDraftSnapshot = ref(null);
	/** 模考剩余秒数（examMode 时顶部倒计时） */
	const examRemainingSeconds = ref(0);
	/** 防止倒计时结束重复交卷 */
	const examAutoSubmitted = ref(false);
	/** 交卷中：防重复点击「完成练习」 */
	const submitting = ref(false);
	/** 当前题是否处于「看解析」阶段（练习模式做一题看一题） */
	const showReview = ref(false);
	/** 已展示过解析的题号，用于切题/答题卡回显 */
	const reviewedIndices = ref([]);
	let examTimerHandle = null;

	const currentQuestion = computed(() => questionList.value[currentIndex.value] || questionList.value[0] || {
		type: '单选题',
		difficulty: '中等',
		title: '',
		rightAnswer: '',
		analysis: '',
		aiSummary: '',
		options: []
	});
	// 当前题用户答案（单选 label / 多选逗号分隔 / 填空文本）
	const currentAnswer = computed(() => String(selectedMap.value[currentIndex.value] || ''));
	// 当前题交互模式：单选 / 多选 / 填空
	const currentInputMode = computed(() => resolveQuestionInputMode(currentQuestion.value));
	// 题型/难度标签：来自题目数据，切题后随 currentQuestion 同步
	const questionTypeLabel = computed(() => resolveQuestionTypeLabel(currentQuestion.value));
	const difficultyLabel = computed(() => resolveDifficultyLabel(currentQuestion.value));
	const difficultyTagClass = computed(() => getDifficultyTagClass(difficultyLabel.value));
	const isCollected = computed(() => Boolean(collectedMap.value[currentIndex.value]));
	// 顶部进度条：当前题号 / 总题数，随切题同步更新
	const totalQuestionCount = computed(() => questionList.value.length);
	const currentQuestionNo = computed(() => {
		if (!totalQuestionCount.value) {
			return 0;
		}
		return Math.min(currentIndex.value + 1, totalQuestionCount.value);
	});
	const showProgressBar = computed(() => !questionLoading.value && totalQuestionCount.value > 0);
	const progressText = computed(() => `${currentQuestionNo.value}/${totalQuestionCount.value}`);
	const isExamMode = computed(() => Boolean(practiceSource.value.examMode));
	const showExamTimer = computed(() => isExamMode.value && showProgressBar.value);
	const examCountdownText = computed(() => formatExamCountdown(examRemainingSeconds.value));
	const examTimerWarn = computed(() => (
		examRemainingSeconds.value > 0 && examRemainingSeconds.value <= EXAM_TIMER_WARN_SECONDS
	));
	const navigationTitle = computed(() => {
		if (showExamTimer.value) {
			return `答题 ${examCountdownText.value}`;
		}
		if (showProgressBar.value) {
			return `答题 ${progressText.value}`;
		}
		return '答题';
	});
	usePageNavigationTitle(navigationTitle);
	const progressWidth = computed(() => `${(currentQuestionNo.value / totalQuestionCount.value) * 100}%`);
	const isFirstQuestion = computed(() => currentIndex.value === 0);
	const isLastQuestion = computed(() => currentIndex.value === questionList.value.length - 1);
	const instantReviewEnabled = computed(() => isInstantReviewEnabled(practiceSource.value));
	const currentReviewItem = computed(() => buildReviewItemFromQuestion(
		currentQuestion.value,
		currentAnswer.value
	));
	const primaryActionLabel = computed(() => resolvePracticePrimaryActionLabel({
		instantReview: instantReviewEnabled.value,
		showReview: showReview.value,
		isLastQuestion: isLastQuestion.value
	}));
	const answeredCount = computed(() => questionList.value.filter((item, index) => (
		hasQuestionAnswer(selectedMap.value[index], item)
	)).length);
	const remainingCount = computed(() => Math.max(questionList.value.length - answeredCount.value, 0));

	const initCollectedMap = async (list = []) => {
		await syncCollectionListFromApi();
		const savedCollectedMap = {};
		list.forEach((item, index) => {
			const key = String(item.questionId || item.questionCode || item.title || '').trim();
			savedCollectedMap[index] = key ? hasCollectionItem(key) : false;
		});
		collectedMap.value = savedCollectedMap;
	};

	/** 当前已累计答题耗时（秒） */
	const getElapsedSeconds = () => Math.max(0, Math.round((Date.now() - startTime.value) / 1000));

	/** 构建当前答题态暂存（题号 + 答案 + 耗时） */
	const captureDraftState = () => buildDraftPayload(
		practiceSource.value,
		currentIndex.value,
		selectedMap.value,
		questionList.value,
		getElapsedSeconds()
	);

	/** 静默写入本地暂存（杀进程续答；不触发服务端） */
	const persistLocalDraftSilent = () => {
		if (questionLoading.value || !questionList.value.length) {
			return;
		}
		const draft = captureDraftState();
		if (!draft.hasDraft) {
			return;
		}
		saveLocalPracticeDraft(practiceSource.value, draft);
	};

	/** 清除模考倒计时定时器 */
	const clearExamTimer = () => {
		if (examTimerHandle) {
			clearInterval(examTimerHandle);
			examTimerHandle = null;
		}
	};

	/** 启动模考倒计时：剩余 = 总时长 - 已用秒数（续答时从 draft 恢复） */
	const initExamTimer = (elapsedSeconds = 0) => {
		clearExamTimer();
		if (!isExamMode.value) {
			examRemainingSeconds.value = 0;
			return;
		}
		const totalSeconds = resolveExamDurationSeconds(practiceSource.value);
		const elapsed = Math.max(0, Number(elapsedSeconds) || 0);
		examRemainingSeconds.value = Math.max(0, totalSeconds - elapsed);
		if (examRemainingSeconds.value <= 0) {
			autoSubmitExam();
			return;
		}
		examTimerHandle = setInterval(() => {
			if (examRemainingSeconds.value <= 1) {
				examRemainingSeconds.value = 0;
				clearExamTimer();
				autoSubmitExam();
				return;
			}
			examRemainingSeconds.value -= 1;
		}, 1000);
	};

	/** 模考到时自动交卷（未作答题按错题计分） */
	const autoSubmitExam = async () => {
		if (
			examAutoSubmitted.value
			|| questionLoading.value
			|| !questionList.value.length
		) {
			return;
		}
		examAutoSubmitted.value = true;
		clearExamTimer();
		uni.showToast({
			title: '考试时间到，已自动交卷',
			icon: 'none'
		});
		await saveSession();
		uni.redirectTo({
			url: QUESTION_RESULT_PAGE
		});
	};

	/** 题目加载完成后恢复暂存：题号、已答答案、累计耗时 */
	const restoreDraftAfterLoad = async () => {
		const draft = await fetchPracticeDraft(practiceSource.value);
		entryDraftSnapshot.value = { ...draft };
		const restored = applyDraftToAnswerState(draft, questionList.value);
		if (!restored) {
			return { restored: false, durationSeconds: 0 };
		}
		selectedMap.value = restored.selectedMap;
		currentIndex.value = restored.currentIndex;
		if (restored.durationSeconds > 0) {
			startTime.value = Date.now() - restored.durationSeconds * 1000;
		}
		uni.showToast({
			title: `已恢复上次进度（第 ${restored.currentIndex + 1} 题）`,
			icon: 'none'
		});
		return { restored: true, durationSeconds: restored.durationSeconds };
	};

	/** 章节列表点某题进入时，从该题开始整章刷题（无暂存时生效） */
	const applyInitialQuestionIndex = (source = {}) => {
		const startQuestionId = String(source.questionId || '').trim();
		if (!startQuestionId || source.mode !== 'all' || !questionList.value.length) {
			return;
		}
		const targetIndex = questionList.value.findIndex((item) => (
			String(item.questionId || item.questionCode || '') === startQuestionId
		));
		if (targetIndex >= 0) {
			currentIndex.value = targetIndex;
		}
	};

	const loadQuestions = async () => {
		practiceSource.value = getPracticeSource();
		const source = practiceSource.value;

		questionLoading.value = true;
		try {
			const list = await fetchPracticeQuestionsBySource(source);
			questionList.value = list;
			if (!list.length) {
				uni.showToast({ title: '暂无题目', icon: 'none' });
			}
		} catch (e) {
			questionList.value = [];
			uni.showToast({ title: '题目加载失败', icon: 'none' });
		} finally {
			questionLoading.value = false;
			currentIndex.value = 0;
			selectedMap.value = {};
			showReview.value = false;
			reviewedIndices.value = [];
			await initCollectedMap(questionList.value);
			const { restored, durationSeconds } = await restoreDraftAfterLoad();
			if (!restored) {
				applyInitialQuestionIndex(source);
			}
			syncReviewStateForIndex(currentIndex.value);
			initExamTimer(durationSeconds);
		}
	};

	onLoad(async () => {
		startTime.value = Date.now();
		entryDraftSnapshot.value = null;
		examAutoSubmitted.value = false;
		examRemainingSeconds.value = 0;
		uni.removeStorageSync(QUESTION_PRACTICE_SESSION_KEY);
		uni.removeStorageSync(QUESTION_REVIEW_KEY);
		await loadQuestions();
	});

	onShow(() => {
		const sheetReturn = consumeAnswerSheetReturn();
		if (!sheetReturn || !questionList.value.length) {
			return;
		}
		if (sheetReturn.jumpToIndex != null) {
			currentIndex.value = Math.min(sheetReturn.jumpToIndex, questionList.value.length - 1);
			syncReviewStateForIndex(currentIndex.value);
			persistLocalDraftSilent();
		}
		if (sheetReturn.requestComplete) {
			completePractice();
		}
	});

	onHide(() => {
		persistLocalDraftSilent();
	});

	onUnload(() => {
		clearExamTimer();
	});

	const navigateBackSafe = () => uni.navigateBack();

	/** 关闭前保存已答进度：暂存 draft + 本地章节 overlay，不交卷、不跳结果页 */
	const savePartialProgress = async () => {
		const draft = captureDraftState();
		await savePracticeDraft(practiceSource.value, draft, { silent: true });

		questionList.value.forEach((item, index) => {
			const myAnswer = selectedMap.value[index] || '';
			if (!hasQuestionAnswer(myAnswer, item)) {
				return;
			}
			const questionId = item.questionId || item.questionCode || '';
			if (questionId) {
				syncChapterQuestionAnswer({
					questionId,
					isCorrect: isQuestionAnswerCorrect(myAnswer, item)
				});
			}
		});
	};

	/** 左上角关闭：无作答直接返回；有作答时可选保存进度后退出（保留供空态「返回」等入口） */
	const back = () => {
		if (questionLoading.value || !answeredCount.value) {
			navigateBackSafe();
			return;
		}
		uni.showModal({
			title: '提示',
			content: '当前练习尚未完成，是否保存答题进度后退出？',
			confirmText: '保存并退出',
			cancelText: '直接退出',
			success: async (res) => {
				if (res.confirm) {
					await savePartialProgress();
					entryDraftSnapshot.value = captureDraftState();
					uni.showToast({ title: '进度已保存', icon: 'none' });
				} else {
					await revertPracticeDraft(practiceSource.value, entryDraftSnapshot.value);
				}
				navigateBackSafe();
			}
		});
	};
	const go = (url) => uni.navigateTo({
		url
	});

	/** AI 讲解：校验剩余次数后跳转 chat，扣减在对话页发送成功后执行 */
	const openAiChat = () => {
		const quota = checkAiChatQuota();
		if (!quota.ok && !quota.notReady) {
			showAiQuotaExhaustedModal();
			return;
		}
		const title = encodeURIComponent(currentQuestion.value.title);
		const tip = encodeURIComponent(currentQuestion.value.aiSummary || '');
		go(`/package/ai/chat?topic=${title}&tip=${tip}`);
	};

	/** 交卷：POST /app/practice/submit 持久化成绩与错题，失败时回退本地判分与错题本 */
	const saveSession = async () => {
		const durationSeconds = Math.max(60, Math.round((Date.now() - startTime.value) / 1000));
		const reviewList = questionList.value.map((item, index) => {
			const myAnswer = selectedMap.value[index] || '';
			return {
				index,
				order: index + 1,
				type: item.type,
				difficulty: item.difficulty,
				title: item.title,
				options: item.options,
				myAnswer,
				rightAnswer: item.rightAnswer,
				analysis: item.analysis,
				aiSummary: item.aiSummary,
				tags: Array.isArray(item.tags) ? item.tags : [],
				isCorrect: isQuestionAnswerCorrect(myAnswer, item),
				questionId: item.questionId || item.questionCode || '',
				questionCode: item.questionCode || item.questionId || ''
			};
		});
		const localCorrectCount = reviewList.filter((item) => item.isCorrect).length;
		const wrongList = reviewList.filter((item) => !item.isCorrect);
		reviewList.forEach((item) => {
			if (item.questionId) {
				syncChapterQuestionAnswer({
					questionId: item.questionId,
					isCorrect: item.isCorrect
				});
			}
		});

		const submitChapterId = String(practiceSource.value.chapterId || '').trim();
		const submitAnswers = reviewList
			.filter((item) => item.myAnswer && item.questionCode)
			.map((item) => ({
				questionCode: String(item.questionCode),
				userAnswer: item.myAnswer
			}));
		let submitOk = false;
		let serverResult = null;
		if (submitChapterId && submitAnswers.length) {
			const submitRes = await submitChapterPractice(submitChapterId, submitAnswers, {
				durationSeconds,
				sourceMode: practiceSource.value.mode || 'all',
				subject: resolveCollectionSubject(practiceSource.value)
			});
			submitOk = Boolean(submitRes.ok);
			serverResult = submitRes.result || null;
		}

		if (submitOk) {
			await syncNotebookListFromApi();
		} else {
			await applyLocalNotebookAfterSubmit(reviewList, practiceSource.value, {
				markNotebookMastered,
				syncWrongNotebook
			});
		}

		const total = questionList.value.length;
		const correctCount = submitOk && serverResult
			? Number(serverResult.correctCount) || localCorrectCount
			: localCorrectCount;
		const wrongCount = submitOk && serverResult
			? Number(serverResult.wrongCount) || wrongList.length
			: wrongList.length;
		const score = submitOk && serverResult
			? Number(serverResult.score) || Math.round((correctCount / Math.max(total, 1)) * 100)
			: Math.round((localCorrectCount / Math.max(total, 1)) * 100);

		const session = {
			total,
			correctCount,
			wrongCount,
			score,
			durationSeconds: submitOk && serverResult
				? Math.max(0, Number(serverResult.durationSeconds) || durationSeconds)
				: durationSeconds,
			reviewList,
			wrongList,
			sourceMode: practiceSource.value.mode,
			recordCode: submitOk && serverResult ? String(serverResult.recordCode || '').trim() : '',
			serverSynced: submitOk,
			chapterId: submitChapterId,
			paperId: String(practiceSource.value.paperId || '').trim(),
			knowledgePoint: String(practiceSource.value.knowledgePoint || '').trim(),
			notebookFilterChip: String(practiceSource.value.notebookFilterChip || '').trim(),
			notebookQuestionKeys: Array.isArray(practiceSource.value.notebookQuestionKeys)
				? practiceSource.value.notebookQuestionKeys.map((key) => String(key || '').trim()).filter(Boolean)
				: [],
			practiceTitle: String(practiceSource.value.title || '').trim() || '章节练习',
			examMode: Boolean(practiceSource.value.examMode),
			examDurationMinutes: resolveExamDurationMinutes(practiceSource.value)
		};
		uni.setStorageSync(QUESTION_PRACTICE_SESSION_KEY, session);
		if (!submitOk) {
			recordLocalStudyActivity();
		}
		if (wrongList.length) {
			uni.setStorageSync(QUESTION_REVIEW_KEY, wrongList[0]);
		}

		clearPracticeSource();
		clearPracticeDraft(practiceSource.value);
		clearAnswerSheetState();
	};

	const markIndexReviewed = (index) => {
		if (!reviewedIndices.value.includes(index)) {
			reviewedIndices.value = [...reviewedIndices.value, index];
		}
	};

	const syncReviewStateForIndex = (index) => {
		showReview.value = reviewedIndices.value.includes(index);
	};

	const syncChapterProgressForCurrent = () => {
		const question = currentQuestion.value;
		const questionId = question.questionId || question.questionCode || '';
		if (!questionId) {
			return;
		}
		syncChapterQuestionAnswer({
			questionId,
			isCorrect: currentReviewItem.value.isCorrect
		});
	};

	/** 练习模式：先展示当前题解析，再进入下一题 */
	const enterCurrentReview = () => {
		showReview.value = true;
		markIndexReviewed(currentIndex.value);
		syncChapterProgressForCurrent();
		persistLocalDraftSilent();
	};

	const proceedFromReview = async () => {
		showReview.value = false;
		if (isLastQuestion.value) {
			await completePractice();
			return;
		}
		currentIndex.value += 1;
		syncReviewStateForIndex(currentIndex.value);
		persistLocalDraftSilent();
	};

	const handlePrimaryAction = () => {
		if (instantReviewEnabled.value) {
			if (showReview.value) {
				proceedFromReview();
				return;
			}
			enterCurrentReview();
			return;
		}
		nextQuestion();
	};

	/** 更新当前题答案（单选 label / 多选逗号分隔 / 填空文本） */
	const updateAnswer = (value) => {
		if (showReview.value) {
			return;
		}
		const mode = currentInputMode.value;
		let nextAnswer = String(value || '');
		if (mode === 'single') {
			nextAnswer = normalizeOptionLabel(nextAnswer);
			if (!nextAnswer) {
				return;
			}
		} else if (mode === 'fill') {
			nextAnswer = nextAnswer.trim();
		}
		selectedMap.value = {
			...selectedMap.value,
			[currentIndex.value]: nextAnswer
		};
		if (hasQuestionAnswer(nextAnswer, currentQuestion.value)) {
			playAnswerTapSound();
		}
		persistLocalDraftSilent();
	};

	const toggleCollect = async () => {
		if (collectLoading.value) {
			return;
		}
		const question = currentQuestion.value;
		const questionId = String(question.questionId || question.questionCode || '').trim();
		if (!questionId) {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		collectLoading.value = true;
		try {
			const result = await toggleCollectionItem({
				...question,
				questionId,
				subject: resolveCollectionSubject(practiceSource.value),
				myAnswer: currentAnswer.value
			});
			collectedMap.value = {
				...collectedMap.value,
				[currentIndex.value]: result.collected
			};
			uni.showToast({ title: result.collected ? '已收藏当前题目' : '已取消收藏', icon: 'none' });
		} catch (e) {
			uni.showToast({ title: e.msg || '收藏操作失败', icon: 'none' });
		} finally {
			collectLoading.value = false;
		}
	};

	/** 交卷并跳转结果页（或单题模式返回上一页） */
	const finishPracticeSession = async () => {
		const isSingleMode = practiceSource.value.mode === 'single';
		clearExamTimer();
		submitting.value = true;
		try {
			await saveSession();
		} finally {
			submitting.value = false;
		}
		if (isSingleMode) {
			uni.navigateBack();
			return;
		}
		uni.redirectTo({
			url: QUESTION_RESULT_PAGE
		});
	};

	/** 完成练习：有未答题时提示，用户可选择仍要交卷 */
	const completePractice = async () => {
		if (submitting.value) {
			return;
		}
		const decision = resolveCompletePracticeAction({
			questionList: questionList.value,
			selectedMap: selectedMap.value
		});
		if (decision.action === 'confirm-unanswered') {
			uni.showModal({
				title: '提示',
				content: buildUnansweredCompleteModalContent(decision.unansweredCount),
				confirmText: '仍要交卷',
				cancelText: '继续作答',
				success: (res) => {
					if (res.confirm) {
						finishPracticeSession();
						return;
					}
					if (decision.firstPendingIndex != null && decision.firstPendingIndex >= 0) {
						currentIndex.value = decision.firstPendingIndex;
						persistLocalDraftSilent();
					}
				}
			});
			return;
		}
		await finishPracticeSession();
	};

	/** 下一题 / 完成练习：允许跳过未答题 */
	const nextQuestion = () => {
		if (isLastQuestion.value) {
			completePractice();
			return;
		}
		currentIndex.value += 1;
		persistLocalDraftSilent();
	};

	/** 上一题：首题禁用，不校验是否已选答案 */
	const prevQuestion = () => {
		if (isFirstQuestion.value) {
			return;
		}
		currentIndex.value -= 1;
		syncReviewStateForIndex(currentIndex.value);
		persistLocalDraftSilent();
	};

	const openAnswerSheet = () => {
		pushAnswerSheetState({
			questionList: questionList.value,
			selectedMap: selectedMap.value,
			currentIndex: currentIndex.value
		});
		go(QUESTION_ANSWER_SHEET_PAGE);
	};
</script>

<style lang="scss" scoped>
	.question-page {
		min-height: 100vh;
		background:
			radial-gradient(circle at top right, rgba(73, 127, 255, 0.14) 0, rgba(73, 127, 255, 0) 22%),
			linear-gradient(180deg, #eef4ff 0, #eaf1fd 180rpx, #f7f9fc 100%);
		display: flex;
		flex-direction: column;
	}

	.question-toolbar {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: var(--space-sm) var(--space-lg) 0;
	}

	.progress-wrap {
		flex: 1;
		min-width: 0;
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 14rpx 16rpx;
		border-radius: 22rpx;
		background: rgba(255, 255, 255, 0.82);
		box-shadow: 0 12rpx 28rpx rgba(15, 23, 42, 0.05);
	}

	.progress-wrap--placeholder {
		min-height: 68rpx;
	}

	.progress-track {
		flex: 1;
		height: 12rpx;
		border-radius: 999rpx;
		background: rgba(200, 209, 225, 0.56);
		overflow: hidden;
	}

	.progress-bar {
		height: 100%;
		border-radius: 999rpx;
		background: linear-gradient(90deg, #3f79ff 0%, #5d8eff 52%, #7ca3ff 100%);
	}

	.icon-shell {
		flex-shrink: 0;
		width: 68rpx;
		height: 68rpx;
		border-radius: 20rpx;
		background: rgba(255, 255, 255, 0.95);
		box-shadow: 0 14rpx 28rpx rgba(15, 23, 42, 0.08);
		display: flex;
		align-items: center;
		justify-content: center;
		border: 2rpx solid rgba(218, 223, 234, 0.9);
	}

	.exam-timer {
		flex-shrink: 0;
		font-size: 24rpx;
		font-weight: 700;
		font-variant-numeric: tabular-nums;
		padding: 10rpx 16rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		color: #3569ea;
	}

	.exam-timer--warn {
		background: #fff1f3;
		color: #e11d48;
	}

	.question-screen {
		flex: 1;
		padding: 18rpx 28rpx 0;
		padding-bottom: calc(226rpx + env(safe-area-inset-bottom));
	}

	.question-stage {
		margin-top: 18rpx;
		border-radius: 36rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 22rpx 54rpx rgba(15, 23, 42, 0.08);
		border: 2rpx solid rgba(229, 235, 244, 0.94);
		overflow: hidden;
	}

	.question-stage-head {
		position: relative;
		padding: 26rpx 28rpx 22rpx;
		padding-right: 120rpx;
		background: linear-gradient(180deg, rgba(236, 243, 255, 0.95) 0%, rgba(247, 250, 255, 0.95) 100%);
		border-bottom: 2rpx solid rgba(231, 236, 245, 0.92);
	}

	.question-progress-badge {
		position: absolute;
		top: 26rpx;
		right: 28rpx;
		padding: 10rpx 18rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.92);
		box-shadow: 0 8rpx 18rpx rgba(56, 109, 245, 0.1);
		font-size: 24rpx;
		font-weight: 800;
		font-variant-numeric: tabular-nums;
		color: #386df5;
		line-height: 1;
	}

	.question-stage-body {
		padding: 0 28rpx 30rpx;
	}

	.label-row {
		display: flex;
		align-items: center;
		gap: 14rpx;
		margin-top: 0;
	}

	.meta-tag {
		display: inline-flex;
		align-items: center;
		height: 56rpx;
		padding: 0 20rpx;
		border-radius: 999rpx;
		font-size: 20rpx;
		font-weight: 700;
	}

	.type-tag {
		background: #eaf1ff;
		color: #386df5;
	}

	.difficulty-tag {
		background: #f4f6fa;
		color: #667085;
	}

	.difficulty-tag--easy {
		background: #e8faf0;
		color: #12945a;
	}

	.difficulty-tag--medium {
		background: #fff5e8;
		color: #d97706;
	}

	.difficulty-tag--hard {
		background: #fff0f3;
		color: #e11d48;
	}

	.difficulty-tag--review {
		background: #edf4ff;
		color: #386df5;
	}

	.question-meta-row {
		margin-top: 16rpx;
		display: flex;
		gap: 14rpx;
		flex-wrap: wrap;
	}

	.question-meta {
		padding: 10rpx 16rpx;
		border-radius: 16rpx;
		background: rgba(255, 255, 255, 0.88);
		font-size: 20rpx;
		color: #7b8698;
	}

	.question-title {
		position: relative;
		margin-top: 26rpx;
		padding: 36rpx 30rpx 26rpx;
		border-radius: 0;
		background: transparent;
		box-shadow: none;
		border: 0;
		font-size: 31rpx;
		line-height: 1.58;
		font-weight: 700;
		color: #17212f;
		word-break: break-word;
	}

	.question-title::before {
		content: '题目';
		position: absolute;
		top: -14rpx;
		left: 0;
		height: 40rpx;
		padding: 0 16rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #386df5 0%, #5f8fff 100%);
		color: #ffffff;
		font-size: 18rpx;
		font-weight: 700;
		line-height: 40rpx;
		box-shadow: 0 10rpx 18rpx rgba(56, 109, 245, 0.18);
	}

	question-option-single,
	question-option-multi,
	question-fill-blank {
		display: block;
		margin-top: 14rpx;
		padding: 2rpx 0 0;
	}

	.review-locked question-option-single,
	.review-locked question-option-multi,
	.review-locked question-fill-blank {
		pointer-events: none;
		opacity: 0.92;
	}

	.question-loading,
	.question-empty {
		margin-top: 180rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 20rpx;
	}

	.question-loading-text,
	.question-empty-text {
		font-size: 26rpx;
		color: #7b8698;
	}

	.question-empty-btn {
		margin-top: 14rpx;
		height: 88rpx;
		padding: 0 48rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #386df5 0%, #5f8fff 100%);
		color: #ffffff;
		font-size: 26rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 16rpx 28rpx rgba(56, 109, 245, 0.2);
	}

	.question-footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
		background: rgba(248, 250, 253, 0.96);
		backdrop-filter: blur(18rpx);
		display: grid;
		grid-template-columns: 104rpx 132rpx 148rpx minmax(0, 1fr);
		gap: 12rpx;
		align-items: center;
		border-top: 1rpx solid rgba(212, 218, 229, 0.8);
		box-shadow: 0 -14rpx 34rpx rgba(15, 23, 42, 0.08);
		z-index: 100;
	}

	.footer-action {
		height: 88rpx;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 22rpx;
		font-weight: 700;
		white-space: nowrap;
	}

	.footer-action.disabled {
		opacity: 0.42;
		pointer-events: none;
	}

	.footer-action.muted {
		background: #ffffff;
		border: 2rpx solid rgba(222, 227, 236, 0.95);
		color: #98a2b3;
		flex-direction: column;
		gap: 2rpx;
		box-shadow: 0 8rpx 18rpx rgba(15, 23, 42, 0.04);
	}

	.footer-action.muted text {
		font-size: 16rpx;
	}

	.footer-action.muted.collected {
		background: #fff6ea;
		border-color: rgba(245, 158, 11, 0.24);
		color: #d97706;
	}

	.footer-action.neutral {
		background: #eef2f7;
		color: #4b5565;
		box-shadow: inset 0 0 0 2rpx rgba(222, 227, 236, 0.82);
	}

	.footer-action.green {
		background: linear-gradient(180deg, #ebfbf2 0%, #ddf8e8 100%);
		color: #12945a;
		box-shadow: inset 0 0 0 2rpx rgba(119, 214, 160, 0.22);
	}

	.footer-action.primary {
		background: linear-gradient(135deg, #386df5 0%, #5d8eff 100%);
		color: #ffffff;
		box-shadow: 0 18rpx 30rpx rgba(56, 109, 245, 0.24);
		position: relative;
		overflow: hidden;
	}

	.footer-action.primary::after {
		content: '';
		position: absolute;
		inset: 0;
		background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.16) 50%, rgba(255, 255, 255, 0) 100%);
		transform: translateX(-100%);
	}

	@media screen and (max-width: 375px) {
		.question-screen {
			padding-left: 22rpx;
			padding-right: 22rpx;
		}

		.question-title {
			font-size: 29rpx;
			padding: 26rpx 22rpx;
		}

		.question-footer {
			grid-template-columns: 96rpx 116rpx 132rpx minmax(0, 1fr);
			padding-left: 18rpx;
			padding-right: 18rpx;
			gap: 10rpx;
		}

		.footer-action {
			height: 82rpx;
			font-size: 20rpx;
		}
	}

	/* #ifdef MP-WEIXIN */
	.question-footer {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		background: rgba(248, 250, 253, 1);
		backdrop-filter: none;
	}

	.footer-action.muted {
		width: 104rpx;
	}

	.footer-action.neutral {
		width: 132rpx;
	}

	.footer-action.green {
		width: 148rpx;
	}

	.footer-action.primary {
		flex: 1;
		min-width: 0;
	}
	/* #endif */
</style>
