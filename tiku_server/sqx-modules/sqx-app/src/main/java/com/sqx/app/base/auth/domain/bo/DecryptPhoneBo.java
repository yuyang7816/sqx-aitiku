package com.sqx.app.base.auth.domain.bo;

import lombok.Data;

/**
 * @author chengming
 * @since 2025/8/6
 */
@Data
public class DecryptPhoneBo {

    private String decryptData;

    private String key;

    private String iv;
}
