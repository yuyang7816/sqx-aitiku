package com.sqx.app.base.homeShortcut.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.homeShortcut.domain.bo.*;
import com.sqx.app.base.homeShortcut.domain.vo.AppHomeShortcutItemVO;
import com.sqx.app.base.homeShortcut.domain.vo.HomeShortcutVo;

import java.util.List;

public interface HomeShortcutService {

    List<AppHomeShortcutItemVO> listAppShortcuts();

    void saveInfo(HomeShortcutSaveBo saveBo);

    IPage<HomeShortcutVo> pageInfo(HomeShortcutPageBo pageBo);

    HomeShortcutVo info(HomeShortcutInfoBo infoBo);

    void updateInfo(HomeShortcutUpdateBo updateBo);

    void delete(HomeShortcutInfoBo infoBo);

    void updateState(HomeShortcutUpdateStateBo updateStateBo);
}
