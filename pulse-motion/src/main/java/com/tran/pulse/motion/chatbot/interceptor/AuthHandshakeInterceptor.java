package com.tran.pulse.motion.chatbot.interceptor;

import com.tran.pulse.auth.context.LoginUserContext;
import com.tran.pulse.auth.domain.LoginUser;
import com.tran.pulse.auth.service.JwtService;
import com.tran.pulse.common.constants.BusinessCode;
import com.tran.pulse.common.constants.Constants;
import com.tran.pulse.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * WebSocket握手拦截器，负责身份验证和业务参数设置
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/8/29 10:22
 */
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthHandshakeInterceptor.class);

    private final JwtService jwtService;

    public AuthHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        try {
            // 获取并验证token
            Optional<String> authToken = extractTokenFromRequest(request);
            if (!authToken.isPresent()) {
                logger.warn("WebSocket握手失败: 未找到认证token, URI: {}", request.getURI());
                setErrorResponse(response, "Missing authentication token");
                return false;
            }

            // 验证token并获取用户信息
            Optional<LoginUser> loginUser = validateTokenAndGetUser(authToken.get());
            if (!loginUser.isPresent()) {
                logger.warn("WebSocket握手失败: token验证失败, URI: {}", request.getURI());
                setErrorResponse(response, "Invalid authentication token");
                return false;
            }

            // 设置用户会话属性
            Long userId = loginUser.get().getUserId();
            attributes.put(Constants.USER_ID, userId);

            // 设置业务代码
            String businessCode = extractBusinessCode(request);
            attributes.put(BusinessCode.HEADER, businessCode);

            logger.info("WebSocket握手成功: userId={}, businessCode={}, URI={}",
                    userId, businessCode, request.getURI());
            return true;

        } catch (Exception e) {
            logger.error("WebSocket握手过程中发生异常, URI: {}", request.getURI(), e);
            setErrorResponse(response, "Authentication error");
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {

        if (exception != null) {
            logger.error("WebSocket握手后处理异常, URI: {}", request.getURI(), exception);
        } else {
            logger.debug("WebSocket握手完成, URI: {}", request.getURI());
        }
    }

    /**
     * 从请求中提取token
     */
    private Optional<String> extractTokenFromRequest(ServerHttpRequest request) {
        if (!(request instanceof ServletServerHttpRequest)) {
            return Optional.empty();
        }

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        // 优先从Header中获取
        String authToken = servletRequest.getHeader(JwtService.TOKEN_HEADER);
        if (StringUtils.isNotEmpty(authToken)) {
            return Optional.of(authToken);
        }

        // 从参数中获取（作为备选方案）
        authToken = servletRequest.getParameter(JwtService.TOKEN_HEADER);
        return StringUtils.isNotEmpty(authToken) ? Optional.of(authToken) : Optional.empty();
    }

    /**
     * 验证token并获取用户信息
     */
    private Optional<LoginUser> validateTokenAndGetUser(String authToken) {
        try {
            // 优先从上下文获取（如果已经通过其他拦截器验证过）
            LoginUser contextUser = LoginUserContext.get();
            if (contextUser != null) {
                return Optional.of(contextUser);
            }

            // 从token解析用户信息
            String token = jwtService.extractToken(authToken);
            if (StringUtils.isEmpty(token)) {
                return Optional.empty();
            }

            LoginUser loginUser = jwtService.getLoginUser(token);
            return Optional.ofNullable(loginUser);

        } catch (Exception e) {
            logger.warn("Token验证失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 提取业务代码
     */
    private String extractBusinessCode(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String businessCode = headers.getFirst(BusinessCode.HEADER);

        // 如果没有指定业务代码，使用默认值
        if (StringUtils.isBlank(businessCode)) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            businessCode = servletRequest.getParameter(BusinessCode.HEADER);
            if (StringUtils.isBlank(businessCode)){
                businessCode = BusinessCode.FITNESS_COACH_BUSINESS_KEY;
                logger.debug("使用默认业务代码: {}", businessCode);
            }
        }

        return businessCode;
    }

    /**
     * 设置错误响应
     */
    private void setErrorResponse(ServerHttpResponse response, String message) {
        try {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("X-Error-Message", message);
        } catch (Exception e) {
            logger.warn("设置错误响应失败", e);
        }
    }
}