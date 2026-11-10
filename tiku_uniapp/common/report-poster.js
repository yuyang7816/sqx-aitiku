/**
 * 月度学习报告海报：canvas 绘制报告摘要 + 二维码（优先服务端小程序码）
 */
import config from '@/common/config';
import { get } from '@/common/api';
import { getAuthState } from '@/common/app-data';
import {
	fetchReportRank,
	REPORT_MONTH_TITLE_FALLBACK,
	REPORT_RANK_PERCENT_FALLBACK,
	REPORT_RANK_SUBTITLE_FALLBACK,
	REPORT_RANK_TITLE_FALLBACK
} from '@/common/analysis-report';
import { POSTER_PAGE } from '@/common/routes';
import { savePosterImage, sharePosterImage } from '@/common/poster-save-share';
import { LEGACY_APP_URL_KEY } from '@/common/storage-keys';

export const REPORT_POSTER_CANVAS_ID = 'reportPosterCanvas';
export const REPORT_POSTER_QR_CANVAS_ID = 'reportPosterQrCanvas';

const POSTER_CANVAS_WIDTH = 750;
const POSTER_CANVAS_HEIGHT = 1100;
const POSTER_PADDING = 40;
const POSTER_CARD_RADIUS = 28;
const POSTER_QR_SIZE = 100;
const POSTER_TITLE_CHARS = 14;
const POSTER_SUB_CHARS = 18;

/** 海报文案换行（canvas 与预览复用） */
export const wrapPosterTextLines = (text = '', maxChars = POSTER_SUB_CHARS) => {
	const source = String(text || '').trim();
	if (!source) {
		return ['—'];
	}
	const lines = [];
	let current = '';
	Array.from(source).forEach((char) => {
		if (current.length >= maxChars) {
			lines.push(current);
			current = char;
			return;
		}
		current += char;
	});
	if (current) {
		lines.push(current);
	}
	return lines.length ? lines : ['—'];
};

/** 二维码内容：优先 H5 首页，其次本地缓存 appurl */
export const buildReportPosterQrText = () => {
	// #ifdef H5
	if (typeof window !== 'undefined' && window.location?.origin) {
		const base = `${window.location.origin}${window.location.pathname || '/'}`;
		return `${base}#/pages/index/index`;
	}
	// #endif

	try {
		const appUrl = uni.getStorageSync(LEGACY_APP_URL_KEY);
		if (appUrl) {
			return String(appUrl).trim();
		}
	} catch (e) {
		// ignore
	}
	return 'https://tiku.xianmaxiong.com';
};

const resolveRemoteAssetUrl = (url = '') => {
	const normalized = String(url || '').trim();
	if (!normalized) {
		return '';
	}
	if (/^https?:\/\//i.test(normalized)) {
		return normalized;
	}
	if (normalized.startsWith('/')) {
		return `${config.APIHOST}${normalized}`;
	}
	return `${config.APIHOST}/${normalized}`;
};

/** 拉取 GET /app/poster/report */
const fetchReportPosterApi = async (month = '') => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return null;
	}
	try {
		const monthKey = String(month || '').trim();
		const res = await get('/app/poster/report', monthKey ? { month: monthKey } : {});
		if (res && res.code === 0 && res.data) {
			return res.data;
		}
	} catch (e) {
		// 静默回退
	}
	return null;
};

/** 将 GET /app/poster/report 或 /app/analysis/report 结果转为海报展示结构 */
export const buildReportPosterPayload = (report = {}) => {
	const monthQuestionCount = typeof report.monthQuestionCount === 'number'
		? Math.max(0, report.monthQuestionCount)
		: 128;
	const correctRate = typeof report.correctRate === 'number'
		? Math.max(0, Math.min(100, report.correctRate))
		: 76;
	const rankPercent = typeof report.rankPercent === 'number'
		? Math.max(0, Math.min(100, report.rankPercent))
		: REPORT_RANK_PERCENT_FALLBACK;

	return {
		pageTitle: '学习报告',
		badge: '本月战报',
		title: report.rankTitle || REPORT_RANK_TITLE_FALLBACK,
		sub: report.rankSubtitle || REPORT_RANK_SUBTITLE_FALLBACK,
		monthTitle: report.monthTitle || REPORT_MONTH_TITLE_FALLBACK,
		month: String(report.month || '').trim(),
		highlights: [
			{ label: '累计刷题', value: String(monthQuestionCount) },
			{ label: '正确率', value: `${correctRate}%` },
			{ label: '超越同龄人', value: `${rankPercent}%` }
		],
		qrCodeUrl: resolveRemoteAssetUrl(report.qrCodeUrl || ''),
		qrText: buildReportPosterQrText(),
		qrHint: '扫码进入 AI 题库'
	};
};

