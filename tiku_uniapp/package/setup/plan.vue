<template>
	<view class="app-page plan-page" :class="{ 'plan-page--edit': isEditMode }">
		<scroll-view scroll-y class="page-scroll" :style="scrollViewStyle">
			<view class="screen plan-screen">
				<view v-if="optionsLoading" class="plan-options-loading">
					<up-loading-icon mode="circle" color="#386df5"></up-loading-icon>
					<text>加载学习计划选项...</text>
				</view>
				<view v-else-if="optionsError" class="plan-options-error card">
					<up-icon name="wifi-off" size="28" color="#98A2B3"></up-icon>
					<text class="plan-options-error-text">{{ optionsError }}</text>
					<view class="ghost-button plan-retry-btn" @tap="loadSetupOptions">
						<text>重新加载</text>
					</view>
				</view>
				<view v-else-if="needsPhoneBind && !isEditMode" class="phone-bind-shell card">
					<view class="phone-bind-head">
						<view class="phone-bind-badge">步骤 1/2</view>
						<text class="phone-bind-title">授权手机号</text>
						<text class="phone-bind-sub">须通过微信官方 API 获取手机号，同手机号在小程序/H5/App 共用同一账号</text>
					</view>
					<button
						class="phone-bind-btn"
						open-type="getPhoneNumber"
						:disabled="phoneBinding"
						@getphonenumber="onPlanGetPhoneNumber"
					>
						<up-loading-icon v-if="phoneBinding" mode="circle" color="#FFFFFF" size="20"></up-loading-icon>
						<text v-else>微信授权手机号</text>
					</button>
				</view>
				<template v-else>
				<view v-if="isEditMode" class="plan-form-shell">
					<view class="edit-hero">
						<view class="form-intro-badge">
							<up-icon name="edit-pen" size="12" color="#386df5"></up-icon>
							<text>计划调整</text>
						</view>
						<text class="form-intro-title">调整你的学习计划</text>
						<text class="form-intro-sub">保存后同步推荐与筛选</text>

						<view class="selection-overview">
							<view class="overview-main">
								<view>
									<text class="overview-kicker">当前配置</text>
									<view class="overview-title-row">
										<text class="overview-primary">{{ selectedStageLabel || '未选择学段' }}</text>
										<text class="overview-dot">·</text>
										<text class="overview-primary">{{ selectedGradeLabel || '未选择年级' }}</text>
									</view>
									<text class="overview-version">{{ selectedVersionLabel || '未选择教材版本' }}</text>
								</view>
								<view class="overview-badge">
									<text>{{ activeCount }} 科已选</text>
								</view>
							</view>
							<view v-if="selectedSubjects.length" class="selected-subject-row">
								<text
									v-for="item in selectedSubjects"
									:key="item"
									class="selected-subject-tag"
								>{{ item }}</text>
							</view>
						</view>
					</view>

					<view class="form-section-block">
						<view class="step-head">
							<text class="step-index">01</text>
							<view>
								<text class="form-section-title">选择学段</text>
								<text class="form-section-sub">小学 / 初中 / 高中</text>
							</view>
						</view>
						<view class="option-row option-row--stage edit-option-row">
							<view
								v-for="item in gradeStages"
								:key="item.value"
								class="option-pill"
								:class="{ active: item.value === selectedStage }"
								@tap="selectStage(item.value)"
							>
								<text>{{ item.label }}</text>
							</view>
						</view>
					</view>

					<view class="form-section-block">
						<view class="step-head">
							<text class="step-index">02</text>
							<view>
								<text class="form-section-title">选择年级</text>
								<text class="form-section-sub">{{ selectedStageLabel ? `${selectedStageLabel}年级` : '先选学段' }}</text>
							</view>
						</view>
						<view v-if="filteredGrades.length" class="option-row edit-option-row">
							<view
								v-for="item in filteredGrades"
								:key="item.value"
								class="option-pill"
								:class="{ active: item.value === selectedGrade }"
								@tap="selectGrade(item.value)"
							>
								<text>{{ item.label }}</text>
							</view>
						</view>
						<text v-else class="stage-empty-tip">当前学段暂无年级选项，请联系管理员在后台配置</text>
					</view>

					<view class="form-section-block">
						<view class="step-head step-head--split">
							<view class="step-title-row">
								<text class="step-index">03</text>
								<view>
									<text class="form-section-title">选择科目</text>
									<text class="form-section-sub">{{ selectedStageLabel ? `${selectedStageLabel}可选科目` : '先选学段' }}</text>
								</view>
							</view>
							<text class="count-badge">{{ activeCount }} 科</text>
						</view>
						<view v-if="filteredSubjects.length" class="subject-grid">
							<view
								v-for="item in filteredSubjects"
								:key="item.name"
								class="subject-tile"
								:class="{ active: item.active }"
								@tap="toggleSubject(item.name)"
							>
								<view class="subject-tile-main">
									<view class="subject-tile-icon">
										<up-icon :name="item.icon" size="16" :color="item.active ? '#386DF5' : '#8A94A6'"></up-icon>
									</view>
									<text class="subject-tile-text">{{ item.name }}</text>
								</view>
								<view v-if="item.active" class="subject-tile-mark">
									<up-icon name="checkmark" size="10" color="#FFFFFF"></up-icon>
								</view>
							</view>
						</view>
						<text v-else class="form-empty-tip">当前学段暂无学科选项，请联系管理员在后台配置</text>
					</view>

					<view class="form-section-block">
						<view class="step-head">
							<text class="step-index">04</text>
							<view>
								<text class="form-section-title">选择教材版本</text>
								<text class="form-section-sub">当前使用版本</text>
							</view>
						</view>
						<view v-if="versionList.length" class="version-trigger version-trigger--edit" @tap="openVersionSheet">
							<view class="version-trigger-main">
								<view class="version-icon">
									<up-icon name="bookmark" size="18" color="#386df5"></up-icon>
								</view>
								<view class="version-copy">
									<text class="version-label">{{ selectedVersionLabel || '请选择教材版本' }}</text>
									<text class="version-hint">{{ versionList.length }} 个可选版本</text>
								</view>
							</view>
							<up-icon name="arrow-right" size="16" color="#98A2B3"></up-icon>
						</view>
						<text v-else class="form-empty-tip">当前暂无教材版本可选，请联系管理员在后台配置</text>
					</view>
				</view>

				<template v-else>
				<view class="plan-top">
					<view class="top-copy">
						<view class="top-badge">
							<up-icon name="map" size="16" color="#386df5"></up-icon>
							<text>学习规划</text>
						</view>
						<text class="top-title">{{ heroTitle }}</text>
						<text class="top-sub">{{ heroSub }}</text>
					</view>
					<view class="hero-paper">
						<view class="paper-head">
							<view class="paper-badge">
								<up-icon name="file-text" size="14" color="#386df5"></up-icon>
								<text>个性化题单</text>
							</view>
							<text class="paper-tag">AI生成</text>
						</view>
						<text class="paper-title">根据年级、科目和教材版本生成更贴近当前进度的练习内容</text>
						<view class="paper-lines">
							<view class="paper-line paper-line-main"></view>
							<view class="paper-line"></view>
							<view class="paper-line short"></view>
						</view>
					</view>
				</view>

				<view class="section-band">
					<view class="section-head">
						<view>
							<text class="section-title">学段</text>
							<text class="section-sub">先选择小学、初中或高中</text>
						</view>
					</view>
					<view class="grade-row">
						<view
							v-for="item in gradeStages"
							:key="item.value"
							class="grade-pill stage-pill"
							:class="{ active: item.value === selectedStage }"
							@tap="selectStage(item.value)"
						>
							<text>{{ item.label }}</text>
						</view>
					</view>
				</view>

				<view class="section-band">
					<view class="section-head">
						<view>
							<text class="section-title">年级</text>
							<text class="section-sub">{{ selectedStageLabel ? `选择${selectedStageLabel}具体年级` : '请先选择学段' }}</text>
						</view>
					</view>
					<view v-if="filteredGrades.length" class="grade-row">
						<view
							v-for="item in filteredGrades"
							:key="item.value"
							class="grade-pill"
							:class="{ active: item.value === selectedGrade }"
							@tap="selectGrade(item.value)"
						>
							<text>{{ item.label }}</text>
						</view>
					</view>
					<text v-else class="stage-empty-tip">当前学段暂无年级选项，请联系管理员在后台配置</text>
				</view>

				<view class="section-band">
					<view class="section-head">
						<view>
							<text class="section-title">学习科目</text>
							<text class="section-sub">{{ selectedStageLabel ? `${selectedStageLabel}可选科目，可多选` : '请先选择学段' }}</text>
						</view>
						<view class="count-badge">
							<text>{{ activeCount }} 科</text>
						</view>
					</view>
					<view v-if="filteredSubjects.length" class="subject-row">
						<view
							v-for="item in filteredSubjects"
							:key="item.name"
							class="subject-pill"
							:class="{ active: item.active }"
							@tap="toggleSubject(item.name)"
						>
							<text>{{ item.name }}</text>
							<view v-if="item.active" class="subject-check">
								<up-icon name="checkmark" size="10" color="#FFFFFF"></up-icon>
							</view>
						</view>
					</view>
					<text v-else class="stage-empty-tip">当前学段暂无学科选项，请联系管理员在后台配置</text>
				</view>

				<view class="section-band">
					<view class="section-head">
						<view>
							<text class="section-title">教材版本</text>
							<text class="section-sub">{{ isEditMode ? '选择当前使用的教材版本' : '从底部列表中选择当前使用的教材' }}</text>
						</view>
					</view>
					<view class="version-trigger select-card" @tap="openVersionSheet">
						<view class="version-trigger-main">
							<view class="version-icon">
								<up-icon name="bookmark" size="18" color="#386df5"></up-icon>
							</view>
							<view class="version-copy">
								<text class="version-label">{{ selectedVersionLabel || '请选择教材版本' }}</text>
								<text class="version-hint">共 {{ versionList.length }} 个版本可选</text>
							</view>
						</view>
						<up-icon name="arrow-right" size="16" color="#98A2B3"></up-icon>
					</view>
				</view>
				</template>
				</template>
			</view>
		</scroll-view>

		<view v-if="!needsPhoneBind || isEditMode" class="plan-footer safe-bottom">
			<view class="solid-button" :class="{ disabled: optionsLoading || !canSubmit || submitting }" @tap="savePlan">
				<text>{{ submitButtonText }}</text>
			</view>
		</view>

		<up-popup
			:show="showVersionSheet"
			mode="bottom"
			round="20"
			:close-on-click-overlay="true"
			@close="closeVersionSheet"
		>
			<view class="version-sheet">
				<view class="sheet-head">
					<view>
						<text class="sheet-title">选择教材版本</text>
						<text class="sheet-sub">保存后将按所选版本推荐内容</text>
					</view>
					<view class="sheet-close" @tap="closeVersionSheet">
						<up-icon name="close" size="16" color="#667085"></up-icon>
					</view>
				</view>
				<scroll-view scroll-y class="sheet-scroll">
					<view
						v-for="(item, index) in versionList"
						:key="item.value"
						class="sheet-option"
						:class="{ active: index === versionIndex }"
						@tap="selectVersion(index)"
					>
						<view class="sheet-option-copy">
							<text class="sheet-option-title">{{ item.label }}</text>
							<text class="sheet-option-sub">教材版本 {{ index + 1 }}</text>
						</view>
						<view class="sheet-option-mark" :class="{ active: index === versionIndex }">
							<up-icon v-if="index === versionIndex" name="checkmark" size="12" color="#FFFFFF"></up-icon>
						</view>
					</view>
				</scroll-view>
			</view>
		</up-popup>
	</view>
