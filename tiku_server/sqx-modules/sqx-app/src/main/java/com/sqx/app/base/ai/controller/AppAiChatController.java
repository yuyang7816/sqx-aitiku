package com.sqx.app.base.ai.controller;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.vo.AppAiChatResultVO;
import com.sqx.app.base.ai.service.AiChatService;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.sqx.app.base.ai.support.AiChatSseSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * 用户端 AI 答疑（POST /app/ai/chat，需登录）
 */
@Tag(name = "用户端-AI 答疑", description = "文档 21.2：POST /app/ai/chat 题目上下文 + 多轮 history 对话，见 docs/ai-chat-api.md")
@Slf4j
@RestController
@RequestMapping("/app/ai")
@SecurityRequirement(name = "appToken")
public class AppAiChatController {

    private static final long SSE_TIMEOUT_MS = 120_000L;

    @Resource
    private AiChatService aiChatService;

    @Resource(name = "asyncServiceExecutor")
    private Executor asyncServiceExecutor;

    @Operation(summary = "AI 答疑对话", description = "传入 topic、message、history，一次性返回 AI 回复与 quota 快照")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppAiChatResultVO.class)))
    @Login
    @PostMapping("chat")
    public Result<AppAiChatResultVO> chat(@RequestAttribute("userId") Long userId,
                                          @RequestBody @Validated AppAiChatBO chatBO) {
        return Result.success(aiChatService.chat(userId, chatBO));
    }

    @Operation(summary = "AI 答疑对话（SSE 流式）", description = "H5 优先使用；校验与扣次逻辑同 POST /app/ai/chat，以 text/event-stream 增量推送 reply")
    @Login
    @PostMapping(value = "chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestAttribute("userId") Long userId,
                                 @RequestBody @Validated AppAiChatBO chatBO) {
        AppAiChatResultVO result = aiChatService.chat(userId, chatBO);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        asyncServiceExecutor.execute(() -> {
            try {
                AiChatSseSupport.streamReply(emitter, result.getReply());
                emitter.complete();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                emitter.completeWithError(ex);
            } catch (Exception ex) {
                log.warn("AI SSE 流式输出失败: {}", ex.getMessage());
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }
}