/** 拉取报告海报数据；优先 GET /app/poster/report */
export const fetchReportPosterData = async (month = '') => {
	const monthKey = String(month || '').trim();
	const posterApi = await fetchReportPosterApi(monthKey);
	if (posterApi) {
		return {
			...posterApi,
			fromApi: true
		};
	}
	const report = await fetchReportRank(monthKey ? { month: monthKey } : {});
	return {
		...report,
		fromApi: Boolean(report.fromApi)
	};
};

const drawRoundRect = (ctx, x, y, width, height, radius, fillColor) => {
	const r = Math.min(radius, width / 2, height / 2);
	ctx.save();
	ctx.beginPath();
	ctx.moveTo(x + r, y);
	ctx.lineTo(x + width - r, y);
	ctx.quadraticCurveTo(x + width, y, x + width, y + r);
	ctx.lineTo(x + width, y + height - r);
	ctx.quadraticCurveTo(x + width, y + height, x + width - r, y + height);
	ctx.lineTo(x + r, y + height);
	ctx.quadraticCurveTo(x, y + height, x, y + height - r);
	ctx.lineTo(x, y + r);
	ctx.quadraticCurveTo(x, y, x + r, y);
	ctx.closePath();
	if (fillColor) {
		ctx.setFillStyle(fillColor);
		ctx.fill();
	}
	ctx.restore();
};

const drawLines = (ctx, lines, x, y, fontSize, color, lineHeight, maxLines = 3) => {
	ctx.setFillStyle(color);
	ctx.setFontSize(fontSize);
	let cursorY = y;
	lines.slice(0, maxLines).forEach((line) => {
		ctx.fillText(line, x, cursorY);
		cursorY += lineHeight;
	});
	return cursorY;
};

const drawReportDecorations = (ctx, cardX, cardY, cardWidth) => {
	ctx.save();
	ctx.setFillStyle('rgba(255,255,255,0.10)');
	ctx.beginPath();
	ctx.arc(cardX + cardWidth - 72, cardY + 86, 96, 0, Math.PI * 2);
	ctx.fill();
	ctx.setFillStyle('rgba(255,255,255,0.06)');
	ctx.beginPath();
	ctx.arc(cardX + 68, cardY + 520, 72, 0, Math.PI * 2);
	ctx.fill();
	ctx.setStrokeStyle('rgba(255,255,255,0.10)');
	ctx.setLineWidth(2);
	ctx.beginPath();
	ctx.arc(cardX + cardWidth - 122, cardY + 132, 148, Math.PI * 0.9, Math.PI * 1.72);
	ctx.stroke();
	ctx.beginPath();
	ctx.arc(cardX + 124, cardY + 256, 92, Math.PI * 1.2, Math.PI * 1.92);
	ctx.stroke();
	ctx.setFillStyle('rgba(255,255,255,0.08)');
	drawRoundRect(ctx, cardX + cardWidth - 208, cardY + 248, 132, 132, 30, 'rgba(255,255,255,0.08)');
	ctx.restore();
};

