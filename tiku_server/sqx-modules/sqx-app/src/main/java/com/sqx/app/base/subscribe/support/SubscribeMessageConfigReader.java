package com.sqx.app.base.subscribe.support;

import com.sqx.app.base.subscribe.constants.SubscribeMessageConstant;
import com.sqx.app.base.subscribe.constants.SubscribeMessageScene;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 读取 sys_config 中的微信订阅消息模板 ID
 */
@Component
public class SubscribeMessageConfigReader {

    public String getTemplateId(SubscribeMessageScene scene) {
        if (scene == null) {
            return null;
        }
        switch (scene) {
            case MONTHLY_REPORT:
                return trimToNull(ConfigUtil.getConfigValueOrDefault(SubscribeMessageConstant.KEY_MONTHLY_REPORT_TEMPLATE));
            case VIP_EXPIRE:
                return trimToNull(ConfigUtil.getConfigValueOrDefault(SubscribeMessageConstant.KEY_VIP_EXPIRE_TEMPLATE));
            case WRONG_REVIEW:
                return trimToNull(ConfigUtil.getConfigValueOrDefault(SubscribeMessageConstant.KEY_WRONG_REVIEW_TEMPLATE));
            default:
                return null;
        }
    }

    public List<String> listConfiguredTemplateIds() {
        List<String> ids = new ArrayList<>(3);
        appendIfPresent(ids, getTemplateId(SubscribeMessageScene.MONTHLY_REPORT));
        appendIfPresent(ids, getTemplateId(SubscribeMessageScene.VIP_EXPIRE));
        appendIfPresent(ids, getTemplateId(SubscribeMessageScene.WRONG_REVIEW));
        return Collections.unmodifiableList(ids);
    }

    public boolean isConfigured(SubscribeMessageScene scene) {
        return StringUtils.isNotBlank(getTemplateId(scene));
    }

    private void appendIfPresent(List<String> ids, String templateId) {
        if (StringUtils.isNotBlank(templateId)) {
            ids.add(templateId);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
