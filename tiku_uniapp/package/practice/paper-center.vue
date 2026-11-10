<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen paper-screen">
				<view class="paper-hero">
					<view class="tab-row">
						<view
							v-for="item in tabs"
							:key="item.key"
							class="tab-item"
							:class="{ active: activeTab === item.key }"
							@tap="switchTab(item.key)"
						>{{ item.label }}</view>
					</view>
				</view>

				<view class="paper-summary">
					<text class="section-sub">{{ activeTabCopy }}</text>
					<text v-if="!loading && paperList.length" class="summary-count">共 {{ paperList.length }} 套题集</text>
				</view>

				<view v-if="loading" class="state-box state-card">
					<view class="state-icon loading-tone">
						<up-loading-icon mode="circle" size="22" color="#4E88F6"></up-loading-icon>
					</view>
					<text class="state-title">加载题集中...</text>
					<text class="state-copy">正在整理适合当前模式的题集</text>
				</view>
				<view v-else-if="!paperList.length" class="state-box state-card">
					<view class="state-icon empty-tone">
						<up-icon name="file-text-fill" size="32" color="#A6B6CC"></up-icon>
					</view>
					<text class="state-title">暂无题集</text>
					<text class="state-copy">当前模式下还没有可练习的题集内容</text>
				</view>
				<view v-else class="paper-list">
					<view
						v-for="item in paperList"
						:key="item.paperId"
						class="paper-card"
						@tap="openPaper(item)"
					>
						<view class="paper-accent"></view>
						<view class="paper-icon" :class="activeTab === PAPER_TYPE_EXAM ? 'exam' : 'special'">
							<up-icon :name="activeTab === PAPER_TYPE_EXAM ? 'file-text-fill' : 'edit-pen-fill'" size="20" :color="activeTab === PAPER_TYPE_EXAM ? '#FF8A3D' : '#4E88F6'"></up-icon>
						</view>
						<view class="paper-copy">
							<text class="paper-title">{{ item.title }}</text>
							<text class="paper-meta">{{ buildPaperMeta(item) }}</text>
							<text v-if="item.description" class="paper-desc">{{ item.description }}</text>
						</view>
						<view class="paper-action">
							<up-icon name="arrow-right" size="14" color="#7FA8F8"></up-icon>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import { getUserProfile, setPracticeSource } from '@/common/app-data';
	import {
		fetchPaperList,
		PAPER_TYPE_EXAM,
		PAPER_TYPE_SPECIAL
	} from '@/common/paper-list';
	import {
		getPracticeActiveChip,
		resolvePracticeChipFromQuery,
		resolvePracticeSubjectFromChip,
		setPracticeActiveChip
	} from '@/common/practice-chip-context';
	import { EXAM_SIMULATION_TITLE, SPECIAL_TRAINING_TITLE } from '@/common/practice-config';
	import { QUESTION_ANSWER_PAGE } from '@/common/routes';

	const profile = ref(getUserProfile());
	const activeChip = ref(getPracticeActiveChip());

	const tabs = [
		{ key: PAPER_TYPE_SPECIAL, label: '专项训练' },
		{ key: PAPER_TYPE_EXAM, label: '真题模拟' }
	];

	const activeTab = ref(PAPER_TYPE_SPECIAL);
	const paperList = ref([]);
	const loading = ref(false);

	const activeTabCopy = ref('按专题攻克薄弱项，精选题集一键开练');

	const applyTab = (key = '') => {
		const tab = String(key || '').trim();
		if (tab !== PAPER_TYPE_EXAM && tab !== PAPER_TYPE_SPECIAL) {
			return;
		}
		activeTab.value = tab;
		activeTabCopy.value = tab === PAPER_TYPE_EXAM
			? '历年真题实战模考，计时交卷更贴近考场'
			: '按专题攻克薄弱项，精选题集一键开练';
	};

	const buildPaperMeta = (item = {}) => {
		const parts = [];
		if (item.subject) {
			parts.push(item.subject);
		}
		if (item.gradeShort || item.grade) {
			parts.push(item.gradeShort || item.grade);
		}
		if (item.questionCount) {
			parts.push(`${item.questionCount}题`);
		}
		return parts.join(' · ') || '精选题集';
	};

	const filterPapersByChip = (list = []) => {
		const subject = resolvePracticeSubjectFromChip(activeChip.value, profile.value);
		if (!subject) {
			return list;
		}
		const matched = list.filter((item) => !item.subject || item.subject === subject);
		return matched.length ? matched : list;
	};

	const refreshPapers = async () => {
		loading.value = true;
		try {
			const list = await fetchPaperList(activeTab.value);
			paperList.value = filterPapersByChip(list);
		} finally {
			loading.value = false;
		}
	};

	const switchTab = (key) => {
		applyTab(key);
		refreshPapers();
	};

	const openPaper = (item) => {
		const isExam = activeTab.value === PAPER_TYPE_EXAM;
		setPracticeSource({
			mode: 'all',
			title: item.title || (isExam ? EXAM_SIMULATION_TITLE : SPECIAL_TRAINING_TITLE),
			paperId: item.paperId,
			examMode: isExam,
			examDurationMinutes: isExam ? 90 : 0
		});
		uni.navigateTo({ url: QUESTION_ANSWER_PAGE });
	};

	onLoad((query) => {
		activeChip.value = resolvePracticeChipFromQuery(query);
		setPracticeActiveChip(activeChip.value);
		applyTab(query?.tab);
	});

	onShow(() => {
		profile.value = getUserProfile();
		refreshPapers();
	});
