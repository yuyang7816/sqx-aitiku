package com.sqx.app.base.kefuChat.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.kefuChat.domain.bo.KefuChatSendBO;
import com.sqx.app.base.kefuChat.domain.vo.KefuChatMessageVO;
import com.sqx.app.base.kefuChat.service.KefuChatService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "用户端-客服 IM", description = "在线客服 IM 对话")
@RestController
@RequestMapping("/app/kefu/chat")
@SecurityRequirement(name = "appToken")
public class AppKefuChatController {

    @Resource
    private KefuChatService kefuChatService;

    @Operation(summary = "消息列表")
    @Login
    @GetMapping("messages")
    public Result<List<KefuChatMessageVO>> messages(@RequestAttribute("userId") Long userId,
                                                  @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        return Result.success(kefuChatService.listMessages(userId, limit));
    }

    @Operation(summary = "发送消息")
    @Login
    @PostMapping("send")
    public Result<KefuChatMessageVO> send(@RequestAttribute("userId") Long userId,
                                          @RequestBody @Validated KefuChatSendBO body) {
        return Result.success(kefuChatService.sendUserMessage(userId, body.getContent()));
    }

    @Operation(summary = "未读数（客服回复）")
    @Login
    @GetMapping("unread-count")
    public Result<Map<String, Integer>> unreadCount(@RequestAttribute("userId") Long userId) {
        Map<String, Integer> data = new HashMap<>(1);
        data.put("count", kefuChatService.countUnreadForUser(userId));
        return Result.success(data);
    }

    @Operation(summary = "标记客服消息已读")
    @Login
    @PostMapping("read")
    public Result<Void> read(@RequestAttribute("userId") Long userId) {
        kefuChatService.markReadForUser(userId);
        return Result.success();
    }
}
