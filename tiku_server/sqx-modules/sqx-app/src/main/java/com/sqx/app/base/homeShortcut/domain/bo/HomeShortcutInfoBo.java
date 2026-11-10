package com.sqx.app.base.homeShortcut.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class HomeShortcutInfoBo {

    @NotNull(message = "id 不能为空")
    private Long id;
}
