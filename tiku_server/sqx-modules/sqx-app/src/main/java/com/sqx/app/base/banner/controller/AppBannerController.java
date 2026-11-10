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
@Tag(name = "用户端-banner", description = "用户端-banner")
@RestController
@RequestMapping(BaseConstant.BASE_APP_PREFIX + "/banner")
public class AppBannerController {

    @Resource
    private BannerService BannerService;

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = BannerVo.class)))
    @PostMapping("page")
    public Result<IPage<BannerVo>> page(@RequestBody @Validated BannerPageBo bannerPageBo) {
        IPage<BannerVo> voIPage = BannerService.pageInfo(bannerPageBo);
        return Result.success(voIPage);
    }
}
