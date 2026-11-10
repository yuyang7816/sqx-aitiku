/**
 * 模块二十二 P0#6 · 拍照/相册选图
 * ActionSheet 选源 → uni.chooseImage → 携带 imagePath 进入 OCR 结果页并启动识别
 * （首页 8 宫格、AI Tab 拍照卡片、对话页相机按钮等共用）
 */
import { OCR_RESULT_PAGE } from '@/common/routes';

/** ActionSheet 选项：拍照 / 相册 */
const OCR_IMAGE_SOURCE_OPTIONS = [{
	key: 'camera',
	label: '拍照',
	sourceType: ['camera']
}, {
	key: 'album',
	label: '从相册选择',
	sourceType: ['album']
}];

const buildOcrResultUrl = ({ imagePath = '', topic = '', fromChat = false } = {}) => {
	const query = [];
	const normalizedImagePath = String(imagePath || '').trim();
	const normalizedTopic = String(topic || '').trim();
	if (normalizedImagePath) {
		query.push(`imagePath=${encodeURIComponent(normalizedImagePath)}`);
	}
	if (normalizedTopic) {
		query.push(`topic=${encodeURIComponent(normalizedTopic)}`);
	}
	if (fromChat) {
		query.push('fromChat=1');
	}
	if (!query.length) {
		return OCR_RESULT_PAGE;
	}
	return `${OCR_RESULT_PAGE}?${query.join('&')}`;
};

const isUserCancelError = (error = {}) => {
	const message = String(error.errMsg || error.message || '');
	return message.includes('cancel') || message.includes('取消');
};

const chooseOcrImageBySource = ({ sourceType = ['album', 'camera'], topic = '', fromChat = false } = {}) => {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType,
		success: (res) => {
			const imagePath = res?.tempFilePaths?.[0] || '';
			if (!imagePath) {
				uni.showToast({
					title: '未获取到图片，请重试',
					icon: 'none'
				});
				return;
			}
			uni.navigateTo({
				url: buildOcrResultUrl({
					imagePath,
					topic,
					fromChat
				})
			});
		},
		fail: (error) => {
			if (isUserCancelError(error)) {
				return;
			}
			uni.showToast({
				title: '选图失败，请重试',
				icon: 'none'
			});
		}
	});
};

/**
 * 调起 uni.chooseImage，选图成功后跳转 OCR 结果页并携带本地临时路径
 * @param {{ topic?: string, fromChat?: boolean }} [options] - 可选题干关键词；fromChat 表示从对话页进入
 */
export const pickOcrImage = ({ topic = '', fromChat = false } = {}) => {
	chooseOcrImageBySource({
		sourceType: ['album', 'camera'],
		topic,
		fromChat
	});
};

export const pickOcrImageBySource = ({ sourceType = ['album', 'camera'], topic = '', fromChat = false } = {}) => {
	chooseOcrImageBySource({ sourceType, topic, fromChat });
};

/**
 * 先展示「拍照 / 相册」选项，再选图并进入 OCR 识别页
 * @param {{ topic?: string, fromChat?: boolean }} [options]
 */
export const openOcrImagePicker = ({ topic = '', fromChat = false } = {}) => {
	uni.showActionSheet({
		itemList: OCR_IMAGE_SOURCE_OPTIONS.map((item) => item.label),
			success: (res) => {
				const option = OCR_IMAGE_SOURCE_OPTIONS[res.tapIndex];
				if (!option) {
					return;
				}
				chooseOcrImageBySource({
					sourceType: option.sourceType,
					topic,
					fromChat
				});
			}
		});
};
