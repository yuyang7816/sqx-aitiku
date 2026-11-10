package com.sqx.app.base.homeShortcut.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class HomeShortcutUpdateStateBo {

    @NotNull(message = "id 不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer state;
}
