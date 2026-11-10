package com.sqx.app.base.template.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.template.domain.bo.SmsTemplateInfoBo;
import com.sqx.app.base.template.domain.bo.SmsTemplatePageBo;
import com.sqx.app.base.template.domain.bo.SmsTemplateSaveBo;
import com.sqx.app.base.template.domain.bo.SmsTemplateUpdateBo;
import com.sqx.app.base.template.domain.vo.SmsTemplateVo;
import com.sqx.common.core.domain.Result;

/**
 * 短信模板 服务类
 *
 * @author sqx
 * @since 2025-06-12 10:20:37
 */
public interface SmsTemplateService {

    Result<Void> saveInfo(SmsTemplateSaveBo smsTemplateSaveBo);

    Result<IPage<SmsTemplateVo>> pageInfo(SmsTemplatePageBo smsTemplatePageBo);

    Result<SmsTemplateVo> info(SmsTemplateInfoBo smsTemplateInfoBo);

    Result<Void> updateInfo(SmsTemplateUpdateBo smsTemplateUpdateBo);

    Result<Void> delete(SmsTemplateInfoBo smsTemplateInfoBo);

    /**
     * 根据业务代码获取短信模板
     * @param bizCode 业务代码
     * @return 短信模板
     */
    String getContentByBizCode(String bizCode);
}
