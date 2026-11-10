package com.sqx.common.pay.channel.ali.domain;

import com.alipay.api.AlipayConstants;
import com.sqx.common.pay.channel.ali.constants.AlipayConstant;
import jodd.util.StringUtil;

/**
 * @author chengming
 * @since 2025/4/21
 *
 * 支付宝配置参数
 */
public class AlipayConfigParam {

    /**
     * 支付宝网关 (固定)
     */
    private String serverUrl;

    /**
     * 参数返回格式，只支持 JSON 格式（固定）
     */
    private String format;

    /**
     * 编码集，支持 GBK/UTF-8
     */
    private String charset;

    /**
     * 生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐使用 RSA2。
     */
    private String signType;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 应用私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 应用公钥证书路径
     */
    private String appCertPath;

    /**
     * 支付宝公钥证书路径
     */
    private String alipayPublicCertPath;

    /**
     * 支付宝根证书路径
     */
    private String rootCertPath;

    /**
     * 是否为证书模式
     */
    private Boolean certMode;

    /**
     * 是否为沙箱模式 默认不开启
     */
    private Boolean sandbox = false;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getAlipayPublicKey() {
        return alipayPublicKey;
    }

    public void setAlipayPublicKey(String alipayPublicKey) {
        this.alipayPublicKey = alipayPublicKey;
    }

    public String getAppCertPath() {
        return appCertPath;
    }

    public void setAppCertPath(String appCertPath) {
        this.appCertPath = appCertPath;
    }

    public String getAlipayPublicCertPath() {
        return alipayPublicCertPath;
    }

    public void setAlipayPublicCertPath(String alipayPublicCertPath) {
        this.alipayPublicCertPath = alipayPublicCertPath;
    }

    public String getRootCertPath() {
        return rootCertPath;
    }

    public void setRootCertPath(String rootCertPath) {
        this.rootCertPath = rootCertPath;
    }

    public Boolean getCertMode() {
        return certMode;
    }

    public AlipayConfigParam setCertMode(Boolean certMode) {
        this.certMode = certMode;
        return this;
    }

    public String getServerUrl() {
        if (StringUtil.isEmpty(serverUrl)) {
            serverUrl = AlipayConstant.SERVER_URL;
        }

        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getFormat() {
        if (StringUtil.isEmpty(format)) {
            format = AlipayConstants.FORMAT_JSON;
        }

        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCharset() {
        if (StringUtil.isEmpty(charset)) {
            charset = AlipayConstants.CHARSET_UTF8;
        }

        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getSignType() {
        if (StringUtil.isEmpty(signType)) {
            signType = AlipayConstants.SIGN_TYPE_RSA2;
        }

        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public Boolean getSandbox() {
        return sandbox;
    }

    public void setSandbox(Boolean sandbox) {
        this.sandbox = sandbox;
    }
}