const drawHighlightPanel = (ctx, highlights, cardX, cardY, cardWidth) => {
	const panelX = cardX + 28;
	const panelY = cardY + 328;
	const panelWidth = cardWidth - 56;
	const heroHeight = 194;
	const sideY = panelY + heroHeight + 18;
	const sideWidth = (panelWidth - 14) / 2;
	const sideHeight = 116;
	const rankItem = highlights[2] || { label: '超越同龄人', value: '—' };
	const questionItem = highlights[0] || { label: '累计刷题', value: '—' };
	const correctItem = highlights[1] || { label: '正确率', value: '—' };

	const heroGradient = ctx.createLinearGradient(panelX, panelY, panelX + panelWidth, panelY + heroHeight);
	heroGradient.addColorStop(0, 'rgba(255,255,255,0.22)');
	heroGradient.addColorStop(1, 'rgba(255,255,255,0.08)');
	drawRoundRect(ctx, panelX, panelY, panelWidth, heroHeight, 26, heroGradient);
	drawRoundRect(ctx, panelX + 20, panelY + 20, 128, 34, 17, 'rgba(255,255,255,0.18)');
	ctx.setFillStyle('#ffffff');
	ctx.setFontSize(18);
	ctx.fillText('月度荣誉', panelX + 42, panelY + 43);

	ctx.setFillStyle('rgba(255,255,255,0.68)');
	ctx.setFontSize(22);
	ctx.fillText(String(rankItem.label || '超越同龄人'), panelX + 24, panelY + 88);

	ctx.setFillStyle('#ffffff');
	ctx.setFontSize(86);
	ctx.fillText(String(rankItem.value || '—'), panelX + 20, panelY + 164);

	ctx.setFillStyle('rgba(255,255,255,0.82)');
	ctx.setFontSize(22);
	ctx.fillText('本月学习表现显著领先', panelX + 24, panelY + 188);

	ctx.setFillStyle('rgba(255,255,255,0.14)');
	drawRoundRect(ctx, panelX + panelWidth - 194, panelY + 26, 156, 110, 24, 'rgba(255,255,255,0.14)');
	ctx.setFillStyle('rgba(255,255,255,0.62)');
	ctx.setFontSize(18);
	ctx.fillText('表现评级', panelX + panelWidth - 170, panelY + 58);
	ctx.setFillStyle('#ffffff');
	ctx.setFontSize(34);
	ctx.fillText('A+', panelX + panelWidth - 170, panelY + 106);

	ctx.setFillStyle('rgba(255,255,255,0.08)');
	ctx.beginPath();
	ctx.arc(panelX + panelWidth - 76, panelY + 160, 30, 0, Math.PI * 2);
	ctx.fill();
	ctx.beginPath();
	ctx.arc(panelX + panelWidth - 118, panelY + 114, 46, 0, Math.PI * 2);
	ctx.fill();

	const drawMiniCard = (item, x) => {
		const miniGradient = ctx.createLinearGradient(x, sideY, x + sideWidth, sideY + sideHeight);
		miniGradient.addColorStop(0, 'rgba(255,255,255,0.16)');
		miniGradient.addColorStop(1, 'rgba(255,255,255,0.10)');
		drawRoundRect(ctx, x, sideY, sideWidth, sideHeight, 22, miniGradient);
		ctx.setFillStyle('rgba(255,255,255,0.68)');
		ctx.setFontSize(20);
		ctx.fillText(String(item.label || ''), x + 18, sideY + 34);
		ctx.setFillStyle('#ffffff');
		ctx.setFontSize(36);
		ctx.fillText(String(item.value || '—'), x + 18, sideY + 82);
		ctx.setFillStyle('rgba(255,255,255,0.42)');
		drawRoundRect(ctx, x + sideWidth - 54, sideY + 18, 24, 24, 12, 'rgba(255,255,255,0.12)');
	};

	drawMiniCard(questionItem, panelX);
	drawMiniCard(correctItem, panelX + sideWidth + 14);
	ctx.setTextAlign('left');
};

