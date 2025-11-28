package com.tran.pulse.auth.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置跨域资源共享 (CORS) 的相关属性。
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/6/18 10:30
 **/
@ConfigurationProperties(prefix = "pulse.web.cors")
public class CORSProperties {

    /**
     * 允许的请求来源列表，多个来源用逗号分隔。
     * 例如："https://example.com,https://api.example.com"
     */
    private String allowedOrigins = "*";

    /**
     * 允许的 HTTP 请求方法列表，多个方法用逗号分隔。
     * 例如："GET,POST,PUT,DELETE,OPTIONS"
     */
    private String allowedMethods  = "GET, POST, PUT, DELETE, OPTIONS";

    /**
     * 允许的 HTTP 请求头列表，多个头用逗号分隔。
     * 例如："Content-Type,Authorization"
     */
    private String allowedHeaders = "*";

    /**
     * 是否允许携带 Cookie 等凭证。
     * 设置为 true 时，客户端可以发送凭证信息。
     */
    private Boolean allowCredentials = false;

    /**
     * 预检请求的缓存时长，单位为秒。
     * 在此时间内，浏览器无需再次发送预检请求。
     */
    private Long maxAge = 3600L;


    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public String getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }
}

