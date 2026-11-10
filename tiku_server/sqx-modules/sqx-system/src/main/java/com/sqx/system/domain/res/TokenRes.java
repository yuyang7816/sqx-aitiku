package com.sqx.system.domain.res;

import lombok.Data;

/**
 * @author chengming
 * @since 2024/7/11
 */
@Data
public class TokenRes {

    private String token;

    private Integer expire;
}
