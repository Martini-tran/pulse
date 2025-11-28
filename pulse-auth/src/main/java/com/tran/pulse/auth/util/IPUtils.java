package com.tran.pulse.auth.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/7/3 13:45
 **/
public class IPUtils {

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // 处理多个IP的情况，取第一个
                int index = ip.indexOf(',');
                if (index != -1) {
                    return ip.substring(0, index).trim();
                } else {
                    return ip.trim();
                }
            }
        }

        return request.getRemoteAddr();
    }

}
