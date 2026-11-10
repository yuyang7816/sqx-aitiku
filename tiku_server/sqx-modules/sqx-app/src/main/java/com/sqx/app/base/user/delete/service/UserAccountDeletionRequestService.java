package com.sqx.app.base.user.delete.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.user.delete.domain.bo.AdminUserDeleteRequestActionBo;
import com.sqx.app.base.user.delete.domain.bo.AdminUserDeleteRequestPageBo;
import com.sqx.app.base.user.delete.domain.vo.AdminUserDeleteRequestVo;
import com.sqx.app.base.user.delete.domain.vo.AppUserDeleteAccountVO;

/**
 * 账号注销冷静期申请
 */
public interface UserAccountDeletionRequestService {

    AppUserDeleteAccountVO getDeleteStatus(Long userId);

    AppUserDeleteAccountVO requestDeletion(Long userId);

    AppUserDeleteAccountVO cancelDeletion(Long userId);

    int executeDueDeletions();

    IPage<AdminUserDeleteRequestVo> adminPage(AdminUserDeleteRequestPageBo pageBo);

    void adminCancel(AdminUserDeleteRequestActionBo actionBo);

    void adminExecuteNow(AdminUserDeleteRequestActionBo actionBo);
}
