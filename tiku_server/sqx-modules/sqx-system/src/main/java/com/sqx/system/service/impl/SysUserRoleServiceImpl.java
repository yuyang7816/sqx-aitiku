package com.sqx.system.service.impl;


import com.sqx.common.core.utils.MapUtils;
import com.sqx.system.db.entity.SysUserRoleEntity;
import com.sqx.system.db.mapper.SysUserRoleDao;
import com.sqx.system.db.service.ISysUserRoleService;
import com.sqx.system.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;



/**
 * 用户与角色对应关系
 *
 */
@Service("sysUserRoleService")
public class SysUserRoleServiceImpl implements SysUserRoleService {
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	@Autowired
	private SysUserRoleDao sysUserRoleDao;

	@Override
	public void saveOrUpdate(Long userId, List<Long> roleIdList) {
		//先删除用户与角色关系
		sysUserRoleService.removeByMap(new MapUtils().put("user_id", userId));

		if(roleIdList == null || roleIdList.size() == 0){
			return ;
		}

		//保存用户与角色关系
		for(Long roleId : roleIdList){
			SysUserRoleEntity sysUserRoleEntity = new SysUserRoleEntity();
			sysUserRoleEntity.setUserId(userId);
			sysUserRoleEntity.setRoleId(roleId);

			sysUserRoleService.save(sysUserRoleEntity);
		}
	}

	@Override
	public List<Long> queryRoleIdList(Long userId) {
		return sysUserRoleDao.queryRoleIdList(userId);
	}

	@Override
	public int deleteBatch(Long[] roleIds){
		return sysUserRoleDao.deleteBatch(roleIds);
	}
}
