package com.sqx.system.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.common.core.constant.Constant;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.SqxException;
import com.sqx.common.shiro.service.ShiroService;
import com.sqx.system.aspect.annotation.SysLog;
import com.sqx.system.db.entity.SysMenuEntity;
import com.sqx.system.db.service.ISysMenuService;
import com.sqx.system.domain.res.SysMenuRes;
import com.sqx.system.domain.res.SysNavRes;
import com.sqx.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 系统菜单
 *
 */
@Tag(name = "系统菜单", description = "系统菜单")
@RestController
@RequestMapping("/sys/menu")
public class SysMenuController extends AbstractController {
	@Autowired
	private SysMenuService sysMenuService;
	@Autowired
	private ShiroService shiroService;
	@Autowired
	private ISysMenuService iSysMenuService;

	/**
	 * 导航菜单
	 */
	@Operation(summary = "导航菜单")
	@GetMapping("/nav")
	public Result nav(){
		SysNavRes navResponse = new SysNavRes();

		List<SysMenuEntity> menuList = sysMenuService.getUserMenuList(getUserId());
		Set<String> permissions = shiroService.getUserPermissions(getUserId());

		navResponse.setMenuList(menuList);
		navResponse.setPermissions(permissions);
		return Result.success(navResponse);
	}

	/**
	 * 所有菜单列表
	 */
	@Operation(summary = "所有菜单列表")
	@GetMapping("/list")
	@RequiresPermissions("sys:menu:list")
	public List<SysMenuEntity> list(){
		return iSysMenuService.listMenu();
	}

	/**
	 * 选择菜单(添加、修改菜单)
	 */
	@Operation(summary = "选择菜单(添加、修改菜单)")
	@GetMapping("/select")
	@RequiresPermissions("sys:menu:select")
	public Result select(){
		SysMenuRes menuRes = new SysMenuRes();

		//查询列表数据
		List<SysMenuEntity> menuList = sysMenuService.queryNotButtonList();

		//添加顶级菜单
		SysMenuEntity root = new SysMenuEntity();
		root.setMenuId(0L);
		root.setName("一级菜单");
		root.setParentId(-1L);
		root.setOpen(true);
		menuList.add(root);

		menuRes.setMenuList(menuList);
		return Result.success(menuRes);
	}

	/**
	 * 菜单信息
	 */
	@Operation(summary = "菜单信息")
	@GetMapping("/info/{menuId}")
	@RequiresPermissions("sys:menu:info")
	public Result info(@PathVariable("menuId") Long menuId){
		SysMenuRes menuRes = new SysMenuRes();

		SysMenuEntity menu = iSysMenuService.getById(menuId);
		menuRes.setMenu(menu);
		return Result.success(menuRes);
	}

	/**
	 * 保存
	 */
	@Operation(summary = "保存")
	@SysLog("保存菜单")
	@PostMapping("/save")
	@RequiresPermissions("sys:menu:save")
	public Result save(@RequestBody SysMenuEntity menu){
		//数据校验
		verifyForm(menu);

		iSysMenuService.save(menu);

		return Result.success();
	}

	/**
	 * 修改
	 */
	@Operation(summary = "修改")
	@SysLog("修改菜单")
	@PostMapping("/update")
	@RequiresPermissions("sys:menu:update")
	public Result update(@RequestBody SysMenuEntity menu){
		//数据校验
		verifyForm(menu);

		iSysMenuService.updateById(menu);

		return Result.success();
	}

	/**
	 * 删除
	 */
	@Operation(summary = "删除")
	@SysLog("删除菜单")
	@PostMapping("/delete/{menuId}")
	@RequiresPermissions("sys:menu:delete")
	public Result delete(@PathVariable("menuId") long menuId){
		if(menuId <= 31){
			return Result.failed("系统菜单，不能删除");
		}

		//判断是否有子菜单或按钮
		List<SysMenuEntity> menuList = sysMenuService.queryListParentId(menuId);
		if(menuList.size() > 0){
			return Result.failed("请先删除子菜单或按钮");
		}

		sysMenuService.delete(menuId);

		return Result.success();
	}

	/**
	 * 验证参数是否正确
	 */
	private void verifyForm(SysMenuEntity menu){
		if(StringUtils.isBlank(menu.getName())){
			throw new SqxException("菜单名称不能为空");
		}

		if(menu.getParentId() == null){
			throw new SqxException("上级菜单不能为空");
		}

		//菜单
		if(menu.getType() == Constant.MenuType.MENU.getValue()){
			if(StringUtils.isBlank(menu.getUrl())){
				throw new SqxException("菜单URL不能为空");
			}
		}

		//上级菜单类型
		int parentType = Constant.MenuType.CATALOG.getValue();
		if(menu.getParentId() != 0){
			SysMenuEntity parentMenu = iSysMenuService.getById(menu.getParentId());
			parentType = parentMenu.getType();
		}

		//目录、菜单
		if(menu.getType() == Constant.MenuType.CATALOG.getValue() ||
				menu.getType() == Constant.MenuType.MENU.getValue()){
			if(parentType != Constant.MenuType.CATALOG.getValue()){
				throw new SqxException("上级菜单只能为目录类型");
			}
			return ;
		}

		//按钮
		if(menu.getType() == Constant.MenuType.BUTTON.getValue()){
			if(parentType != Constant.MenuType.MENU.getValue()){
				throw new SqxException("上级菜单只能为菜单类型");
			}
			return ;
		}
	}
}
