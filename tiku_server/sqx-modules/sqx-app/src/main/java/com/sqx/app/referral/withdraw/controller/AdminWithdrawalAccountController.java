package com.sqx.app.referral.withdraw.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountInfoBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountPageBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountSaveBo;
import com.sqx.app.referral.withdraw.domain.bo.WithdrawalAccountUpdateBo;
import com.sqx.app.referral.withdraw.domain.vo.WithdrawalAccountVo;
import com.sqx.app.referral.withdraw.service.WithdrawalAccountService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 提现账号 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-06-20 14:02:39
 */
@Tag(name = "管理端-提现账号", description = "管理端-提现账号")
@RestController
@RequestMapping("/admin/withdrawalAccount")
public class AdminWithdrawalAccountController {

    @Resource
    private WithdrawalAccountService withdrawalAccountService;

    /**
     * 保存
     */
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated WithdrawalAccountSaveBo withdrawalAccountSaveBo) {
        withdrawalAccountService.saveInfo(withdrawalAccountSaveBo);
        return Result.success();
    }

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @PostMapping("page")
    public Result<IPage<WithdrawalAccountVo>> page(@RequestBody @Validated WithdrawalAccountPageBo withdrawalAccountPageBo) {
        if (withdrawalAccountPageBo.getSource() == null) {
            withdrawalAccountPageBo.setSource(1);
        }
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
}
