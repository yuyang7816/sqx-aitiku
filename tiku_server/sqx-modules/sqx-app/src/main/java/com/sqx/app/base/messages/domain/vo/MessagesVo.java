package com.sqx.app.base.messages.domain.vo;

import com.sqx.app.base.messages.domain.entity.Messages;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MessagesVo extends Messages{

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号")
    private String phone;
}
