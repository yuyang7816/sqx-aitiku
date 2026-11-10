package com.sqx.app.base.practiceEntry.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class PracticeEntryUpdateBo extends PracticeEntrySaveBo {

    @NotNull(message = "id 不能为空")
    private Long id;
}
