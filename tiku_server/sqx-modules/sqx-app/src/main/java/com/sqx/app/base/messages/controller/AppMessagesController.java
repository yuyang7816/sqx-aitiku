package com.sqx.app.base.messages.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.messages.domain.bo.MessagesInfoBo;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.bo.MessagesSaveBo;
import com.sqx.app.base.messages.domain.bo.MessagesUpdateBo;
import com.sqx.app.base.messages.domain.vo.AppMessageItemVO;
import com.sqx.app.base.messages.domain.vo.MessagesVo;
import com.sqx.app.base.messages.service.MessagesService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

/**
 * 消息信息表 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@Tag(name = "用户端-消息信息表", description = "用户端消息；GET /app/messages 需登录")
@RestController
@RequestMapping("/app/messages")
@SecurityRequirement(name = "appToken")
public class AppMessagesController {

    @Resource
    private MessagesService messagesService;

    /**
     * 分页消息列表（uniapp 消息中心 / 首页铃铛）
     */
    @Operation(summary = "分页消息列表", description = "返回 read 状态与跳转 id（notice-review 等），仅当前登录用户消息")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppMessageItemVO.class)))
    @Login
    @GetMapping
    public Result<IPage<AppMessageItemVO>> list(@RequestAttribute("userId") Long userId,
                                                @RequestParam(value = "page", required = false) Long page,
                                                @RequestParam(value = "limit", required = false) Long limit,
                                                @RequestParam(value = "read", required = false) Boolean read) {
        return Result.success(messagesService.pageAppMessages(userId, page, limit, read));
    }

    /**
     * 全部标记已读（uniapp 消息中心「全部已读」）
     */
    @Operation(summary = "全部标记已读", description = "将当前登录用户全部未读消息标记为已读")
    @Login
    @PostMapping("read-all")
    public Result<Void> readAll(@RequestAttribute("userId") Long userId) {
        messagesService.markAllAppMessagesRead(userId);
        return Result.success();
    }

    /**
     * 保存
     */
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated MessagesSaveBo messagesSaveBo) {
        messagesService.saveInfo(messagesSaveBo);
        return Result.success();
    }

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MessagesVo.class)))
    @PostMapping("page")
    public Result<IPage<MessagesVo>> page(@RequestBody @Validated MessagesPageBo messagesPageBo) {
        IPage<MessagesVo> voIPage = messagesService.pageInfo(messagesPageBo);
        return Result.success(voIPage);
    }

    /**
     * 信息
     */
    @Operation(summary = "信息")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MessagesVo.class)))
    @PostMapping("info")
    public Result<MessagesVo> info(@RequestBody @Validated MessagesInfoBo messagesInfoBo) {
        MessagesVo vo = messagesService.info(messagesInfoBo);
        return Result.success(vo);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated MessagesUpdateBo messagesUpdateBo) {
        messagesService.updateInfo(messagesUpdateBo);
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated MessagesInfoBo messagesInfoBo) {
        messagesService.delete(messagesInfoBo);
        return Result.success();
    }
}
