package com.sqx.app.base.user.badge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionInfoBo;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionPageBo;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionSaveBo;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionUpdateBo;
import com.sqx.app.base.user.badge.domain.vo.UserBadgeDefinitionVo;
import com.sqx.app.base.user.badge.service.UserBadgeDefinitionService;
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
 * 管理端-用户勋章里程碑
 */
@Tag(name = "管理端-用户勋章", description = "运营维护勋章名称、条件与解锁规则")
@RestController
@RequestMapping("/admin/userBadge")
public class AdminUserBadgeController {

    @Resource
    private UserBadgeDefinitionService userBadgeDefinitionService;

    @Operation(summary = "新增勋章")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated UserBadgeDefinitionSaveBo saveBo) {
        userBadgeDefinitionService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "勋章分页列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = UserBadgeDefinitionVo.class)))
    @PostMapping("page")
    public Result<IPage<UserBadgeDefinitionVo>> page(@RequestBody @Validated UserBadgeDefinitionPageBo pageBo) {
        return Result.success(userBadgeDefinitionService.pageInfo(pageBo));
    }

    @Operation(summary = "勋章详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = UserBadgeDefinitionVo.class)))
    @PostMapping("info")
    public Result<UserBadgeDefinitionVo> info(@RequestBody @Validated UserBadgeDefinitionInfoBo infoBo) {
        return Result.success(userBadgeDefinitionService.info(infoBo));
    }

    @Operation(summary = "更新勋章")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated UserBadgeDefinitionUpdateBo updateBo) {
        userBadgeDefinitionService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除勋章")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated UserBadgeDefinitionInfoBo infoBo) {
        userBadgeDefinitionService.delete(infoBo);
        return Result.success();
    }
}
