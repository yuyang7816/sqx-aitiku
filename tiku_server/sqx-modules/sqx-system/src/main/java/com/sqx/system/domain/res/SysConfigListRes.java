package com.sqx.system.domain.res;

import com.sqx.system.db.entity.SysConfigEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author chengming
 * @since 2025/4/21
 */
@Data
public class SysConfigListRes {

    @Schema(description = "类型")
    private String paramType;

    @Schema(description = "类型描述")
    private String paramTypeDesc;

    @Schema(description = "系统配置列表")
    private List<SysConfigEntity> sysConfigEntityList;
}
