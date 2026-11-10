package com.sqx.system.db.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.SysUserEntity;
import com.sqx.system.db.mapper.SysUserDao;
import com.sqx.system.db.service.ISysUserService;
import org.springframework.stereotype.Service;


/**
 * 系统用户
 *
 */
@Service
public class ISysUserServiceImpl extends ServiceImpl<SysUserDao, SysUserEntity> implements ISysUserService {

}