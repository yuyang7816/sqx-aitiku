package com.sqx.app.referral.withdraw.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.referral.withdraw.domain.bo.*;
import com.sqx.app.referral.withdraw.domain.vo.WithdrawRecordVo;
import com.sqx.app.referral.withdraw.service.WithdrawRecordService;
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
 * 提现记录 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-05 10:10:32
 */
@Tag(name = "管理端-提现记录", description = "管理端-提现记录")
@RestController
@RequestMapping("/admin/withdrawRecord")
public class AdminWithdrawRecordController {

    @Resource
    private WithdrawRecordService withdrawRecordService;

    /**
     * 保存
     */
    @Operation(summary = "发起提现")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated WithdrawRecordSaveBo withdrawRecordSaveBo) {
        withdrawRecordService.saveInfo(withdrawRecordSaveBo);
        return Result.success();
    }

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @PostMapping("page")
    public Result<IPage<WithdrawRecordVo>> page(@RequestBody @Validated WithdrawRecordPageBo withdrawRecordPageBo) {
        IPage<WithdrawRecordVo> voIPage = withdrawRecordService.pageInfo(withdrawRecordPageBo);
        return Result.success(voIPage);
    }

    /**
     * 信息
     */
    @Operation(summary = "信息")
    @PostMapping("info")
    public Result<WithdrawRecordVo> info(@RequestBody @Validated WithdrawRecordInfoBo withdrawRecordInfoBo) {
        WithdrawRecordVo vo = withdrawRecordService.info(withdrawRecordInfoBo);
        return Result.success(vo);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated WithdrawRecordUpdateBo withdrawRecordUpdateBo) {
        withdrawRecordService.updateInfo(withdrawRecordUpdateBo);
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated WithdrawRecordInfoBo withdrawRecordInfoBo) {
        withdrawRecordService.delete(withdrawRecordInfoBo);
        return Result.success();
    }

    @Operation(summary = "确认转帐")
    @PostMapping("confirm")
    public Result<Void> confirm(@RequestBody WithdrawRecordConfirmBo withdrawRecordConfirmBo) {
        withdrawRecordService.confirm(withdrawRecordConfirmBo);
        return Result.success();
    }
}
