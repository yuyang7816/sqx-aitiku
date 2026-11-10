package com.sqx.app.base.practiceEntry.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PracticeEntryVo {

    private Long id;
    private String title;
    private String subtitle;
    private String icon;
    private String iconColor;
    private String bgStyle;
    private String url;
    private String entryType;
    private Integer sortOrder;
    private Integer state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
