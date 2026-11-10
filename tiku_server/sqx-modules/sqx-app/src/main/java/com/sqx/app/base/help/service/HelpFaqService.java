package com.sqx.app.base.help.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.help.domain.bo.HelpFaqInfoBo;
import com.sqx.app.base.help.domain.bo.HelpFaqPageBo;
import com.sqx.app.base.help.domain.bo.HelpFaqSaveBo;
import com.sqx.app.base.help.domain.bo.HelpFaqUpdateBo;
import com.sqx.app.base.help.domain.vo.AppHelpFaqItemVO;
import com.sqx.app.base.help.domain.vo.HelpFaqVo;

import java.util.List;

public interface HelpFaqService {

    List<AppHelpFaqItemVO> listAppFaqs();

    void saveInfo(HelpFaqSaveBo saveBo);

    IPage<HelpFaqVo> pageInfo(HelpFaqPageBo pageBo);

    HelpFaqVo info(HelpFaqInfoBo infoBo);

    void updateInfo(HelpFaqUpdateBo updateBo);

    void delete(HelpFaqInfoBo infoBo);
}
