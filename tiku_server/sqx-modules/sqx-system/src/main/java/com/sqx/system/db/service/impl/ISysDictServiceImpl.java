package com.sqx.system.db.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.SysDictEntity;
import com.sqx.system.db.mapper.SysDictDao;
import com.sqx.system.db.service.ISysDictService;
import org.springframework.stereotype.Service;


@Service
public class ISysDictServiceImpl extends ServiceImpl<SysDictDao, SysDictEntity> implements ISysDictService {

}