const drawFooterPanel = (ctx, payload, qrImagePath, cardX, cardY, cardWidth, cardHeight) => {
	const footerHeight = 242;
	const footerY = cardY + cardHeight - footerHeight;
	const footerRadius = 28;
	drawRoundRect(ctx, cardX, footerY, cardWidth, footerHeight, footerRadius, '#ffffff');

	ctx.setStrokeStyle('rgba(56,109,245,0.06)');
	ctx.setLineWidth(1);
	ctx.beginPath();
	ctx.moveTo(cardX + 28, footerY);
	ctx.lineTo(cardX + cardWidth - 28, footerY);
	ctx.stroke();

	const textX = cardX + 36;
	const qrSize = 120;
	const qrX = cardX + cardWidth - qrSize - 34;
	const qrY = footerY + 62;

	ctx.setFillStyle('#eff4ff');
	drawRoundRect(ctx, textX, footerY + 28, 116, 34, 17, '#eff4ff');
	ctx.setFillStyle('#386df5');
	ctx.setFontSize(18);
	ctx.fillText('MONTHLY REPORT', textX + 12, footerY + 50);

	ctx.setFillStyle('#2f3440');
	ctx.setFontSize(36);
	ctx.fillText(String(payload.monthTitle || REPORT_MONTH_TITLE_FALLBACK), textX, footerY + 104);

	ctx.setFillStyle('#386df5');
	ctx.setFontSize(24);
	ctx.fillText('月度学习报告', textX, footerY + 148);

	ctx.setFillStyle('#8b93a7');
	ctx.setFontSize(20);
	ctx.fillText('记录你的刷题进步与阶段表现', textX, footerY + 182);

	ctx.setFillStyle('#8b93a7');
	ctx.setFontSize(22);
	const hintLines = wrapPosterTextLines(payload.qrHint || '扫码进入 AI 题库', 12).slice(0, 2);
	let hintY = footerY + 122;
	ctx.setTextAlign('left');
	hintLines.forEach((line) => {
		ctx.fillText(line, qrX - 118, hintY);
		hintY += 30;
	});
	ctx.setFillStyle('#c7d2f5');
	ctx.fillRect(qrX - 24, footerY + 74, 1, 92);

	if (qrImagePath) {
		drawRoundRect(ctx, qrX - 8, qrY - 8, qrSize + 16, qrSize + 16, 18, '#ffffff');
		ctx.setStrokeStyle('#eef2ff');
		ctx.setLineWidth(1);
		ctx.strokeRect(qrX - 8, qrY - 8, qrSize + 16, qrSize + 16);
		ctx.drawImage(qrImagePath, qrX, qrY, qrSize, qrSize);
	} else {
		drawRoundRect(ctx, qrX, qrY, qrSize, qrSize, 16, '#eef2ff');
		ctx.setFillStyle('#386df5');
		ctx.setFontSize(24);
		ctx.setTextAlign('center');
		ctx.fillText('QR', qrX + qrSize / 2, qrY + qrSize / 2 + 8);
		ctx.setTextAlign('left');
	}
};

/** 在 canvas 上绘制报告海报 */
export const drawReportPosterCanvas = (ctx, payload = {}, qrImagePath = '', width = POSTER_CANVAS_WIDTH) => {
	const height = POSTER_CANVAS_HEIGHT;
	const cardX = POSTER_PADDING;
	const cardY = POSTER_PADDING;
	const cardWidth = width - POSTER_PADDING * 2;
	const cardHeight = height - POSTER_PADDING * 2;

	const bgGradient = ctx.createLinearGradient(0, 0, 0, height);
	bgGradient.addColorStop(0, '#eef3ff');
	bgGradient.addColorStop(1, '#f8faff');
	ctx.setFillStyle(bgGradient);
	ctx.fillRect(0, 0, width, height);

	const mainGradient = ctx.createLinearGradient(cardX, cardY, cardX + cardWidth, cardY + cardHeight);
	mainGradient.addColorStop(0, '#20356a');
	mainGradient.addColorStop(0.55, '#3a68dc');
	mainGradient.addColorStop(1, '#6c56ee');
	drawRoundRect(ctx, cardX, cardY, cardWidth, cardHeight, POSTER_CARD_RADIUS, mainGradient);
	ctx.setFillStyle('rgba(255,255,255,0.05)');
	drawRoundRect(ctx, cardX + 18, cardY + 18, cardWidth - 36, 308, 24, 'rgba(255,255,255,0.05)');
	drawReportDecorations(ctx, cardX, cardY, cardWidth);

	ctx.setFillStyle('rgba(255,255,255,0.16)');
	drawRoundRect(ctx, cardX + 28, cardY + 34, 156, 42, 21, 'rgba(255,255,255,0.16)');
	ctx.setFillStyle('#ffffff');
	ctx.setFontSize(20);
	ctx.fillText(String(payload.badge || '本月战报'), cardX + 48, cardY + 62);
	ctx.setFillStyle('rgba(255,255,255,0.14)');
	drawRoundRect(ctx, cardX + cardWidth - 176, cardY + 34, 148, 42, 21, 'rgba(255,255,255,0.14)');
	ctx.setFillStyle('#ffffff');
	ctx.setFontSize(20);
	ctx.setTextAlign('center');
	ctx.fillText(String(payload.month || '').trim() || '本月', cardX + cardWidth - 102, cardY + 62);
	ctx.setTextAlign('left');

	const titleLines = wrapPosterTextLines(payload.title, POSTER_TITLE_CHARS).slice(0, 2);
	let cursorY = drawLines(ctx, titleLines, cardX + 28, cardY + 128, 44, '#ffffff', 56, 2);

	const subLines = wrapPosterTextLines(payload.sub, POSTER_SUB_CHARS).slice(0, 3);
	cursorY = drawLines(ctx, subLines, cardX + 28, cursorY + 12, 24, 'rgba(255,255,255,0.82)', 36, 3);

	ctx.setFillStyle('rgba(255,255,255,0.18)');
	drawRoundRect(ctx, cardX + 28, cursorY + 6, 154, 34, 17, 'rgba(255,255,255,0.18)');
	ctx.setFillStyle('rgba(255,255,255,0.74)');
	ctx.setFontSize(18);
	ctx.fillText('KEEP LEARNING', cardX + 48, cursorY + 30);

	drawHighlightPanel(ctx, payload.highlights || [], cardX, cardY, cardWidth);
	drawFooterPanel(ctx, payload, qrImagePath, cardX, cardY, cardWidth, cardHeight);

	return height;
};

