package com.sqx.app.base.banner.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author sqx
 * @since 2025-05-16 13:49:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BannerPageBo extends PageParam {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "分类1banner图 2金刚区分类")
    private Integer classify;

    @Schema(description = "状态1正常 2隐藏")
    private Integer state;
}
