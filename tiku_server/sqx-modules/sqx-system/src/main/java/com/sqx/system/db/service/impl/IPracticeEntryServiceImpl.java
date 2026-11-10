package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.PracticeEntry;
import com.sqx.system.db.mapper.PracticeEntryMapper;
import com.sqx.system.db.service.IPracticeEntryService;
import org.springframework.stereotype.Service;

@Service
public class IPracticeEntryServiceImpl extends ServiceImpl<PracticeEntryMapper, PracticeEntry> implements IPracticeEntryService {
}
