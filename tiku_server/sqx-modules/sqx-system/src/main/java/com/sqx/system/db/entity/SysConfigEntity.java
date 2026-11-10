package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统配置信息
 *
 */
@Data
@TableName("sys_config")
public class SysConfigEntity {
	@TableId(type = IdType.AUTO)
	private Long id;

	private String paramKey;

	private String paramValue;

	private String status;

	private String remark;

	private String paramType;

	private String paramTypeDesc;

}
