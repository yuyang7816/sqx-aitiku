package com.sqx.app.base.setup.controller;

import com.sqx.app.base.setup.domain.vo.SetupOptionsVO;
import com.sqx.app.base.setup.service.SetupOptionsService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端学习计划选项（替代 uniapp 硬编码 mock）
 */
@Tag(name = "用户端-学习计划选项", description = "返回年级/学科/教材版本选项，无需登录")
@RestController
@RequestMapping("app/setup")
public class AppSetupOptionsController {

    @Resource
    private SetupOptionsService setupOptionsService;

    @Operation(summary = "获取学习计划选项", description = "返回 grades、subjects、versions，结构与 plan.vue mock 兼容")
    @GetMapping("options")
    public Result<SetupOptionsVO> options() {
        return Result.success(setupOptionsService.getSetupOptions());
    }
}
