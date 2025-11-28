package com.tran.pulse.web.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/7/3 13:35
 **/
public class ResponseUtils {

    /**
     * 获取Response
     *
     * @return
     */
    public static HttpServletResponse currentResponse() {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attr != null ? attr.getResponse() : null;
    }


}

