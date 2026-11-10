package com.sqx.app.referral.invitecode.support;

import com.sqx.app.referral.constants.ReferralConstant;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 推广邀请海报配置（海报底图、邀请语，来自 sys_config）
 */
@Component
public class ReferralPosterConfigSupport {

    public String resolvePosterImageUrl() {
        return resolvePublicAssetUrl(readConfig(ReferralConstant.poster.SYS_POSTER_IMAGE));
    }

    public String resolveInviteText() {
        return StringUtils.trimToEmpty(readConfig(ReferralConstant.poster.SYS_INVITE_TEXT));
    }

    private String readConfig(String key) {
        return StringUtils.defaultString(ConfigUtil.getConfigValueOrDefault(key));
    }

    /**
     * 与 LocalFileStorage 一致：优先 file_public_base_url，其次 sys_h5_domain。
     * 相对路径如 /upload/2026/08/25/xxx.jpg 需带上 /sqx_fast 前缀才能被静态资源映射访问。
     */
    private String resolvePublicAssetUrl(String raw) {
        String normalized = StringUtils.trimToNull(raw);
        if (normalized == null) {
            return "";
        }
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }
        String path = normalized.startsWith("/") ? normalized : "/" + normalized;
        String baseUrl = resolvePublicBaseUrl();
        if (StringUtils.isBlank(baseUrl)) {
            return path;
        }
        return baseUrl + path;
    }

    private String resolvePublicBaseUrl() {
        String baseUrl = normalizeDomain(ConfigUtil.getConfigValueOrDefault("file_public_base_url"));
        if (StringUtils.isNotBlank(baseUrl)) {
            return baseUrl;
        }
        return normalizeDomain(ConfigUtil.getConfigValueOrDefault("sys_h5_domain"));
    }

    private static String normalizeDomain(String domain) {
        if (StringUtils.isBlank(domain)) {
            return "";
        }
        return domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
    }
}
