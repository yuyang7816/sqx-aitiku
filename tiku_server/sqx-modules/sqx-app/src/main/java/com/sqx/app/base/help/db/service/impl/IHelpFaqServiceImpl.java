package com.sqx.app.base.help.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.help.db.mapper.HelpFaqMapper;
import com.sqx.app.base.help.db.service.IHelpFaqService;
import com.sqx.app.base.help.domain.entity.HelpFaq;
import org.springframework.stereotype.Service;

@Service
public class IHelpFaqServiceImpl extends ServiceImpl<HelpFaqMapper, HelpFaq> implements IHelpFaqService {
}
