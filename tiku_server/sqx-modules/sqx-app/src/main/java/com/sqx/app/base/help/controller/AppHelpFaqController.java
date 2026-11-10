package com.sqx.app.base.help.controller;

import com.sqx.app.base.help.domain.vo.AppHelpFaqItemVO;
import com.sqx.app.base.help.service.HelpFaqService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 帮助中心 FAQ（GET /app/help/faq/list，无需登录）
 */
@Tag(name = "用户端-帮助中心FAQ", description = "常见问题列表，供帮助与反馈页展示")
@RestController
@RequestMapping("/app/help/faq")
public class AppHelpFaqController {

    @Resource
    private HelpFaqService helpFaqService;

    @Operation(summary = "常见问题列表", description = "返回启用中的 FAQ，按 sort 升序；无数据时返回空数组")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppHelpFaqItemVO.class))))
    @GetMapping("list")
    public Result<List<AppHelpFaqItemVO>> list() {
        return Result.success(helpFaqService.listAppFaqs());
    }
}
