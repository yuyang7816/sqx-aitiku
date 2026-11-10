package com.sqx.app.base.user.badge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionInfoBo;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionPageBo;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionSaveBo;
import com.sqx.app.base.user.badge.domain.bo.UserBadgeDefinitionUpdateBo;
import com.sqx.app.base.user.badge.domain.vo.UserBadgeDefinitionVo;

public interface UserBadgeDefinitionService {

    void saveInfo(UserBadgeDefinitionSaveBo saveBo);

    IPage<UserBadgeDefinitionVo> pageInfo(UserBadgeDefinitionPageBo pageBo);

    UserBadgeDefinitionVo info(UserBadgeDefinitionInfoBo infoBo);

    void updateInfo(UserBadgeDefinitionUpdateBo updateBo);

    void delete(UserBadgeDefinitionInfoBo infoBo);
}
