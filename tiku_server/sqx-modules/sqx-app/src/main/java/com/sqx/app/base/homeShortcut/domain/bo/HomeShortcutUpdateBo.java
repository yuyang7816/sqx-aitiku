package com.sqx.app.base.homeShortcut.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class HomeShortcutUpdateBo extends HomeShortcutSaveBo {

    @NotNull(message = "id 不能为空")
    private Long id;
}
