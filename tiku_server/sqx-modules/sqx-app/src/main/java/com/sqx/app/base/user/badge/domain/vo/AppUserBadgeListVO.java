package com.sqx.app.base.user.badge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端勋章列表出参（GET /app/user/badges）
 */
@Data
@Schema(description = "用户勋章列表")
public class AppUserBadgeListVO {

    @Schema(description = "全部勋章（含未解锁项及获得条件）")
    private List<AppUserBadgeItemVO> list;

    @Schema(description = "已解锁勋章数量")
    private Integer unlockedCount;
}
