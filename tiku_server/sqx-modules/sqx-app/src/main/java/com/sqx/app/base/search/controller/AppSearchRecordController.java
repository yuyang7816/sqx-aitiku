package com.sqx.app.base.search.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.search.domain.bo.AppSearchRecordUpsertBO;
import com.sqx.app.base.search.domain.vo.AppSearchRecordItemVO;
import com.sqx.app.base.search.service.SearchRecordService;
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
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端搜题记录云端同步（GET/POST/DELETE /app/search/records，需登录）
 */
@Tag(name = "用户端-搜题记录", description = "文档 21.2：GET/POST/DELETE /app/search/records 搜题历史云端同步，见 docs/search-records-api.md")
@RestController
@RequestMapping("/app/search/records")
@SecurityRequirement(name = "appToken")
public class AppSearchRecordController {

    @Resource
    private SearchRecordService searchRecordService;

    @Operation(summary = "搜题记录列表", description = "返回当前用户最近 20 条搜题记录，含 title/time/source/imageUrl")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppSearchRecordItemVO.class)))
    @Login
    @GetMapping
    public Result<List<AppSearchRecordItemVO>> list(@RequestAttribute("userId") Long userId) {
        return Result.success(searchRecordService.listRecords(userId));
    }

    @Operation(summary = "写入搜题记录", description = "同 title 去重并置顶；供 uniapp appendSearchRecord 云端同步")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppSearchRecordItemVO.class)))
    @Login
    @PostMapping
    public Result<AppSearchRecordItemVO> upsert(@RequestAttribute("userId") Long userId,
                                                @RequestBody @Validated AppSearchRecordUpsertBO upsert) {
        return Result.success(searchRecordService.upsertRecord(userId, upsert));
    }

    @Operation(summary = "清空搜题记录", description = "删除当前用户全部搜题记录；与 uniapp clearSearchRecords 同步")
    @Login
    @DeleteMapping
    public Result<Void> clear(@RequestAttribute("userId") Long userId) {
        searchRecordService.clearRecords(userId);
        return Result.success();
    }
}
