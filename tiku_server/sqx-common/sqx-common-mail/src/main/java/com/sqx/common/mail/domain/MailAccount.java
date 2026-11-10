package com.sqx.common.mail.domain;

import lombok.Data;

/**
 * @author chengming
 * @since 2025/6/12
 */
@Data
public class MailAccount {

    /**
     * 邮箱服务器地址
     */
    private String host;

    /**
     * 邮箱服务器端口号
     */
    private String port;

    /**
     * 发送邮箱用户名
     */
    private String username;

    /**
     * 邮箱服务器授权码
     */
    private String password;

    /**
     * 发件人名称
     */
    private String fromName;

    /**
     * 发件人邮箱地址
     */
    private String fromAddress;
}
