package com.sqx.app.base.practiceEntry.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.practiceEntry.domain.bo.*;
import com.sqx.app.base.practiceEntry.domain.vo.AppPracticeEntryItemVO;
import com.sqx.app.base.practiceEntry.domain.vo.PracticeEntryVo;

import java.util.List;

public interface PracticeEntryService {

    List<AppPracticeEntryItemVO> listAppEntries();

    void saveInfo(PracticeEntrySaveBo saveBo);

    IPage<PracticeEntryVo> pageInfo(PracticeEntryPageBo pageBo);

    PracticeEntryVo info(PracticeEntryInfoBo infoBo);

    void updateInfo(PracticeEntryUpdateBo updateBo);

    void delete(PracticeEntryInfoBo infoBo);

    void updateState(PracticeEntryUpdateStateBo updateStateBo);
}
