package com.sqx.system.controller;

import com.sqx.common.core.domain.Result;
import com.sqx.common.core.validator.ValidatorUtils;
import com.sqx.system.aspect.annotation.SysLog;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.domain.res.SysConfigListRes;
import com.sqx.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置信息
 *
 */
@Tag(name = "系统配置信息", description = "系统配置信息")
@RestController
@RequestMapping("/sys/config")
public class SysConfigController extends AbstractController {
	@Autowired
	private SysConfigService sysConfigService;

	/**
	 * 所有配置列表
	 */
	@Operation(summary = "所有配置列表")
	@GetMapping("/list")
	//@RequiresPermissions("sys:config:list")
	public Result<List<SysConfigListRes>> list(@RequestParam(required = false) Map<String, Object> params){
		return sysConfigService.listInfo(params);
	}


	/**
	 * 配置信息
	 */
	@Operation(summary = "配置信息")
	@GetMapping("/info/{id}")
	@RequiresPermissions("sys:config:info")
	public Result info(@PathVariable("id") Long id){
		return sysConfigService.info(id);
	}

	/**
	 * 保存配置
	 */
	@Operation(summary = "保存配置")
	@SysLog("保存配置")
	@PostMapping("/save")
	@RequiresPermissions("sys:config:save")
	public Result save(@RequestBody SysConfigEntity config){
		ValidatorUtils.validateEntity(config);
		sysConfigService.saveConfig(config);
		return Result.success();
	}

	/**
	 * 修改配置
	 */
	@Operation(summary = "修改配置")
	@SysLog("修改配置")
	@PostMapping("/update")
	@RequiresPermissions("sys:config:update")
	public Result update(@RequestBody SysConfigEntity config){
		ValidatorUtils.validateEntity(config);
		sysConfigService.update(config);

		return Result.success();
	}

	/**
	 * 删除配置
	 */
	@Operation(summary = "删除配置")
	@SysLog("删除配置")
	@PostMapping("/delete")
	@RequiresPermissions("sys:config:delete")
	public Result delete(@RequestBody Long[] ids){
		sysConfigService.deleteBatch(ids);

		return Result.success();
	}

	@Operation(summary = "刷新缓存")
	@SysLog("刷新缓存")
	@GetMapping("refresh")
	public Result<Void> refresh(){
		return sysConfigService.refresh();
	}

}
