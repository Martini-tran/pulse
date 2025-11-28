package com.tran.pulse.auth.handler;

import com.tran.pulse.auth.util.IPUtils;
import com.tran.pulse.common.constants.PulseHttpStatus;
import com.tran.pulse.common.domain.model.PulseResult;
import com.tran.pulse.common.util.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 访问拒绝处理器
 * 当用户已经通过认证但没有足够权限访问特定资源时，Spring Security 会调用此处理器。
 * 该处理器负责向客户端返回统一格式的权限不足响应。
 * 适用场景：
 *   用户已登录但访问超出其权限范围的API
 *   用户角色不满足方法级别的权限要求
 *   用户尝试访问其他用户的私有资源
 *   用户权限已被管理员撤销但token仍有效
 * 与认证入口点的区别：
 *   AuthenticationEntryPoint：用户未认证（401 Unauthorized）
 *   AccessDeniedHandler：用户已认证但权限不足（403 Forbidden）
 *
 * @author tran
 * @date 2025/6/17 16:45
 * @see AccessDeniedHandler
 */
public class AccessDeniedDefaultHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(AccessDeniedDefaultHandler.class);

    /**
     * 处理访问拒绝的情况
     * 当已认证用户尝试访问权限不足的资源时，此方法会被调用。
     * 它会向客户端返回一个JSON格式的权限不足响应。
     * 
     *
     * @param request               HTTP 请求对象
     * @param response              HTTP 响应对象
     * @param accessDeniedException 访问拒绝异常，包含拒绝访问的详细信息
     * @throws IOException      如果发生I/O错误
     * @throws ServletException 如果发生Servlet错误
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        final String remoteAddr = IPUtils.getClientIpAddress(request);

        // 获取当前认证用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "unknown";
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }

        // 记录访问拒绝日志
        log.warn("访问拒绝 - 用户：{}，请求：{} {}，客户端：{}，异常：{}",
                username, method, requestURI, remoteAddr, accessDeniedException.getMessage());

        // 详细日志（Debug级别）
        if (log.isDebugEnabled()) {
            String userAgent = request.getHeader("User-Agent");
            log.debug("访问拒绝详情 - User-Agent: {}, 用户权限: {}, Exception: {}",
                    userAgent,
                    authentication != null ? authentication.getAuthorities() : "none",
                    accessDeniedException.getClass().getSimpleName());
        }

        // 根据请求类型确定错误消息
        String errorMessage = determineErrorMessage(accessDeniedException, requestURI, username);

        // 构建统一格式的错误响应
        sendErrorResponse(response, errorMessage);
    }

    /**
     * 根据访问拒绝异常和上下文确定错误消息
     *
     * @param accessDeniedException 访问拒绝异常
     * @param requestURI            请求URI
     * @param username              用户名
     * @return 错误消息
     */
    private String determineErrorMessage(AccessDeniedException accessDeniedException,
                                         String requestURI, String username) {
        String message = accessDeniedException.getMessage();

        // 根据异常消息或URI路径返回更友好的错误信息
        if (StringUtils.hasText(message)) {
            // 常见的权限异常消息处理
            if (message.contains("Access is denied")) {
                return getContextualMessage(requestURI);
            } else if (message.contains("Insufficient scope")) {
                return "API访问权限不足，请联系管理员";
            } else if (message.contains("Role")) {
                return "用户角色权限不足，无法执行此操作";
            }
        }

        // 根据URI路径返回特定消息
        return getContextualMessage(requestURI);
    }

    /**
     * 根据请求URI返回上下文相关的错误消息
     *
     * @param requestURI 请求URI
     * @return 上下文相关的错误消息
     */
    private String getContextualMessage(String requestURI) {
        if (requestURI == null) {
            return "权限不足，无法访问该资源";
        }

        // 根据不同的API路径返回不同的错误消息
        if (requestURI.contains("/admin/")) {
            return "需要管理员权限才能访问此资源";
        } else if (requestURI.contains("/mapper/user/")) {
            return "无权访问其他用户的资源";
        } else if (requestURI.contains("/delete") || requestURI.contains("/remove")) {
            return "没有删除权限";
        } else if (requestURI.contains("/edit") || requestURI.contains("/update")) {
            return "没有编辑权限";
        } else if (requestURI.contains("/create") || requestURI.contains("/add")) {
            return "没有创建权限";
        } else if (requestURI.contains("/api/")) {
            return "API访问权限不足";
        } else {
            return "权限不足，无法访问该资源";
        }
    }

    /**
     * 发送错误响应
     *
     * @param response     HTTP响应对象
     * @param errorMessage 错误消息
     * @throws IOException 如果发生I/O错误
     */
    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        // 设置响应状态码
        response.setStatus(PulseHttpStatus.FORBIDDEN);

        // 设置响应头
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // 构建错误响应对象
        PulseResult errorResult = PulseResult.accessDenied(errorMessage);

        // 写入响应体
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JacksonUtils.toJson(errorResult));
            writer.flush();
        } catch (Exception e) {
            log.error("写入访问拒绝响应时发生异常", e);
            // 如果JSON序列化失败，返回简单的错误信息
            try (PrintWriter writer = response.getWriter()) {
                writer.write("{\"success\":false,\"code\":" + PulseHttpStatus.FORBIDDEN +
                        ",\"message\":\"" + errorMessage + "\"}");
                writer.flush();
            }
        }
    }


}