</template>

<script setup>
	/** 模块二十二 P1#14 · 计划页编辑模式：profile 回填 + POST /app/user 同步，保存后 navigateBack */
	import { onLoad, onReady } from '@dcloudio/uni-app';
	import { computed, onMounted, ref } from 'vue';
	import {
		LAUNCH_ROUTE_HOME,
		SETUP_PLAN_EDIT_MODE,
		getUserProfile
	} from '@/common/app-data';
	import { fetchSetupOptions, filterGradesByStage, filterSubjectsByStage, pruneSubjectSelectionByStage, resolveSubjectIcon } from '@/common/setup-options';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import { syncUserProfileToServer } from '@/common/user-info';
	import {
		bindWechatPhoneByCode,
		extractWechatPhoneCode,
		needsWechatPhoneBind
	} from '@/common/wechat-phone';
	import { isMpWeixin } from '@/common/platform';

	/** 是否编辑模式（设置页进入） */
	const isEditMode = ref(false);
	const scrollViewHeight = ref('');
	const phoneBinding = ref(false);
	const needsPhoneBind = ref(isMpWeixin() && needsWechatPhoneBind());

	const scrollViewStyle = computed(() => (
		scrollViewHeight.value ? { height: scrollViewHeight.value } : {}
	));

	const measureScrollViewHeight = () => {
		try {
			const sys = uni.getSystemInfoSync();
			const statusBarHeight = Number(sys.statusBarHeight || 0);
			const safeBottom = Number(sys.safeAreaInsets?.bottom || 0);
			const navBarHeight = statusBarHeight + 44;
			const showFooter = !needsPhoneBind.value || isEditMode.value;
			const footerHeight = showFooter ? 112 + safeBottom : safeBottom;
			const height = Math.max(Number(sys.windowHeight || 0) - navBarHeight - footerHeight, 320);
			scrollViewHeight.value = `${height}px`;
		} catch (error) {
			scrollViewHeight.value = '72vh';
		}
	};
	const heroTitle = computed(() => {
		if (needsPhoneBind.value && !isEditMode.value) {
			return '绑定手机号';
		}
		return isEditMode.value ? '调整你的学习计划' : '一分钟定制学习计划';
	});
	const navigationTitle = computed(() => {
		if (needsPhoneBind.value && !isEditMode.value) {
			return '绑定手机号';
		}
		return isEditMode.value ? '计划调整' : '学习计划';
	});
	usePageNavigationTitle(navigationTitle);
	const heroSub = computed(() => {
		if (needsPhoneBind.value && !isEditMode.value) {
			return '授权后可定制年级、科目与教材版本';
		}
		return isEditMode.value ? '修改年级、科目与教材版本' : '选择年级、科目与教材版本';
	});
	const submitButtonText = computed(() => (isEditMode.value ? '保存修改' : '保存并进入首页'));

	onLoad((options = {}) => {
		isEditMode.value = String(options.mode || '').trim() === SETUP_PLAN_EDIT_MODE;
	});

	onReady(() => {
		measureScrollViewHeight();
	});

	const refreshPhoneBindState = () => {
		needsPhoneBind.value = isMpWeixin() && needsWechatPhoneBind();
	};

	const onPlanGetPhoneNumber = async (event) => {
		if (phoneBinding.value) {
			return;
		}
		const phoneCode = extractWechatPhoneCode(event);
		if (!phoneCode) {
			uni.showToast({ title: '需要授权手机号才能继续', icon: 'none' });
			return;
		}
		phoneBinding.value = true;
		try {
			await bindWechatPhoneByCode(phoneCode);
			refreshPhoneBindState();
			measureScrollViewHeight();
			uni.showToast({ title: '手机号已绑定', icon: 'none' });
		} catch (error) {
			uni.showToast({
				title: error?.msg || error?.message || '绑定手机号失败，请重试',
				icon: 'none'
			});
		} finally {
			phoneBinding.value = false;
		}
	};

	onMounted(() => {
		refreshPhoneBindState();
		loadSetupOptions();
	});

	/** 学段 / 年级选项走 GET /app/setup/options，失败须重试 */
	const gradeStages = ref([]);
	const grades = ref([]);
	const versionList = ref([]);
	const selectedStage = ref('');
	/** 选中年级完整名称，写入 profile.grade */
	const selectedGrade = ref('');
	/** 选项配置是否加载中 */
	const optionsLoading = ref(true);
	const optionsFromApi = ref(false);
	const optionsError = ref('');
	const showVersionSheet = ref(false);

	const selectedStageLabel = computed(() =>
		gradeStages.value.find((item) => item.value === selectedStage.value)?.label || ''
	);
	const selectedGradeLabel = computed(() =>
		grades.value.find((item) => item.value === selectedGrade.value)?.label || selectedGrade.value || ''
	);
	const filteredGrades = computed(() => filterGradesByStage(grades.value, selectedStage.value));
	const filteredSubjects = computed(() => filterSubjectsByStage(subjects.value, selectedStage.value));

	const selectStage = (stageCode) => {
		if (!stageCode || selectedStage.value === stageCode) {
			return;
		}
		selectedStage.value = stageCode;
		const stageGrades = filterGradesByStage(grades.value, stageCode);
		const stillValid = stageGrades.some((item) => item.value === selectedGrade.value);
		if (!stillValid) {
			selectedGrade.value = stageGrades[0]?.value || '';
		}
		subjects.value = pruneSubjectSelectionByStage(subjects.value, stageCode);
	};

	const selectGrade = (grade) => {
		selectedGrade.value = grade;
	};
	/** 当前教材版本下标 */
	const versionIndex = ref(0);
	const selectedVersionLabel = computed(() => versionList.value[versionIndex.value]?.label || '');
	const selectedVersionValue = computed(() => versionList.value[versionIndex.value]?.value || '');
	/** 科目列表，active 标记选中状态 */
	const subjects = ref([]);
	const activeCount = computed(() => filteredSubjects.value.filter((item) => item.active).length);
	const selectedSubjects = computed(() =>
		filteredSubjects.value.filter((item) => item.active).map((item) => item.name)
	);
	const selectedSubjectsText = computed(() => {
		if (!selectedSubjects.value.length) {
			return '未选择';
		}
		return selectedSubjects.value.join(' / ');
	});

	/** 新建模式：接口选项加载后使用默认学段/年级/版本 */
	const applySetupOptionsDefaults = (options) => {
		gradeStages.value = options.gradeStages || [];
		grades.value = options.grades;
		versionList.value = options.versions;
		subjects.value = options.subjects.map((item) => ({
			...item,
			icon: resolveSubjectIcon(item.icon)
		}));
		const defaultStage = gradeStages.value[1]?.value || gradeStages.value[0]?.value || 'junior';
		selectedStage.value = defaultStage;
		const stageGrades = filterGradesByStage(grades.value, defaultStage);
		selectedGrade.value = stageGrades[stageGrades.length - 1]?.value
			|| stageGrades[0]?.value
			|| '';
		subjects.value = pruneSubjectSelectionByStage(subjects.value, defaultStage);
		versionIndex.value = 0;
	};

	/** 编辑模式：用 profile 中年级/学科/版本回填表单 */
	const applyProfileToForm = (options, profile = {}) => {
		gradeStages.value = options.gradeStages || [];
		grades.value = options.grades;
		versionList.value = options.versions;
		const selectedSubjectSet = new Set(
			Array.isArray(profile.subjects) ? profile.subjects.filter(Boolean) : []
		);
		subjects.value = options.subjects.map((item) => ({
			...item,
			icon: resolveSubjectIcon(item.icon),
			active: selectedSubjectSet.has(item.name)
		}));
		const profileGrade = String(profile.grade || '').trim();
		const matchedGrade = options.grades.find((item) => item.value === profileGrade);
		if (matchedGrade) {
			selectedStage.value = matchedGrade.stageCode || 'junior';
			selectedGrade.value = matchedGrade.value;
		} else {
			const defaultStage = gradeStages.value[1]?.value || gradeStages.value[0]?.value || 'junior';
			selectedStage.value = defaultStage;
			const stageGrades = filterGradesByStage(grades.value, defaultStage);
			selectedGrade.value = stageGrades[0]?.value || '';
		}
		subjects.value = pruneSubjectSelectionByStage(subjects.value, selectedStage.value);
		const profileVersion = String(profile.version || '').trim();
		const versionIdx = options.versions.findIndex((item) => item.value === profileVersion);
		versionIndex.value = versionIdx >= 0 ? versionIdx : 0;
	};

	const loadSetupOptions = async () => {
		optionsLoading.value = true;
		optionsError.value = '';
		const options = await fetchSetupOptions();
		optionsFromApi.value = Boolean(options.fromApi);
		if (!options.fromApi) {
			optionsError.value = '学习计划选项加载失败，请检查网络后重试';
			gradeStages.value = [];
			grades.value = [];
			versionList.value = [];
			subjects.value = [];
			optionsLoading.value = false;
			return;
		}
		if (isEditMode.value) {
			applyProfileToForm(options, getUserProfile());
		} else {
			applySetupOptionsDefaults(options);
		}
		optionsLoading.value = false;
	};

	/** 学段、年级、科目、教材版本均来自接口且有效才可提交 */
	const canSubmit = computed(() =>
		optionsFromApi.value
		&& selectedStage.value
		&& selectedGrade.value
		&& versionList.value.length
		&& filteredSubjects.value.some((item) => item.active)
	);
	/** 防止重复提交 */
	const submitting = ref(false);

	const toggleSubject = (name) => {
		subjects.value = subjects.value.map((item) =>
			item.name === name ? { ...item, active: !item.active } : item
		);
	};

	const openVersionSheet = () => {
		if (!versionList.value.length) {
			return;
		}
		showVersionSheet.value = true;
	};

	const closeVersionSheet = () => {
		showVersionSheet.value = false;
	};

	const selectVersion = (index) => {
		if (index < 0 || index >= versionList.value.length) {
			return;
		}
		versionIndex.value = index;
		closeVersionSheet();
	};

	/** POST /app/user 成功后写 Storage；新建 reLaunch 首页，编辑 navigateBack 回用户信息页 */
	const savePlan = async () => {
		if (optionsLoading.value || submitting.value) {
			return;
		}
		if (!selectedStage.value || !selectedGrade.value) {
			uni.showToast({ title: '请选择学段和年级', icon: 'none' });
			return;
		}
		if (!canSubmit.value) {
			uni.showToast({ title: '请至少选择一门科目', icon: 'none' });
			return;
		}
		const selectedSubjects = filteredSubjects.value
			.filter((item) => item.active)
			.map((item) => item.name);
		submitting.value = true;
		try {
			const result = await syncUserProfileToServer({
				grade: selectedGrade.value,
				version: selectedVersionValue.value,
				subjects: selectedSubjects,
				ready: true
			});
			if (!result.success) {
				uni.showToast({
					title: result.msg || '学习计划保存失败，请稍后重试',
					icon: 'none'
				});
				return;
			}
			if (isEditMode.value) {
				uni.showToast({ title: '学习计划已更新', icon: 'none' });
				setTimeout(() => {
					uni.navigateBack();
				}, 300);
				return;
			}
			uni.reLaunch({
				url: LAUNCH_ROUTE_HOME
			});
		} finally {
			submitting.value = false;
		}
	};
