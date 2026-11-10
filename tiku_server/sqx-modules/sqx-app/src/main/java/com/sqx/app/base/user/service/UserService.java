package com.sqx.app.base.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.domain.bo.DeleteUserBO;
import com.sqx.app.base.user.domain.bo.ForgetPasswordBO;
import com.sqx.app.base.user.domain.bo.ListUserBO;
import com.sqx.app.base.user.domain.bo.UpdateAppUserProfileBO;
import com.sqx.app.base.user.domain.bo.UpdateUserBO;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.delete.domain.vo.AppUserDeleteAccountVO;
import com.sqx.app.base.user.domain.vo.AppUserProfileVO;
import com.sqx.app.base.user.domain.vo.UserVo;
import com.sqx.common.core.domain.Result;

import java.util.List;

/**
 * @author chengming
 * @since 2025/4/24
 */
public interface UserService {
    Result<UserVo> info(Long userId);

    /**
     * 用户端获取当前登录用户资料（GET /app/user）
     */
    Result<AppUserProfileVO> getAppUserProfile(Long userId);

    /**
     * 用户端更新学习计划资料（PUT/POST /app/user）
     */
    Result<AppUserProfileVO> updateAppUserProfile(Long userId, UpdateAppUserProfileBO updateAppUserProfileBO);

    /**
     * 用户端账号注销（POST /app/user/delete）
     */
    Result<AppUserDeleteAccountVO> deleteAppAccount(Long userId);

    Result<Void> forgetPassword(ForgetPasswordBO forgetPasswordBO);

    Page<UserVo> list(ListUserBO listUserBO);

    void update(UpdateUserBO updateUserBO);

    void delete(DeleteUserBO deleteUserBO);
}
