package com.sqx.app.wallet.transaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionInfoBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionPageBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionSaveBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionUpdateBo;
import com.sqx.app.wallet.transaction.domain.vo.UserWalletTransactionVo;
import com.sqx.app.wallet.transaction.service.UserWalletTransactionService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户钱包交易记录表 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
@Tag(name = "管理端-用户钱包交易记录表", description = "管理端-用户钱包交易记录表")
@RestController
@RequestMapping("/admin/userWalletTransaction")
public class AdminUserWalletTransactionController {

    @Resource
    private UserWalletTransactionService userWalletTransactionService;

    /**
     * 保存
     */
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated UserWalletTransactionSaveBo userWalletTransactionSaveBo) {
        userWalletTransactionService.saveInfo(userWalletTransactionSaveBo);
        return Result.success();
    }

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = UserWalletTransactionVo.class)))
    @PostMapping("page")
    public Result<IPage<UserWalletTransactionVo>> page(@RequestBody @Validated UserWalletTransactionPageBo userWalletTransactionPageBo) {
        IPage<UserWalletTransactionVo> voIPage = userWalletTransactionService.pageInfo(userWalletTransactionPageBo);
        return Result.success(voIPage);
    }

    /**
     * 信息
     */
    @Operation(summary = "信息")
    @ApiResponse(content = @Content(schema = @Schema(implementation = UserWalletTransactionVo.class)))
    @PostMapping("info")
    public Result<UserWalletTransactionVo> info(@RequestBody @Validated UserWalletTransactionInfoBo userWalletTransactionInfoBo) {
        UserWalletTransactionVo vo = userWalletTransactionService.info(userWalletTransactionInfoBo);
        return Result.success(vo);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated UserWalletTransactionUpdateBo userWalletTransactionUpdateBo) {
        userWalletTransactionService.updateInfo(userWalletTransactionUpdateBo);
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated UserWalletTransactionInfoBo userWalletTransactionInfoBo) {
        userWalletTransactionService.delete(userWalletTransactionInfoBo);
        return Result.success();
    }
}
