/**
 * 模考倒计时：真题模拟 examMode 启用，到时自动交卷
 */
import { EXAM_DEFAULT_DURATION_MINUTES } from '@/common/practice-config';

/** 剩余不足该秒数时倒计时高亮警示 */
export const EXAM_TIMER_WARN_SECONDS = 5 * 60;

/**
 * 解析模考时长（分钟）
 * @param {object} source - practiceSource
 */
export const resolveExamDurationMinutes = (source = {}) => {
	if (!source.examMode) {
		return 0;
	}
	const minutes = Number(source.examDurationMinutes);
	if (minutes > 0) {
		return minutes;
	}
	return EXAM_DEFAULT_DURATION_MINUTES;
};

/** 模考总时长（秒） */
export const resolveExamDurationSeconds = (source = {}) => (
	resolveExamDurationMinutes(source) * 60
);

/**
 * 格式化为 MM:SS 或 HH:MM:SS
 * @param {number} totalSeconds
 */
export const formatExamCountdown = (totalSeconds = 0) => {
	const seconds = Math.max(0, Math.floor(Number(totalSeconds) || 0));
	const hours = Math.floor(seconds / 3600);
	const minutes = Math.floor((seconds % 3600) / 60);
	const secs = seconds % 60;
	const pad = (value) => String(value).padStart(2, '0');
	if (hours > 0) {
		return `${pad(hours)}:${pad(minutes)}:${pad(secs)}`;
	}
	return `${pad(minutes)}:${pad(secs)}`;
};
