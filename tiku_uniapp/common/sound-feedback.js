/**
 * 提示音反馈（设置页 soundEnabled 开关，持久化 SETTINGS_KEY）
 * 关闭后所有 play* 方法静默不播放
 */
import { isSoundEnabled } from '@/common/app-data';

/** 轻量点击提示音（880Hz / 60ms，base64 WAV） */
const PROMPT_TAP_WAV = 'data:audio/wav;base64,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';

let promptAudio = null;

const getPromptAudio = () => {
	if (!promptAudio) {
		promptAudio = uni.createInnerAudioContext();
		promptAudio.volume = 0.35;
		// #ifdef MP-WEIXIN
		promptAudio.obeyMuteSwitch = false;
		// #endif
	}
	return promptAudio;
};

/** 播放轻量提示音（soundEnabled 关闭时不播放） */
const playPromptTap = () => {
	if (!isSoundEnabled()) {
		return;
	}
	try {
		const audio = getPromptAudio();
		audio.stop();
		audio.src = PROMPT_TAP_WAV;
		audio.play();
	} catch (e) {
		// 播放失败不影响主流程
	}
};

/** 答题页选择/填写答案时 */
export const playAnswerTapSound = () => {
	playPromptTap();
};

/** 消息中心操作（打开消息、全部已读） */
export const playMessageActionSound = () => {
	playPromptTap();
};
