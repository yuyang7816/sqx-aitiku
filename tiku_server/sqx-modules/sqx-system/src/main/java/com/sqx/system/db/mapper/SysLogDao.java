package com.sqx.system.db.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.system.db.entity.SysLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志
 *
 */
@Mapper
public interface SysLogDao extends BaseMapper<SysLogEntity> {
	
}
