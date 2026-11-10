package com.sqx.app.base.notebook.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.notebook.domain.bo.AdminUserNotebookPageBo;
import com.sqx.app.base.notebook.domain.vo.AdminUserNotebookItemVo;
import com.sqx.app.base.notebook.domain.vo.AdminUserNotebookSummaryVo;
import com.sqx.app.base.notebook.service.AdminUserNotebookService;
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
 * 管理端-用户错题本
 */
@Tag(name = "管理端-用户错题", description = "按用户查看错题，便于老师针对性辅导")
@RestController
@RequestMapping("/admin/userNotebook")
public class AdminUserNotebookController {

    @Resource
    private AdminUserNotebookService adminUserNotebookService;

    @Operation(summary = "有错题的用户汇总分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdminUserNotebookSummaryVo.class)))
    @PostMapping("userPage")
    public Result<IPage<AdminUserNotebookSummaryVo>> userPage(@RequestBody @Validated AdminUserNotebookPageBo pageBo) {
        return Result.success(adminUserNotebookService.pageUserSummary(pageBo));
    }

    @Operation(summary = "指定用户的错题分页列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdminUserNotebookItemVo.class)))
    @PostMapping("page")
    public Result<IPage<AdminUserNotebookItemVo>> page(@RequestBody @Validated AdminUserNotebookPageBo pageBo) {
        return Result.success(adminUserNotebookService.pageNotebook(pageBo));
    }
}
