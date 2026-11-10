package com.sqx.app.base.ocr.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.ocr.domain.bo.AppOcrSearchBO;
import com.sqx.app.base.ocr.domain.vo.AppOcrSearchResultVO;
import com.sqx.app.base.ocr.service.OcrSearchService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端 OCR 搜题（POST /app/ocr/search，需登录）
 */
@Tag(name = "用户端-OCR", description = "文档 21.2：POST /app/ocr/search 拍照搜题识别与题库匹配，见 docs/ocr-api.md")
@RestController
@RequestMapping("/app/ocr")
@SecurityRequirement(name = "appToken")
public class AppOcrSearchController {

    @Resource
    private OcrSearchService ocrSearchService;

    @Operation(summary = "OCR 搜题", description = "图片 url 或文本识别后匹配题库，按相似度返回题目、答案、解析与 tips")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppOcrSearchResultVO.class)))
    @Login
    @PostMapping("search")
    public Result<AppOcrSearchResultVO> search(@RequestAttribute("userId") Long userId,
                                               @RequestBody @Validated AppOcrSearchBO searchBO) {
        return Result.success(ocrSearchService.search(userId, searchBO));
    }
}
