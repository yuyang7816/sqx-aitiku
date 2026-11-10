package com.sqx.app.base.practiceEntry.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PracticeEntryPageBo extends PageParam {

    private String title;

    private Integer state;
}
