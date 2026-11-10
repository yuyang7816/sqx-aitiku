/**
 * 错题本导出：当前 Chip 筛选下的题干与解析 → 图片 / PDF（H5 打印）
 */
import {
	buildReviewItemFromNotebook,
	formatNotebookAnswer,
	formatNotebookItemSubject,
	getNotebookFilteredList
} from '@/common/app-data';
import { resolveNotebookItemSubject } from '@/common/notebook-rules';
import { NOTEBOOK_EXPORT_KEY } from '@/common/storage-keys';

export { NOTEBOOK_EXPORT_KEY } from '@/common/storage-keys';
export const NOTEBOOK_EXPORT_PAGE = '/package/common/notebook-export';

const EXPORT_CANVAS_WIDTH = 750;
const EXPORT_PADDING = 40;
const EXPORT_LINE_HEIGHT = 36;
const EXPORT_TITLE_SIZE = 30;
const EXPORT_SECTION_SIZE = 26;
const EXPORT_BODY_SIZE = 24;
const EXPORT_META_SIZE = 22;
const EXPORT_CHARS_PER_LINE = 28;
const EXPORT_CARD_GAP = 20;
const EXPORT_CARD_RADIUS = 22;

const formatExportDate = () => {
	const now = new Date();
	const pad = (value) => String(value).padStart(2, '0');
	return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}`;
};

/** 按固定字符数换行，便于 canvas 与打印 HTML 复用 */
export const wrapExportTextLines = (text = '', maxChars = EXPORT_CHARS_PER_LINE) => {
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

/** 构建导出文档 */
export const buildNotebookExportPayload = ({
	chip = '全部学科',
	list = [],
	profile = {},
	items = null,
	subjects = [],
	includeAnswerAnalysis = true,
} = {}) => {
	const sourceList = Array.isArray(items) && items.length
		? items
		: getNotebookFilteredList(chip, list, profile);
	const subjectSet = new Set((subjects || []).filter(Boolean));
	const filtered = sourceList.filter((item) => {
		if (!subjectSet.size) {
			return true;
		}
		const itemSubject = resolveNotebookItemSubject(item);
		return [...subjectSet].some((subject) => (
			resolveNotebookItemSubject({ subject }) === itemSubject
		));
	});
	const exportedAt = formatExportDate();
	const subjectLabel = subjectSet.size
		? [...subjectSet].join('、')
		: String(chip || '全部学科').trim() || '全部学科';
	return {
		chip: subjectLabel,
		exportedAt,
		count: filtered.length,
		includeAnswerAnalysis: includeAnswerAnalysis !== false,
		subjects: subjectSet.size ? [...subjectSet] : [],
		items: filtered.map((item, index) => resolveNotebookExportItem(item, index, includeAnswerAnalysis !== false))
	};
};

/** 单题导出结构：补齐 analysis 等业务字段 */
export const resolveNotebookExportItem = (item = {}, index = 0, includeAnswerAnalysis = true) => {
	const review = buildReviewItemFromNotebook(item);
	const base = {
		index: index + 1,
		subject: formatNotebookItemSubject(item),
		question: String(review.title || item.question || '未命名题目').trim() || '未命名题目',
	};
	if (!includeAnswerAnalysis) {
		return base;
	}
	return {
		...base,
		wrong: formatNotebookAnswer(review.myAnswer || item.wrong),
		right: formatNotebookAnswer(review.rightAnswer || item.right),
		analysis: String(review.analysis || '暂无详细解析').trim() || '暂无详细解析'
	};
};

export const setNotebookExportPayload = (payload = {}) => {
	uni.setStorageSync(NOTEBOOK_EXPORT_KEY, payload);
	return payload;
};

export const getNotebookExportPayload = () => {
	try {
		const payload = uni.getStorageSync(NOTEBOOK_EXPORT_KEY);
		if (payload && Array.isArray(payload.items)) {
			return payload;
		}
	} catch (e) {
		// ignore
	}
	return {
		chip: '全部学科',
		exportedAt: formatExportDate(),
		count: 0,
		includeAnswerAnalysis: true,
		items: []
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

const resolveExportItemLayout = (item = {}, includeAnswerAnalysis = true) => {
	const questionLines = wrapExportTextLines(item.question, 24);
	const wrongLines = includeAnswerAnalysis ? wrapExportTextLines(item.wrong, 10) : [];
	const rightLines = includeAnswerAnalysis ? wrapExportTextLines(item.right, 10) : [];
	const analysisLines = includeAnswerAnalysis ? wrapExportTextLines(item.analysis, 24) : [];
	const answerLineCount = includeAnswerAnalysis ? Math.max(wrongLines.length, rightLines.length, 1) : 0;
	const answerHeight = includeAnswerAnalysis ? 82 + Math.max(0, answerLineCount - 1) * 28 : 0;
	const analysisHeight = includeAnswerAnalysis ? 54 + analysisLines.length * EXPORT_LINE_HEIGHT + 16 : 0;
	const totalHeight = 88 + questionLines.length * EXPORT_LINE_HEIGHT
		+ (includeAnswerAnalysis ? 20 + answerHeight + 18 + analysisHeight : 24);
	return {
		questionLines,
		wrongLines,
		rightLines,
		analysisLines,
		answerHeight,
		totalHeight
	};
};

const blockHeight = (title, bodyLines, extraLines = 0, includeAnswerAnalysis = true) => {
	const answerLines = includeAnswerAnalysis
		? extraLines * EXPORT_LINE_HEIGHT
		: 0;
	return 56 + wrapExportTextLines(title).length * EXPORT_LINE_HEIGHT
		+ answerLines
		+ (includeAnswerAnalysis ? bodyLines.length * EXPORT_LINE_HEIGHT : 0)
		+ 36;
};

/** 预估 canvas 高度 */
export const measureNotebookExportHeight = (payload = {}) => {
	const items = Array.isArray(payload.items) ? payload.items : [];
	const includeAnswerAnalysis = payload.includeAnswerAnalysis !== false;
	let height = EXPORT_PADDING * 2 + 176;
	items.forEach((item) => {
		height += resolveExportItemLayout(item, includeAnswerAnalysis).totalHeight + EXPORT_CARD_GAP;
	});
	if (!items.length) {
		height += 120;
	}
	return Math.max(height, 900);
};

const drawLines = (ctx, lines, x, y, fontSize, color, lineHeight = EXPORT_LINE_HEIGHT) => {
	ctx.setFillStyle(color);
	ctx.setFontSize(fontSize);
	let cursorY = y;
	lines.forEach((line) => {
		ctx.fillText(line, x, cursorY);
		cursorY += lineHeight;
	});
	return cursorY;
};

/** 在 canvas 上绘制错题导出文档 */
export const drawNotebookExportCanvas = (ctx, payload = {}, width = EXPORT_CANVAS_WIDTH) => {
	const padding = EXPORT_PADDING;
	const contentWidth = width - padding * 2;
	const includeAnswerAnalysis = payload.includeAnswerAnalysis !== false;
	let y = padding + 8;

	ctx.setFillStyle('#f4f7fb');
	ctx.fillRect(0, 0, width, measureNotebookExportHeight(payload));

	const headerGradient = ctx.createLinearGradient(padding, y, padding + contentWidth, y + 128);
	headerGradient.addColorStop(0, '#386df5');
	headerGradient.addColorStop(1, '#4d7dff');
	drawRoundRect(ctx, padding, y, contentWidth, 128, 26, headerGradient);
	ctx.setFillStyle('#ffffff');
	ctx.setFontSize(38);
	ctx.fillText('错题本导出', padding + 26, y + 50);
	drawRoundRect(ctx, padding + 26, y + 68, 244, 34, 17, 'rgba(255,255,255,0.16)');
	ctx.setFontSize(20);
	ctx.fillText(String(payload.chip || '全部学科'), padding + 42, y + 91);
	ctx.setTextAlign('right');
	ctx.setFontSize(20);
	ctx.fillText(`共 ${payload.count || 0} 题`, padding + contentWidth - 26, y + 91);
	ctx.setTextAlign('left');
	y += 150;

	ctx.setFillStyle('#8793a8');
	ctx.setFontSize(EXPORT_META_SIZE);
	ctx.fillText(`导出时间：${payload.exportedAt || formatExportDate()}`, padding, y);
	y += 48;

	const items = Array.isArray(payload.items) ? payload.items : [];
	if (!items.length) {
		drawLines(ctx, ['当前筛选下暂无错题'], padding, y + 20, EXPORT_BODY_SIZE, '#5d6777');
		return measureNotebookExportHeight(payload);
	}

	items.forEach((item) => {
		const layout = resolveExportItemLayout(item, includeAnswerAnalysis);
		drawRoundRect(ctx, padding, y, contentWidth, layout.totalHeight, EXPORT_CARD_RADIUS, '#ffffff');

		ctx.setFillStyle('#386df5');
		ctx.setFontSize(EXPORT_META_SIZE);
		ctx.fillText(`第 ${item.index} 题 · ${item.subject}`, padding + 16, y + 36);

		y += 56;
		y = drawLines(ctx, layout.questionLines, padding + 16, y, EXPORT_SECTION_SIZE, '#243041');
		y += 8;

		if (includeAnswerAnalysis) {
			const answerY = y + 12;
			const answerWidth = (contentWidth - 44) / 2;
			drawRoundRect(ctx, padding + 16, answerY, answerWidth, layout.answerHeight, 18, '#fff4f5');
			drawRoundRect(ctx, padding + 28 + answerWidth, answerY, answerWidth, layout.answerHeight, 18, '#f3fbf5');

			ctx.setFillStyle('#e25562');
			ctx.setFontSize(20);
			ctx.fillText('错误答案', padding + 30, answerY + 28);
			drawLines(ctx, layout.wrongLines, padding + 30, answerY + 60, EXPORT_META_SIZE, '#e25562', 28);

			ctx.setFillStyle('#24995b');
			ctx.setFontSize(20);
			ctx.fillText('正确答案', padding + 42 + answerWidth, answerY + 28);
			drawLines(ctx, layout.rightLines, padding + 42 + answerWidth, answerY + 60, EXPORT_META_SIZE, '#24995b', 28);

			y = answerY + layout.answerHeight + 30;

			ctx.setFillStyle('#16a34a');
			ctx.setFontSize(EXPORT_META_SIZE);
			ctx.fillText('详细解析', padding + 16, y);
			y += 32;
			y = drawLines(ctx, layout.analysisLines, padding + 16, y, EXPORT_BODY_SIZE, '#5d6777');
		}
		y += EXPORT_CARD_GAP;
	});

	return measureNotebookExportHeight(payload);
};

/**
 * 生成导出图片临时路径
 * @param {string} canvasId
 * @param {object} payload
 * @param {object} componentInstance - 页面/组件实例，canvasToTempFilePath 需要
 */
export const renderNotebookExportImage = (canvasId, payload = {}, componentInstance) => {
	const width = EXPORT_CANVAS_WIDTH;
	const height = measureNotebookExportHeight(payload);

	return new Promise((resolve, reject) => {
		const ctx = uni.createCanvasContext(canvasId, componentInstance);
		drawNotebookExportCanvas(ctx, payload, width);
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

/** 保存图片到相册或触发 H5 下载 */
export const saveNotebookExportImage = async (filePath = '') => {
	const path = String(filePath || '').trim();
	if (!path) {
		throw new Error('图片路径无效');
	}

	// #ifdef H5
	const link = document.createElement('a');
	link.href = path;
	link.download = `错题本-${Date.now()}.png`;
	document.body.appendChild(link);
	link.click();
	document.body.removeChild(link);
	return;
	// #endif

	await new Promise((resolve, reject) => {
		uni.saveImageToPhotosAlbum({
			filePath: path,
			success: resolve,
			fail: reject
		});
	});
};

/** H5：打开打印窗口导出 PDF */
export const printNotebookExportPdf = (payload = {}) => {
	// #ifdef H5
	const items = Array.isArray(payload.items) ? payload.items : [];
	const includeAnswerAnalysis = payload.includeAnswerAnalysis !== false;
	const blocks = items.map((item) => {
		const questionHtml = wrapExportTextLines(item.question).map((line) => `<p class="question-line">${escapeHtml(line)}</p>`).join('');
		const answerHtml = includeAnswerAnalysis
			? `
				<p class="meta wrong">错误答案：${escapeHtml(item.wrong)}</p>
				<p class="meta right">正确答案：${escapeHtml(item.right)}</p>
				<h4>详细解析</h4>
				<div class="analysis">${wrapExportTextLines(item.analysis).map((line) => `<p class="analysis-line">${escapeHtml(line)}</p>`).join('')}</div>
			`
			: '';
		return `
			<section class="question-block">
				<h3>第 ${item.index} 题 · ${escapeHtml(item.subject)}</h3>
				<div class="question">${questionHtml}</div>
				${answerHtml}
			</section>
		`;
	}).join('');

	const modeLabel = includeAnswerAnalysis ? '含答案与解析' : '纯题目';

	const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8" />
<title>错题本导出 · ${escapeHtml(payload.chip || '全部学科')}</title>
<style>
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; color: #243041; padding: 24px; }
header { border-bottom: 2px solid #386df5; padding-bottom: 12px; margin-bottom: 24px; }
header h1 { margin: 0 0 8px; font-size: 22px; }
header p { margin: 0; color: #97a0af; font-size: 13px; }
.question-block { page-break-inside: avoid; margin-bottom: 28px; padding: 16px; border: 1px solid #e6ebf3; border-radius: 12px; }
.question-block h3 { margin: 0 0 12px; font-size: 15px; color: #386df5; }
.question-block h4 { margin: 16px 0 8px; font-size: 14px; color: #16a34a; }
.question-line { margin: 0 0 6px; font-size: 15px; line-height: 1.6; font-weight: 600; }
.analysis-line { margin: 0 0 6px; font-size: 14px; line-height: 1.7; color: #5d6777; }
.meta { margin: 6px 0; font-size: 13px; }
.meta.wrong { color: #f05963; }
.meta.right { color: #16a34a; }
.empty { color: #97a0af; }
</style>
</head>
<body>
<header>
	<h1>错题本导出 · ${escapeHtml(payload.chip || '全部学科')}</h1>
	<p>导出时间：${escapeHtml(payload.exportedAt || formatExportDate())} · ${escapeHtml(modeLabel)} · 共 ${payload.count || 0} 题</p>
</header>
${items.length ? blocks : '<p class="empty">当前筛选下暂无错题</p>'}
</body>
</html>`;

	const printWindow = window.open('', '_blank');
	if (!printWindow) {
		throw new Error('无法打开打印窗口，请允许弹窗');
	}
	printWindow.document.write(html);
	printWindow.document.close();
	printWindow.focus();
	setTimeout(() => {
		printWindow.print();
	}, 300);
	return;
	// #endif

	throw new Error('当前平台请使用保存图片');
};

