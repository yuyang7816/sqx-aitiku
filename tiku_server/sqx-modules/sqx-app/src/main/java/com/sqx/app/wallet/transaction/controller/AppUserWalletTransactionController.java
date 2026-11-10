package com.sqx.app.wallet.transaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionInfoBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionPageBo;
import com.sqx.app.wallet.transaction.domain.vo.AppUserWalletTransactionDetailVO;
import com.sqx.app.wallet.transaction.domain.vo.UserWalletTransactionVo;
import com.sqx.app.wallet.transaction.service.UserWalletTransactionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户钱包交易记录表 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
@Tag(name = "用户端-用户钱包交易记录表", description = "用户端收益流水；GET /app/userWalletTransaction 需登录")
@RestController
@RequestMapping("/app/userWalletTransaction")
@SecurityRequirement(name = "appToken")
public class AppUserWalletTransactionController {

    @Resource
    private UserWalletTransactionService userWalletTransactionService;

    /**
     * 收益流水详情（累计/待结算 + 分页列表，兼容 uniapp 收益明细页）
     */
    @Login
    @Operation(summary = "收益流水详情", description = "返回累计收益、待结算金额与分页流水列表（标题/时间/金额/状态）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppUserWalletTransactionDetailVO.class)))
    @GetMapping
    public Result<AppUserWalletTransactionDetailVO> detail(@RequestAttribute("userId") Long userId,
                                                           @RequestParam(value = "page", required = false) Long page,
                                                           @RequestParam(value = "limit", required = false) Long limit) {
        return Result.success(userWalletTransactionService.getDetail(userId, page, limit));
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
}
