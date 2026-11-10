package com.sqx.app.base.ai.enums;

import com.sqx.common.core.enums.IErrorCode;

public enum AiError implements IErrorCode {

    MESSAGE_EMPTY(1050009, "\u8bf7\u8f93\u5165\u8ffd\u95ee\u5185\u5bb9"),
    QUOTA_EXCEEDED(1050010, "\u4eca\u65e5 AI \u7b54\u7591\u6b21\u6570\u5df2\u7528\u5b8c\uff0c\u5f00\u901a\u4f1a\u5458\u53ef\u65e0\u9650\u6b21\u4f7f\u7528"),
    AI_PLATFORM_NOT_FOUND(1050011, "AI \u4f9b\u5e94\u5546\u914d\u7f6e\u65e0\u6548"),
    CHAT_FAILED(1050012, "AI \u56de\u590d\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5"),
    SENSITIVE_INPUT(1050013, "\u60a8\u53d1\u9001\u7684\u5185\u5bb9\u5305\u542b\u4e0d\u5f53\u4fe1\u606f\uff0c\u8bf7\u4fee\u6539\u540e\u91cd\u8bd5"),
    SENSITIVE_REPLY(1050014, "AI \u56de\u590d\u672a\u901a\u8fc7\u5185\u5bb9\u5ba1\u6838\uff0c\u8bf7\u6362\u4e2a\u95ee\u6cd5\u518d\u8bd5"),
    ;

    private final Integer code;
    private final String message;

    AiError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}