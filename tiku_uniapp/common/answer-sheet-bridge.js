/**
 * 答题页 ↔ 答题卡页临时状态桥接（跳转独立答题卡页时读写）
 */
import { QUESTION_ANSWER_SHEET_KEY } from '@/common/storage-keys';

const readState = () => {
	const saved = uni.getStorageSync(QUESTION_ANSWER_SHEET_KEY);
	return saved && typeof saved === 'object' ? saved : null;
};

/** 答题页进入答题卡前写入当前答题态 */
export const pushAnswerSheetState = ({
	questionList = [],
	selectedMap = {},
	currentIndex = 0
} = {}) => {
	uni.setStorageSync(QUESTION_ANSWER_SHEET_KEY, {
		questionList: Array.isArray(questionList) ? questionList : [],
		selectedMap: selectedMap && typeof selectedMap === 'object' ? { ...selectedMap } : {},
		currentIndex: Math.max(0, Number(currentIndex) || 0),
		jumpToIndex: null,
		requestComplete: false
	});
};

/** 答题卡页读取展示数据 */
export const getAnswerSheetState = () => readState();

/** 答题卡页选中题号后标记跳转并返回答题页 */
export const markAnswerSheetJump = (index = 0) => (
	markAnswerSheetReturn({ index, complete: false })
);

/**
 * 答题卡返回答题页：同步题号，可选触发完成练习
 * @param {{ index?: number, complete?: boolean }} [options]
 */
export const markAnswerSheetReturn = ({ index, complete = false } = {}) => {
	const state = readState();
	if (!state) {
		return false;
	}
	const nextIndex = index != null
		? Math.max(0, Number(index) || 0)
		: Math.max(0, Number(state.currentIndex) || 0);
	uni.setStorageSync(QUESTION_ANSWER_SHEET_KEY, {
		...state,
		currentIndex: nextIndex,
		jumpToIndex: nextIndex,
		requestComplete: Boolean(complete)
	});
	return true;
};

/**
 * 答题页 onShow 时消费答题卡回传（题号 / 完成练习）
 * @returns {{ jumpToIndex: number|null, requestComplete: boolean }|null}
 */
export const consumeAnswerSheetReturn = () => {
	const state = readState();
	if (!state) {
		return null;
	}
	const jumpToIndex = state.jumpToIndex != null
		? Math.max(0, Number(state.jumpToIndex) || 0)
		: null;
	const requestComplete = Boolean(state.requestComplete);
	if (jumpToIndex == null && !requestComplete) {
		return null;
	}
	uni.setStorageSync(QUESTION_ANSWER_SHEET_KEY, {
		...state,
		currentIndex: jumpToIndex ?? state.currentIndex,
		jumpToIndex: null,
		requestComplete: false
	});
	return {
		jumpToIndex,
		requestComplete
	};
};

/** @deprecated 请使用 consumeAnswerSheetReturn */
export const consumeAnswerSheetJumpIndex = () => {
	const ret = consumeAnswerSheetReturn();
	if (!ret || ret.jumpToIndex == null) {
		return null;
	}
	return ret.jumpToIndex;
};

/** 交卷或退出练习时清除桥接态 */
export const clearAnswerSheetState = () => {
	uni.removeStorageSync(QUESTION_ANSWER_SHEET_KEY);
};
