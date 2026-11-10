package com.sqx.app.base.collection.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.collection.domain.bo.AppCollectionCreateBO;
import com.sqx.app.base.collection.domain.bo.AppCollectionQueryBO;
import com.sqx.app.base.collection.domain.vo.AppCollectionItemVO;
import com.sqx.app.base.collection.domain.vo.AppCollectionToggleResultVO;
import com.sqx.app.base.collection.service.CollectionService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端题目收藏（GET/POST/DELETE /app/collection，需登录）
 */
@Tag(name = "用户端-题目收藏", description = "文档 21.2：GET/POST/DELETE /app/collection 收藏云端同步 CRUD，见 docs/collection-api.md")
@RestController
@RequestMapping("/app/collection")
@SecurityRequirement(name = "appToken")
public class AppCollectionController {

    @Resource
    private CollectionService collectionService;

    @Operation(summary = "收藏列表", description = "返回当前用户收藏，含题干、选项、解析与 subject；可按学科筛选")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppCollectionItemVO.class)))
    @Login
    @GetMapping
    public Result<List<AppCollectionItemVO>> list(@RequestAttribute("userId") Long userId,
                                                  AppCollectionQueryBO query) {
        return Result.success(collectionService.listCollections(userId, query));
    }

    @Operation(summary = "新增收藏", description = "questionId 支持 id 或 question_code；已收藏则更新 subject")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppCollectionToggleResultVO.class)))
    @Login
    @PostMapping
    public Result<AppCollectionToggleResultVO> add(@RequestAttribute("userId") Long userId,
                                                   @RequestBody @Validated AppCollectionCreateBO create) {
        return Result.success(collectionService.addCollection(userId, create));
    }

    @Operation(summary = "取消收藏", description = "questionId 支持 id 或 question_code；不存在时静默成功")
    @Login
    @DeleteMapping
    public Result<Void> remove(@RequestAttribute("userId") Long userId,
                               @RequestParam("questionId") String questionId) {
        collectionService.removeCollection(userId, questionId);
        return Result.success();
    }
}
