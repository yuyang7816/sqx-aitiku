package com.sqx.app.base.banner.controller;

import com.sqx.app.base.banner.domain.vo.AppBannerItemVO;
import com.sqx.app.base.banner.service.BannerService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端 Banner 轮播（GET /app/banner，无需登录）
 */
@Tag(name = "用户端-Banner轮播", description = "按位置/状态返回启用中的轮播图列表")
@RestController
@RequestMapping("/app/banner")
public class AppBannerListController {

    @Resource
    private BannerService bannerService;

    /**
     * 轮播图列表
     */
    @Operation(summary = "轮播图列表", description = "按位置（classify）与状态筛选，默认返回首页启用 Banner，按 sort 升序")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppBannerItemVO.class))))
    @GetMapping
    public Result<List<AppBannerItemVO>> list(
            @RequestParam(value = "position", required = false) Integer position,
            @RequestParam(value = "state", required = false) Integer state) {
        return Result.success(bannerService.listAppBanners(position, state));
    }
}
