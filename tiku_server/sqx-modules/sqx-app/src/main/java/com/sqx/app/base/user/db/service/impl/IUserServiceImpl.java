package com.sqx.app.base.user.db.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.user.db.mapper.UserMapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import org.springframework.stereotype.Service;


/**
 * 用户
 *
 * @author fang
 * @date 2021/2/27
 */

@Service
public class IUserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements IUserService {

    @Override
    public UserEntity getByOpenId(String openid) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getMaOpenId, openid);

        return this.getOne(queryWrapper);
    }

    @Override
    public UserEntity getByMpOpenId(String mpOpenId) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getMpOpenId, mpOpenId);
        return this.getOne(queryWrapper);
    }
}
