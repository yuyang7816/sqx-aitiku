package com.sqx.app.base.template.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.template.db.mapper.SmsTemplateMapper;
import com.sqx.app.base.template.db.service.ISmsTemplateService;
import com.sqx.app.base.template.domain.entity.SmsTemplate;
import org.springframework.stereotype.Service;

/**
 * 短信模板 服务实现类
 *
 * @author sqx
 * @since 2025-06-12 10:20:37
 */
@Service
public class ISmsTemplateServiceImpl extends ServiceImpl<SmsTemplateMapper, SmsTemplate> implements ISmsTemplateService {

}
