/**
 * 模块二十二 P1#13 · 会员到期降级（expireAt 校验）
 * 规则：expireAt yyyy-MM-dd 当日仍有效，次日 0 点起 isVip=false、level=普通会员
 * 触发：App onShow refreshMemberExpireState · getMemberState/saveMemberState 读写均 normalize
 * UI：Hero Banner · 会员卡片 · AI/OCR 次数 · member-ad-slot 去广告
 * 服务端：GET /app/user vip/vipExpireAt 合并（支付回调延迟时保留本地有效 expireAt）
 */
export {
	getMemberState,
	refreshMemberExpireState,
	saveMemberState
} from '@/common/app-data';