</script>

<style lang="scss" scoped>
	.plan-page {
		background:
			linear-gradient(180deg, #edf3ff 0, #f5f7fb 240rpx),
			var(--app-bg);
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		overflow: hidden;
	}

	.page-scroll {
		flex: 1;
		min-height: 0;
	}

	.plan-screen {
		padding-top: 8rpx;
		padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
	}

	.plan-page--edit .plan-screen {
		padding-top: 6rpx;
		padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
	}

	.plan-form-shell {
		display: flex;
		flex-direction: column;
		gap: 18rpx;
	}

	.edit-hero {
		padding: 12rpx 0 8rpx;
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.form-intro-badge {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		align-self: flex-start;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		font-size: 20rpx;
		font-weight: 500;
		color: #386df5;
	}

	.form-intro-title {
		display: block;
		font-size: 40rpx;
		line-height: 1.3;
		font-weight: 700;
		color: #182230;
	}

	.form-intro-sub {
		display: block;
		font-size: 22rpx;
		line-height: 1.55;
		color: #667085;
	}

	.selection-overview {
		padding: 22rpx;
		border-radius: 30rpx;
		background: linear-gradient(135deg, #1f4fb8 0%, #3f73ef 55%, #6b96ff 100%);
		box-shadow: 0 20rpx 42rpx rgba(37, 74, 163, 0.2);
	}

	.overview-main {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.overview-kicker {
		display: block;
		font-size: 20rpx;
		color: rgba(255, 255, 255, 0.7);
	}

	.overview-title-row {
		margin-top: 10rpx;
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	.overview-primary {
		display: block;
		font-size: 34rpx;
		font-weight: 600;
		color: #ffffff;
	}

	.overview-dot {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.68);
	}

	.overview-version {
		display: block;
		margin-top: 10rpx;
		font-size: 24rpx;
		line-height: 1.5;
		color: rgba(255, 255, 255, 0.82);
	}

	.overview-badge {
		padding: 12rpx 18rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.16);
		font-size: 20rpx;
		font-weight: 600;
		color: #ffffff;
		flex-shrink: 0;
	}

	.selected-subject-row {
		margin-top: 18rpx;
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	.selected-subject-tag {
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.16);
		font-size: 20rpx;
		font-weight: 600;
		color: #ffffff;
	}

	.form-section-block {
		padding: 24rpx;
		border-radius: 28rpx;
		background: rgba(255, 255, 255, 0.95);
		box-shadow: 0 16rpx 36rpx rgba(37, 58, 96, 0.06);
		border: 2rpx solid rgba(228, 235, 247, 0.9);
	}

	.step-head,
	.step-title-row {
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
	}

	.step-head--split {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 20rpx;
	}

	.step-index {
		width: 48rpx;
		height: 48rpx;
		border-radius: 16rpx;
		background: #edf3ff;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 22rpx;
		font-weight: 700;
		color: #386df5;
		flex-shrink: 0;
	}

	.form-section-title {
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		color: #1d2939;
	}

	.form-section-sub {
		display: block;
		margin-top: 6rpx;
		font-size: var(--text-sm);
		line-height: 1.45;
		color: #98a2b3;
	}

	.form-empty-tip {
		display: block;
		margin-top: 18rpx;
		padding: 18rpx 20rpx;
		border-radius: 18rpx;
		background: linear-gradient(180deg, #fbfcfe 0%, #f8fafc 100%);
		box-shadow: inset 0 0 0 2rpx #edf2f7, 0 8rpx 18rpx rgba(31, 45, 61, 0.03);
		font-size: 22rpx;
		line-height: 1.55;
		color: #98a2b3;
	}

	.option-row {
		margin-top: 18rpx;
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
	}

	.option-row--stage .option-pill {
		flex: 1 1 calc(33.33% - 12rpx);
		min-width: calc(33.33% - 12rpx);
	}

	.edit-option-row .option-pill {
		min-height: 74rpx;
	}

	.option-pill {
		padding: 0 20rpx;
		border-radius: 22rpx;
		background: #f7f9fc;
		box-shadow: inset 0 0 0 2rpx #e5ebf4;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-weight: 600;
		color: #475467;
	}

	.option-pill.active {
		background: #eef4ff;
		box-shadow: inset 0 0 0 2rpx #386df5, 0 10rpx 20rpx rgba(56, 109, 245, 0.12);
		color: #386df5;
	}

	.subject-grid {
		margin-top: 18rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 12rpx;
	}

	.subject-tile {
		position: relative;
		min-height: 92rpx;
		padding: 0 18rpx;
		border-radius: 24rpx;
		background: #f8fafc;
		box-shadow: inset 0 0 0 2rpx #e5ebf4;
		display: flex;
		align-items: center;
		justify-content: flex-start;
	}

	.subject-tile-main {
		display: flex;
		align-items: center;
		gap: 14rpx;
	}

	.subject-tile-icon {
		width: 48rpx;
		height: 48rpx;
		border-radius: 16rpx;
		background: #ffffff;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 18rpx rgba(31, 45, 61, 0.05);
	}

	.subject-tile-text {
		font-size: 26rpx;
		font-weight: 600;
		color: #475467;
	}

	.subject-tile-mark {
		position: absolute;
		top: 12rpx;
		right: 12rpx;
		width: 28rpx;
		height: 28rpx;
		border-radius: 50%;
		background: #16a34a;
		border: 3rpx solid #ffffff;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.subject-tile.active {
		background: linear-gradient(135deg, #edf3ff 0%, #f8faff 100%);
		box-shadow: inset 0 0 0 2rpx #386df5, 0 12rpx 22rpx rgba(56, 109, 245, 0.12);
	}

	.subject-tile.active .subject-tile-icon {
		box-shadow: inset 0 0 0 2rpx rgba(56, 109, 245, 0.12);
	}

	.subject-tile.active .subject-tile-text {
		color: #386df5;
	}

	.plan-top {
		padding: 6rpx 0 10rpx;
		position: relative;
	}

	.plan-top::before {
		content: '';
		position: absolute;
		top: 96rpx;
		right: 10rpx;
		width: 180rpx;
		height: 180rpx;
		border-radius: 50%;
		background: radial-gradient(circle, rgba(56, 109, 245, 0.12) 0%, rgba(56, 109, 245, 0) 72%);
		pointer-events: none;
	}

	.top-copy {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.top-badge {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		align-self: flex-start;
		padding: 10rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.78);
		box-shadow: 0 10rpx 22rpx rgba(36, 48, 65, 0.05);
		font-size: 22rpx;
		font-weight: 500;
		color: #386df5;
	}

	.top-title {
		display: block;
		font-size: 50rpx;
		line-height: 1.12;
		font-weight: 600;
		color: #182230;
	}

	.top-sub {
		display: block;
		font-size: 24rpx;
		line-height: 1.6;
		color: #667085;
	}

	.hero-paper {
		position: relative;
		margin-top: 24rpx;
		padding: 24rpx 22rpx 22rpx;
		border-radius: 28rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, rgba(247, 250, 255, 0.98) 100%);
		box-shadow: 0 18rpx 42rpx rgba(36, 48, 65, 0.08);
		overflow: hidden;
	}

	.hero-paper::after {
		content: '';
		position: absolute;
		inset: 0;
		background-image: linear-gradient(rgba(56, 109, 245, 0.05) 1rpx, transparent 1rpx), linear-gradient(90deg, rgba(56, 109, 245, 0.04) 1rpx, transparent 1rpx);
		background-size: 24rpx 24rpx;
		opacity: 0.4;
		pointer-events: none;
	}

	.paper-head {
		position: relative;
		z-index: 1;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.paper-badge {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 10rpx 16rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		font-size: 22rpx;
		font-weight: 500;
		color: #386df5;
	}

	.paper-tag {
		font-size: 20rpx;
		font-weight: 500;
		color: #98a2b3;
	}

	.paper-title {
		position: relative;
		z-index: 1;
		display: block;
		margin-top: 18rpx;
		font-size: 26rpx;
		line-height: 1.55;
		font-weight: 600;
		color: #182230;
	}

	.paper-lines {
		position: relative;
		z-index: 1;
		margin-top: 18rpx;
		display: flex;
		flex-direction: column;
		gap: 10rpx;
	}

	.paper-line {
		height: 10rpx;
		border-radius: 999rpx;
		background: #d7e3fb;
	}

	.paper-line-main {
		width: 100%;
		background: linear-gradient(90deg, #386df5 0%, #7ea3ff 100%);
	}

	.paper-line.short {
		width: 58%;
	}

	.section-band {
		margin-top: 18rpx;
		padding: 24rpx;
		border-radius: 28rpx;
		background: #ffffff;
		box-shadow: var(--shadow-card);
		border: 2rpx solid rgba(228, 235, 247, 0.86);
	}

	.plan-page--edit .section-band {
		margin-top: 16rpx;
	}

	.section-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 20rpx;
	}

	.section-title {
		display: block;
		font-size: 30rpx;
		line-height: 1.2;
		font-weight: 600;
		color: #1d2939;
	}

	.section-sub {
		display: block;
		margin-top: 10rpx;
		font-size: 22rpx;
		line-height: 1.55;
		color: #98a2b3;
	}

	.count-badge {
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: linear-gradient(180deg, #f4f7ff 0%, #edf3ff 100%);
		font-size: 20rpx;
		font-weight: 500;
		color: #386df5;
		flex-shrink: 0;
		box-shadow: inset 0 0 0 2rpx rgba(56, 109, 245, 0.08);
	}

	.grade-row {
		margin-top: 18rpx;
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 12rpx;
	}

	.grade-pill {
		min-width: 0;
		height: 74rpx;
		padding: 0 16rpx;
		border-radius: 999rpx;
		background: linear-gradient(180deg, #ffffff 0%, #f6f8fc 100%);
		box-shadow: inset 0 0 0 2rpx #ebeff5, 0 6rpx 14rpx rgba(31, 45, 61, 0.03);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-weight: 500;
		color: #475467;
		transition: all 0.2s ease;
	}

	.grade-pill.active {
		background: #386df5;
		box-shadow: 0 16rpx 30rpx rgba(56, 109, 245, 0.24);
		color: #ffffff;
	}

	.stage-pill {
		min-width: 180rpx;
	}

	.stage-empty-tip {
		display: block;
		margin-top: 24rpx;
		font-size: 22rpx;
		line-height: 1.55;
		color: #98a2b3;
	}

	.subject-row {
		margin-top: 18rpx;
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 12rpx;
	}

	.subject-pill {
		position: relative;
		min-width: 0;
		height: 74rpx;
		padding: 0 12rpx;
		border-radius: 999rpx;
		background: #fcfdff;
		box-shadow: inset 0 0 0 2rpx #e4eaf3;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		font-weight: 500;
		color: #475467;
		transition: all 0.2s ease;
	}

	.subject-pill.active {
		background: #386df5;
		box-shadow: 0 12rpx 24rpx rgba(56, 109, 245, 0.22);
		color: #ffffff;
	}

	.subject-check {
		position: absolute;
		top: -6rpx;
		right: -6rpx;
		width: 28rpx;
		height: 28rpx;
		border-radius: 50%;
		background: #16a34a;
		border: 3rpx solid #ffffff;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.version-row {
		margin-top: 24rpx;
	}

	.version-pill {
		min-width: calc(50% - 6rpx);
		flex: 1 1 calc(50% - 6rpx);
	}

	.version-trigger {
		margin-top: 18rpx;
		padding: 20rpx;
		border-radius: 24rpx;
		background: linear-gradient(180deg, #fbfcff 0%, #f4f8ff 100%);
		box-shadow: inset 0 0 0 2rpx #e3eaf6, 0 8rpx 16rpx rgba(31, 45, 61, 0.04);
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 20rpx;
	}

	.version-trigger-main {
		min-width: 0;
		display: flex;
		align-items: center;
		gap: 18rpx;
	}

	.version-icon {
		width: 60rpx;
		height: 60rpx;
		border-radius: 18rpx;
		background: #ffffff;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: 0 10rpx 24rpx rgba(36, 48, 65, 0.06);
	}

	.version-copy {
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	.version-label {
		display: block;
		font-size: 26rpx;
		line-height: 1.35;
		font-weight: 600;
		color: #111827;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.version-hint {
		display: block;
		font-size: 22rpx;
		line-height: 1.45;
		color: #98a2b3;
	}

	.plan-footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 10;
		padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));
		background: rgba(255, 255, 255, 0.98);
		box-shadow: 0 -8rpx 24rpx rgba(36, 48, 65, 0.06);
		backdrop-filter: blur(8px);
	}

	.plan-footer .solid-button {
		height: 88rpx;
		border-radius: 20rpx;
		font-weight: 600;
	}

	.solid-button.disabled {
		background: #b9c9f8;
		opacity: 1;
		box-shadow: none;
	}

	.version-sheet {
		padding: 28rpx 28rpx calc(28rpx + env(safe-area-inset-bottom));
		background: #ffffff;
	}

	.sheet-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 20rpx;
	}

	.sheet-title {
		display: block;
		font-size: 32rpx;
		line-height: 1.25;
		font-weight: 600;
		color: #101828;
	}

	.sheet-sub {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		line-height: 1.5;
		color: #98a2b3;
	}

	.sheet-close {
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: #f4f6fa;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.sheet-scroll {
		max-height: 58vh;
		margin-top: 18rpx;
	}

	.sheet-option {
		padding: 20rpx 18rpx;
		border-radius: 24rpx;
		background: #f8fafc;
		box-shadow: inset 0 0 0 2rpx #edf2f7;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 18rpx;
		margin-top: 16rpx;
	}

	.sheet-option:first-child {
		margin-top: 0;
	}

	.sheet-option.active {
		background: #eef4ff;
		box-shadow: inset 0 0 0 2rpx #386df5;
	}

	.sheet-option-copy {
		min-width: 0;
		flex: 1;
	}

	.sheet-option-title {
		display: block;
		font-size: 28rpx;
		line-height: 1.35;
		font-weight: 500;
		color: #111827;
	}

	.plan-footer .solid-button {
		font-weight: 600;
	}

	.sheet-option-sub {
		display: block;
		margin-top: 6rpx;
		font-size: 22rpx;
		line-height: 1.45;
		color: #98a2b3;
	}

	.sheet-option-mark {
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background: #d0d5dd;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.sheet-option-mark.active {
		background: #386df5;
	}

	.plan-options-loading,
	.plan-options-error {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 20rpx;
		min-height: 420rpx;
		padding: 48rpx 32rpx;
		text-align: center;
		color: #667085;
		font-size: 26rpx;
	}

	.plan-options-error-text {
		line-height: 1.5;
	}

	.plan-retry-btn {
		margin-top: 8rpx;
		padding: 18rpx 40rpx;
	}

	.phone-bind-shell {
		margin-top: 12rpx;
		padding: 36rpx 28rpx;
		display: flex;
		flex-direction: column;
		align-items: stretch;
		gap: 24rpx;
	}

	.phone-bind-head {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.phone-bind-badge {
		align-self: flex-start;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		font-size: 20rpx;
		font-weight: 600;
		color: #386df5;
	}

	.phone-bind-title {
		font-size: 34rpx;
		font-weight: 700;
		color: #182230;
	}

	.phone-bind-sub {
		font-size: 24rpx;
		line-height: 1.6;
		color: #667085;
	}

	.phone-bind-btn {
		height: 88rpx;
		border-radius: 20rpx;
		background: linear-gradient(135deg, #386df5 0%, #4f46e5 100%);
		color: #ffffff;
		font-size: 28rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		border: none;
		padding: 0;
		line-height: 1;
	}

	.phone-bind-btn::after {
		border: none;
	}

	/* #ifdef MP-WEIXIN */
	.subject-grid,
	.grade-row,
	.subject-row {
		display: flex;
		flex-wrap: wrap;
	}

	.subject-grid .subject-tile {
		flex: 0 0 calc(50% - 6rpx);
	}

	.grade-row .grade-pill,
	.subject-row .subject-pill {
		flex: 0 0 calc(33.33% - 8rpx);
	}

	.plan-footer {
		backdrop-filter: none;
		background: #ffffff;
	}
	/* #endif */

	@media screen and (max-width: 375px) {
		.overview-main,
		.step-head--split {
			flex-direction: column;
		}

		.top-title {
			font-size: 44rpx;
		}

		.subject-row,
		.grade-row {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}

		.subject-grid {
			grid-template-columns: 1fr;
		}

		/* #ifdef MP-WEIXIN */
		.grade-row .grade-pill,
		.subject-row .subject-pill,
		.subject-grid .subject-tile {
			flex-basis: calc(50% - 6rpx);
		}
		/* #endif */
	}
</style>
