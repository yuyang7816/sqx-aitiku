package com.sqx.app.base.ai.enums;

import lombok.Getter;

/**
 * AI 答疑供应商编码（对应 sys_config.ai_platform）
 */
@Getter
public enum AiPlatformCodeEnums {

    LOCAL_TEMPLATE("local_template", "本地模板回复（开发/测试）"),
    DOUBAO("doubao", "豆包大模型"),
    ;

    private final String code;
    private final String desc;

    AiPlatformCodeEnums(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
