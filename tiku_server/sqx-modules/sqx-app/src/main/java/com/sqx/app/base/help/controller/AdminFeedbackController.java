package com.sqx.app.base.help.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.help.domain.bo.FeedbackInfoBo;
import com.sqx.app.base.help.domain.bo.FeedbackPageBo;
import com.sqx.app.base.help.domain.bo.FeedbackUpdateBo;
import com.sqx.app.base.help.domain.vo.FeedbackVo;
import com.sqx.app.base.help.service.AdminFeedbackService;
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
 * 管理端-用户反馈工单
 */
@Tag(name = "管理端-用户反馈", description = "运营查看与处理用户反馈工单")
@RestController
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    @Resource
    private AdminFeedbackService adminFeedbackService;

    @Operation(summary = "反馈分页列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = FeedbackVo.class)))
    @PostMapping("page")
    public Result<IPage<FeedbackVo>> page(@RequestBody @Validated FeedbackPageBo pageBo) {
        return Result.success(adminFeedbackService.pageInfo(pageBo));
    }

    @Operation(summary = "反馈详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = FeedbackVo.class)))
    @PostMapping("info")
    public Result<FeedbackVo> info(@RequestBody @Validated FeedbackInfoBo infoBo) {
        return Result.success(adminFeedbackService.info(infoBo));
    }

    @Operation(summary = "更新反馈状态/回复")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated FeedbackUpdateBo updateBo) {
        adminFeedbackService.updateInfo(updateBo);
        return Result.success();
    }
}
