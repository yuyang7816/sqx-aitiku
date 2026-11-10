package com.sqx.app.base.homeShortcut.controller;

import com.sqx.app.base.homeShortcut.domain.vo.AppHomeShortcutItemVO;
import com.sqx.app.base.homeShortcut.service.HomeShortcutService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "用户端-首页快捷入口", description = "GET /app/homeShortcut 返回启用的 8 宫格入口")
@RestController
@RequestMapping("/app/homeShortcut")
public class AppHomeShortcutController {

    @Resource
    private HomeShortcutService homeShortcutService;

    @Operation(summary = "首页快捷入口列表", description = "仅返回启用项，按 sort_order 升序")
    @GetMapping
    public Result<List<AppHomeShortcutItemVO>> list() {
        return Result.success(homeShortcutService.listAppShortcuts());
    }
}
