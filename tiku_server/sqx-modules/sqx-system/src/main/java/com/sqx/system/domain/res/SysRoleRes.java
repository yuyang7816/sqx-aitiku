package com.sqx.system.domain.res;

import com.sqx.system.db.entity.SysRoleEntity;
import lombok.Data;

import java.util.List;

/**
 * @author chengming
 * @since 2024/7/11
 */
@Data
public class SysRoleRes {

    private List<SysRoleEntity> list;

    private SysRoleEntity role;
}
