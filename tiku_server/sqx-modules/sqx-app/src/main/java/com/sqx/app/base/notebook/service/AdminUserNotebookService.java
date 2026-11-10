package com.sqx.app.base.notebook.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.notebook.domain.bo.AdminUserNotebookPageBo;
import com.sqx.app.base.notebook.domain.vo.AdminUserNotebookItemVo;
import com.sqx.app.base.notebook.domain.vo.AdminUserNotebookSummaryVo;

public interface AdminUserNotebookService {

    IPage<AdminUserNotebookSummaryVo> pageUserSummary(AdminUserNotebookPageBo pageBo);

    IPage<AdminUserNotebookItemVo> pageNotebook(AdminUserNotebookPageBo pageBo);
}
