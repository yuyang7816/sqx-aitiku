package com.sqx.app.base.practiceEntry.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PracticeEntryInfoBo {

    @NotNull(message = "id 不能为空")
    private Long id;
}
