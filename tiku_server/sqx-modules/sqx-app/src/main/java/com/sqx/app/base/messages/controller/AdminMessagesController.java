package com.sqx.app.base.messages.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.messages.domain.vo.MessagesVo;
import com.sqx.app.base.messages.domain.bo.MessagesInfoBo;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.bo.MessagesSaveBo;
import com.sqx.app.base.messages.domain.bo.MessagesUpdateBo;
import com.sqx.app.base.messages.service.MessagesService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

/**
 * 消息信息表 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@Tag(name = "管理端-消息信息表", description = "管理端-消息信息表")
@RestController
@RequestMapping("/admin/messages")
public class AdminMessagesController {

    @Resource
    private MessagesService messagesService;

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
