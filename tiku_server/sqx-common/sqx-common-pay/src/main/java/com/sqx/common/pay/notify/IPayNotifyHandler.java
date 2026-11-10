package com.sqx.common.pay.notify;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author chengming
 * @since 2026/1/9
 */
public interface IPayNotifyHandler {
    boolean support(String channel);

    String handler(HttpServletRequest request);
}
