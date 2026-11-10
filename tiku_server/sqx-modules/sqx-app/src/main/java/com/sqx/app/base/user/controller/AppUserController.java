package com.sqx.app.base.user.controller;

import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.user.domain.bo.ForgetPasswordBO;
import com.sqx.app.base.user.domain.bo.UpdateUserBO;
import com.sqx.app.base.user.domain.vo.UserVo;
import com.sqx.app.base.user.service.UserService;
import com.sqx.common.core.domain.Result;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.user.domain.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author chengming
 * @since 2025/4/24
 */
@Tag(name = "用户端-用户", description = "用户端-用户；需登录接口请在请求头携带 Authorization: Bearer {token}")
@RestController
@RequestMapping(BaseConstant.BASE_APP_PREFIX + "/user")
@SecurityRequirement(name = "appToken")
public class AppUserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取当前登录用户信息")
    @Login
    @GetMapping("info")
    public Result<UserVo> info(@RequestAttribute("userId") Long userId) {
        return userService.info(userId);
    }

    @Operation(summary = "根据id获取用户信息")
    @Login
    @GetMapping("getUserInfoById")
    public Result<UserVo> getUserInfoById(Long userId) {
        return userService.info(userId);
    }

    @Operation(summary = "忘记密码")
    @Login
    @PostMapping("forgetPassword")
    public Result<Void> forgetPassword(@RequestAttribute("userId") Long userId, @RequestBody ForgetPasswordBO forgetPasswordBO) {
        forgetPasswordBO.setUserId(userId);
        return userService.forgetPassword(forgetPasswordBO);
    }

    @Operation(summary = "修改用户信息")
    @Login
    @PostMapping("update")
    public Result<UserEntity> update(@RequestBody @Validated UpdateUserBO updateUserBO) {
        userService.update(updateUserBO);
        return Result.success();
    }
}
