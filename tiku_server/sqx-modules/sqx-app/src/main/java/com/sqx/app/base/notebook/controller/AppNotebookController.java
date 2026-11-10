package com.sqx.app.base.notebook.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.notebook.domain.bo.AppNotebookCreateBO;
import com.sqx.app.base.notebook.domain.bo.AppNotebookQueryBO;
import com.sqx.app.base.notebook.domain.bo.AppNotebookUpdateBO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookAddResultVO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookItemVO;
import com.sqx.app.base.notebook.service.NotebookService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端错题本（GET/POST/DELETE /app/notebook，需登录）
 */
@Tag(name = "用户端-错题本", description = "文档 21.2：GET/POST/DELETE /app/notebook 错题本云端同步 CRUD，见 docs/notebook-api.md")
@RestController
@RequestMapping("/app/notebook")
@SecurityRequirement(name = "appToken")
public class AppNotebookController {

    @Resource
    private NotebookService notebookService;

    @Operation(summary = "错题本列表", description = "返回当前用户错题，含题干、选项、解析与 subject；可按学科/掌握状态筛选")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppNotebookItemVO.class)))
    @Login
    @GetMapping
    public Result<List<AppNotebookItemVO>> list(@RequestAttribute("userId") Long userId,
                                                AppNotebookQueryBO query) {
        return Result.success(notebookService.listNotebook(userId, query));
    }

    @Operation(summary = "加入错题本", description = "questionId 支持 id 或 question_code；已存在则更新 subject/答案并返回 duplicate=true")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppNotebookAddResultVO.class)))
    @Login
    @PostMapping
    public Result<AppNotebookAddResultVO> add(@RequestAttribute("userId") Long userId,
                                              @RequestBody @Validated AppNotebookCreateBO create) {
        return Result.success(notebookService.addNotebook(userId, create));
    }

    @Operation(summary = "更新掌握状态", description = "questionId 支持 id 或 question_code；切换已掌握/待复习并更新 lastPracticeAt")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppNotebookItemVO.class)))
    @Login
    @PatchMapping
    public Result<AppNotebookItemVO> updateMastered(@RequestAttribute("userId") Long userId,
                                                    @RequestBody @Validated AppNotebookUpdateBO update) {
        return Result.success(notebookService.updateMastered(userId, update));
    }

    @Operation(summary = "移除错题", description = "questionId 支持 id 或 question_code；不传 questionId 时清空当前用户全部错题")
    @Login
    @DeleteMapping
    public Result<Void> remove(@RequestAttribute("userId") Long userId,
                               @RequestParam(value = "questionId", required = false) String questionId) {
        if (StringUtils.isBlank(questionId)) {
            notebookService.clearNotebook(userId);
        } else {
            notebookService.removeNotebook(userId, questionId);
        }
        return Result.success();
    }
}
