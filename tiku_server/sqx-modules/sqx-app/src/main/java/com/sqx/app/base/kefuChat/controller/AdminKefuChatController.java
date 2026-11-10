package com.sqx.app.base.kefuChat.controller;

import com.sqx.app.base.kefuChat.domain.bo.KefuChatReplyBO;
import com.sqx.app.base.kefuChat.domain.vo.KefuChatMessageVO;
import com.sqx.app.base.kefuChat.domain.vo.KefuChatSessionVO;
import com.sqx.app.base.kefuChat.service.KefuChatService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理端-客服 IM", description = "在线客服 IM 会话管理")
@RestController
@RequestMapping("/admin/kefu/chat")
public class AdminKefuChatController {

    @Resource
    private KefuChatService kefuChatService;

    @Operation(summary = "会话列表")
    @GetMapping("sessions")
    public Result<List<KefuChatSessionVO>> sessions(
            @RequestParam(value = "limit", required = false, defaultValue = "50") int limit) {
        return Result.success(kefuChatService.listSessions(limit));
    }

    @Operation(summary = "指定用户消息列表")
    @GetMapping("messages")
    public Result<List<KefuChatMessageVO>> messages(@RequestParam("userId") Long userId,
                                                    @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        return Result.success(kefuChatService.listStaffMessages(userId, limit));
    }

    @Operation(summary = "客服回复")
    @PostMapping("reply")
    public Result<KefuChatMessageVO> reply(@RequestBody @Validated KefuChatReplyBO body) {
        return Result.success(kefuChatService.sendStaffReply(body.getUserId(), body.getContent()));
    }

    @Operation(summary = "未读总数（用户发来未读）")
    @GetMapping("unread-count")
    public Result<Map<String, Integer>> unreadCount() {
        Map<String, Integer> data = new HashMap<>(1);
        data.put("count", kefuChatService.countUnreadForStaff());
        return Result.success(data);
    }

    @Operation(summary = "标记用户消息已读")
    @PostMapping("read")
    public Result<Void> read(@RequestParam("userId") Long userId) {
        kefuChatService.markReadForStaff(userId);
        return Result.success();
    }
}
