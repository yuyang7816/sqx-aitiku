package com.sqx.app.base.messages.domain.bo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@Data
public class MessagesInfoBo {

    @Schema(description = "消息ID")
    @NotNull(message = "id不能为空")
    private Long id;

}
