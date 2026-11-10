/** 模块十九 · app-data / member · 模块二十二 P1#13 expireAt 到期降级（Storage + API 对齐） */
export {
	VIP_RENEW_NOTICE_DAYS,
	AI_NOTICE_REMAINING_THRESHOLD,
	getMemberRemainingDays,
	buildDynamicNotices,
	getMemberState,
	refreshMemberExpireState,
	saveMemberState,
	activateMemberPlan,
	getRemainingAiTimes,
	checkAiChatQuota,
	consumeAiTimes,
	showAiQuotaExhaustedModal,
	getRemainingOcrTimes,
	checkOcrSearchQuota,
	syncAiQuotaFromApi,
	syncOcrQuotaFromApi,
	showOcrQuotaExhaustedModal,
	getMemberPlanByName
} from './impl.js';
