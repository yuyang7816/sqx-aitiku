<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen notebook-screen">
				<view class="chip-panel">
					<view class="subject-selector" @tap="openSubjectPicker">
						<text class="subject-selector-label">{{ activeChip }}</text>
						<up-icon name="arrow-down" size="14" color="#697386"></up-icon>
					</view>
					<view class="top-actions">
						<view class="icon-btn filter-trigger" @tap="openFilterPopup">
							<up-icon name="list-dot" size="18" color="#4E88F6"></up-icon>
						</view>
					</view>
				</view>

				<view v-if="list.length" class="summary-bar">
					<view class="summary-item">
						<text class="summary-value orange">{{ pendingCount }}</text>
						<text class="summary-label">待复习</text>
					</view>
					<view class="summary-divider"></view>
					<view class="summary-item">
						<text class="summary-value blue">{{ masteryRateText }}</text>
						<text class="summary-label">掌握率</text>
					</view>
					<view class="summary-divider"></view>
					<view class="summary-item">
						<text class="summary-value">{{ list.length }}</text>
						<text class="summary-label">累计错题</text>
					</view>
				</view>

				<view v-if="syncBannerText" class="sync-banner" :class="syncBannerClass">
					<text class="sync-banner-text">{{ syncBannerText }}</text>
					<text v-if="syncState === 'error'" class="sync-banner-action" @tap="refreshNotebookList">重试</text>
				</view>

				<view v-if="listLoading && !filteredList.length" class="list-loading">
					<up-loading-icon mode="circle" size="22" color="#386df5"></up-loading-icon>
					<text>正在加载错题本…</text>
				</view>

				<view v-else-if="filteredList.length && !exportMode" class="batch-toolbar">
					<view class="batch-action primary" @tap="batchRedoAll">
						<up-icon name="reload" size="14" color="#386df5"></up-icon>
						<text>全选重做</text>
					</view>
					<view class="batch-action" :class="{ active: batchMode }" @tap="toggleBatchMode">
						<up-icon name="checkmark-circle" size="14" :color="batchMode ? '#386df5' : '#697386'"></up-icon>
						<text>{{ batchMode ? '取消批量' : '批量删除' }}</text>
					</view>
				</view>

				<view v-if="exportMode && filteredList.length" class="export-hint-bar">
					<up-icon name="share-fill" size="14" color="#386df5"></up-icon>
					<text class="export-hint-text">在下方列表勾选要导出的题目，再确认导出设置</text>
					<text class="export-hint-cancel" @tap="exitExportMode">取消</text>
				</view>

				<view v-if="filteredList.length" class="note-list">
					<view
						v-for="item in filteredList"
						:key="item.id || item.question"
						class="note-item"
						:class="{
							'batch-mode': batchMode || exportMode,
							selected: batchMode ? isItemSelected(item) : (exportMode && isExportItemSelected(item))
						}"
						@tap="onNoteItemTap(item)"
					>
						<view v-if="batchMode || exportMode" class="item-check" @tap.stop="onItemCheckTap(item)">
							<view
								class="check-box"
								:class="{ checked: batchMode ? isItemSelected(item) : isExportItemSelected(item) }"
							>
								<up-icon
									v-if="batchMode ? isItemSelected(item) : isExportItemSelected(item)"
									name="checkmark"
									size="12"
									color="#FFFFFF"
								></up-icon>
							</view>
						</view>
						<notebook-card class="note-card-host" :item="item">
						<template #actions>
							<view v-if="!batchMode && !exportMode" class="btn-row">
								<view class="pill-btn primary" @tap="redoItem(item)">
									<up-icon name="reload" size="14" color="#FFFFFF"></up-icon>
									<text>重做</text>
								</view>
								<view class="pill-btn" @tap="openAnalysis(item)">
									<up-icon name="file-text" size="14" color="#5d6777"></up-icon>
									<text>解析</text>
								</view>
								<view class="pill-btn green" @tap="askAi(item)">
									<up-icon name="chat" size="14" color="#1caa55"></up-icon>
									<text>AI讲解</text>
								</view>
								<view class="pill-btn status" :class="item.mastered ? 'reopen' : 'done'" @tap="toggleMastered(item)">
									<up-icon :name="item.mastered ? 'reload' : 'checkmark'" size="14" :color="item.mastered ? '#d97706' : '#16a34a'"></up-icon>
									<text>{{ item.mastered ? '标记待复习' : '标记已掌握' }}</text>
								</view>
							</view>
						</template>
						</notebook-card>
					</view>
				</view>
				<app-empty
					v-else-if="!listLoading"
					class="notebook-empty card"
					:content="emptyContent"
					:sub-content="emptySubContent"
					mode="data"
					padding-top="80rpx"
					:button-text="emptyButtonText"
					button-icon="arrow-right"
					@button="goPractice"
				></app-empty>

			</view>
		</view>
		<view v-if="batchMode && filteredList.length" class="batch-bar">
			<view class="batch-bar-left" @tap="toggleSelectAll">
				<view class="check-box" :class="{ checked: allFilteredSelected }">
					<up-icon v-if="allFilteredSelected" name="checkmark" size="12" color="#FFFFFF"></up-icon>
				</view>
				<text>{{ allFilteredSelected ? '取消全选' : '全选' }}</text>
			</view>
			<text class="batch-count">已选 {{ selectedCount }} 题</text>
			<view class="batch-redo-btn" :class="{ disabled: !selectedCount }" @tap="batchRedoSelected">
				重做
			</view>
			<view class="batch-delete-btn" :class="{ disabled: !selectedCount }" @tap="batchDeleteSelected">
				删除
			</view>
		</view>
		<view v-if="exportMode && filteredList.length" class="batch-bar export-bar">
			<view class="batch-bar-left" @tap="toggleExportSelectAll">
				<view class="check-box" :class="{ checked: exportAllSelected }">
					<up-icon v-if="exportAllSelected" name="checkmark" size="12" color="#FFFFFF"></up-icon>
				</view>
				<text>{{ exportAllSelected ? '取消全选' : '全选' }}</text>
			</view>
			<text class="batch-count">已选 {{ exportSelectedCount }} 题</text>
			<view class="batch-settings-btn" @tap="openExportPopup">
				导出设置
			</view>
			<view class="batch-export-btn" :class="{ disabled: !exportSelectedCount }" @tap="confirmExport">
				确认导出
			</view>
		</view>
		<up-popup
			:show="showSubjectPicker"
			mode="bottom"
			round="20"
			:close-on-click-overlay="true"
			@close="closeSubjectPicker"
		>
			<view class="subject-picker-sheet">
				<view class="sheet-head">
					<view>
						<text class="sheet-title">选择筛选条件</text>
						<text class="sheet-sub">按学科、时间或频率筛选错题</text>
					</view>
					<view class="sheet-close" @tap="closeSubjectPicker">
						<up-icon name="close" size="16" color="#667085"></up-icon>
					</view>
				</view>
				<scroll-view scroll-y class="subject-picker-scroll">
					<view
						v-for="item in chips"
						:key="item"
						class="subject-picker-item"
						:class="{ active: item === activeChip }"
						@tap="selectChipAndClose(item)"
					>
						<text class="subject-picker-name">{{ item }}</text>
						<up-icon v-if="item === activeChip" name="checkmark" size="16" color="#386df5"></up-icon>
					</view>
				</scroll-view>
			</view>
		</up-popup>
		<up-popup
			:show="showFilterPopup"
			mode="bottom"
			round="20"
			:close-on-click-overlay="true"
			@close="closeFilterPopup"
		>
			<view class="filter-sheet">
				<view class="sheet-head">
					<view>
						<text class="sheet-title">筛选与操作</text>
						<text class="sheet-sub">可导出当前筛选结果、删除当前筛选结果，或按日期范围进一步筛选</text>
					</view>
					<view class="sheet-close" @tap="closeFilterPopup">
						<up-icon name="close" size="16" color="#667085"></up-icon>
					</view>
				</view>

				<view class="sheet-actions">
					<view class="sheet-action-card" @tap="handlePopupPhotoAdd">
						<up-icon name="camera-fill" size="18" color="#28B66A"></up-icon>
						<text>拍照加错题</text>
					</view>
					<view class="sheet-action-card" @tap="handlePopupExport">
						<up-icon name="share-fill" size="18" color="#4E88F6"></up-icon>
						<text>分享</text>
					</view>
					<view class="sheet-action-card danger" @tap="handlePopupClear">
						<up-icon name="trash-fill" size="18" color="#FF5E66"></up-icon>
						<text>删除当前筛选</text>
					</view>
				</view>

				<view class="date-filter-card">
					
					<view class="date-picker-row">
						<view class="date-picker-trigger" @tap="openStartDatePicker">
							<view class="date-picker-box">
								<text class="date-picker-label">开始日期</text>
								<text class="date-picker-value">{{ dateStart || '请选择' }}</text>
							</view>
						</view>
						<view class="date-picker-trigger" @tap="openEndDatePicker">
							<view class="date-picker-box">
								<text class="date-picker-label">结束日期</text>
								<text class="date-picker-value">{{ dateEnd || '请选择' }}</text>
							</view>
						</view>
					</view>
					<view class="date-filter-actions">
						<view class="sheet-secondary-btn" @tap="resetDateFilter">
							<text>清空日期</text>
						</view>
						<view class="sheet-primary-btn" @tap="applyDateFilter">
							<text>应用筛选</text>
						</view>
					</view>
				</view>
			</view>
		</up-popup>
		<up-popup
			:show="showExportPopup"
			mode="bottom"
			round="20"
			:close-on-click-overlay="true"
			@close="closeExportPopup"
		>
			<view class="filter-sheet export-sheet">
				<view class="sheet-head">
					<view>
						<text class="sheet-title">导出设置</text>
						<text class="sheet-sub">题目请在列表中勾选，此处设置导出学科与是否含答案解析</text>
					</view>
					<view class="sheet-close" @tap="closeExportPopup">
						<up-icon name="close" size="16" color="#667085"></up-icon>
					</view>
				</view>

				<view class="export-option-block">
					<text class="export-option-label">导出学科</text>
					<text class="export-option-tip export-option-tip--inline">不选则包含全部学科</text>
					<scroll-view scroll-x show-scrollbar="false" class="export-subject-scroll">
						<view class="export-subject-tabs">
							<view
								v-for="item in exportSubjectOptions"
								:key="item"
								class="export-subject-tab"
								:class="{ active: exportSubjects.includes(item) }"
								@tap="toggleExportSubject(item)"
							>
								<text class="export-subject-name">{{ item }}</text>
							</view>
						</view>
					</scroll-view>
				</view>

				<view class="export-option-block">
					<text class="export-option-label">答案与解析</text>
					<view class="export-radio-group">
						<view
							class="export-radio-row"
							:class="{ active: !exportIncludeAnswerAnalysis }"
							@tap="exportIncludeAnswerAnalysis = false"
						>
							<view class="radio-circle" :class="{ checked: !exportIncludeAnswerAnalysis }">
								<view v-if="!exportIncludeAnswerAnalysis" class="radio-circle-dot"></view>
							</view>
							<view class="export-radio-copy">
								<text class="export-radio-title">不含答案与解析</text>
								<text class="export-radio-sub">模拟考试做题</text>
							</view>
						</view>
						<view
							class="export-radio-row"
							:class="{ active: exportIncludeAnswerAnalysis }"
							@tap="exportIncludeAnswerAnalysis = true"
						>
							<view class="radio-circle" :class="{ checked: exportIncludeAnswerAnalysis }">
								<view v-if="exportIncludeAnswerAnalysis" class="radio-circle-dot"></view>
							</view>
							<view class="export-radio-copy">
								<text class="export-radio-title">包含答案与解析</text>
								<text class="export-radio-sub">错题分析复盘</text>
							</view>
						</view>
					</view>
				</view>

				<text class="export-option-tip">已选 {{ exportSelectedCount }} / {{ filteredList.length }} 题</text>

				<view class="date-filter-actions">
					<view class="sheet-secondary-btn" @tap="closeExportPopup">
						<text>返回</text>
					</view>
					<view class="sheet-primary-btn" :class="{ disabled: !exportSelectedCount }" @tap="confirmExport">
						<text>确认导出</text>
					</view>
				</view>
			</view>
		</up-popup>
		<view class="light-picker-theme" data-up-theme="light">
			<up-datetime-picker
				:show="showStartDatePicker"
				mode="date"
				:title="'选择开始日期'"
				:model-value="startDateValue"
				@close="closeStartDatePicker"
				@cancel="closeStartDatePicker"
				@confirm="onStartDateConfirm"
			></up-datetime-picker>
			<up-datetime-picker
				:show="showEndDatePicker"
				mode="date"
				:title="'选择结束日期'"
				:model-value="endDateValue"
				@close="closeEndDatePicker"
				@cancel="closeEndDatePicker"
				@confirm="onEndDateConfirm"
			></up-datetime-picker>
		</view>
		<app-tabbar current="notebook"></app-tabbar>
	</view>
