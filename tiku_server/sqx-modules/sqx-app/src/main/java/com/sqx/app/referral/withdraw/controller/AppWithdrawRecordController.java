package com.sqx.app.referral.withdraw.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.referral.withdraw.domain.bo.AppWithdrawApplyBO;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawRecordPageBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawRecordSaveBo;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawApplyResultVO;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawRulesVO;
import com.sqx.app.referral.withdraw.domain.vo.WithdrawRecordVo;
import com.sqx.app.referral.withdraw.service.WithdrawRecordService;
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
 * 用户端提现记录（POST /app/withdrawRecord 申请提现，需登录）
 */
@Tag(name = "用户端-提现记录", description = "提现申请与记录查询；默认微信零钱渠道，管理端审核")
@RestController
@RequestMapping("/app/withdrawRecord")
@SecurityRequirement(name = "appToken")
public class AppWithdrawRecordController {

    @Resource
    private WithdrawRecordService withdrawRecordService;

    @Login
    @Operation(summary = "申请提现", description = "提交提现金额与渠道（默认微信零钱），入库待审核并冻结钱包余额")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppWithdrawApplyResultVO.class)))
    @PostMapping
    public Result<AppWithdrawApplyResultVO> apply(@RequestAttribute("userId") Long userId,
                                                  @RequestBody @Validated AppWithdrawApplyBO applyBO) {
        return Result.success(withdrawRecordService.applyWithdraw(userId, applyBO));
    }

    /**
     * 兼容旧路径
     */
    @Login
    @Operation(summary = "发起提现（兼容）")
    @PostMapping("save")
    public Result<AppWithdrawApplyResultVO> save(@RequestAttribute("userId") Long userId,
                                                 @RequestBody @Validated WithdrawRecordSaveBo withdrawRecordSaveBo) {
        AppWithdrawApplyBO applyBO = new AppWithdrawApplyBO();
        applyBO.setAmount(withdrawRecordSaveBo.getAmount());
        applyBO.setAccountType(withdrawRecordSaveBo.getAccountType());
        return Result.success(withdrawRecordService.applyWithdraw(userId, applyBO));
    }

    @Login
    @Operation(summary = "提现规则", description = "最低/最高提现金额、手续费率")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppWithdrawRulesVO.class)))
    @GetMapping("rules")
    public Result<AppWithdrawRulesVO> rules() {
        return Result.success(withdrawRecordService.getWithdrawRules());
    }

    @Login
    @Operation(summary = "提现记录列表")
    @PostMapping("page")
    public Result<IPage<WithdrawRecordVo>> page(@RequestAttribute("userId") Long userId,
                                                @RequestBody @Validated WithdrawRecordPageBo withdrawRecordPageBo) {
        withdrawRecordPageBo.setUserId(userId);
        return Result.success(withdrawRecordService.pageInfo(withdrawRecordPageBo));
    }
}
