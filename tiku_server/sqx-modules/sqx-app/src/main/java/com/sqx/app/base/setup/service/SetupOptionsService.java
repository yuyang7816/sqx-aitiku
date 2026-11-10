package com.sqx.app.base.setup.service;

import com.sqx.app.base.setup.domain.vo.SetupOptionsVO;

/**
 * 学习计划选项（年级/学科/教材版本）
 */
public interface SetupOptionsService {

    /**
     * 获取 C 端学习计划选项，优先读取 sys_dict，无数据时使用内置默认项
     */
    SetupOptionsVO getSetupOptions();
}
