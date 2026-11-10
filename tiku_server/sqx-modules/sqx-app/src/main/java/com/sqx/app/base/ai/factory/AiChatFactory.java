package com.sqx.app.base.ai.factory;

import com.sqx.app.base.ai.enums.AiError;
import com.sqx.app.base.ai.enums.AiPlatformCodeEnums;
import com.sqx.app.base.ai.strategy.DoubaoAiChatStrategy;
import com.sqx.app.base.ai.strategy.IAiChatPlatformStrategy;
import com.sqx.app.base.ai.strategy.LocalTemplateAiChatStrategy;
import com.sqx.app.base.ai.support.AiConfigReader;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 答疑供应商工厂
 */
@Component
public class AiChatFactory {

    private static final Map<String, IAiChatPlatformStrategy> STATIC_PLATFORM_MAP = new ConcurrentHashMap<>();

    static {
        STATIC_PLATFORM_MAP.put(AiPlatformCodeEnums.LOCAL_TEMPLATE.getCode(), new LocalTemplateAiChatStrategy());
    }

    @Resource
    private AiConfigReader aiConfigReader;

    public IAiChatPlatformStrategy getPlatform() {
        String code = aiConfigReader.getPlatform();
        if (StringUtils.isBlank(code)) {
            code = AiPlatformCodeEnums.LOCAL_TEMPLATE.getCode();
        }
        if (AiPlatformCodeEnums.DOUBAO.getCode().equals(code)) {
            return new DoubaoAiChatStrategy(aiConfigReader);
        }
        IAiChatPlatformStrategy strategy = STATIC_PLATFORM_MAP.get(code);
        if (strategy == null) {
            throw new BizException(AiError.AI_PLATFORM_NOT_FOUND);
        }
        return strategy;
    }
}
