package com.sqx.system.controller;


import com.sqx.common.core.domain.Result;
import com.sqx.common.mybatis.utils.PageUtils;
import com.sqx.system.domain.res.PageRes;
import com.sqx.system.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 系统日志
 *
 */
@Tag(name = "系统日志", description = "系统日志")
@RestController
@RequestMapping("/sys/log")
public class SysLogController {
	@Autowired
	private SysLogService sysLogService;
	
	/**
	 * 列表
	 */
	@Operation(summary = "列表")
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("sys:log:list")
	public Result<PageRes> list(@RequestParam Map<String, Object> params){
		PageUtils page = sysLogService.queryPage(params);

		PageRes res = new PageRes();
		res.setPage(page);
		return Result.success(res);
	}
	
}
