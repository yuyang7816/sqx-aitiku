package com.sqx.app.base.practiceEntry.controller;

import com.sqx.app.base.practiceEntry.domain.vo.AppPracticeEntryItemVO;
import com.sqx.app.base.practiceEntry.service.PracticeEntryService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "用户端-练习入口", description = "GET /app/practiceEntry 返回启用的练习卡片")
@RestController
@RequestMapping("/app/practiceEntry")
public class AppPracticeEntryController {

    @Resource
    private PracticeEntryService practiceEntryService;

    @Operation(summary = "练习入口列表", description = "仅返回启用项，按 sort_order 升序")
    @GetMapping
    public Result<List<AppPracticeEntryItemVO>> list() {
        return Result.success(practiceEntryService.listAppEntries());
    }
}
