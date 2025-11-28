package com.tran.pulse.auth.filter;

import com.tran.pulse.auth.context.LoginUserContext;
import com.tran.pulse.auth.domain.LoginUser;
import com.tran.pulse.auth.properties.AuthProperties;
import com.tran.pulse.auth.service.JwtService;
import com.tran.pulse.common.constants.Constants;
import com.tran.pulse.common.domain.model.PulseResult;
import com.tran.pulse.common.util.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * JWT 认证过滤器
 * 负责从 HTTP 请求中提取 JWT token，验证其有效性，并设置 Spring Security 的认证信息。
 * 该过滤器在 Spring Security 过滤器链中执行，用于实现基于 JWT 的无状态认证。
 * 主要功能：
 *   从请求头中提取 JWT token
 *   验证 token 的有效性（签名、过期时间、缓存状态）
 *   从 token 中获取用户信息并设置到 Security Context
 *   支持配置白名单路径，跳过不需要认证的请求
 * 工作流程：
 * <ol>
 *   检查请求路径是否在白名单中，如果是则跳过认证
 *   从 Authorization 请求头中提取 token
 *   使用 JwtService 验证 token 并获取用户信息
 *   创建认证对象并设置到 SecurityContext
 *   继续执行后续过滤器
 * </ol>
 * 
 *
 * @author tran
 * @date 2025/6/17 15:48
 * @see OncePerRequestFilter
 * @see JwtService
 */
