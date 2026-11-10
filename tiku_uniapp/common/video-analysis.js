import { getMemberState, QUESTION_REVIEW_KEY, setQuestionReviewItem } from '@/common/app-data';
import { fetchQuestionVideo } from '@/common/question-video-api';
import { QUESTION_VIDEO_ANALYSIS_PAGE } from '@/common/routes';

/** 空视频结构（接口无数据时） */
const EMPTY_VIDEO = {
	videoUrl: '',
	posterUrl: '',
	duration: '',
	teacher: '',
	summary: ''
};

/** 规范化题目上下文：兼容解析页 reviewItem / 错题本 / OCR 结果字段 */
export const normalizeQuestionVideoContext = (raw = {}) => {
	if (!raw || typeof raw !== 'object') {
		return {
			title: '',
			questionCode: '',
			questionId: '',
			subject: '',
			analysis: '',
			rightAnswer: ''
		};
	}
	return {
		title: String(raw.title || raw.question || '').trim(),
		questionCode: String(raw.questionCode || '').trim(),
		questionId: String(raw.questionId || raw.id || '').trim(),
		subject: String(raw.subject || '').trim(),
		analysis: String(raw.analysis || '').trim(),
		rightAnswer: String(raw.rightAnswer || raw.right || raw.answer || '').trim()
	};
};

/** 按 API 返回组装视频信息，无数据时返回空结构 */
export const resolveQuestionVideo = (raw = {}) => {
	const context = normalizeQuestionVideoContext(raw);
	return {
		title: context.title || '题目视频讲解',
		subject: context.subject,
		analysis: context.analysis,
		rightAnswer: context.rightAnswer,
		videoUrl: '',
		posterUrl: '',
		duration: '',
		teacher: '',
		summary: '',
		fromCatalog: false,
		...EMPTY_VIDEO
	};
};

/** 异步解析视频：优先 GET /app/question/video */
export const resolveQuestionVideoAsync = async (raw = {}) => {
	const context = normalizeQuestionVideoContext(raw);
	const apiResult = await fetchQuestionVideo({
		questionCode: context.questionCode,
		questionId: context.questionId
	});
	if (apiResult.fromApi && apiResult.video?.videoUrl) {
		return {
			...apiResult.video,
			title: apiResult.video.title || context.title || '题目视频讲解',
			subject: context.subject,
			analysis: context.analysis,
			rightAnswer: context.rightAnswer,
			fromCatalog: apiResult.video.fromServer
		};
	}
	return resolveQuestionVideo(raw);
};

/** 校验高清视频解析权益：仅 VIP 可播放 */
export const checkVideoAnalysisAccess = () => {
	const member = getMemberState();
	return {
		ok: Boolean(member.isVip),
		isVip: Boolean(member.isVip)
	};
};

/** 非 VIP 引导开通会员（与 AI/OCR 次数用尽弹窗风格一致） */
export const showVideoVipRequiredModal = () => {
	uni.showModal({
		title: '高清视频解析为会员权益',
		content: '开通 AI 至尊会员后可观看名师高清视频讲解，逐步拆解解题思路。',
		confirmText: '开通会员',
		cancelText: '知道了',
		success: (res) => {
			if (res.confirm) {
				uni.navigateTo({ url: '/package/member/center' });
			}
		}
	});
};

/**
 * 跳转高清视频解析页
 * @param {Object} [item] 可选题目上下文；传入时写入 QUESTION_REVIEW_KEY
 */
export const openQuestionVideoAnalysis = (item) => {
	const context = normalizeQuestionVideoContext(item || uni.getStorageSync(QUESTION_REVIEW_KEY));
	if (!context.title) {
		uni.showToast({ title: '暂无题目可播放视频', icon: 'none' });
		return false;
	}

	const access = checkVideoAnalysisAccess();
	if (!access.ok) {
		showVideoVipRequiredModal();
		return false;
	}

	if (item && typeof item === 'object') {
		setQuestionReviewItem({
			...item,
			title: context.title,
			questionCode: context.questionCode || item.questionCode,
			questionId: context.questionId || item.questionId
		});
	}

	uni.navigateTo({ url: QUESTION_VIDEO_ANALYSIS_PAGE });
	return true;
};

/** 视频解析页读取 Storage 中的题目并解析视频信息（异步 API 优先） */
export const loadQuestionVideoFromReview = async () => {
	const saved = uni.getStorageSync(QUESTION_REVIEW_KEY);
	const context = normalizeQuestionVideoContext(saved);
	if (!context.title) {
		return {
			hasQuestion: false,
			canPlay: false,
			video: resolveQuestionVideo({})
		};
	}
	const access = checkVideoAnalysisAccess();
	const video = await resolveQuestionVideoAsync(saved);
	return {
		hasQuestion: true,
		canPlay: access.ok,
		isVip: access.isVip,
		video
	};
};
