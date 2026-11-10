package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.HomeShortcut;
import com.sqx.system.db.mapper.HomeShortcutMapper;
import com.sqx.system.db.service.IHomeShortcutService;
import org.springframework.stereotype.Service;

@Service
public class IHomeShortcutServiceImpl extends ServiceImpl<HomeShortcutMapper, HomeShortcut> implements IHomeShortcutService {
}
