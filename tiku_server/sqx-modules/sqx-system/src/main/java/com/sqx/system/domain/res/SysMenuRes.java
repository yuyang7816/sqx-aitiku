package com.sqx.system.domain.res;

import com.sqx.system.db.entity.SysMenuEntity;
import lombok.Data;

import java.util.List;

/**
 * @author chengming
 * @since 2024/7/11
 */
@Data
public class SysMenuRes {

    private List<SysMenuEntity> menuList;

    private SysMenuEntity menu;
}
