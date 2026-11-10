package com.sqx.app.base.ai.service.impl;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.vo.AppAiChatResultVO;
import com.sqx.app.base.ai.domain.vo.AppAiQuotaVO;
import com.sqx.app.base.ai.enums.AiError;
import com.sqx.app.base.ai.enums.AiPlatformCodeEnums;
import com.sqx.app.base.ai.factory.AiChatFactory;
import com.sqx.app.base.ai.service.AiChatService;
import com.sqx.app.base.ai.service.AiQuotaService;
import com.sqx.app.base.ai.strategy.IAiChatPlatformStrategy;
import com.sqx.app.base.ai.strategy.LocalTemplateAiChatStrategy;
import com.sqx.app.base.ai.support.AiConfigReader;
import com.sqx.app.base.ai.support.AiSensitiveWordFilter;
import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final LocalTemplateAiChatStrategy LOCAL_TEMPLATE_STRATEGY = new LocalTemplateAiChatStrategy();

    @Resource
    private AiQuotaService aiQuotaService;

    @Resource
    private AiChatFactory aiChatFactory;

    @Resource
    private AiSensitiveWordFilter aiSensitiveWordFilter;

    @Resource
    private AiConfigReader aiConfigReader;

    @Override
    public AppAiChatResultVO chat(Long userId, AppAiChatBO chatBO) {
        validateInput(chatBO);
        aiSensitiveWordFilter.validateChatInput(chatBO);
        AppAiQuotaVO quota = aiQuotaService.consumeChatQuota(userId);
        String reply = resolveReply(chatBO);
        aiSensitiveWordFilter.validateReply(reply);

        AppAiChatResultVO result = new AppAiChatResultVO();
        result.setReply(reply.trim());
        result.setQuota(quota);
        return result;
    }

    void validateInput(AppAiChatBO chatBO) {
        if (chatBO == null || StringUtils.isBlank(chatBO.getMessage())) {
            throw new BizException(AiError.MESSAGE_EMPTY);
        }
    }

    /** 调用当前 AI 供应商；豆包不可用时降级本地模板，避免前端直接看到 500「请求失败」 */
    String resolveReply(AppAiChatBO chatBO) {
        IAiChatPlatformStrategy platform = aiChatFactory.getPlatform();
        try {
            String reply = platform.chat(chatBO);
            if (StringUtils.isBlank(reply)) {
                return fallbackReply(chatBO, "empty reply");
            }
            return reply.trim();
        } catch (BizException ex) {
            if (shouldFallbackToLocalTemplate(ex)) {
                return fallbackReply(chatBO, ex.getMessage());
            }
            throw ex;
        } catch (Exception ex) {
            if (isDoubaoPlatform()) {
                return fallbackReply(chatBO, ex.getMessage());
            }
            log.error("AI 供应商调用异常", ex);
            throw new BizException(AiError.CHAT_FAILED);
        }
    }

    boolean shouldFallbackToLocalTemplate(BizException ex) {
        if (!isDoubaoPlatform()) {
            return false;
        }
        if (ex.getErrorCode() == AiError.CHAT_FAILED) {
            return true;
        }
        if (ex.getErrorCode() != null) {
            return false;
        }
        String message = StringUtils.defaultString(ex.getMessage()).trim();
        return message.isEmpty()
                || "请求失败".equals(message)
                || message.startsWith("错误的code:");
    }

    boolean isDoubaoPlatform() {
        return AiPlatformCodeEnums.DOUBAO.getCode().equals(StringUtils.trimToEmpty(aiConfigReader.getPlatform()));
    }

    String fallbackReply(AppAiChatBO chatBO, String reason) {
        log.warn("豆包 AI 不可用（{}），降级为本地模板回复", reason);
        String reply = LOCAL_TEMPLATE_STRATEGY.chat(chatBO);
        if (StringUtils.isBlank(reply)) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        return reply.trim();
    }
}