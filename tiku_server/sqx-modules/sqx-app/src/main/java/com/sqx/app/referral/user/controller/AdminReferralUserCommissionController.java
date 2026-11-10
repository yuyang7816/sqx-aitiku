package com.sqx.app.referral.user.controller;

import com.sqx.app.referral.user.domain.bo.ReferralUserCommissionUpdateBo;
import com.sqx.app.referral.user.domain.vo.ReferralUserCommissionVo;
import com.sqx.app.referral.user.service.ReferralUserCommissionService;
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
 * 用户佣金 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:53
 */
@Tag(name = "管理端-用户佣金", description = "管理端-用户佣金")
@RestController
@RequestMapping("/admin/referral/userCommission")
public class AdminReferralUserCommissionController {

    @Resource
    private ReferralUserCommissionService referralUserCommissionService;

    /**
     * 信息
     */
    @Operation(summary = "信息")
    @PostMapping("info")
    public Result<ReferralUserCommissionVo> info(Long userId) {
        ReferralUserCommissionVo vo = referralUserCommissionService.info(userId);
        return Result.success(vo);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated ReferralUserCommissionUpdateBo referralUserCommissionUpdateBo) {
        referralUserCommissionService.updateInfo(referralUserCommissionUpdateBo);
        return Result.success();
    }
}