/**
 * 生成报告海报图片
 * @param {string} canvasId
 * @param {object} payload
 * @param {string} fallbackQrPath up-qrcode 兜底路径
 * @param {object} componentInstance
 */
export const renderReportPosterImage = async (canvasId, payload = {}, fallbackQrPath = '', componentInstance) => {
	const width = POSTER_CANVAS_WIDTH;
	const height = POSTER_CANVAS_HEIGHT;
	const qrPath = await loadReportQrImagePath(payload, fallbackQrPath);

	return new Promise((resolve, reject) => {
		const ctx = uni.createCanvasContext(canvasId, componentInstance);
		drawReportPosterCanvas(ctx, payload, qrPath, width);
		ctx.draw(false, () => {
			setTimeout(() => {
				uni.canvasToTempFilePath({
					canvasId,
					width,
					height,
					destWidth: width,
					destHeight: height,
					fileType: 'png',
					success: (res) => resolve(res.tempFilePath),
					fail: (err) => reject(err)
				}, componentInstance);
			}, 400);
		});
	});
};

const loadReportQrImagePath = (payload = {}, fallbackQrPath = '') => {
	const serverUrl = String(payload.qrCodeUrl || '').trim();
	if (serverUrl) {
		return new Promise((resolve) => {
			uni.getImageInfo({
				src: serverUrl,
				success: (res) => resolve(res.path || serverUrl),
				fail: () => resolve(String(fallbackQrPath || '').trim())
			});
		});
	}
	const localPath = String(fallbackQrPath || '').trim();
	if (!localPath) {
		return Promise.resolve('');
	}
	return new Promise((resolve) => {
		uni.getImageInfo({
			src: localPath,
			success: (res) => resolve(res.path || localPath),
			fail: () => resolve('')
		});
	});
};

/** 保存报告海报图片 */
export const saveReportPosterImage = async (filePath = '') => {
	await savePosterImage(filePath, { fileNamePrefix: '学习报告海报' });
};

/** 分享报告海报图片 */
export const shareReportPosterImage = async (filePath = '') => {
	await sharePosterImage(filePath, {
		fileNamePrefix: '学习报告',
		shareTitle: '学习报告海报',
		shareText: '分享我的月度学习报告'
	});
};

/** 构建报告海报页路由（scene=report，可选 month=yyyy-MM） */
export const buildReportPosterPageUrl = (month = '') => {
	const key = String(month || '').trim();
	return key
		? `${POSTER_PAGE}?scene=report&month=${encodeURIComponent(key)}`
		: `${POSTER_PAGE}?scene=report`;
};
