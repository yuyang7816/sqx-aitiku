package com.sqx.system.db.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.SysLogEntity;
import com.sqx.system.db.mapper.SysLogDao;
import com.sqx.system.db.service.ISysLogService;
import org.springframework.stereotype.Service;


@Service
public class ISysLogServiceImpl extends ServiceImpl<SysLogDao, SysLogEntity> implements ISysLogService {

}
