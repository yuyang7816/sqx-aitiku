package com.sqx.system.db.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.SysRoleMenuEntity;
import com.sqx.system.db.mapper.SysRoleMenuDao;
import com.sqx.system.db.service.ISysRoleMenuService;
import org.springframework.stereotype.Service;



/**
 * 角色与菜单对应关系
 *
 */
@Service
public class ISysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuDao, SysRoleMenuEntity> implements ISysRoleMenuService {

}
