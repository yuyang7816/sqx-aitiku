package com.sqx.app.base.ai.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.bo.AppAiChatHistoryItemBO;
import com.sqx.app.base.ai.enums.AiError;
import com.sqx.app.base.ai.support.AiChatReplySupport;
import com.sqx.app.base.ai.support.AiConfigReader;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 豆包大模型（Volcengine Ark Chat Completions）
 */
@Slf4j
public class DoubaoAiChatStrategy implements IAiChatPlatformStrategy {

    private final String apiKey;
    private final String model;
    private final String endpoint;

    public DoubaoAiChatStrategy(AiConfigReader aiConfigReader) {
        this.apiKey = aiConfigReader.getDoubaoApiKey();
        this.model = aiConfigReader.getDoubaoModel();
        this.endpoint = aiConfigReader.getDoubaoEndpoint();
    }

    @Override
    public String chat(AppAiChatBO chatBO) {
        if (StringUtils.isBlank(apiKey)) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        try {
            JSONObject body = buildRequestBody(chatBO);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey.trim());
            String resp = OkHttpUtil.doPost(endpoint, body.toJSONString(), "application/json", headers);
            return parseReply(resp);
        } catch (BizException ex) {
            log.warn("豆包 AI 调用失败: {}", ex.getMessage());
            throw new BizException(AiError.CHAT_FAILED);
        } catch (Exception ex) {
            log.error("豆包 AI 调用失败", ex);
            throw new BizException(AiError.CHAT_FAILED);
        }
    }

    JSONObject buildRequestBody(AppAiChatBO chatBO) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        JSONArray messages = new JSONArray();

        String topic = chatBO == null ? "" : StringUtils.defaultString(chatBO.getTopic()).trim();
        if (StringUtils.isNotBlank(topic)) {
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", "你是 K12 题库 AI 答疑助手，请结合题目上下文用中文简洁解答。" + topic);
            messages.add(system);
        }

        List<AppAiChatHistoryItemBO> history = chatBO == null ? null : chatBO.getHistory();
        if (history != null) {
            for (AppAiChatHistoryItemBO item : history) {
                String text = AiChatReplySupport.resolveHistoryText(item);
                if (StringUtils.isBlank(text)) {
                    continue;
                }
                JSONObject historyMsg = new JSONObject();
                historyMsg.put("role", mapRole(item.getRole()));
                historyMsg.put("content", text);
                messages.add(historyMsg);
            }
        }

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", chatBO == null ? "" : StringUtils.defaultString(chatBO.getMessage()).trim());
        messages.add(userMsg);

        body.put("messages", messages);
        return body;
    }

    String parseReply(String resp) {
        if (StringUtils.isBlank(resp)) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        JSONObject json = JSON.parseObject(resp);
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        if (message == null) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        String content = message.getString("content");
        if (StringUtils.isBlank(content)) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        return content.trim();
    }

    private String mapRole(String role) {
        if ("ai".equalsIgnoreCase(role) || "assistant".equalsIgnoreCase(role)) {
            return "assistant";
        }
        return "user";
    }
}
