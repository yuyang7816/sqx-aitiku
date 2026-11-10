<template>
	<view class="app-page ocr-page">
		<view class="page-bg">
			<view class="bg-glow bg-glow-a"></view>
			<view class="bg-glow bg-glow-b"></view>
		</view>

		<view class="screen detail-screen">
			<view class="hero-card card">
				<view v-if="previewImageSrc" class="preview-wrap">
					<image :src="previewImageSrc" mode="aspectFit" class="preview-image" />
					<view class="preview-mask"></view>
					<view class="preview-badge">
						<up-icon name="camera-fill" size="12" color="#FFFFFF"></up-icon>
						<text>题目图片</text>
					</view>
				</view>
				<view class="hero-copy">
					<text class="capture-title">{{ sourceTitle }}</text>
					<text class="capture-sub">{{ sourceSub }}</text>
				</view>
				<view class="capture-actions">
					<view v-if="status === 'idle'" class="mini-action primary" @tap="retryRecognition">
						<up-icon name="camera" size="14" color="#FFFFFF"></up-icon>
						<text>拍照搜题</text>
					</view>
					<template v-else-if="status !== 'error'">
						<view class="mini-action" @tap="retryRecognition">
							<up-icon name="reload" size="14" color="#386df5"></up-icon>
							<text>{{ retryButtonText }}</text>
						</view>
						<view class="mini-action ghost" @tap="toggleManual">
							<up-icon name="edit-pen" size="14" color="#5f6979"></up-icon>
							<text>手动输入</text>
						</view>
					</template>
				</view>
			</view>

			<view v-if="showManualInput" class="manual-card card">
				<view class="manual-head">
					<view class="manual-title-wrap">
						<view class="manual-icon">
							<up-icon name="edit-pen-fill" size="14" color="#386df5"></up-icon>
						</view>
						<text class="manual-title">手动输入题干</text>
					</view>
					<view
						class="manual-submit"
						:class="{ disabled: !canSubmitManual || status === 'loading' }"
						@tap="applyManualTopic"
					>
						<text>立即识别</text>
						<up-icon name="arrow-right" size="12" color="#FFFFFF"></up-icon>
					</view>
				</view>
				<textarea
					v-model="manualTopic"
					class="manual-textarea"
					auto-height
					maxlength="200"
					:disabled="status === 'loading'"
					placeholder="粘贴或输入题目关键词，系统将匹配题库相似题"
					@confirm="applyManualTopic"
				/>
				<text class="manual-count">{{ manualCharCount }}/200</text>
			</view>

			<view v-if="status === 'idle'" class="state-card idle-card card">
				<view class="state-visual idle">
					<up-icon name="search" size="28" color="#386df5"></up-icon>
				</view>
				<text class="state-title">开始你的智能搜题</text>
				<text class="state-copy">拍照上传题目，或手动输入关键词，系统将自动匹配答案与解析。</text>
				<view class="state-steps">
					<view class="step-item">
						<text class="step-num">1</text>
						<text class="step-text">输入题干</text>
					</view>
					<view class="step-line"></view>
					<view class="step-item">
						<text class="step-num">2</text>
						<text class="step-text">智能匹配</text>
					</view>
					<view class="step-line"></view>
					<view class="step-item">
						<text class="step-num">3</text>
						<text class="step-text">查看解析</text>
					</view>
				</view>
			</view>

			<view v-if="status === 'loading'" class="state-card loading-card card">
				<view class="loader-ring">
					<view class="loader-core"></view>
				</view>
				<text class="state-title">正在识别题目</text>
				<text class="state-copy">提取题干、匹配题库、生成解析，通常只需几秒钟。</text>
				<view class="loading-bar">
					<view class="loading-bar-inner"></view>
				</view>
			</view>

			<view v-else-if="status === 'error'" class="state-card error-card card">
				<view class="state-visual error">
					<up-icon name="info-circle-fill" size="28" color="#FFFFFF"></up-icon>
				</view>
				<text class="state-title">{{ errorTitle }}</text>
				<text class="state-copy">{{ errorCopy }}</text>
				<text v-if="errorKeywordHint" class="state-keyword">当前关键词：{{ errorKeywordHint }}</text>
			</view>

			<view v-if="status === 'error'" class="capture-actions error-actions-bar">
				<view class="mini-action" @tap="retryRecognition">
					<up-icon name="reload" size="14" color="#386df5"></up-icon>
					<text>{{ retryButtonText }}</text>
				</view>
				<view class="mini-action ghost" @tap="toggleManual">
					<up-icon name="edit-pen" size="14" color="#5f6979"></up-icon>
					<text>手动输入</text>
				</view>
			</view>

			<template v-else-if="status === 'success'">
				<view class="result-stack">
					<view class="question-card card">
						<view class="card-head">
							<view class="head-left">
								<text class="head-label">识别题干</text>
								<text v-if="canSwitchSimilar" class="head-tag">{{ similarPositionText }}</text>
							</view>
							<view
								v-if="canSwitchSimilar"
								class="head-link-btn"
								@tap="openSimilarSheet"
							>
								<text>切换相似题</text>
								<up-icon name="arrow-right" size="12" color="#386df5"></up-icon>
							</view>
							<text v-else class="head-link disabled">暂无更多相似题</text>
						</view>
						<text class="card-copy">{{ currentQuestion.title }}</text>
					</view>

					<view :key="currentQuestionKey" class="answer-card">
						<view class="answer-head">
							<view class="answer-icon">
								<up-icon name="checkmark-circle-fill" size="16" color="#FFFFFF"></up-icon>
							</view>
							<text class="answer-label">标准答案</text>
						</view>
						<text class="answer-value">{{ displayAnswer }}</text>
					</view>

					<view :key="`analysis-${currentQuestionKey}`" class="analysis-card card">
						<view class="analysis-head">
							<view class="analysis-icon">
								<up-icon name="file-text-fill" size="14" color="#386df5"></up-icon>
							</view>
							<text class="analysis-title">详细解析</text>
						</view>
						<text class="analysis-copy">{{ displayAnalysis }}</text>
					</view>

					<view :key="`tips-${currentQuestionKey}`" class="tips-panel card">
						<view class="tips-header">
							<view class="tips-icon">
								<up-icon name="kefu-ermai" size="16" color="#FFFFFF"></up-icon>
							</view>
							<view class="tips-head-copy">
								<text class="tips-title">AI 通俗讲解</text>
								<text class="tips-sub">用更易懂的方式帮你理解</text>
							</view>
						</view>
						<view v-if="displayTips.length" class="tips-list">
							<view
								v-for="(item, tipIndex) in displayTips"
								:key="`${currentQuestionKey}-tip-${tipIndex}`"
								class="tip-item"
							>
								<view class="tip-index">{{ tipIndex + 1 }}</view>
								<text class="tip-text">{{ item }}</text>
							</view>
						</view>
						<text v-else class="tips-empty">暂无 AI 通俗讲解</text>
					</view>
				</view>

				<view class="bottom-bar">
					<view class="bottom-actions">
						<view class="action-chip" :class="{ active: isCollected }" @tap="toggleCollect">
							<up-icon :name="isCollected ? 'star-fill' : 'star'" size="16" :color="isCollected ? '#FF9A48' : '#64748b'"></up-icon>
							<text>{{ isCollected ? '已收藏' : '收藏' }}</text>
						</view>
						<view
							class="action-chip"
							:class="{ active: isInNotebook, disabled: notebookLoading || isInNotebook }"
							@tap="addToNotebook"
						>
							<up-icon name="bookmark" size="16" :color="isInNotebook ? '#386df5' : '#64748b'"></up-icon>
							<text>{{ isInNotebook ? '已在错题本' : '错题本' }}</text>
						</view>
						<view class="primary-cta" @tap="askAi">
							<up-icon name="chat-fill" size="16" color="#FFFFFF"></up-icon>
							<text>{{ askAiButtonText }}</text>
						</view>
					</view>
				</view>
			</template>
		</view>

		<up-popup
			:show="showSimilarSheet"
			mode="bottom"
			round="16"
			:close-on-click-overlay="true"
			@close="closeSimilarSheet"
		>
			<view class="similar-sheet">
				<view class="similar-head">
					<text class="similar-title">相似题列表</text>
					<text class="similar-sub">共 {{ questionList.length }} 道，按相似度排序</text>
				</view>
				<scroll-view scroll-y class="similar-scroll">
					<view
						v-for="(item, index) in questionList"
						:key="getSimilarItemKey(item, index)"
						class="similar-item"
						:class="{ active: index === currentIndex }"
						@tap="selectSimilarQuestion(index)"
					>
						<view class="similar-item-head">
							<text class="similar-order">相似题 {{ index + 1 }}</text>
							<text v-if="formatSimilarity(item.similarity)" class="similar-rate">{{ formatSimilarity(item.similarity) }}</text>
						</view>
						<text class="similar-copy">{{ item.title }}</text>
						<text v-if="index === currentIndex" class="similar-current">当前展示</text>
					</view>
				</scroll-view>
				<view v-if="canSwitchSimilar" class="similar-actions">
					<view
						class="ghost-button"
						:class="{ disabled: currentIndex <= 0 }"
						@tap="selectPrevSimilar"
					>上一题</view>
					<view
						class="solid-button small"
						:class="{ disabled: currentIndex >= questionList.length - 1 }"
						@tap="selectNextSimilar"
					>下一题</view>
				</view>
			</view>
		</up-popup>
	</view>
