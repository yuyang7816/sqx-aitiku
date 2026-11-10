package com.sqx.app.base.ai.support;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * H5 SSE 流式输出：按增量推送 AI 回复，与 uniapp common/chat-api.js 解析格式对齐。
 */
public final class AiChatSseSupport {

    private static final int STREAM_CHUNK_SIZE = 8;
    private static final long STREAM_INTERVAL_MS = 20L;

    private AiChatSseSupport() {
    }

    public static void streamReply(SseEmitter emitter, String reply) throws IOException, InterruptedException {
        String text = StringUtils.defaultString(reply).trim();
        if (text.isEmpty()) {
            emitter.send(SseEmitter.event().data("[DONE]"));
            return;
        }

        for (int index = 0; index < text.length(); index += STREAM_CHUNK_SIZE) {
            int end = Math.min(index + STREAM_CHUNK_SIZE, text.length());
            String chunk = text.substring(index, end);
            JSONObject payload = new JSONObject();
            payload.put("reply", chunk);
            emitter.send(SseEmitter.event().data(payload.toJSONString()));
            if (end < text.length()) {
                Thread.sleep(STREAM_INTERVAL_MS);
            }
        }
        emitter.send(SseEmitter.event().data("[DONE]"));
    }
}
