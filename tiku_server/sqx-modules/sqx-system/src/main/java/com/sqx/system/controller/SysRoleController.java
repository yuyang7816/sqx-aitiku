package com.sqx.system.controller;


import com.sqx.common.core.domain.Result;
import com.sqx.common.core.validator.ValidatorUtils;
import com.sqx.common.mybatis.utils.PageUtils;
import com.sqx.system.aspect.annotation.SysLog;
import com.sqx.system.db.entity.SysRoleEntity;
import com.sqx.system.db.service.ISysRoleService;
import com.sqx.system.domain.res.PageRes;
import com.sqx.system.domain.res.SysRoleRes;
import com.sqx.system.service.SysRoleMenuService;
import com.sqx.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理
 *
 */
@Tag(name = "角色管理", description = "角色管理")
@RestController
@RequestMapping("/sys/role")
public class SysRoleController extends AbstractController {
	@Autowired
	private SysRoleService sysRoleService;
	@Autowired
	private SysRoleMenuService sysRoleMenuService;
	@Autowired
	private ISysRoleService iSysRoleService;

	/**
	 * 角色列表
	 */
	@Operation(summary = "角色列表")
	@GetMapping("/list")
	@RequiresPermissions("sys:role:list")
	public Result list(@RequestParam Map<String, Object> params){
		PageRes pageRes = new PageRes();

		//如果不是超级管理员，则只查询自己创建的角色列表
		/*if(getUserId() != Constant.SUPER_ADMIN){
			params.put("createUserId", getUserId());
		}*/
		PageUtils page = sysRoleService.queryPage(params);
		pageRes.setPage(page);
		return Result.success(pageRes);
	}

	/**
	 * 角色列表
	 */
	@Operation(summary = "角色列表")
	@GetMapping("/select")
	@RequiresPermissions("sys:role:select")
	public Result select(){
		SysRoleRes sysRoleRes = new SysRoleRes();
		Map<String, Object> map = new HashMap<>();

		//如果不是超级管理员，则只查询自己所拥有的角色列表
		/*if(getUserId() != Constant.SUPER_ADMIN){
			map.put("create_user_id", getUserId());
		}*/
		List<SysRoleEntity> list = (List<SysRoleEntity>) iSysRoleService.listByMap(map);
		sysRoleRes.setList(list);
		return Result.success(sysRoleRes);
	}

	/**
	 * 角色信息
	 */
	@Operation(summary = "角色信息")
	@GetMapping("/info/{roleId}")
	@RequiresPermissions("sys:role:info")
	public Result info(@PathVariable("roleId") Long roleId){
		SysRoleRes sysRoleRes = new SysRoleRes();

		SysRoleEntity role = iSysRoleService.getById(roleId);

		//查询角色对应的菜单
		List<Long> menuIdList = sysRoleMenuService.queryMenuIdList(roleId);
		role.setMenuIdList(menuIdList);

		sysRoleRes.setRole(role);
		return Result.success(sysRoleRes);
	}

	/**
	 * 保存角色
	 */
	@Operation(summary = "保存角色")
	@SysLog("保存角色")
	@PostMapping("/save")
	@RequiresPermissions("sys:role:save")
	public Result save(@RequestBody SysRoleEntity role){
		ValidatorUtils.validateEntity(role);

		role.setCreateUserId(getUserId());
		sysRoleService.saveRole(role);

		return Result.success();
	}

	/**
	 * 修改角色
	 */
	@Operation(summary = "修改角色")
	@SysLog("修改角色")
	@PostMapping("/update")
	@RequiresPermissions("sys:role:update")
	public Result update(@RequestBody SysRoleEntity role){
		ValidatorUtils.validateEntity(role);

		role.setCreateUserId(getUserId());
		sysRoleService.update(role);

		return Result.success();
	}

	/**
	 * 删除角色
	 */
	@Operation(summary = "删除角色")
	@SysLog("删除角色")
	@PostMapping("/delete")
	@RequiresPermissions("sys:role:delete")
	public Result delete(@RequestBody Long[] roleIds){
		sysRoleService.deleteBatch(roleIds);

		return Result.success();
	}
}