</template>

<script setup>
	import { computed, onBeforeUnmount, ref } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import {
		addNotebookItem,
		appendSearchRecord,
		buildOcrNotebookPayload,
		checkOcrSearchQuota,
		checkAiChatQuota,
		getMemberState,
		hasCollectionItem,
		hasNotebookItem,
		resolveCollectionSubject,
		SEARCH_RECORD_SOURCES,
		showAiQuotaExhaustedModal,
		showOcrQuotaExhaustedModal,
		syncCollectionListFromApi,
		syncNotebookListFromApi,
		syncOcrQuotaFromApi,
		toggleCollectionItem
	} from '@/common/app-data';
	import { buildOcrQuotaExhaustedPatch } from '@/common/member-rules';
	import { openOcrImagePicker } from '@/common/ocr-camera';
	import { buildChatPageUrl } from '@/common/chat-entry';
	import {
		normalizeOcrMatchItem,
		OCR_QUOTA_EXCEEDED_CODE,
		resolveOcrImageUrl,
		runOcrRecognition
	} from '@/common/ocr-search';

	const go = (url) => uni.navigateTo({ url });
	const fromChat = ref(false);
	const questionList = ref([]);
	const currentIndex = ref(0);
	const topic = ref('');
	const imagePath = ref('');
	/** 上传后或服务端可访问的原图地址（写入搜题记录、历史回看） */
	const savedImageUrl = ref('');
	const manualTopic = ref('');
	const status = ref('idle');
	const showManualInput = ref(false);
	const isManualSearch = ref(false);
	const isCollected = ref(false);
	const collectLoading = ref(false);
	const isInNotebook = ref(false);
	const notebookLoading = ref(false);
	const showSimilarSheet = ref(false);
	/** 识别失败原因：no_match 无匹配 / recognition_failed 识别失败 / input_empty 未输入 / network 网络异常 */
	const failReason = ref('');
	let recognitionSeq = 0;

	const OCR_FAIL_REASON = {
		NO_MATCH: 'no_match',
		RECOGNITION_FAILED: 'recognition_failed',
		INPUT_EMPTY: 'input_empty',
		NETWORK: 'network'
	};

	const currentQuestion = computed(() => {
		const item = questionList.value[currentIndex.value] || questionList.value[0];
		return normalizeOcrMatchItem(item || {});
	});
	/** 标准答案展示：优先 answer 字段，空值显示占位文案 */
	const displayAnswer = computed(() => {
		const text = String(currentQuestion.value.answer || '').trim();
		return text || '暂无标准答案';
	});
	/** 详细解析展示：优先 analysis 字段，空值显示占位文案 */
	const displayAnalysis = computed(() => {
		const text = String(currentQuestion.value.analysis || '').trim();
		return text || '暂无详细解析';
	});
	/** tips[] 展示：过滤空字符串，保证每条 tip 独立渲染 */
	const displayTips = computed(() => (
		Array.isArray(currentQuestion.value.tips)
			? currentQuestion.value.tips.map((item) => String(item || '').trim()).filter(Boolean)
			: []
	));
	const canSwitchSimilar = computed(() => questionList.value.length > 1);
	const similarPositionText = computed(() => {
		if (!questionList.value.length) {
			return '';
		}
		return `${currentIndex.value + 1}/${questionList.value.length}`;
	});
	const currentQuestionKey = computed(() => {
		const item = currentQuestion.value;
		if (item.questionId) {
			return `q-${item.questionId}`;
		}
		if (item.questionCode) {
			return `c-${item.questionCode}`;
		}
		return `i-${currentIndex.value}`;
	});
	const pageTitle = computed(() => {
		if (status.value === 'success') {
			return '识别结果';
		}
		if (status.value === 'error') {
			return failReason.value === OCR_FAIL_REASON.NO_MATCH ? '未找到题目' : '识别失败';
		}
		if (status.value === 'loading') {
			return '正在识别';
		}
		return '手动搜题';
	});
	usePageNavigationTitle(pageTitle);
	const errorKeywordHint = computed(() => {
		const keyword = String(topic.value || manualTopic.value || '').trim();
		if (!keyword || failReason.value !== OCR_FAIL_REASON.NO_MATCH) {
			return '';
		}
		return keyword.length > 36 ? `${keyword.slice(0, 36)}…` : keyword;
	});
	const errorTitle = computed(() => {
		if (failReason.value === OCR_FAIL_REASON.NO_MATCH) {
			return '未找到匹配题目';
		}
		if (failReason.value === OCR_FAIL_REASON.INPUT_EMPTY) {
			return '请先输入题干';
		}
		return '这次没有识别成功';
	});
	const errorCopy = computed(() => {
		if (failReason.value === OCR_FAIL_REASON.NO_MATCH) {
			return '题库中暂无相似题目，可调整关键词后重试，或改用手动输入继续搜索。';
		}
		if (failReason.value === OCR_FAIL_REASON.INPUT_EMPTY) {
			return '请在上方输入题目关键词，或拍照上传题目图片后再识别。';
		}
		if (failReason.value === OCR_FAIL_REASON.NETWORK) {
			return '网络或服务异常，请检查连接后重试，也可以手动输入题干关键词。';
		}
		return '可以重新识别，或者直接输入题干关键词继续搜索。';
	});
	const retryButtonText = computed(() => {
		if (isManualSearch.value && !previewImageSrc.value) {
			return '重新搜索';
		}
		return '重新识别';
	});
	const manualCharCount = computed(() => String(manualTopic.value || '').length);
	const canSubmitManual = computed(() => Boolean(String(manualTopic.value || '').trim()));
	/** 预览图：本地临时路径优先，其次为已保存的服务端 imageUrl */
	const previewImageSrc = computed(() => {
		const localPath = String(imagePath.value || '').trim();
		if (localPath) {
			return localPath;
		}
		const remoteUrl = String(savedImageUrl.value || '').trim();
		return remoteUrl ? resolveOcrImageUrl(remoteUrl) : '';
	});
	const sourceTitle = computed(() => {
		if (status.value === 'idle') {
			return '手动输入题干搜题';
		}
		if (previewImageSrc.value) {
			return '已获取题目图片';
		}
		return topic.value ? '已获取当前题目线索' : '拍照搜题已启动';
	});
	const sourceSub = computed(() => {
		if (status.value === 'idle') {
			return '输入题目关键词后点击「立即识别」，系统将匹配题库中的相似题目。';
		}
		if (previewImageSrc.value) {
			return '系统正在根据图片识别题干，你也可以手动补充关键词。';
		}
		return topic.value ? `当前搜索：${topic.value}` : '你可以重新识别，也可以手动输入关键词继续搜索。';
	});
	/** 从对话页进入时，主按钮文案为「继续对话」 */
	const askAiButtonText = computed(() => (fromChat.value ? '继续对话' : '再问AI'));

	const getSearchSource = () => (
		isManualSearch.value ? SEARCH_RECORD_SOURCES.MANUAL : SEARCH_RECORD_SOURCES.PHOTO
	);

	/** 收藏态查询键：优先 questionId/questionCode */
	const getCollectionKey = (item = currentQuestion.value) => String(
		item.questionId || item.questionCode || item.id || ''
	).trim();

	const syncCollectedState = () => {
		const key = getCollectionKey();
		isCollected.value = Boolean(key && hasCollectionItem(key));
	};

	/** 进入成功态时从服务端同步收藏列表并刷新当前题收藏态 */
	const initCollectedState = async () => {
		await syncCollectionListFromApi();
		syncCollectedState();
	};

	/** 刷新当前题是否已在错题本（questionId / questionCode / 题干） */
	const syncNotebookState = () => {
		const question = currentQuestion.value;
		const key = getCollectionKey(question);
		isInNotebook.value = Boolean(
			(key && hasNotebookItem(key))
			|| (String(question.title || '').trim() && hasNotebookItem(question))
		);
	};

	/** 进入成功态时同步错题本列表并刷新入库态 */
	const initNotebookState = async () => {
		await syncNotebookListFromApi();
		syncNotebookState();
	};

	const finishRecognition = (nextStatus, reason = '') => {
		status.value = nextStatus;
		if (nextStatus === 'error') {
			failReason.value = reason || OCR_FAIL_REASON.RECOGNITION_FAILED;
			if (isManualSearch.value || reason === OCR_FAIL_REASON.NO_MATCH) {
				showManualInput.value = true;
			}
			if (reason === OCR_FAIL_REASON.NO_MATCH) {
				uni.showToast({ title: '未找到匹配题目', icon: 'none' });
			}
			if (reason === OCR_FAIL_REASON.INPUT_EMPTY) {
				uni.showToast({ title: '请先输入题目关键词', icon: 'none' });
			}
			return;
		}
		failReason.value = '';
		if (nextStatus === 'success') {
			appendSearchRecord({
				title: currentQuestion.value.title,
				source: getSearchSource(),
				imageUrl: savedImageUrl.value
			});
			if (fromChat.value) {
				scheduleAutoContinueToChat();
			}
		}
	};

	const applyRecognitionResult = async (result) => {
		showSimilarSheet.value = false;
		questionList.value = (Array.isArray(result.matches) ? result.matches : []).map(normalizeOcrMatchItem);
		currentIndex.value = 0;
		if (result.recognizedText) {
			topic.value = result.recognizedText;
			manualTopic.value = result.recognizedText;
		}
		finishRecognition('success');
		await Promise.all([initCollectedState(), initNotebookState()]);
	};

	const startRecognition = async (searchTopic = '') => {
		const seq = ++recognitionSeq;
		showSimilarSheet.value = false;

		const normalizedTopic = String(searchTopic || '').trim();
		const hasImage = Boolean(String(imagePath.value || '').trim());

		if (normalizedTopic) {
			topic.value = normalizedTopic;
		}

		if (!normalizedTopic && !hasImage) {
			finishRecognition('error', OCR_FAIL_REASON.INPUT_EMPTY);
			return;
		}

		const quotaCheck = checkOcrSearchQuota();
		if (!quotaCheck.ok && !quotaCheck.notReady) {
			showOcrQuotaExhaustedModal();
			return;
		}

		const previousStatus = status.value;
		status.value = 'loading';

		try {
			const result = await runOcrRecognition({
				text: normalizedTopic,
				imagePath: normalizedTopic ? '' : imagePath.value
			});
			if (seq !== recognitionSeq) {
				return;
			}
			if (result.imageUrl) {
				savedImageUrl.value = result.imageUrl;
			}
			if (result.fromApi && result.quota) {
				syncOcrQuotaFromApi(result.quota);
			}
			if (result.matches.length) {
				await applyRecognitionResult(result);
				return;
			}
			if (result.recognizedText) {
				topic.value = result.recognizedText;
				manualTopic.value = result.recognizedText;
			}
			finishRecognition('error', OCR_FAIL_REASON.NO_MATCH);
		} catch (error) {
			if (seq !== recognitionSeq) {
				return;
			}
			if (error && error.code === OCR_QUOTA_EXCEEDED_CODE) {
				status.value = previousStatus === 'loading' ? 'idle' : previousStatus;
				syncOcrQuotaFromApi(buildOcrQuotaExhaustedPatch(getMemberState()));
				showOcrQuotaExhaustedModal();
				if (error.msg) {
					uni.showToast({ title: error.msg, icon: 'none' });
				}
				return;
			}
			const searchText = normalizedTopic || topic.value;
			const reason = searchText
				? OCR_FAIL_REASON.NO_MATCH
				: OCR_FAIL_REASON.RECOGNITION_FAILED;
			finishRecognition('error', reason);
			if (error && error.msg) {
				uni.showToast({ title: error.msg, icon: 'none' });
			}
		}
	};

	onLoad((options) => {
		const manualMode = options?.manual === '1';
		fromChat.value = options?.fromChat === '1';
		topic.value = decodeURIComponent(options?.topic || '');
		imagePath.value = decodeURIComponent(options?.imagePath || '');
		savedImageUrl.value = decodeURIComponent(options?.imageUrl || '');
		manualTopic.value = topic.value;

		if (topic.value || imagePath.value) {
			startRecognition(topic.value);
			return;
		}

		if (savedImageUrl.value) {
			status.value = 'idle';
			showManualInput.value = true;
			return;
		}

		showManualInput.value = true;
		status.value = 'idle';
		if (manualMode) {
			isManualSearch.value = true;
		}
	});

	onBeforeUnmount(() => {
		recognitionSeq += 1;
		if (autoContinueTimer) {
			clearTimeout(autoContinueTimer);
			autoContinueTimer = null;
		}
	});

	const retryRecognition = () => {
		if (status.value === 'loading') {
			return;
		}
		if (!previewImageSrc.value) {
			const manualText = String(manualTopic.value || topic.value || '').trim();
			if (isManualSearch.value && manualText) {
				startRecognition(manualText);
				return;
			}
			openOcrImagePicker({
				topic: topic.value,
				fromChat: fromChat.value
			});
			return;
		}
		isManualSearch.value = false;
		showManualInput.value = false;
		startRecognition('');
	};

	const openManualInput = () => {
		showManualInput.value = true;
		isManualSearch.value = true;
	};

	const toggleManual = () => {
		showManualInput.value = !showManualInput.value;
		if (showManualInput.value) {
			isManualSearch.value = true;
		}
	};

	const applyManualTopic = () => {
		if (status.value === 'loading') {
			return;
		}
		const normalizedTopic = String(manualTopic.value || '').trim();
		if (!normalizedTopic) {
			uni.showToast({ title: '请先输入题目关键词', icon: 'none' });
			return;
		}
		if (normalizedTopic.length > 200) {
			uni.showToast({ title: '题干最多 200 字', icon: 'none' });
			return;
		}
		isManualSearch.value = true;
		showManualInput.value = true;
		startRecognition(normalizedTopic);
	};

	const formatSimilarity = (value) => {
		const rate = Number(value);
		if (!Number.isFinite(rate) || rate <= 0) {
			return '';
		}
		return `${Math.round(Math.min(rate, 1) * 100)}% 相似`;
	};

	const getSimilarItemKey = (item, index) => {
		if (item?.questionId) {
			return `similar-${item.questionId}`;
		}
		if (item?.questionCode) {
			return `similar-${item.questionCode}`;
		}
		return `similar-${index}`;
	};

	const applySimilarIndex = (index) => {
		if (index < 0 || index >= questionList.value.length) {
			return;
		}
		currentIndex.value = index;
		syncCollectedState();
		syncNotebookState();
	};

	const openSimilarSheet = () => {
		if (!canSwitchSimilar.value) {
			uni.showToast({ title: '暂无更多相似题', icon: 'none' });
			return;
		}
		showSimilarSheet.value = true;
	};

	const closeSimilarSheet = () => {
		showSimilarSheet.value = false;
	};

	const selectSimilarQuestion = (index) => {
		applySimilarIndex(index);
		closeSimilarSheet();
	};

	const selectPrevSimilar = () => {
		if (currentIndex.value <= 0) {
			return;
		}
		applySimilarIndex(currentIndex.value - 1);
	};

	const selectNextSimilar = () => {
		if (currentIndex.value >= questionList.value.length - 1) {
			return;
		}
		applySimilarIndex(currentIndex.value + 1);
	};

	/** 对话页入口：识别成功后携带题干与 AI 讲解跳转 chat，继续多轮追问 */
	const continueToChatFromOcr = () => {
		if (autoContinueTimer) {
			clearTimeout(autoContinueTimer);
			autoContinueTimer = null;
		}
		if (status.value !== 'success') {
			uni.showToast({ title: '请先完成题目识别', icon: 'none' });
			return false;
		}
		const questionTitle = String(currentQuestion.value.title || '').trim();
		if (!questionTitle) {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return false;
		}
		const quota = checkAiChatQuota();
		if (!quota.ok && !quota.notReady) {
			showAiQuotaExhaustedModal();
			return false;
		}
		const tipText = displayTips.value.length
			? displayTips.value.join(' ')
			: displayAnalysis.value;
		appendSearchRecord({
			title: questionTitle,
			source: SEARCH_RECORD_SOURCES.AI,
			imageUrl: savedImageUrl.value
		});
		const chatUrl = buildChatPageUrl({ topic: questionTitle, tip: tipText });
		if (fromChat.value) {
			uni.redirectTo({ url: chatUrl });
		} else {
			go(chatUrl);
		}
		return true;
	};

	let autoContinueTimer = null;
	/** 从对话页拍照进入时，识别成功后自动进入对话（OCR+问答一条龙） */
	const scheduleAutoContinueToChat = () => {
		if (!fromChat.value || autoContinueTimer) {
			return;
		}
		uni.showToast({ title: '识别成功，进入对话', icon: 'none' });
		autoContinueTimer = setTimeout(() => {
			autoContinueTimer = null;
			continueToChatFromOcr();
		}, 800);
	};

	/** 再问 AI / 继续对话：写入搜题记录并跳转 chat，携带题干与 AI 讲解上下文 */
	const askAi = () => {
		continueToChatFromOcr();
	};

	const toggleCollect = async () => {
		if (collectLoading.value || status.value !== 'success') {
			return;
		}
		const question = currentQuestion.value;
		const questionId = getCollectionKey(question);
		if (!questionId) {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		collectLoading.value = true;
		try {
			const result = await toggleCollectionItem({
				...question,
				questionId,
				subject: resolveCollectionSubject(),
				rightAnswer: displayAnswer.value === '暂无标准答案' ? '' : question.answer,
				analysis: displayAnalysis.value === '暂无详细解析' ? '' : question.analysis,
				aiSummary: displayTips.value.join(' '),
				tags: [`#${getSearchSource()}`, '#OCR搜题']
			});
			isCollected.value = result.collected;
			uni.showToast({ title: result.collected ? '已收藏当前题目' : '已取消收藏', icon: 'none' });
		} catch (e) {
			uni.showToast({ title: e.msg || '收藏操作失败', icon: 'none' });
		} finally {
			collectLoading.value = false;
		}
	};

	/** OCR 识别成功后手动入库：无需答题，无 userAnswer/wrong */
	const addToNotebook = async () => {
		if (notebookLoading.value || status.value !== 'success') {
			return;
		}
		if (isInNotebook.value) {
			uni.showToast({ title: '已在错题本中', icon: 'none' });
			return;
		}
		const question = currentQuestion.value;
		const questionId = getCollectionKey(question);
		if (!questionId && !String(question.title || '').trim()) {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		notebookLoading.value = true;
		try {
			const payload = buildOcrNotebookPayload(question, {
				searchSource: getSearchSource(),
				subject: resolveCollectionSubject()
			});
			const result = await addNotebookItem(payload);
			if (result.duplicate) {
				isInNotebook.value = true;
				uni.showToast({ title: '已在错题本中', icon: 'none' });
				return;
			}
			isInNotebook.value = true;
			uni.showToast({ title: '已加入错题本', icon: 'none' });
		} catch (e) {
			uni.showToast({ title: e.msg || '加入错题本失败', icon: 'none' });
		} finally {
			notebookLoading.value = false;
		}
	};
</script>

<style lang="scss" scoped>
	.ocr-page {
		position: relative;
		min-height: 100vh;
		background: #f3f6fc;
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
		top: -80rpx;
		right: -60rpx;
		background: rgba(56, 109, 245, 0.22);
	}

	.bg-glow-b {
		width: 320rpx;
		height: 320rpx;
		left: -80rpx;
		bottom: 120rpx;
		background: rgba(34, 197, 94, 0.12);
	}

	.detail-screen {
		position: relative;
		z-index: 1;
		padding: 12rpx 24rpx calc(180rpx + env(safe-area-inset-bottom));
	}

	.hero-card,
	.manual-card,
	.question-card,
	.state-card,
	.analysis-card,
	.tips-panel {
		margin-top: 20rpx;
	}

	.hero-card {
		padding: 24rpx;
		border-radius: 32rpx;
		background: rgba(255, 255, 255, 0.96);
		border: 2rpx solid rgba(226, 232, 240, 0.9);
		box-shadow: 0 18rpx 38rpx rgba(37, 58, 96, 0.08);
		overflow: hidden;
	}

	.hero-copy {
		display: flex;
		flex-direction: column;
		gap: 10rpx;
	}

	.preview-wrap {
		position: relative;
		margin-bottom: 20rpx;
		width: 100%;
		height: 332rpx;
		border-radius: 26rpx;
		overflow: hidden;
		background: linear-gradient(180deg, #eef2ff 0%, #f8fafc 100%);
		box-shadow: inset 0 0 0 2rpx rgba(219, 228, 248, 0.8);
	}

	.preview-image {
		width: 100%;
		height: 100%;
	}

	.preview-mask {
		position: absolute;
		inset: 0;
		background: linear-gradient(180deg, rgba(15, 23, 42, 0) 32%, rgba(15, 23, 42, 0.2) 100%);
	}

	.preview-badge {
		position: absolute;
		left: 18rpx;
		bottom: 18rpx;
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(15, 23, 42, 0.52);
		backdrop-filter: blur(8rpx);
		color: #fff;
		font-size: 20rpx;
		font-weight: 700;
	}

	.capture-title {
		font-size: 38rpx;
		font-weight: 700;
		line-height: 1.35;
		color: var(--text-main);
	}

	.capture-sub {
		font-size: 24rpx;
		line-height: 1.65;
		color: #64748b;
	}

	.capture-actions,
	.state-actions {
		margin-top: 20rpx;
		display: flex;
		gap: 14rpx;
	}

	.error-actions-bar {
		margin-top: 16rpx;
	}

	.mini-action {
		flex: 1;
		height: 80rpx;
		border-radius: 24rpx;
		background: #f8fafc;
		border: 2rpx solid rgba(226, 232, 240, 0.92);
		color: var(--primary);
		font-size: 24rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
	}

	.mini-action.primary {
		background: linear-gradient(135deg, #386df5 0%, #4f46e5 100%);
		border-color: transparent;
		color: #fff;
		box-shadow: var(--shadow-button);
	}

	.mini-action.ghost {
		background: #f8fafc;
		color: #5f6979;
		border: 2rpx solid rgba(226, 232, 240, 0.95);
	}

	.manual-card {
		padding: 24rpx;
		border-radius: 30rpx;
	}

	.manual-head {
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: 16rpx;
	}

	.manual-title-wrap {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.manual-icon {
		width: 44rpx;
		height: 44rpx;
		border-radius: 14rpx;
		background: var(--primary-light);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.manual-title {
		font-size: var(--text-lg);
		font-weight: 800;
		color: #253143;
	}

	.manual-submit {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 12rpx 18rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #386df5 0%, #4f46e5 100%);
		color: #fff;
		font-size: var(--text-sm);
		font-weight: 700;
		box-shadow: var(--shadow-button);
	}

	.manual-submit.disabled {
		opacity: 0.45;
		box-shadow: none;
	}

	.manual-count {
		margin-top: 12rpx;
		display: block;
		text-align: right;
		font-size: var(--text-sm);
		color: #9aa3b1;
	}

	.manual-textarea {
		margin-top: 18rpx;
		width: 92%;
		min-height: 140rpx;
		padding: 24rpx;
		border-radius: 24rpx;
		background: linear-gradient(180deg, #fbfcfe 0%, #f8fafc 100%);
		border: 2rpx solid rgba(226, 232, 240, 0.95);
		font-size: var(--text-lg);
		line-height: 1.65;
		color: var(--text-main);
	}

	.loading-card,
	.error-card,
	.idle-card {
		display: flex;
		flex-direction: column;
		align-items: center;
		text-align: center;
		padding: 48rpx 30rpx;
		border-radius: 32rpx;
	}

	.state-visual {
		width: 96rpx;
		height: 96rpx;
		border-radius: 28rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.state-visual.idle {
		background: linear-gradient(145deg, rgba(56, 109, 245, 0.14) 0%, rgba(99, 102, 241, 0.08) 100%);
		border: 2rpx solid rgba(56, 109, 245, 0.12);
	}

	.state-visual.error {
		background: linear-gradient(145deg, #ef4444 0%, #f97316 100%);
		box-shadow: 0 16rpx 32rpx rgba(239, 68, 68, 0.22);
	}

	.loader-ring {
		width: 88rpx;
		height: 88rpx;
		border-radius: 50%;
		border: 6rpx solid rgba(56, 109, 245, 0.14);
		border-top-color: var(--primary);
		animation: spin 1s linear infinite;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.loader-core {
		width: 24rpx;
		height: 24rpx;
		border-radius: 50%;
		background: var(--primary);
		opacity: 0.35;
	}

	.loading-bar {
		margin-top: 28rpx;
		width: 100%;
		max-width: 420rpx;
		height: 8rpx;
		border-radius: 999rpx;
		background: rgba(56, 109, 245, 0.12);
		overflow: hidden;
	}

	.loading-bar-inner {
		width: 42%;
		height: 100%;
		border-radius: inherit;
		background: linear-gradient(90deg, #386df5 0%, #6366f1 100%);
		animation: slide 1.4s ease-in-out infinite;
	}

	.state-title {
		margin-top: 24rpx;
		font-size: 38rpx;
		font-weight: 700;
		color: var(--text-main);
	}

	.state-copy {
		margin-top: 14rpx;
		font-size: 24rpx;
		line-height: 1.7;
		color: #64748b;
	}

	.state-keyword {
		margin-top: 20rpx;
		padding: 16rpx 24rpx;
		border-radius: 20rpx;
		background: var(--error-light);
		font-size: var(--text-base);
		line-height: 1.6;
		color: var(--error);
		word-break: break-all;
	}

	.state-steps {
		margin-top: 32rpx;
		width: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
	}

	.step-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 10rpx;
	}

	.step-num {
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		background: var(--primary-light);
		color: var(--primary);
		font-size: 22rpx;
		font-weight: 800;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.step-text {
		font-size: 22rpx;
		color: #64748b;
	}

	.step-line {
		width: 48rpx;
		height: 2rpx;
		background: rgba(56, 109, 245, 0.18);
		margin-bottom: 28rpx;
	}

	.result-stack {
		display: flex;
		flex-direction: column;
		gap: 18rpx;
	}

	.question-card,
	.analysis-card {
		padding: 24rpx;
		border-radius: 30rpx;
	}

	.tips-panel {
		padding: 24rpx;
		border-radius: 30rpx;
		background: linear-gradient(180deg, #f0fdf4 0%, #ecfdf5 100%);
		border: 2rpx solid rgba(34, 197, 94, 0.1);
	}

	.card-head {
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: 16rpx;
	}

	.head-left {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.head-label {
		font-size: var(--text-base);
		color: #94a3b8;
		font-weight: 600;
	}

	.head-tag {
		padding: 4rpx 12rpx;
		border-radius: 999rpx;
		background: var(--primary-light);
		color: var(--primary);
		font-size: 20rpx;
		font-weight: 700;
	}

	.head-link-btn {
		display: inline-flex;
		align-items: center;
		gap: 4rpx;
		font-size: var(--text-base);
		font-weight: 700;
		color: var(--primary);
	}

	.head-link.disabled {
		font-size: var(--text-base);
		color: #b8c0cc;
		font-weight: 500;
	}

	.card-copy {
		margin-top: 18rpx;
		display: block;
		font-size: var(--text-xl);
		line-height: 1.7;
		color: var(--text-main);
		font-weight: 600;
	}

	.answer-card {
		padding: 26rpx 24rpx;
		border-radius: 30rpx;
		background: linear-gradient(135deg, #386df5 0%, #4f46e5 52%, #6366f1 100%);
		box-shadow: 0 20rpx 48rpx rgba(56, 109, 245, 0.24);
		color: #fff;
	}

	.answer-head {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.answer-icon {
		width: 40rpx;
		height: 40rpx;
		border-radius: 12rpx;
		background: rgba(255, 255, 255, 0.18);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.answer-label {
		font-size: var(--text-base);
		color: rgba(255, 255, 255, 0.82);
		font-weight: 600;
	}

	.answer-value {
		margin-top: 16rpx;
		display: block;
		font-size: 52rpx;
		font-weight: 800;
		line-height: 1.35;
		letter-spacing: 0.5rpx;
	}

	.analysis-head,
	.tips-header {
		display: flex;
		align-items: center;
		gap: 14rpx;
	}

	.analysis-icon,
	.tips-icon {
		width: 44rpx;
		height: 44rpx;
		border-radius: 14rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.analysis-icon {
		background: var(--primary-light);
	}

	.tips-icon {
		background: linear-gradient(145deg, #16a34a 0%, #22c55e 100%);
		box-shadow: 0 10rpx 20rpx rgba(34, 197, 94, 0.22);
	}

	.analysis-title {
		font-size: var(--text-xl);
		font-weight: 800;
		color: var(--text-main);
	}

	.analysis-copy {
		margin-top: 16rpx;
		display: block;
		font-size: var(--text-lg);
		line-height: 1.75;
		color: #475569;
	}

	.tips-head-copy {
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}

	.tips-title {
		font-size: var(--text-xl);
		font-weight: 800;
		color: #166534;
	}

	.tips-sub {
		font-size: 22rpx;
		color: #6b8f78;
	}

	.tips-list {
		margin-top: 22rpx;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.tip-item {
		display: flex;
		align-items: flex-start;
		gap: 14rpx;
		padding: 18rpx;
		border-radius: 20rpx;
		background: rgba(255, 255, 255, 0.72);
		border: 2rpx solid rgba(34, 197, 94, 0.12);
	}

	.tip-index {
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background: rgba(34, 197, 94, 0.14);
		color: #16a34a;
		font-size: 20rpx;
		font-weight: 800;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.tip-text {
		flex: 1;
		font-size: var(--text-lg);
		line-height: 1.75;
		color: #2b5b40;
	}

	.tips-empty {
		margin-top: 22rpx;
		display: block;
		font-size: var(--text-lg);
		line-height: 1.75;
		color: #6b8f78;
	}

	.bottom-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 10;
		padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
		background: linear-gradient(180deg, rgba(243, 246, 252, 0) 0%, rgba(243, 246, 252, 0.94) 28%, #f3f6fc 100%);
	}

	.bottom-actions {
		display: flex;
		align-items: center;
		gap: 12rpx;
		padding: 12rpx;
		border-radius: 28rpx;
		background: rgba(255, 255, 255, 0.94);
		border: 2rpx solid rgba(226, 232, 240, 0.92);
		box-shadow: 0 18rpx 48rpx rgba(36, 48, 65, 0.1);
		backdrop-filter: blur(16rpx);
	}

	.action-chip {
		flex: 1;
		height: 84rpx;
		border-radius: 22rpx;
		background: #f8fafc;
		border: 2rpx solid rgba(226, 232, 240, 0.92);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
		font-size: 20rpx;
		color: #64748b;
		font-weight: 600;
	}

	.action-chip.active {
		background: var(--primary-light);
		border-color: rgba(56, 109, 245, 0.18);
		color: var(--primary);
	}

	.action-chip.disabled {
		opacity: 0.72;
	}

	.primary-cta {
		flex: 1.35;
		height: 84rpx;
		border-radius: 22rpx;
		background: linear-gradient(135deg, #386df5 0%, #4f46e5 100%);
		box-shadow: var(--shadow-button);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		color: #fff;
		font-size: var(--text-base);
		font-weight: 800;
	}

	@keyframes spin {
		from {
			transform: rotate(0deg);
		}

		to {
			transform: rotate(360deg);
		}
	}

	@keyframes slide {
		0% {
			transform: translateX(-120%);
		}

		100% {
			transform: translateX(320%);
		}
	}

	.similar-sheet {
		padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
		max-height: 72vh;
		display: flex;
		flex-direction: column;
		background: #fff;
	}

	.similar-head {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.similar-title {
		font-size: 34rpx;
		font-weight: 800;
		color: var(--text-main);
	}

	.similar-sub {
		font-size: var(--text-base);
		color: #64748b;
	}

	.similar-scroll {
		margin-top: 24rpx;
		max-height: 48vh;
	}

	.similar-item {
		margin-bottom: 16rpx;
		padding: 24rpx;
		border-radius: 24rpx;
		background: #f8fafc;
		border: 2rpx solid transparent;
	}

	.similar-item.active {
		background: var(--primary-light);
		border-color: rgba(56, 109, 245, 0.28);
	}

	.similar-item-head {
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: 16rpx;
	}

	.similar-order {
		font-size: var(--text-base);
		font-weight: 800;
		color: var(--primary);
	}

	.similar-rate {
		font-size: var(--text-sm);
		color: #16a34a;
		font-weight: 700;
	}

	.similar-copy {
		margin-top: 12rpx;
		display: block;
		font-size: var(--text-lg);
		line-height: 1.7;
		color: var(--text-main);
	}

	.similar-current {
		margin-top: 12rpx;
		display: inline-block;
		padding: 6rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(56, 109, 245, 0.12);
		font-size: var(--text-sm);
		color: var(--primary);
		font-weight: 700;
	}

	.similar-actions {
		margin-top: 24rpx;
		display: flex;
		gap: 16rpx;
	}

	.similar-actions .ghost-button,
	.similar-actions .solid-button {
		flex: 1;
	}

	.similar-actions .disabled {
		opacity: 0.45;
	}

	@media screen and (max-width: 375px) {
		.hero-card,
		.manual-card,
		.question-card,
		.state-card,
		.analysis-card,
		.tips-panel,
		.answer-card {
			padding-left: 20rpx;
			padding-right: 20rpx;
		}

		.preview-wrap {
			height: 304rpx;
		}

		.capture-title,
		.state-title {
			font-size: 34rpx;
		}

		.capture-actions,
		.state-actions {
			flex-direction: column;
		}

		.bottom-actions {
			gap: 10rpx;
		}
	}

	/* #ifdef MP-WEIXIN */
	.preview-badge {
		background: rgba(15, 23, 42, 0.66);
		backdrop-filter: none;
	}

	.bottom-actions {
		background: rgba(255, 255, 255, 1);
		backdrop-filter: none;
	}

	.capture-title,
	.state-title,
	.manual-title,
	.card-copy,
	.similar-title,
	.similar-copy {
		color: #243041;
	}

	.mini-action,
	.head-link-btn,
	.action-chip.active,
	.similar-order,
	.similar-current {
		color: #386df5;
	}

	.mini-action.primary,
	.manual-submit,
	.primary-cta {
		box-shadow: 0 12rpx 24rpx rgba(56, 109, 245, 0.18);
	}

	.manual-icon,
	.step-num,
	.head-tag,
	.action-chip.active,
	.similar-item.active {
		background: #eef4ff;
	}

	.manual-title,
	.manual-submit,
	.manual-count,
	.manual-textarea,
	.state-keyword,
	.head-label,
	.head-link-btn,
	.head-link.disabled,
	.primary-cta,
	.similar-sub,
	.similar-order,
	.similar-rate,
	.similar-current {
		font-size: 24rpx;
	}

	.manual-textarea {
		color: #243041;
	}

	.loader-ring {
		border-top-color: #386df5;
	}

	.loader-core {
		background: #386df5;
	}

	.state-keyword {
		background: #ffefef;
		color: #ef4444;
	}

	.similar-title,
	.card-copy {
		font-size: 30rpx;
	}
	/* #endif */
</style>