/** 分享图片：H5 尝试系统分享，其余端提示保存后分享 */
export const shareNotebookExportImage = async (filePath = '') => {
	const path = String(filePath || '').trim();
	if (!path) {
		throw new Error('图片路径无效');
	}

	// #ifdef H5
	if (navigator.share && typeof fetch === 'function') {
		try {
			const response = await fetch(path);
			const blob = await response.blob();
			const file = new File([blob], `错题本-${Date.now()}.png`, { type: 'image/png' });
			await navigator.share({
				title: '错题本导出',
				text: '分享我的错题整理',
				files: [file]
			});
			return;
		} catch (e) {
			if (String(e?.name || '') === 'AbortError') {
				return;
			}
		}
	}
	await saveNotebookExportImage(path);
	uni.showToast({ title: '图片已下载，可手动分享', icon: 'none' });
	return;
	// #endif

	// #ifdef MP-WEIXIN
	await new Promise((resolve, reject) => {
		if (typeof wx !== 'undefined' && wx.showShareImageMenu) {
			wx.showShareImageMenu({
				path,
				success: resolve,
				fail: reject
			});
			return;
		}
		reject(new Error('当前微信版本不支持图片分享'));
	});
	return;
	// #endif

	await saveNotebookExportImage(path);
	uni.showToast({ title: '图片已保存到相册', icon: 'none' });
};

const escapeHtml = (value = '') => String(value)
	.replace(/&/g, '&amp;')
	.replace(/</g, '&lt;')
	.replace(/>/g, '&gt;')
	.replace(/"/g, '&quot;');