</template>

<script setup>	import AppTabbar from '@/components/app-tabbar/app-tabbar.vue';
	import NotebookCard from '@/components/notebook-card/notebook-card.vue';
	import { computed, ref, watch } from 'vue';
	import { storeToRefs } from 'pinia';
	import { onShow } from '@dcloudio/uni-app';
	import {
		NOTEBOOK_CHIP_ALL_SUBJECTS,
		buildNotebookBatchRedoSource,
		buildReviewItemFromNotebook,
		canSyncNotebookToApi,
		checkAiChatQuota,
		clearNotebookFilteredItems,
		deleteNotebookItemsByKeys,
		formatNotebookItemSubject,
		getNotebookChips,
		getNotebookFilteredList,
		getNotebookItemKey,
		getNotebookMasteryRateText,
		getNotebookPendingCount,
		resolveNotebookQuestionToken,
		setPracticeSource,
		setQuestionReviewItem,
		showAiQuotaExhaustedModal,
		syncNotebookListFromApi,
		toggleNotebookMastered
	} from '@/common/app-data';
	import { buildChatPageUrl, buildNotebookChatEntry } from '@/common/chat-entry';
	import { openOcrImagePicker } from '@/common/ocr-camera';
	import {
		NOTEBOOK_EXPORT_PAGE,
		buildNotebookExportPayload,
		setNotebookExportPayload
	} from '@/common/notebook-export';
	import { NOTEBOOK_REDO_TITLE, PRACTICE_TAB_PAGE, QUESTION_ANALYSIS_PAGE, QUESTION_ANSWER_PAGE } from '@/common/routes';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import { useTabPageState } from '@/common/use-tab-page-state';
	import { useUserStore } from '@/stores/user';
	import { useNotebookStore } from '@/stores/notebook';

	const userStore = useUserStore();
	const notebookStore = useNotebookStore();
	const { profile } = storeToRefs(userStore);
	const { list } = storeToRefs(notebookStore);
	const chips = computed(() => getNotebookChips(profile.value));
	const activeChip = ref(NOTEBOOK_CHIP_ALL_SUBJECTS);
	const { restoreScrollAfterReady } = useTabPageState('notebook', {
		activeChip,
		deferScrollRestore: true
	});
	const batchMode = ref(false);
	const exportMode = ref(false);
	const selectedKeys = ref([]);
	const listLoading = ref(false);
	const syncState = ref('idle');
	const showFilterPopup = ref(false);
	const showExportPopup = ref(false);
	const showSubjectPicker = ref(false);
	const showStartDatePicker = ref(false);
	const showEndDatePicker = ref(false);
	const dateStart = ref('');
	const dateEnd = ref('');
	const exportSubjects = ref([]);
	const exportIncludeAnswerAnalysis = ref(true);
	const exportSelectedKeys = ref([]);


	const normalizeDateOnly = (value = '') => String(value || '').trim().slice(0, 10);
	const formatPickerDate = (value) => {
		const date = new Date(value || Date.now());
		if (Number.isNaN(date.getTime())) {
			return '';
		}
		const pad = (num) => String(num).padStart(2, '0');
		return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
	};
	const startDateValue = computed(() => dateStart.value ? new Date(`${dateStart.value}T00:00:00`).getTime() : Date.now());
	const endDateValue = computed(() => dateEnd.value ? new Date(`${dateEnd.value}T00:00:00`).getTime() : Date.now());
	const chipFilteredList = computed(() => getNotebookFilteredList(activeChip.value, list.value, profile.value));
	const filteredList = computed(() => {
		const start = normalizeDateOnly(dateStart.value);
		const end = normalizeDateOnly(dateEnd.value);
		if (!start && !end) {
			return chipFilteredList.value;
		}
		return chipFilteredList.value.filter((item) => {
			const date = normalizeDateOnly(item.lastPracticeAt || item.date);
			if (!date) {
				return false;
			}
			if (start && date < start) {
				return false;
			}
			if (end && date > end) {
				return false;
			}
			return true;
		});
	});
	const filteredItemKeys = computed(() => filteredList.value.map((item) => getNotebookItemKey(item)).filter(Boolean));
	const selectedCount = computed(() => selectedKeys.value.length);
	const allFilteredSelected = computed(() => (
		filteredItemKeys.value.length > 0
		&& filteredItemKeys.value.every((key) => selectedKeys.value.includes(key))
	));
	const hasDateFilter = computed(() => Boolean(normalizeDateOnly(dateStart.value) || normalizeDateOnly(dateEnd.value)));
	const exportSubjectOptions = computed(() => {
		const fromProfile = Array.isArray(profile.value?.subjects) ? profile.value.subjects : [];
		const fromList = list.value.map((item) => formatNotebookItemSubject(item)).filter(Boolean);
		return [...new Set([...fromProfile, ...fromList])];
	});
	const exportSelectedCount = computed(() => exportSelectedKeys.value.length);
	const exportAllSelected = computed(() => (
		filteredList.value.length > 0
		&& filteredList.value.every((item) => exportSelectedKeys.value.includes(getNotebookItemKey(item)))
	));
	const pendingCount = computed(() => getNotebookPendingCount(list.value));
	const masteryRateText = computed(() => getNotebookMasteryRateText(list.value));
	const syncBannerText = computed(() => {
		if (syncState.value === 'loading') {
			return '正在加载错题本…';
		}
		if (syncState.value === 'error') {
			return '错题本加载失败，请检查网络后重试';
		}
		if (!canSyncNotebookToApi()) {
			return '登录后可查看云端错题本';
		}
		return '';
	});
	const syncBannerClass = computed(() => {
		if (syncState.value === 'loading') {
			return 'sync-banner--loading';
		}
		if (syncState.value === 'error') {
			return 'sync-banner--error';
		}
		return 'sync-banner--info';
	});

	const resetBatchSelection = () => {
		selectedKeys.value = [];
	};

	const exitBatchMode = () => {
		batchMode.value = false;
		resetBatchSelection();
	};

	const exitExportMode = () => {
		exportMode.value = false;
		exportSelectedKeys.value = [];
		showExportPopup.value = false;
	};

	const enterExportMode = () => {
		exitBatchMode();
		exportMode.value = true;
		resetExportSelection();
	};

	watch(activeChip, () => {
		resetBatchSelection();
		if (exportMode.value) {
			resetExportSelection();
		}
	});

	watch([dateStart, dateEnd], () => {
		resetBatchSelection();
		if (exportMode.value) {
			resetExportSelection();
		}
	});

	const refreshNotebookList = async () => {
		listLoading.value = true;
		syncState.value = 'loading';
		try {
			const synced = await syncNotebookListFromApi();
			if (!canSyncNotebookToApi()) {
				syncState.value = 'idle';
				return;
			}
			if (synced.fromApi) {
				syncState.value = 'synced';
			} else if (synced.error) {
				syncState.value = 'error';
				uni.showToast({
					title: synced.error.msg || synced.error.message || '错题本加载失败',
					icon: 'none'
				});
			} else {
				syncState.value = 'error';
			}
		} catch (e) {
			syncState.value = 'error';
		} finally {
			listLoading.value = false;
			restoreScrollAfterReady();
		}
	};

	onShow(() => {
		if (!chips.value.includes(activeChip.value)) {
			activeChip.value = NOTEBOOK_CHIP_ALL_SUBJECTS;
		}
		refreshNotebookList();
	});

	const navbarTitle = computed(() => `错题本（${filteredList.value.length}题）`);
	usePageNavigationTitle(navbarTitle);

	/** 错题本为空：引导去刷题；仅筛选无结果：提示切换筛选 */
	const isNotebookEmpty = computed(() => list.value.length === 0);
	const emptyContent = computed(() => (
		isNotebookEmpty.value ? '暂无错题' : '当前筛选下暂无错题'
	));
	const emptySubContent = computed(() => (
		isNotebookEmpty.value
			? '答错的题目会自动收录到这里，先去刷题练一练吧'
			: '试试切换学科筛选或时间筛选'
	));
	const emptyButtonText = computed(() => (isNotebookEmpty.value ? '去刷题' : ''));

	const goPractice = () => {
		go(PRACTICE_TAB_PAGE);
	};

	const isItemSelected = (item) => selectedKeys.value.includes(getNotebookItemKey(item));

	const toggleItemSelect = (item) => {
		const key = getNotebookItemKey(item);
		if (!key) {
			return;
		}
		if (selectedKeys.value.includes(key)) {
			selectedKeys.value = selectedKeys.value.filter((entry) => entry !== key);
			return;
		}
		selectedKeys.value = [...selectedKeys.value, key];
	};

	const onNoteItemTap = (item) => {
		if (batchMode.value) {
			toggleItemSelect(item);
			return;
		}
		if (exportMode.value) {
			toggleExportItem(item);
		}
	};

	const onItemCheckTap = (item) => {
		if (batchMode.value) {
			toggleItemSelect(item);
			return;
		}
		if (exportMode.value) {
			toggleExportItem(item);
		}
	};

	const toggleSelectAll = () => {
		if (allFilteredSelected.value) {
			resetBatchSelection();
			return;
		}
		selectedKeys.value = [...filteredItemKeys.value];
	};

	const toggleBatchMode = () => {
		if (batchMode.value) {
			exitBatchMode();
			return;
		}
		if (exportMode.value) {
			exitExportMode();
		}
		if (!filteredList.value.length) {
			uni.showToast({ title: '当前筛选下暂无题目', icon: 'none' });
			return;
		}
		batchMode.value = true;
		resetBatchSelection();
	};

	/** 全选重做：当前 Chip 筛选下全部错题进入 mode=wrong 练习 */
	const batchRedoAll = () => {
		const count = filteredList.value.length;
		if (!count) {
			uni.showToast({ title: '当前筛选下暂无题目', icon: 'none' });
			return;
		}
		exitBatchMode();
		setPracticeSource(buildNotebookBatchRedoSource(
			activeChip.value,
			hasDateFilter.value ? filteredItemKeys.value : [],
			profile.value
		));
		go(QUESTION_ANSWER_PAGE);
	};

	/** 批量重做：仅重做当前筛选内已勾选题 */
	const batchRedoSelected = () => {
		const keys = selectedKeys.value.filter((key) => filteredItemKeys.value.includes(key));
		if (!keys.length) {
			uni.showToast({ title: '请先选择要重做的错题', icon: 'none' });
			return;
		}
		exitBatchMode();
		setPracticeSource(buildNotebookBatchRedoSource(
			activeChip.value,
			keys,
			profile.value
		));
		go(QUESTION_ANSWER_PAGE);
	};

	/** 批量删除：仅删除当前筛选内已勾选题 */
	const batchDeleteSelected = () => {
		const keys = selectedKeys.value.filter((key) => filteredItemKeys.value.includes(key));
		if (!keys.length) {
			uni.showToast({ title: '请先选择要删除的错题', icon: 'none' });
			return;
		}
		const chipLabel = activeChip.value;
		uni.showModal({
			title: '批量删除',
			content: `确定删除「${chipLabel}」下已选的 ${keys.length} 道错题吗？未选中的错题将保留。`,
			confirmText: '删除',
			confirmColor: '#FF5E66',
			cancelText: '取消',
			success: async (res) => {
				if (!res.confirm) {
					return;
				}
				list.value = (await deleteNotebookItemsByKeys(keys, list.value))
					.map((entry) => ({ ...entry }));
				exitBatchMode();
				uni.showToast({ title: '已删除选中错题', icon: 'none' });
			}
		});
	};

	const selectChip = (chip) => {
		activeChip.value = chip;
	};

	const openSubjectPicker = () => {
		showSubjectPicker.value = true;
	};

	const closeSubjectPicker = () => {
		showSubjectPicker.value = false;
	};

	const selectChipAndClose = (chip) => {
		selectChip(chip);
		closeSubjectPicker();
	};

	const openFilterPopup = () => {
		showFilterPopup.value = true;
	};

	const closeFilterPopup = () => {
		showFilterPopup.value = false;
	};

	const openStartDatePicker = () => {
		showStartDatePicker.value = true;
	};

	const closeStartDatePicker = () => {
		showStartDatePicker.value = false;
	};

	const openEndDatePicker = () => {
		showEndDatePicker.value = true;
	};

	const closeEndDatePicker = () => {
		showEndDatePicker.value = false;
	};

	const onStartDateConfirm = (event) => {
		dateStart.value = formatPickerDate(event?.value);
		closeStartDatePicker();
	};

	const onEndDateConfirm = (event) => {
		dateEnd.value = formatPickerDate(event?.value);
		closeEndDatePicker();
	};

	const resetDateFilter = () => {
		dateStart.value = '';
		dateEnd.value = '';
	};

	const applyDateFilter = () => {
		const start = normalizeDateOnly(dateStart.value);
		const end = normalizeDateOnly(dateEnd.value);
		if (start && end && start > end) {
			uni.showToast({ title: '开始日期不能晚于结束日期', icon: 'none' });
			return;
		}
		closeFilterPopup();
		uni.showToast({ title: hasDateFilter.value ? '已应用日期筛选' : '已清空日期筛选', icon: 'none' });
	};

	/** 导出错题：支持选择题目、学科、是否带答案解析 */
	const exportNotebook = ({
		items = [],
		subjects = [],
		includeAnswerAnalysis = true,
	} = {}) => {
		const selectedItems = Array.isArray(items) && items.length ? items : filteredList.value;
		if (!selectedItems.length) {
			uni.showToast({ title: '请至少选择一题', icon: 'none' });
			return;
		}
		exitBatchMode();
		exitExportMode();
		const payload = buildNotebookExportPayload({
			chip: activeChip.value,
			list: list.value,
			profile: profile.value,
			items: selectedItems,
			subjects,
			includeAnswerAnalysis,
		});
		if (!payload.items.length) {
			uni.showToast({ title: '没有符合导出条件的题目', icon: 'none' });
			return;
		}
		setNotebookExportPayload(payload);
		go(NOTEBOOK_EXPORT_PAGE);
	};

	const resetExportSelection = () => {
		exportSelectedKeys.value = filteredItemKeys.value.slice();
	};

	const openExportPopup = () => {
		if (!filteredList.value.length) {
			uni.showToast({ title: '当前筛选下暂无题目', icon: 'none' });
			return;
		}
		if (!exportMode.value) {
			enterExportMode();
			exportSubjects.value = [];
			exportIncludeAnswerAnalysis.value = true;
		}
		showExportPopup.value = true;
	};

	const closeExportPopup = () => {
		showExportPopup.value = false;
	};

	const toggleExportSubject = (subject) => {
		const value = String(subject || '').trim();
		if (!value) {
			return;
		}
		if (exportSubjects.value.includes(value)) {
			exportSubjects.value = exportSubjects.value.filter((item) => item !== value);
			return;
		}
		exportSubjects.value = [...exportSubjects.value, value];
	};

	const isExportItemSelected = (item) => exportSelectedKeys.value.includes(getNotebookItemKey(item));

	const toggleExportItem = (item) => {
		const key = getNotebookItemKey(item);
		if (!key) {
			return;
		}
		if (exportSelectedKeys.value.includes(key)) {
			exportSelectedKeys.value = exportSelectedKeys.value.filter((entry) => entry !== key);
			return;
		}
		exportSelectedKeys.value = [...exportSelectedKeys.value, key];
	};

	const toggleExportSelectAll = () => {
		if (exportAllSelected.value) {
			exportSelectedKeys.value = [];
			return;
		}
		resetExportSelection();
	};

	const confirmExport = () => {
		if (!exportSelectedCount.value) {
			uni.showToast({ title: '请至少选择一题', icon: 'none' });
			return;
		}
		const selectedItems = filteredList.value.filter((item) => exportSelectedKeys.value.includes(getNotebookItemKey(item)));
		closeExportPopup();
		exportNotebook({
			items: selectedItems,
			subjects: exportSubjects.value,
			includeAnswerAnalysis: exportIncludeAnswerAnalysis.value,
		});
	};

	/** 清空当前筛选：确认后仅删除 filteredList 命中项，其余错题保留 */
	const clearList = () => {
		const count = filteredList.value.length;
		if (!count) {
			uni.showToast({ title: '当前筛选下暂无题目', icon: 'none' });
			return;
		}
		const chipLabel = activeChip.value;
		uni.showModal({
			title: '清空当前筛选',
			content: `确定删除「${chipLabel}」下的 ${count} 道错题吗？未在当前筛选中的错题将保留。`,
			confirmText: '删除',
			confirmColor: '#FF5E66',
			cancelText: '取消',
			success: async (res) => {
				if (!res.confirm) {
					return;
				}
				if (hasDateFilter.value) {
					list.value = (await deleteNotebookItemsByKeys(filteredItemKeys.value, list.value))
						.map((entry) => ({ ...entry }));
				} else {
					list.value = (await clearNotebookFilteredItems(activeChip.value, list.value, profile.value))
						.map((entry) => ({ ...entry }));
				}
				uni.showToast({ title: '已清空当前筛选', icon: 'none' });
			}
		});
	};

	const handlePopupPhotoAdd = () => {
		closeFilterPopup();
		openOcrImagePicker();
	};

	const handlePopupExport = () => {
		closeFilterPopup();
		if (!filteredList.value.length) {
			uni.showToast({ title: '当前筛选下暂无题目', icon: 'none' });
			return;
		}
		enterExportMode();
		exportSubjects.value = [];
		exportIncludeAnswerAnalysis.value = true;
		showExportPopup.value = true;
	};

	const handlePopupClear = () => {
		closeFilterPopup();
		clearList();
	};

	const go = (url) => {
		if (url.startsWith('/pages/')) {
			uni.reLaunch({ url });
			return;
		}
		uni.navigateTo({ url });
	};

	/** 错题解析：写入 REVIEW_KEY 后跳转解析页（analysis 与 reviewItem 字段对齐） */
	const openAnalysis = (item) => {
		const questionTitle = String(item.question || '').trim();
		if (!questionTitle) {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		setQuestionReviewItem(buildReviewItemFromNotebook(item));
		go(QUESTION_ANALYSIS_PAGE);
	};

	/** AI 讲解：校验剩余次数后跳转 chat，携带题干与错题追问 tip */
	const askAi = (item) => {
		const { topic, tip } = buildNotebookChatEntry(item);
		if (!topic) {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		const quota = checkAiChatQuota();
		if (!quota.ok && !quota.notReady) {
			showAiQuotaExhaustedModal();
			return;
		}
		go(buildChatPageUrl({ topic, tip }));
	};

	/** 错题重做：mode=single + questionId → 答题页拉题（文档 12.2.3） */
	const redoItem = (item) => {
		const questionTitle = String(item.question || '').trim();
		const questionId = resolveNotebookQuestionToken(item);
		if (!questionTitle && !questionId) {
			uni.showToast({ title: '题目信息不完整', icon: 'none' });
			return;
		}
		setPracticeSource({
			mode: 'single',
			title: NOTEBOOK_REDO_TITLE,
			questionId,
			questionTitle
		});
		go(QUESTION_ANSWER_PAGE);
	};

	const toggleMastered = async (item) => {
		const nextMastered = !item.mastered;
		list.value = (await toggleNotebookMastered(item.question)).map((entry) => ({ ...entry }));
		uni.showToast({
			title: nextMastered ? '已标记为掌握' : '已改为待复习',
			icon: 'none'
		});
	};
</script>

<style lang="scss" scoped>
	.notebook-screen {
		padding: 20rpx 24rpx 36rpx;
		background: linear-gradient(180deg, #F4F8FF 0, #F8FAFD 240rpx, #F6F8FC 100%);
	}

	.chip-panel {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 20rpx;
		border-radius: 26rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 10rpx 24rpx rgba(45, 86, 156, 0.06);
		position: sticky;
		top: 0;
		z-index: 12;
		backdrop-filter: blur(10rpx);
	}

	.subject-selector {
		flex: 1;
		min-width: 0;
		height: 64rpx;
		padding: 0 24rpx;
		border-radius: 999rpx;
		background: #F5F7FB;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	.subject-selector-label {
		flex: 1;
		min-width: 0;
		font-size: 26rpx;
		font-weight: 600;
		color: #23324a;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.top-actions {
		display: flex;
		gap: 16rpx;
		flex-shrink: 0;
	}

	.icon-btn {
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: #F4F6F9;
		box-shadow: inset 0 0 0 2rpx rgba(231, 235, 243, 0.8);
		display: flex;
		align-items: center;
		justify-content: center;
		transition: background 0.2s ease;
	}

	.icon-btn:active {
		background: #e8ecf3;
	}

	.filter-trigger {
		flex-shrink: 0;
	}

	.export-hint-bar {
		margin-top: 20rpx;
		padding: 16rpx 20rpx;
		border-radius: 18rpx;
		background: #EEF4FF;
		display: flex;
		align-items: center;
		gap: 10rpx;
		font-size: 22rpx;
		color: #386df5;
		line-height: 1.5;
	}

	.export-hint-bar text.export-hint-text {
		flex: 1;
		min-width: 0;
	}

	.export-hint-cancel {
		flex-shrink: 0;
		font-weight: 700;
		color: #697386;
	}

	.summary-bar {
		margin-top: 16rpx;
		padding: 20rpx 24rpx;
		border-radius: 22rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 8rpx 20rpx rgba(45, 86, 156, 0.05);
		display: flex;
		align-items: center;
		justify-content: space-around;
	}

	.summary-item {
		flex: 1;
		text-align: center;
	}

	.summary-value {
		display: block;
		font-size: 32rpx;
		font-weight: 800;
		line-height: 1.2;
		color: #23324a;
	}

	.summary-value.orange {
		color: #ea7d24;
	}

	.summary-value.blue {
		color: #386df5;
	}

	.summary-label {
		display: block;
		margin-top: 6rpx;
		font-size: 20rpx;
		color: #8792a6;
	}

	.summary-divider {
		width: 1rpx;
		height: 48rpx;
		background: #e8edf4;
		flex-shrink: 0;
	}

	.sync-banner {
		margin-top: 14rpx;
		padding: 14rpx 18rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
		font-size: 22rpx;
		line-height: 1.5;
	}

	.sync-banner--loading {
		background: rgba(237, 244, 255, 0.96);
		color: #386df5;
	}

	.sync-banner--error {
		background: rgba(255, 243, 244, 0.96);
		color: #dc2626;
	}

	.sync-banner--info {
		background: rgba(255, 247, 237, 0.96);
		color: #b45309;
	}

	.sync-banner-action {
		flex-shrink: 0;
		font-weight: 700;
		color: #386df5;
	}

	.list-loading {
		margin-top: 80rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		font-size: 24rpx;
		color: #8792a6;
	}

	.batch-toolbar {
		margin-top: 20rpx;
		display: flex;
		gap: 14rpx;
	}

	.batch-action {
		flex: 1;
		height: 72rpx;
		border-radius: 22rpx;
		border: 2rpx solid #e6ebf3;
		background: #FFFFFF;
		color: #5d6777;
		font-size: 24rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
	}

	.batch-action.primary {
		background: #F3F8FF;
		border-color: #c9d9ff;
		color: #386df5;
	}

	.batch-action.active {
		background: #eef3ff;
		border-color: #c9d9ff;
		color: #386df5;
	}

	.note-item {
		display: flex;
		align-items: flex-start;
		gap: 14rpx;
	}

	.note-item.batch-mode.selected :deep(.note-card) {
		box-shadow: 0 12rpx 28rpx rgba(78, 136, 246, 0.08), inset 0 0 0 2rpx rgba(56, 109, 245, 0.35);
	}

	.item-check {
		padding-top: 24rpx;
		flex-shrink: 0;
	}

	.check-box {
		width: 36rpx;
		height: 36rpx;
		border-radius: 10rpx;
		border: 2rpx solid #c5cedb;
		background: #fff;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.check-box.checked {
		background: #386df5;
		border-color: #386df5;
	}

	/* #ifdef MP-WEIXIN */
	.chip-panel {
		position: relative;
		backdrop-filter: none;
	}

	.sheet-actions {
		display: flex;
		flex-wrap: wrap;
	}

	.sheet-actions .sheet-action-card {
		width: calc(33.33% - 8rpx);
	}
	/* #endif */

	.note-item :deep(.note-card) {
		flex: 1;
		min-width: 0;
	}

	.note-card-host {
		flex: 1;
		min-width: 0;
	}

	.batch-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: calc(100rpx + env(safe-area-inset-bottom));
		padding: 18rpx 24rpx;
		background: rgba(255, 255, 255, 0.98);
		box-shadow: 0 -8rpx 32rpx rgba(19, 33, 60, 0.08);
		display: flex;
		align-items: center;
		gap: 20rpx;
		z-index: 20;
	}

	.batch-bar-left {
		display: flex;
		align-items: center;
		gap: 12rpx;
		font-size: 24rpx;
		color: #5d6777;
		font-weight: 600;
	}

	.batch-count {
		flex: 1;
		font-size: 21rpx;
		color: #97a0af;
		min-width: 0;
	}

	.batch-redo-btn {
		padding: 0 24rpx;
		height: 72rpx;
		border-radius: 999rpx;
		background: #eef3ff;
		color: #386df5;
		font-size: 24rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.batch-redo-btn.disabled {
		opacity: 0.45;
	}

	.batch-delete-btn {
		padding: 0 32rpx;
		height: 72rpx;
		border-radius: 999rpx;
		background: #ff5e66;
		color: #fff;
		font-size: 24rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.batch-delete-btn.disabled {
		opacity: 0.45;
	}

	.export-bar .batch-settings-btn {
		padding: 0 20rpx;
		height: 72rpx;
		border-radius: 999rpx;
		background: #F3F6FB;
		color: #5d6777;
		font-size: 24rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.batch-export-btn {
		padding: 0 28rpx;
		height: 72rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #4E88F6 0%, #6AA0FB 100%);
		color: #fff;
		font-size: 24rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.batch-export-btn.disabled {
		opacity: 0.45;
	}

	.note-list {
		margin-top: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 18rpx;
	}

	.btn-row {
		margin-top: 24rpx;
		display: flex;
		gap: 12rpx;
		flex-wrap: wrap;
	}

	.pill-btn {
		flex: 1 1 46%;
		height: 74rpx;
		border-radius: 18rpx;
		border: 2rpx solid #e6ebf3;
		background: #F8FAFC;
		color: #5d6777;
		font-size: 23rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		transition: all 0.15s ease;
	}

	.pill-btn:active {
		transform: scale(0.98);
	}

	.pill-btn.primary {
		background: linear-gradient(135deg, #4E88F6 0%, #6AA0FB 100%);
		border-color: #4E88F6;
		color: #fff;
	}

	.pill-btn.green {
		background: #FFFFFF;
		border-color: #d5f3dd;
		color: #1caa55;
	}

	.pill-btn.status.done {
		background: #eaf8ef;
		border-color: #d9f3e2;
		color: #16a34a;
	}

	.pill-btn.status.reopen {
		background: #fff7e8;
		border-color: #ffe1b0;
		color: #d97706;
	}

	.notebook-empty {
		margin-top: 24rpx;
		border-radius: 28rpx;
		box-shadow: 0 12rpx 28rpx rgba(45, 86, 156, 0.06);
	}

	.filter-sheet {
		background: #FFFFFF;
		border-radius: 32rpx 32rpx 0 0;
		padding: 24rpx 24rpx calc(32rpx + env(safe-area-inset-bottom));
	}

	.light-picker-theme {
		--up-main-color: #303133;
		--up-content-color: #606266;
		--up-tips-color: #909193;
		--up-light-color: #c0c4cc;
		--up-border-color: #dadbde;
		--up-bg-color: #f3f4f6;
		--up-page-bg-color: #f3f4f6;
		--up-card-bg-color: #ffffff;
		--up-disabled-color: #c8c9cc;
		--up-primary: #3c9cff;
		--up-primary-dark: #398ade;
		--up-primary-disabled: #9acafc;
		--up-primary-light: #ecf5ff;
		--up-warning: #f9ae3d;
		--up-warning-dark: #f1a532;
		--up-warning-disabled: #f9d39b;
		--up-warning-light: #fdf6ec;
		--up-success: #5ac725;
		--up-success-dark: #53c21d;
		--up-success-disabled: #a9e08f;
		--up-success-light: #f5fff0;
		--up-error: #f56c6c;
		--up-error-dark: #e45656;
		--up-error-disabled: #f7b2b2;
		--up-error-light: #fef0f0;
		--up-info: #909399;
		--up-info-dark: #767a82;
		--up-info-disabled: #c4c6c9;
		--up-info-light: #f4f4f5;
	}

	.sheet-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 20rpx;
	}

	.sheet-title {
		display: block;
		font-size: 34rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #23324A;
	}

	.sheet-sub {
		display: block;
		margin-top: 10rpx;
		font-size: 22rpx;
		line-height: 1.6;
		color: #6f7f96;
	}

	.sheet-close {
		width: 60rpx;
		height: 60rpx;
		border-radius: 20rpx;
		background: #F3F6FB;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.sheet-actions {
		margin-top: 24rpx;
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 12rpx;
	}

	.sheet-action-card {
		height: 96rpx;
		border-radius: 22rpx;
		background: #F5F8FF;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 20rpx;
		font-weight: 600;
		color: #386DF5;
	}

	.sheet-action-card.danger {
		background: #FFF3F4;
		color: #FF5E66;
	}

	.date-filter-card {
		margin-top: 18rpx;
		padding: 0;
		border-radius: 0;
		background: transparent;
		box-shadow: none;
	}

	.date-filter-head {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.date-filter-title {
		font-size: 28rpx;
		font-weight: 700;
		line-height: 1.35;
		color: #23324A;
	}

	.date-filter-sub {
		font-size: 22rpx;
		line-height: 1.5;
		color: #7B8798;
	}

	.date-picker-row {
		margin-top: 18rpx;
		margin-bottom: 18rpx;
		display: flex;
		gap: 16rpx;
	}

	.date-picker-trigger {
		display: block;
		flex: 1;
		min-width: 0;
	}

	.date-picker-box {
		padding: 22rpx 20rpx;
		border-radius: 22rpx;
		background: #F6F8FC;
		min-height: 120rpx;
		display: flex;
		flex-direction: column;
		justify-content: center;
	}

	.date-picker-label {
		display: block;
		font-size: 20rpx;
		line-height: 1.4;
		color: #8b97aa;
	}

	.date-picker-value {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		font-weight: 600;
		line-height: 1.4;
		color: #23324A;
	}

	.date-filter-actions {
		margin-top: 4rpx;
		display: flex;
		gap: 16rpx;
	}

	.sheet-secondary-btn,
	.sheet-primary-btn {
		flex: 1;
		height: 84rpx;
		border-radius: 22rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		font-weight: 600;
	}

	.sheet-secondary-btn {
		background: #F3F6FB;
		color: #5D6777;
	}

	.sheet-primary-btn {
		background: linear-gradient(135deg, #4E88F6 0%, #6AA0FB 100%);
		color: #FFFFFF;
	}

	.sheet-primary-btn.disabled {
		opacity: 0.45;
	}

	.subject-picker-sheet {
		background: #FFFFFF;
		border-radius: 32rpx 32rpx 0 0;
		padding: 24rpx 24rpx calc(32rpx + env(safe-area-inset-bottom));
	}

	.subject-picker-scroll {
		margin-top: 20rpx;
		max-height: 60vh;
	}

	.subject-picker-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		padding: 24rpx 8rpx;
		border-bottom: 1rpx solid #EEF2F8;
	}

	.subject-picker-item.active {
		background: rgba(238, 244, 255, 0.6);
		border-radius: 16rpx;
	}

	.subject-picker-name {
		font-size: 28rpx;
		color: #23324A;
		font-weight: 500;
	}

	.subject-picker-item.active .subject-picker-name {
		color: #386df5;
		font-weight: 700;
	}

	.export-sheet {
		max-height: 70vh;
	}

	.export-option-block {
		margin-bottom: 24rpx;
	}

	.export-option-label {
		display: block;
		font-size: 24rpx;
		font-weight: 700;
		color: #23324A;
	}

	.export-option-tip {
		display: block;
		margin-top: 12rpx;
		font-size: 20rpx;
		color: #8b97aa;
	}

	.export-option-tip--inline {
		margin-top: 8rpx;
	}

	.export-subject-scroll {
		margin-top: 16rpx;
		white-space: nowrap;
	}

	.export-subject-tabs {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 6rpx 4rpx 10rpx;
	}

	.export-subject-tab {
		flex-shrink: 0;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 18rpx 28rpx;
		border-radius: 999rpx;
		background: #F6F8FC;
		border: 2rpx solid transparent;
		transition: all 0.2s ease;
	}

	.export-subject-tab.active {
		background: #EEF4FF;
		border-color: rgba(78, 136, 246, 0.35);
		box-shadow: 0 10rpx 24rpx rgba(56, 109, 245, 0.10);
	}

	.export-subject-name {
		font-size: 26rpx;
		color: #23324A;
		line-height: 1;
	}

	.export-subject-tab.active .export-subject-name {
		color: #386df5;
		font-weight: 700;
	}

	.export-radio-group {
		margin-top: 16rpx;
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.export-radio-row {
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
		padding: 20rpx;
		border-radius: 22rpx;
		background: #F6F8FC;
		border: 2rpx solid transparent;
	}

	.export-radio-row.active {
		background: #EEF4FF;
		border-color: rgba(78, 136, 246, 0.35);
	}

	.radio-circle {
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		border: 2rpx solid #c5cedb;
		background: #fff;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		margin-top: 2rpx;
	}

	.radio-circle.checked {
		border-color: #386df5;
	}

	.radio-circle-dot {
		width: 18rpx;
		height: 18rpx;
		border-radius: 50%;
		background: #386df5;
	}

	.export-radio-copy {
		flex: 1;
		min-width: 0;
	}

	.export-radio-title {
		display: block;
		font-size: 26rpx;
		font-weight: 700;
		color: #23324A;
	}

	.export-radio-sub {
		display: block;
		margin-top: 6rpx;
		font-size: 22rpx;
		color: #8b97aa;
	}

	/* #ifdef MP-WEIXIN */
	.chip-panel {
		position: relative;
		top: auto;
		background: #FFFFFF;
	}

	.sheet-actions {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
	}

	.sheet-action-card {
		width: calc(33.3333% - 8rpx);
	}
	/* #endif */
</style>
