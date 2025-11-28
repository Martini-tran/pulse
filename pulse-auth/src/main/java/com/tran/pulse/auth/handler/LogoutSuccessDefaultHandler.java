package com.tran.pulse.auth.handler;

import com.tran.pulse.auth.domain.LoginUser;
import com.tran.pulse.auth.service.JwtService;
import com.tran.pulse.common.constants.PulseHttpStatus;
import com.tran.pulse.common.domain.model.PulseResult;
import com.tran.pulse.common.util.JacksonUtils;
import com.tran.pulse.common.util.StringUtils;
import com.tran.pulse.auth.util.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 登出成功处理器
 * 处理用户登出成功后的逻辑，包括日志记录、token清理（如果需要）、
 * 以及向客户端返回统一格式的成功响应。
 * 主要功能：
 *   记录用户登出日志
 *   清理服务端token缓存（如果有黑名单机制）
 *   返回统一格式的JSON响应
 *   收集登出统计信息
 * 注意事项：
 *   JWT是无状态的，通常不需要服务端清理
 *   如果实现了token黑名单，需要在这里将token加入黑名单
 *   客户端应该主动删除本地存储的token
 *
 * @author tran
 * @date 2025/6/17 17:00
 * @see LogoutSuccessHandler
 */
public class LogoutSuccessDefaultHandler implements LogoutSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(LogoutSuccessDefaultHandler.class);

    @Autowired(required = false)
    private JwtService jwtService;

    /**
     * 处理登出成功的情况
     * 
     * 当用户成功登出时，此方法会被调用。
     * 记录登出信息并向客户端返回成功响应。
     * 
     *
     * @param request        HTTP 请求对象
     * @param response       HTTP 响应对象
     * @param authentication 认证信息，可能为null（如果用户未认证就调用登出）
     * @throws IOException      如果发生I/O错误
     * @throws ServletException 如果发生Servlet错误
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        final String remoteAddr = IPUtils.getClientIpAddress(request);
        final String userAgent = request.getHeader("User-Agent");
        final String logoutTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String authHeader = request.getHeader(JwtService.ACCESS_TOKEN);
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith(JwtService.TOKEN_PREFIX)) {
            String token = jwtService.extractToken(authHeader);
            LoginUser loginUser = jwtService.getLoginUser(token);
            // 处理token清理（如果有黑名单机制）
            jwtService.logout(token);
            // 记录登出成功日志
            log.info("用户登出成功 - 用户：{}[{}]，时间：{}，请求：{} {}，客户端：{}",
                    loginUser.getUsername(), loginUser.getUserId(), logoutTime, method, requestURI, remoteAddr);

            // 详细日志（Debug级别）
            if (log.isDebugEnabled()) {
                log.debug("用户登出详情 - User-Agent: {}, 会话信息: {}",
                        userAgent, authentication != null ? authentication.getDetails() : "none");
            }
            // 发送登出成功响应
            sendLogoutSuccessResponse(response, loginUser.getUsername(), logoutTime);
        }else {
            log.warn("清理token时发生异常 - token 不存在");
        }

    }


    /**
     * 发送登出成功响应
     *
     * @param response   HTTP响应对象
     * @param username   用户名
     * @param logoutTime 登出时间
     * @throws IOException 如果发生I/O错误
     */
    private void sendLogoutSuccessResponse(HttpServletResponse response, String username, String logoutTime) throws IOException {
        // 设置响应状态码
        response.setStatus(PulseHttpStatus.OK);

        // 设置响应头
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // 构建成功响应对象
        PulseResult successResult = Objects.requireNonNull(Objects.requireNonNull(PulseResult.success("您已安全登出，感谢使用")
                                .put("username", username))
                        .put("logoutTime", logoutTime));

        // 写入响应体
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JacksonUtils.toJson(successResult));
            writer.flush();
        } catch (Exception e) {
            log.error("写入登出成功响应时发生异常", e);
            // 如果JSON序列化失败，返回简单的成功信息
            try (PrintWriter writer = response.getWriter()) {
                writer.write("{\"success\":true,\"code\":" + PulseHttpStatus.OK +
                        ",\"message\":\"登出成功\",\"logoutTime\":\"" + logoutTime + "\"}");
                writer.flush();
            }
        }
    }

}