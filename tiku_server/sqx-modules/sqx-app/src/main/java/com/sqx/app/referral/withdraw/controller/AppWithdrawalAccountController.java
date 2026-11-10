package com.sqx.app.referral.withdraw.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.referral.withdraw.domain.bo.AppWithdrawalAccountBindBO;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountInfoBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountPageBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountSaveBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountUpdateBo;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawalAccount;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawalAccountBindResultVO;
import com.sqx.app.referral.withdraw.domain.vo.WithdrawalAccountVo;
import com.sqx.app.referral.withdraw.service.WithdrawalAccountService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 提现账号 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-06-20 14:02:39
 */
@Tag(name = "用户端-提现账号", description = "用户端-提现账号")
@RestController
@RequestMapping("/app/withdrawalAccount")
@SecurityRequirement(name = "appToken")
public class AppWithdrawalAccountController {

    @Resource
    private WithdrawalAccountService withdrawalAccountService;

    /**
     * 实名绑定提现账户（提现前置条件）
     */
    @Login
    @Operation(summary = "实名绑定提现账户", description = "提交真实姓名、身份证号与提现账户信息，完成后方可申请提现")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppWithdrawalAccountBindResultVO.class)))
    @PostMapping
    public Result<AppWithdrawalAccountBindResultVO> bind(@RequestAttribute("userId") Long userId,
                                                         @RequestBody @Validated AppWithdrawalAccountBindBO bindBO) {
        return Result.success(withdrawalAccountService.bindRealNameAccount(userId, bindBO));
    }

    /**
     * 保存
     */
    @Login
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestAttribute("userId") Long userId, @RequestBody @Validated WithdrawalAccountSaveBo withdrawalAccountSaveBo) {
        withdrawalAccountSaveBo.setUserId(userId);
        withdrawalAccountService.saveInfo(withdrawalAccountSaveBo);
        return Result.success();
    }

    /**
     * 列表
     */
    @Login
    @Operation(summary = "列表")
    @PostMapping("page")
    public Result<IPage<WithdrawalAccountVo>> page(@RequestAttribute Long userId, @RequestBody @Validated WithdrawalAccountPageBo withdrawalAccountPageBo) {
        withdrawalAccountPageBo.setUserId(userId);
        IPage<WithdrawalAccountVo> voIPage = withdrawalAccountService.pageInfo(withdrawalAccountPageBo);
        return Result.success(voIPage);
    }

    /**
     * 信息
     */
    @Operation(summary = "信息")
    @PostMapping("info")
    public Result<WithdrawalAccountVo> info(@RequestBody @Validated WithdrawalAccountInfoBo withdrawalAccountInfoBo) {
        WithdrawalAccountVo vo = withdrawalAccountService.info(withdrawalAccountInfoBo);
        return Result.success(vo);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated WithdrawalAccountUpdateBo withdrawalAccountUpdateBo) {
        withdrawalAccountService.updateInfo(withdrawalAccountUpdateBo);
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated WithdrawalAccountInfoBo withdrawalAccountInfoBo) {
        withdrawalAccountService.delete(withdrawalAccountInfoBo);
        return Result.success();
    }

    @Login
    @Operation(summary = "获取用户提现账户")
    @GetMapping("getUserWithdrawalAccount")
    public Result<WithdrawalAccount> getUserWithdrawalAccount(@RequestAttribute("userId") Long userId,
                                                              @RequestParam("type") Integer type) {
        WithdrawalAccount withdrawalAccount = withdrawalAccountService.getUserWithdrawalAccount(userId, type);
        return Result.success(withdrawalAccount);
    }
}
