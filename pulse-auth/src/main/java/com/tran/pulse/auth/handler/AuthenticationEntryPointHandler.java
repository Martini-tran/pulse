package com.tran.pulse.auth.handler;

import com.tran.pulse.common.constants.PulseHttpStatus;
import com.tran.pulse.common.domain.model.PulseResult;
import com.tran.pulse.common.util.JacksonUtils;
import com.tran.pulse.auth.util.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 认证入口点处理器
 * 当用户访问受保护的资源但未提供有效的认证信息时，Spring Security 会调用此处理器。
 * 该处理器负责向客户端返回统一格式的认证失败响应，而不是默认的重定向到登录页面。
 * 适用场景：
 *   用户访问需要认证的API接口但未携带token
 *   用户携带的token已过期或无效
 *   用户没有访问特定资源的权限
 *   认证过程中发生异常
 * 工作原理：
 * <ol>
 *   Spring Security 检测到未认证的请求
 *   调用 commence 方法处理认证失败
 *   返回JSON格式的错误响应，而不是重定向
 *   设置适当的HTTP状态码（401 Unauthorized）
 * </ol>
 * 
 *
 * @author tran
 * @date 2025/6/17 16:30
 * @see AuthenticationEntryPoint
 */
public class AuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationEntryPointHandler.class);

    /**
     * 处理认证失败的情况
     * 当用户尝试访问受保护的资源但认证失败时，此方法会被调用。
     * 它会向客户端返回一个JSON格式的错误响应，而不是传统的重定向到登录页面。
     * 
     *
     * @param request       HTTP 请求对象
     * @param response      HTTP 响应对象
     * @param authException 认证异常，包含失败的详细信息
     * @throws IOException      如果发生I/O错误
     * @throws ServletException 如果发生Servlet错误
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        final String remoteAddr = IPUtils.getClientIpAddress(request);
        final String userAgent = request.getHeader("User-Agent");

        // 记录认证失败日志
        log.warn("认证失败 - 请求：{} {}，客户端：{}，异常：{}",
                method, requestURI, remoteAddr, authException.getMessage());

        // 详细日志（Debug级别）
        if (log.isDebugEnabled()) {
            log.debug("认证失败详情 - User-Agent: {}, Exception: {}",
                    userAgent, authException.getClass().getSimpleName());
        }

        // 根据异常类型确定错误消息和状态码
        ErrorInfo errorInfo = determineErrorInfo(authException, requestURI);

        // 构建统一格式的错误响应
        sendErrorResponse(response, errorInfo);
    }

    /**
     * 根据认证异常类型确定错误信息
     *
     * @param authException 认证异常
     * @param requestURI    请求URI
     * @return 错误信息对象
     */
    private ErrorInfo determineErrorInfo(AuthenticationException authException, String requestURI) {
        String exceptionName = authException.getClass().getSimpleName();
        String message = authException.getMessage();

        // 根据异常类型返回不同的错误信息
        switch (exceptionName) {
            case "BadCredentialsException":
                return new ErrorInfo(PulseHttpStatus.UNAUTHORIZED, "用户名或密码错误");

            case "UsernameNotFoundException":
                return new ErrorInfo(PulseHttpStatus.UNAUTHORIZED, "用户不存在");

            case "AccountExpiredException":
                return new ErrorInfo(PulseHttpStatus.UNAUTHORIZED, "账户已过期，请联系管理员");

            case "CredentialsExpiredException":
                return new ErrorInfo(PulseHttpStatus.UNAUTHORIZED, "密码已过期，请重新设置密码");

            case "DisabledException":
                return new ErrorInfo(PulseHttpStatus.FORBIDDEN, "账户已被禁用，请联系管理员");

            case "LockedException":
                return new ErrorInfo(PulseHttpStatus.FORBIDDEN, "账户已被锁定，请联系管理员");

            case "InsufficientAuthenticationException":
                // 检查是否是API请求
                if (isApiRequest(requestURI)) {
                    return new ErrorInfo(PulseHttpStatus.UNAUTHORIZED, "请提供有效的访问令牌");
                } else {
                    return new ErrorInfo(PulseHttpStatus.UNAUTHORIZED, "请先登录后再访问");
                }

            case "AccessDeniedException":
                return new ErrorInfo(PulseHttpStatus.FORBIDDEN, "权限不足，无法访问该资源");

            default:
                // 默认认证失败消息
                if (StringUtils.hasText(message)) {
                    return new ErrorInfo(PulseHttpStatus.UNAUTHORIZED, "认证失败：" + message);
                } else {
                    return new ErrorInfo(PulseHttpStatus.UNAUTHORIZED, "认证失败，请重新登录");
                }
        }
    }

    /**
     * 判断是否为API请求
     *
     * @param requestURI 请求URI
     * @return 如果是API请求返回true
     */
    private boolean isApiRequest(String requestURI) {
        return requestURI != null && (
                requestURI.startsWith("/api/") ||
                        requestURI.startsWith("/v1/") ||
                        requestURI.startsWith("/v2/") ||
                        requestURI.contains("/rest/")
        );
    }

    /**
     * 发送错误响应
     *
     * @param response  HTTP响应对象
     * @param errorInfo 错误信息
     * @throws IOException 如果发生I/O错误
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorInfo errorInfo) throws IOException {
        // 设置响应状态码
        response.setStatus(errorInfo.getStatusCode());

        // 设置响应头
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // 构建错误响应对象
        PulseResult errorResult = PulseResult.fail(errorInfo.getStatusCode(), errorInfo.getMessage());

        // 写入响应体
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JacksonUtils.toJson(errorResult));
            writer.flush();
        } catch (Exception e) {
            log.error("写入认证失败响应时发生异常", e);
            // 如果JSON序列化失败，返回简单的错误信息
            try (PrintWriter writer = response.getWriter()) {
                writer.write("{\"success\":false,\"code\":" + errorInfo.getStatusCode() +
                        ",\"message\":\"" + errorInfo.getMessage() + "\"}");
                writer.flush();
            }
        }
    }



    /**
     * 错误信息内部类
     */
    private static class ErrorInfo {
        private final int statusCode;
        private final String message;

        public ErrorInfo(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getMessage() {
            return message;
        }
    }
}