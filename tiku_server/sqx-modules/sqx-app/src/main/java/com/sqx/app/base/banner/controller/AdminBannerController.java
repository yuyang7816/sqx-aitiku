package com.sqx.app.base.banner.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.banner.domain.bo.*;
import com.sqx.app.base.banner.domain.vo.BannerVo;
import com.sqx.app.base.banner.service.BannerService;
import com.sqx.app.base.constants.BaseConstant;
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
 * banner 前端控制器
 *
 * @author sqx
 * @since 2025-05-16 13:49:04
 */
@Tag(name = "管理端-banner", description = "管理端-banner")
@RestController
@RequestMapping(BaseConstant.BASE_ADMIN_PREFIX + "/banner")
public class AdminBannerController {

    @Resource
    private BannerService bannerService;

    /**
     * 保存
     */
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated BannerSaveBo bannerSaveBo) {
        bannerService.saveInfo(bannerSaveBo);
        return Result.success();
    }

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = BannerVo.class)))
    @PostMapping("page")
    public Result<IPage<BannerVo>> page(@RequestBody @Validated BannerPageBo bannerPageBo) {
        IPage<BannerVo> voIPage = bannerService.pageInfo(bannerPageBo);
        return Result.success(voIPage);
    }

    /**
     * 信息
     */
    @Operation(summary = "信息")
    @ApiResponse(content = @Content(schema = @Schema(implementation = BannerVo.class)))
    @PostMapping("info")
    public Result<BannerVo> info(@RequestBody @Validated BannerInfoBo bannerInfoBo) {
        BannerVo vo = bannerService.info(bannerInfoBo);
        return Result.success(vo);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated BannerUpdateBo bannerUpdateBo) {
        bannerService.updateInfo(bannerUpdateBo);
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated BannerInfoBo bannerInfoBo) {
        bannerService.delete(bannerInfoBo);
        return Result.success();
    }

    @Operation(summary = "修改状态")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated BannerUpdateStateBo bannerUpdateStateBo) {
        bannerService.updateState(bannerUpdateStateBo);
        return Result.success();
    }
}
