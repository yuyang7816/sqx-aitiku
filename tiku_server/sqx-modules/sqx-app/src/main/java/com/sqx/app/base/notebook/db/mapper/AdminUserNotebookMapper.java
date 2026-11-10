package com.sqx.app.base.notebook.db.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.notebook.domain.bo.AdminUserNotebookPageBo;
import com.sqx.app.base.notebook.domain.vo.AdminUserNotebookSummaryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理端用户错题汇总查询
 */
@Mapper
public interface AdminUserNotebookMapper {

    IPage<AdminUserNotebookSummaryVo> pageUserSummary(
            Page<AdminUserNotebookSummaryVo> page,
            @Param("params") AdminUserNotebookPageBo params);
}
