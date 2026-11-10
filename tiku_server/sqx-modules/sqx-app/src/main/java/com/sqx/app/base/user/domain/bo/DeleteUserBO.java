package com.sqx.app.base.user.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/12/8
 */
@Data
public class DeleteUserBO {

    @Schema(description = "用户ID")
    private Long userId;
}
