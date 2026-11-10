package com.sqx.system.db.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.SysUserRoleEntity;
import com.sqx.system.db.mapper.SysUserRoleDao;
import com.sqx.system.db.service.ISysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户与角色对应关系
 *
 */
@Service
public class ISysUserRoleServiceImpl extends ServiceImpl<SysUserRoleDao, SysUserRoleEntity> implements ISysUserRoleService {

}
