package com.sqx.app.base.homeShortcut.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.homeShortcut.domain.bo.*;
import com.sqx.app.base.homeShortcut.domain.vo.HomeShortcutVo;
import com.sqx.app.base.homeShortcut.service.HomeShortcutService;
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

@Tag(name = "管理端-首页快捷入口")
@RestController
@RequestMapping(BaseConstant.BASE_ADMIN_PREFIX + "/homeShortcut")
public class AdminHomeShortcutController {

    @Resource
    private HomeShortcutService homeShortcutService;

    @Operation(summary = "新增")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated HomeShortcutSaveBo saveBo) {
        homeShortcutService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "分页列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HomeShortcutVo.class)))
    @PostMapping("page")
    public Result<IPage<HomeShortcutVo>> page(@RequestBody @Validated HomeShortcutPageBo pageBo) {
        return Result.success(homeShortcutService.pageInfo(pageBo));
    }

    @Operation(summary = "详情")
    @PostMapping("info")
    public Result<HomeShortcutVo> info(@RequestBody @Validated HomeShortcutInfoBo infoBo) {
        return Result.success(homeShortcutService.info(infoBo));
    }

    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated HomeShortcutUpdateBo updateBo) {
        homeShortcutService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated HomeShortcutInfoBo infoBo) {
        homeShortcutService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "修改启用状态")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated HomeShortcutUpdateStateBo updateStateBo) {
        homeShortcutService.updateState(updateStateBo);
        return Result.success();
    }
}
