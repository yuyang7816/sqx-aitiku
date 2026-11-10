package com.sqx.system.service;

import com.sqx.common.mybatis.utils.PageUtils;

import java.util.Map;


/**
 * 系统日志
 *
 */
public interface SysLogService {

    PageUtils queryPage(Map<String, Object> params);

}
