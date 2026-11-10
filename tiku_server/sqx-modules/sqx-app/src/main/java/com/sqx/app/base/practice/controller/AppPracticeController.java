package com.sqx.app.base.practice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftSaveBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeProgressQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeRecordQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeSubmitBO;
import com.sqx.app.base.practice.domain.vo.AppPracticeDraftSaveResultVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeDraftVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeProgressVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeRecordItemVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeSubmitResultVO;
import com.sqx.app.base.practice.service.PracticeService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端章节练习进度与提交（需登录）
 */
@Tag(name = "用户端-练习进度", description = "文档 21.2：POST /app/practice/submit、GET /app/practice/records 练习提交与记录，见 docs/practice-submit-api.md")
@RestController
@RequestMapping("/app/practice")
@SecurityRequirement(name = "appToken")
public class AppPracticeController {

    @Resource
    private PracticeService practiceService;

    @Operation(summary = "章节练习进度", description = "按章业务编码返回已学百分比、已做/总题数")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppPracticeProgressVO.class)))
    @Login
    @GetMapping("/progress")
    public Result<AppPracticeProgressVO> progress(@RequestAttribute("userId") Long userId,
                                                  @Validated AppPracticeProgressQueryBO query) {
        return Result.success(practiceService.getChapterProgress(userId, query));
    }

    @Operation(summary = "提交练习答卷", description = "服务端判分，持久化答卷/得分/耗时/sourceMode，更新错题与掌握状态，返回 recordCode 与最新章节进度；见 docs/practice-submit-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppPracticeSubmitResultVO.class)))
    @Login
    @PostMapping("/submit")
    public Result<AppPracticeSubmitResultVO> submit(@RequestAttribute("userId") Long userId,
                                                  @RequestBody @Validated AppPracticeSubmitBO submit) {
        return Result.success(practiceService.submitPractice(userId, submit));
    }

    @Operation(summary = "练习记录列表", description = "分页返回当前用户练习提交记录，含 recordCode/得分/耗时/sourceMode；支持 chapterId、sourceMode 筛选；见 docs/practice-submit-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppPracticeRecordItemVO.class)))
    @Login
    @GetMapping("/records")
    public Result<IPage<AppPracticeRecordItemVO>> records(@RequestAttribute("userId") Long userId,
                                                          @Validated AppPracticeRecordQueryBO query) {
        return Result.success(practiceService.pagePracticeRecords(userId, query));
    }

    @Operation(summary = "保存答题暂存", description = "未交卷时保存当前题号与已答明细，中断后可续答")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppPracticeDraftSaveResultVO.class)))
    @Login
    @PostMapping("/draft")
    public Result<AppPracticeDraftSaveResultVO> saveDraft(@RequestAttribute("userId") Long userId,
                                                          @RequestBody @Validated AppPracticeDraftSaveBO save) {
        return Result.success(practiceService.savePracticeDraft(userId, save));
    }

    @Operation(summary = "恢复答题暂存", description = "按章/题集/来源模式恢复未交卷进度；无暂存时 hasDraft=false")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppPracticeDraftVO.class)))
    @Login
    @GetMapping("/draft")
    public Result<AppPracticeDraftVO> getDraft(@RequestAttribute("userId") Long userId,
                                               AppPracticeDraftQueryBO query) {
        return Result.success(practiceService.getPracticeDraft(userId, query));
    }
}
