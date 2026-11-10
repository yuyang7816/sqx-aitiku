/**
 * 学习日历：GET /app/analysis/calendar、POST /app/analysis/check-in
 * 本地 Storage 兜底（API 失败或未同步的练习记录）
 */
import { get, post } from '@/common/api';
import { getAuthState } from '@/common/app-data';
import { STUDY_CALENDAR_KEY } from '@/common/storage-keys';

const DATE_KEY_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const WEEKDAY_LABELS = ['日', '一', '二', '三', '四', '五', '六'];

const readLocalStore = () => {
	const saved = uni.getStorageSync(STUDY_CALENDAR_KEY);
	if (saved && typeof saved === 'object') {
		return {
			checkInDates: Array.isArray(saved.checkInDates)
				? saved.checkInDates.filter((date) => DATE_KEY_PATTERN.test(String(date)))
				: [],
			activityDates: Array.isArray(saved.activityDates)
				? saved.activityDates.filter((date) => DATE_KEY_PATTERN.test(String(date)))
				: []
		};
	}
	return { checkInDates: [], activityDates: [] };
};

const writeLocalStore = (store = {}) => {
	uni.setStorageSync(STUDY_CALENDAR_KEY, {
		checkInDates: [...new Set((store.checkInDates || []).map(String))].sort(),
		activityDates: [...new Set((store.activityDates || []).map(String))].sort()
	});
};

/** 规范化日历数据 */
export const normalizeStudyCalendar = (data = {}) => ({
	month: String(data.month || '').trim(),
	activeDates: Array.isArray(data.activeDates) ? data.activeDates.map(String) : [],
	streakDays: typeof data.streakDays === 'number' ? Math.max(0, data.streakDays) : 0,
	todayChecked: Boolean(data.todayChecked),
	monthStudyDays: typeof data.monthStudyDays === 'number' ? Math.max(0, data.monthStudyDays) : 0
});

/** 今日 yyyy-MM-dd */
export const formatTodayKey = () => {
	const now = new Date();
	return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
};

/** 日期 ±N 天 */
export const shiftDateKey = (dateKey = '', delta = 0) => {
	const matched = String(dateKey || '').trim().match(/^(\d{4})-(\d{2})-(\d{2})$/);
	if (!matched) {
		return '';
	}
	const date = new Date(Number(matched[1]), Number(matched[2]) - 1, Number(matched[3]));
	date.setDate(date.getDate() + delta);
	return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

/** 本机全部活跃日期（打卡 + 本地练习） */
export const getLocalActiveDates = () => {
	const store = readLocalStore();
	return [...new Set([...store.checkInDates, ...store.activityDates])].sort();
};

/** 记录本地练习活跃日（交卷未同步服务端时调用） */
export const recordLocalStudyActivity = (dateKey = formatTodayKey()) => {
	const key = String(dateKey || '').trim();
	if (!DATE_KEY_PATTERN.test(key)) {
		return false;
	}
	const store = readLocalStore();
	if (store.activityDates.includes(key) || store.checkInDates.includes(key)) {
		return false;
	}
	store.activityDates.push(key);
	writeLocalStore(store);
	return true;
};

/** 本机手动打卡 */
export const recordLocalCheckIn = (dateKey = formatTodayKey()) => {
	const key = String(dateKey || '').trim();
	if (!DATE_KEY_PATTERN.test(key)) {
		return { newlyChecked: false };
	}
	const store = readLocalStore();
	const alreadyChecked = store.checkInDates.includes(key) || store.activityDates.includes(key);
	if (!store.checkInDates.includes(key)) {
		store.checkInDates.push(key);
		writeLocalStore(store);
	}
	return { newlyChecked: !alreadyChecked };
};

/** 连续打卡天数（截至 endDate，含当日） */
export const calculateStreakDays = (activeDates = [], endDate = formatTodayKey()) => {
	const activeSet = new Set((activeDates || []).map(String));
	let streak = 0;
	let cursor = endDate;
	while (activeSet.has(cursor)) {
		streak += 1;
		cursor = shiftDateKey(cursor, -1);
	}
	return streak;
};

/** 筛选指定月份活跃日期 */
export const filterActiveDatesByMonth = (activeDates = [], monthKey = '') => {
	const prefix = String(monthKey || '').trim();
	if (!/^\d{4}-\d{2}$/.test(prefix)) {
		return [];
	}
	return (activeDates || []).filter((date) => String(date).startsWith(`${prefix}-`)).sort();
};

/** 由本机记录构建月历 */
export const buildLocalStudyCalendar = (monthKey = getCurrentMonthKey()) => {
	const month = String(monthKey || getCurrentMonthKey()).trim();
	const allDates = getLocalActiveDates();
	const monthDates = filterActiveDatesByMonth(allDates, month);
	const today = formatTodayKey();
	return normalizeStudyCalendar({
		month,
		activeDates: monthDates,
		monthStudyDays: monthDates.length,
		todayChecked: allDates.includes(today),
		streakDays: calculateStreakDays(allDates, today)
	});
};

/** 合并服务端与本机月历（本机可能含未同步练习日） */
export const mergeStudyCalendar = (apiCalendar = {}, localCalendar = {}, monthKey = '') => {
	const month = String(monthKey || apiCalendar.month || localCalendar.month || getCurrentMonthKey()).trim();
	const mergedDates = [...new Set([
		...(apiCalendar.activeDates || []),
		...(localCalendar.activeDates || [])
	])].filter((date) => String(date).startsWith(`${month}-`)).sort();
	const allDates = [...new Set([
		...getLocalActiveDates(),
		...(apiCalendar.activeDates || [])
	])];
	const today = formatTodayKey();
	return normalizeStudyCalendar({
		month,
		activeDates: mergedDates,
		monthStudyDays: mergedDates.length,
		todayChecked: Boolean(apiCalendar.todayChecked) || allDates.includes(today),
		streakDays: typeof apiCalendar.streakDays === 'number'
			? Math.max(0, apiCalendar.streakDays)
			: calculateStreakDays(allDates, today)
	});
};

/** 拉取指定月份学习日历 */
export const fetchStudyCalendar = async (month = '') => {
	const monthKey = String(month || getCurrentMonthKey()).trim();
	const localCalendar = buildLocalStudyCalendar(monthKey);
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return {
			calendar: localCalendar,
			fromApi: false,
			message: ''
		};
	}
	try {
		const res = await get('/app/analysis/calendar', { month: monthKey });
		if (res && res.code === 0 && res.data) {
			const apiCalendar = normalizeStudyCalendar({
				...res.data,
				month: res.data.month || monthKey
			});
			return {
				calendar: mergeStudyCalendar(apiCalendar, localCalendar, monthKey),
				fromApi: true,
				message: ''
			};
		}
	} catch (e) {
		// 回退本机记录
	}
	return {
		calendar: localCalendar,
		fromApi: false,
		message: '日历数据加载失败，已展示本机记录'
	};
};