</script>

<style lang="scss" scoped>
	.paper-screen {
		padding: 24rpx 24rpx 40rpx;
		background: linear-gradient(180deg, #F4F8FF 0, #F8FAFD 220rpx, #F6F8FC 100%);
	}

	.paper-hero {
		padding: 20rpx;
		border-radius: 28rpx;
		background: linear-gradient(135deg, #FFFFFF 0%, #F5F9FF 100%);
		box-shadow: 0 16rpx 34rpx rgba(45, 86, 156, 0.08);
	}

	.tab-row {
		display: flex;
		gap: 12rpx;
	}

	.tab-item {
		flex: 1;
		height: 74rpx;
		border-radius: 999rpx;
		background: #F2F5FA;
		color: #667085;
		font-size: 24rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.tab-item.active {
		background: linear-gradient(135deg, #4E88F6 0%, #6AA0FB 100%);
		color: #fff;
		box-shadow: 0 12rpx 24rpx rgba(78, 136, 246, 0.18);
	}

	.paper-summary,
	.state-box,
	.paper-list {
		margin-top: 24rpx;
	}

	.paper-summary {
		padding: 0 8rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.section-sub {
		flex: 1;
		font-size: 22rpx;
		line-height: 1.5;
		color: #7B8798;
	}

	.summary-count {
		font-size: 20rpx;
		color: #9AA5B6;
		flex-shrink: 0;
	}

	.state-box {
		padding: 72rpx 28rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		text-align: center;
	}

	.state-card {
		border-radius: 28rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 12rpx 28rpx rgba(45, 86, 156, 0.06);
	}

	.state-icon {
		width: 88rpx;
		height: 88rpx;
		border-radius: 26rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.loading-tone {
		background: rgba(78, 136, 246, 0.1);
	}

	.empty-tone {
		background: #EEF3FA;
	}

	.state-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #23324A;
	}

	.state-copy {
		font-size: 22rpx;
		line-height: 1.6;
		color: #7B8798;
	}

	.paper-list {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.paper-card {
		position: relative;
		padding: 24rpx;
		display: flex;
		align-items: center;
		gap: 20rpx;
		border-radius: 26rpx;
		background: #FFFFFF;
		box-shadow: 0 12rpx 28rpx rgba(33, 70, 136, 0.06);
		border: 1rpx solid rgba(102, 144, 214, 0.08);
	}

	.paper-accent {
		position: absolute;
		left: 0;
		top: 20rpx;
		bottom: 20rpx;
		width: 8rpx;
		border-radius: 0 999rpx 999rpx 0;
		background: linear-gradient(180deg, #4E88F6 0%, #79ABFA 100%);
	}

	.paper-icon {
		width: 84rpx;
		height: 84rpx;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.5);
	}

	.paper-icon.special {
		background: linear-gradient(180deg, #EEF4FF 0%, #E4EEFF 100%);
	}

	.paper-icon.exam {
		background: linear-gradient(180deg, #FFF3E8 0%, #FFE7D0 100%);
	}

	.paper-copy {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
		justify-content: center;
	}

	.paper-title {
		font-size: 29rpx;
		font-weight: 600;
		color: #22324B;
		line-height: 1.4;
	}

	.paper-meta {
		font-size: 21rpx;
		line-height: 1.5;
		color: #8591A3;
	}

	.paper-desc {
		font-size: 22rpx;
		color: #667085;
		line-height: 1.55;
	}

	.paper-action {
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		background: #F2F6FD;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}
</style>