public class JwtAuthRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthRequestFilter.class);

    /**
     * JWT 服务
     */
    private final JwtService jwtService;

    /**
     * 认证配置
     */
    private final AuthProperties authProperties;

    /**
     * 路径匹配器，用于匹配白名单路径
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 默认的白名单路径
     */
    private static final List<String> DEFAULT_WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/public/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/static/**",
            "/favicon.ico",
            "/error"
    );

    /**
     * 构造函数
     *
     * @param jwtService      JWT 服务
     * @param authProperties  认证配置
     */
    public JwtAuthRequestFilter(JwtService jwtService, AuthProperties authProperties) {
        this.jwtService = jwtService;
        this.authProperties = authProperties;
    }

    /**
     * 判断是否应该跳过当前过滤器
     * 
     * 对于不需要鉴权的静态资源、登录/注册接口、Swagger 文档等，
     * 直接跳过 JWT 解析和认证逻辑，减少不必要的开销。
     * 
     *
     * @param request HTTP 请求
     * @return 如果应该跳过返回 true，否则返回 false
     * @throws ServletException 如果发生 Servlet 异常
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();

        // 获取配置的白名单，如果没有配置则使用默认白名单
        List<String> whiteList = authProperties.getWhiteList();
        if (whiteList == null || whiteList.isEmpty()) {
            whiteList = DEFAULT_WHITE_LIST;
        }

        // 检查路径是否匹配白名单中的任一模式
        boolean shouldSkip = whiteList.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (shouldSkip) {
            log.debug("跳过 JWT 认证，路径：{}", path);
        }

        return shouldSkip;
    }

    /**
     * 执行过滤器的核心逻辑
     * 
     * 从请求中提取 JWT token，验证其有效性，并设置认证信息到 Security Context。
     * 如果 token 无效，直接返回认证失败响应，不继续执行后续过滤器。
     * 如果没有提供 token，继续执行后续过滤器，由 Spring Security 的其他组件决定是否允许访问。
     * 
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException 如果发生 Servlet 异常
     * @throws IOException      如果发生 I/O 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();

        try {
            Boolean skipJwt = (Boolean) request.getAttribute(Constants.SKIP_JWT_CHECK);
            if (Boolean.TRUE.equals(skipJwt)) {
                log.debug("检测到 @AnonymousAccess 注解，跳过 JWT 校验，请求：{} {}", method, requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // 1. 获取 Authorization 请求头
            String authHeader = request.getHeader(JwtService.TOKEN_HEADER);

            if(authHeader == null) {
                authHeader = request.getParameter(JwtService.TOKEN_HEADER);
            }

            // 2. 提取 token
            String token = jwtService.extractToken(authHeader);

            if (StringUtils.hasText(token)) {
                // 3. 验证 token 并获取用户信息
                Optional<LoginUser> userOptional = jwtService.validateAndGetUser(token);

                if (userOptional.isPresent()) {
                    LoginUser loginUser = userOptional.get();
                    LoginUserContext.set(loginUser);
                    // 4. 检查 SecurityContext 中是否已有认证信息
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 5. 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        loginUser,                    // principal
                                        null,                        // credentials (密码不需要)
                                        loginUser.getAuthorities()   // 权限列表
                                );

                        // 6. 设置请求详情
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 7. 设置认证信息到 SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("JWT 认证成功 - 用户：{}，请求：{} {}",
                                loginUser.getUsername(), method, requestURI);
                    }

                    // 认证成功，继续执行后续过滤器
                    filterChain.doFilter(request, response);
                } else {
                    // Token 无效，直接返回认证失败，不继续执行后续过滤器
                    log.warn("JWT 认证失败 - Token 无效或用户不存在，请求：{} {}", method, requestURI);
                    sendAuthFailResponse(response, 401, "Token无效或已过期，请重新登录");
                    return;
                }
            } else {
                // 没有提供 token，继续执行后续过滤器
                // 由 Spring Security 的其他组件（如 AuthenticationEntryPoint）决定是否允许访问
                log.debug("未提供 JWT Token，请求：{} {}", method, requestURI);
                filterChain.doFilter(request, response);
            }

        } catch (Exception e) {
            log.error("JWT 认证过程发生异常，请求：{} {}", method, requestURI, e);
            // 清除可能存在的认证信息
            SecurityContextHolder.clearContext();
            // 返回服务器错误
            sendAuthFailResponse(response, 500, "认证服务异常，请稍后重试");
            return;
        }finally {
            LoginUserContext.clear();
        }
    }

    /**
     * 发送认证失败响应
     *
     * @param response    HTTP 响应
     * @param statusCode  HTTP 状态码
     * @param message     错误消息
     * @throws IOException 如果发生 I/O 异常
     */
    private void sendAuthFailResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        // 设置响应状态码
        response.setStatus(statusCode);
        // 设置响应类型和编码
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 构建错误响应
        PulseResult fail = PulseResult.fail(statusCode, message);

        // 写入响应
        try (PrintWriter out = response.getWriter()) {
            out.print(JacksonUtils.toJson(fail));
            out.flush();
        }
    }

    /**
     * 手动验证 Token（用于特殊场景）
     * 
     * 在某些场景下，可能需要手动触发 token 验证和认证设置，
     * 例如 WebSocket 连接建立时。
     * 
     *
     * @param token   JWT token
     * @param request HTTP 请求（可选，用于设置认证详情）
     * @return 如果认证成功返回 true，否则返回 false
     */
    public boolean authenticateToken(String token, HttpServletRequest request) {
        try {
            if (!StringUtils.hasText(token)) {
                return false;
            }

            Optional<LoginUser> userOptional = jwtService.validateAndGetUser(token);

            if (userOptional.isPresent()) {
                LoginUser loginUser = userOptional.get();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                loginUser,
                                null,
                                loginUser.getAuthorities()
                        );

                if (request != null) {
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                }

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("手动认证成功 - 用户：{}", loginUser.getUsername());
                return true;
            }
        } catch (Exception e) {
            log.error("手动认证 Token 失败", e);
        }

        return false;
    }

    /**
     * 从 SecurityContext 获取当前登录用户
     * 
     * 工具方法，方便在其他地方获取当前认证的用户信息
     * 
     *
     * @return 当前登录用户，如果未认证返回 null
     */
    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUser) {
                return (LoginUser) principal;
            }
        }
        return null;
    }

    /**
     * 清除当前的认证信息
     * 
     * 在某些场景下需要主动清除认证信息，例如用户主动登出
     * 
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
        log.debug("已清除 Security Context 中的认证信息");
    }
}