/** 手动打卡 */
export const submitStudyCheckIn = async () => {
	const auth = getAuthState();
	const today = formatTodayKey();
	if (!auth.loggedIn) {
		const local = recordLocalCheckIn(today);
		const dates = getLocalActiveDates();
		return {
			ok: dates.includes(today),
			fromApi: false,
			checkDate: today,
			streakDays: calculateStreakDays(dates, today),
			newlyChecked: local.newlyChecked
		};
	}
	try {
		const res = await post('/app/analysis/check-in', {});
		if (res && res.code === 0 && res.data) {
			const checkDate = String(res.data.checkDate || today).trim() || today;
			recordLocalCheckIn(checkDate);
			return {
				ok: true,
				fromApi: true,
				checkDate,
				streakDays: Number(res.data.streakDays) || 0,
				newlyChecked: Boolean(res.data.newlyChecked)
			};
		}
	} catch (e) {
		// 回退本机打卡
	}
	const local = recordLocalCheckIn(today);
	const dates = getLocalActiveDates();
	return {
		ok: true,
		fromApi: false,
		checkDate: today,
		streakDays: calculateStreakDays(dates, today),
		newlyChecked: local.newlyChecked
	};
};

/** 构建月历格子 */
export const buildCalendarCells = (monthKey = '', activeDates = []) => {
	const key = String(monthKey || '').trim();
	if (!/^\d{4}-\d{2}$/.test(key)) {
		return [];
	}
	const [year, month] = key.split('-').map(Number);
	const firstDay = new Date(year, month - 1, 1);
	const startWeekday = firstDay.getDay();
	const daysInMonth = new Date(year, month, 0).getDate();
	const activeSet = new Set((activeDates || []).map(String));
	const cells = [];
	for (let i = 0; i < startWeekday; i += 1) {
		cells.push({ day: '', active: false, empty: true });
	}
	for (let day = 1; day <= daysInMonth; day += 1) {
		const dateKey = `${key}-${String(day).padStart(2, '0')}`;
		cells.push({
			day,
			dateKey,
			active: activeSet.has(dateKey),
			empty: false,
			isToday: dateKey === formatTodayKey()
		});
	}
	return cells;
};

/** 当前月份 yyyy-MM */
export const getCurrentMonthKey = () => {
	const now = new Date();
	return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
};

/** 上/下月 */
export const shiftMonthKey = (monthKey = '', delta = 0) => {
	const [year, month] = String(monthKey || getCurrentMonthKey()).split('-').map(Number);
	const date = new Date(year, month - 1 + delta, 1);
	return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
};

/** 月份展示文案 */
export const formatMonthTitle = (monthKey = '') => `${String(monthKey).replace('-', '年')}月`;

/** 是否为当月 */
export const isCurrentMonthKey = (monthKey = '') => String(monthKey || '').trim() === getCurrentMonthKey();

/** 是否为未来月份 */
export const isFutureMonthKey = (monthKey = '') => {
	const key = String(monthKey || '').trim();
	if (!/^\d{4}-\d{2}$/.test(key)) {
		return false;
	}
	return key > getCurrentMonthKey();
};

/** 日期展示：8月21日 周四 */
export const formatDateLabel = (dateKey = '') => {
	const matched = String(dateKey || '').trim().match(/^(\d{4})-(\d{2})-(\d{2})$/);
	if (!matched) {
		return '';
	}
	const date = new Date(Number(matched[1]), Number(matched[2]) - 1, Number(matched[3]));
	return `${Number(matched[2])}月${Number(matched[3])}日 周${WEEKDAY_LABELS[date.getDay()]}`;
};

/** 选中日期详情文案 */
export const buildDateDetailText = (dateKey = '', active = false) => {
	const label = formatDateLabel(dateKey);
	if (!label) {
		return '';
	}
	if (dateKey === formatTodayKey()) {
		return active ? `${label} · 今日已学习` : `${label} · 今日尚未学习，完成练习或打卡即可记录`;
	}
	return active ? `${label} · 当日有学习记录` : `${label} · 当日无学习记录`;
};
