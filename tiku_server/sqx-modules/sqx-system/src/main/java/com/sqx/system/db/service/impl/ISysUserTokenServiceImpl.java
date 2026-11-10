package com.sqx.system.db.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.SysUserTokenEntity;
import com.sqx.system.db.mapper.SysUserTokenDao;
import com.sqx.system.db.service.ISysUserTokenService;
import org.springframework.stereotype.Service;


@Service
public class ISysUserTokenServiceImpl extends ServiceImpl<SysUserTokenDao, SysUserTokenEntity> implements ISysUserTokenService {

}
