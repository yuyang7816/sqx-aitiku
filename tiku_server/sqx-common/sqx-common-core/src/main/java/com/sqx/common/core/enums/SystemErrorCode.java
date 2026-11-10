package com.sqx.common.core.enums;

/**
 * @author chengming
 * @since 2024/7/11
 */
public enum SystemErrorCode implements IErrorCode {

    /**
     * 系统错误
     */
    SYSTEM_ERROR(-200, "系统内部错误"),
    SUCCESS(0, "操作成功"),
    FORBIDDEN(403, "没有资源权限"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(50001, "参数检验失败"),
    UNAUTHORIZED(50002, "暂未登录或token已经过期"),
    USERNAME_ERROR(50003, "用户名或密码错误"),
    DISABLE_USER(50004, "用户禁止登录"),
    USERNAME_EXIST(50005, "用户名已存在"),
    USERNAME_NOT_EXIST(50006, "用户不存在"),

    NO_ROLE(50007,"当前用户无角色，无权查看数据"),
    CODE_NO_ROLE(50008, "角色信息不匹配, 无权查看数据"),
    NO_HANDLER_FOUND(50009, "路径不存在，请检查路径是否正确"),
        ;
    /**
     * 错误码
     */
    private final Integer code;
    /**
     * 错误信息
     */
    private final String msg;

    SystemErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
