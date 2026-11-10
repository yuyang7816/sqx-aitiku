package com.sqx.system.db.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.SysMenuEntity;
import com.sqx.system.db.mapper.SysMenuDao;
import com.sqx.system.db.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
public class ISysMenuServiceImpl extends ServiceImpl<SysMenuDao, SysMenuEntity> implements ISysMenuService {

    @Override
    public List<SysMenuEntity> listMenu() {
        return this.baseMapper.listMenu();
    }
}
