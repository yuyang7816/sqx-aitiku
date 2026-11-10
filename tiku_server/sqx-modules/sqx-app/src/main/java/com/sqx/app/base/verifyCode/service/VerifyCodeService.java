package com.sqx.app.base.verifyCode.service;

import com.sqx.app.base.verifyCode.domain.entity.VerifyCode;

/**
 * 验证码 服务类
 *
 * @author sqx
 * @since 2025-04-22 15:31:50
 */
public interface VerifyCodeService {

    /**
     * 校验验证码
     * @param phone 手机号
     * @param code 验证码
     */
    Boolean verify(String phone, String code);

    /**
     * 校验并消费验证码（登录场景：过期/错误抛出明确业务异常）
     * @param phone 手机号
     * @param code 验证码
     */
    void verifyForLogin(String phone, String code);

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @param bizCode 业务类型
     */
    VerifyCode sendPhoneCode(String phone, String bizCode);

    /**
     * 发送邮件验证码
     * @param email 邮箱
     * @param bizCode 业务类型
     */
    VerifyCode sendMailCode(String email, String bizCode);

    /**
     * 用户端发送短信验证码（POST /app/verifyCode）
     * @param phone 手机号
     */
    void sendAppVerifyCode(String phone);
}
