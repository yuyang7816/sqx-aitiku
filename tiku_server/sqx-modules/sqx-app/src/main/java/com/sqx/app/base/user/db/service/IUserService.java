package com.sqx.app.base.user.db.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.app.base.user.domain.entity.UserEntity;


/**
 * 用户
 *
 * @author fang
 * @date 2021/2/27
 */
public interface IUserService extends IService<UserEntity> {

    UserEntity getByOpenId(String openid);

    UserEntity getByMpOpenId(String mpOpenId);
}
