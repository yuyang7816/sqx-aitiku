package com.sqx.system.service;

import com.sqx.common.mybatis.utils.PageUtils;

import java.util.Map;

/**
 * 数据字典
 *
 */
public interface SysDictService {

    PageUtils queryPage(Map<String, Object> params);
}

