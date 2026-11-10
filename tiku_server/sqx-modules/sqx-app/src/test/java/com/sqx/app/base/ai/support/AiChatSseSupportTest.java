package com.sqx.app.base.ai.support;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatSseSupportTest {

    @Test
    void streamReply_shouldEmitDeltaChunksAndDone() throws Exception {
        RecordingSseEmitter emitter = new RecordingSseEmitter();
        AiChatSseSupport.streamReply(emitter, "你好世界");

        assertTrue(emitter.payloads.size() >= 2);
        assertEquals("[DONE]", emitter.payloads.get(emitter.payloads.size() - 1));

        StringBuilder merged = new StringBuilder();
        for (int index = 0; index < emitter.payloads.size() - 1; index++) {
            String payload = emitter.payloads.get(index);
            merged.append(payload.replace("{\"reply\":\"", "").replace("\"}", ""));
        }
        assertEquals("你好世界", merged.toString());
    }

    @Test
    void streamReply_blankReply_shouldOnlyEmitDone() throws Exception {
        RecordingSseEmitter emitter = new RecordingSseEmitter();
        AiChatSseSupport.streamReply(emitter, "   ");

        assertEquals(1, emitter.payloads.size());
        assertEquals("[DONE]", emitter.payloads.get(0));
    }

    private static final class RecordingSseEmitter extends SseEmitter {

        private final List<String> payloads = new ArrayList<>();

        private RecordingSseEmitter() {
            super(60_000L);
        }

        @Override
        public synchronized void send(Object object) throws IOException {
            payloads.add(String.valueOf(object));
        }
    }
}
