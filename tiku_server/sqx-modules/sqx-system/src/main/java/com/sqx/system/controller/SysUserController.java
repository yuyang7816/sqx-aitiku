package com.sqx.system.controller;


import com.sqx.common.core.domain.Result;
import com.sqx.common.core.validator.Assert;
import com.sqx.common.core.validator.ValidatorUtils;
import com.sqx.common.core.validator.group.AddGroup;
import com.sqx.common.core.validator.group.UpdateGroup;
import com.sqx.common.mybatis.utils.PageUtils;
import com.sqx.system.aspect.annotation.SysLog;
import com.sqx.system.db.entity.SysUserEntity;
import com.sqx.system.db.service.ISysUserService;
import com.sqx.system.domain.form.PasswordForm;
import com.sqx.system.domain.res.PageRes;
import com.sqx.system.domain.res.SysUserRes;
import com.sqx.system.service.SysUserRoleService;
import com.sqx.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统用户
 *
 */
@Tag(name = "系统用户", description = "系统用户")
@RestController
@RequestMapping("/sys/user")
public class SysUserController extends AbstractController {
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysUserRoleService sysUserRoleService;
	@Autowired
	private ISysUserService iSysUserService;


	/**
	 * 所有用户列表
	 */
	@Operation(summary = "所有用户列表")
	@GetMapping("/list")
	@RequiresPermissions("sys:user:list")
	public Result list(@RequestParam Map<String, Object> params){
		//只有超级管理员，才能查看所有管理员列表
		/*if(getUserId() != Constant.SUPER_ADMIN){
			params.put("createUserId", getUserId());
		}*/
		PageUtils page = sysUserService.queryPage(params);

		PageRes res = new PageRes();
		res.setPage(page);
		return Result.success(res);
	}
	
	/**
	 * 获取登录的用户信息
	 */
	@Operation(summary = "获取登录的用户信息")
	@GetMapping("/info")
	public Result info(){
		SysUserRes res = new SysUserRes();
		res.setUser(getUser());
		return Result.success(res);
	}
	
	/**
	 * 修改登录用户密码
	 */
	@Operation(summary = "修改登录用户密码")
	@SysLog("修改密码")
	@PostMapping("/password")
	public Result password(@RequestBody PasswordForm form){
		Assert.isBlank(form.getNewPassword(), "新密码不为能空");
		
		//sha256加密
		String password = new Sha256Hash(form.getPassword(), getUser().getSalt()).toHex();
		//sha256加密
		String newPassword = new Sha256Hash(form.getNewPassword(), getUser().getSalt()).toHex();
				
		//更新密码
		boolean flag = sysUserService.updatePassword(getUserId(), password, newPassword);
		if(!flag){
			return Result.failed("原密码不正确");
		}
		
		return Result.success();
	}
	
	/**
	 * 用户信息
	 */
	@Operation(summary = "用户信息")
	@GetMapping("/info/{userId}")
	@RequiresPermissions("sys:user:info")
	public Result info(@PathVariable("userId") Long userId){
		SysUserEntity user = iSysUserService.getById(userId);
		
		//获取用户所属的角色列表
		List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userId);
		user.setRoleIdList(roleIdList);

		SysUserRes res = new SysUserRes();
		res.setUser(user);
		return Result.success(res);
	}
	
	/**
	 * 保存用户
	 */
	@Operation(summary = "保存用户")
	@SysLog("保存用户")
	@PostMapping("/save")
	@RequiresPermissions("sys:user:save")
	public Result save(@RequestBody SysUserEntity user){
		ValidatorUtils.validateEntity(user, AddGroup.class);
		
		user.setCreateUserId(getUserId());
		sysUserService.saveUser(user);
		
		return Result.success();
	}
	
	/**
	 * 修改用户
	 */
	@Operation(summary = "修改用户")
	@SysLog("修改用户")
	@PostMapping("/update")
	@RequiresPermissions("sys:user:update")
	public Result update(@RequestBody SysUserEntity user){
		ValidatorUtils.validateEntity(user, UpdateGroup.class);

		user.setCreateUserId(getUserId());
		sysUserService.update(user);
		
		return Result.success();
	}
	
	/**
	 * 删除用户
	 */
	@Operation(summary = "删除用户")
	@SysLog("删除用户")
	@PostMapping("/delete")
	@RequiresPermissions("sys:user:delete")
	public Result delete(@RequestBody Long[] userIds){
		if(ArrayUtils.contains(userIds, 1L)){
			return Result.failed("系统管理员不能删除");
		}
		
		if(ArrayUtils.contains(userIds, getUserId())){
			return Result.failed("当前用户不能删除");
		}
		
		sysUserService.deleteBatch(userIds);
		
		return Result.success();
	}
}
