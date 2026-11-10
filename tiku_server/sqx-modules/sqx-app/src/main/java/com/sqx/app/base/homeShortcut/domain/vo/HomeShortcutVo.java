package com.sqx.app.base.homeShortcut.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeShortcutVo {

    private Long id;
    private String title;
    private String icon;
    private String iconColor;
    private String bgStyle;
    private String url;
    private String actionType;
    private Integer sortOrder;
    private Integer state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
