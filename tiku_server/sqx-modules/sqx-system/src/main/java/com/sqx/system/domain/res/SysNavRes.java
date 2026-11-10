package com.sqx.system.domain.res;

import com.sqx.system.db.entity.SysMenuEntity;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author chengming
 * @since 2024/7/11
 */
@Data
public class SysNavRes {

    private List<SysMenuEntity> menuList;

    private Set<String> permissions;
}
