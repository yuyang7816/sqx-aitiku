package com.sqx.app.referral.withdraw.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.support.AdminUserQuerySupport;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawalAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-06-20 14:02:39
 */
@Getter
@Setter
public class WithdrawalAccountVo extends WithdrawalAccount implements AdminUserQuerySupport.UserDisplayTarget {

    @Schema(description = "用户名（展示）")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "用户信息")
    @JsonIgnore
    private UserEntity userEntity;
}
