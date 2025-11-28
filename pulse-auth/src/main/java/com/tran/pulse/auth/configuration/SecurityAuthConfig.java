package com.tran.pulse.auth.configuration;

import com.tran.pulse.auth.filter.JwtAuthRequestFilter;
import com.tran.pulse.auth.handler.AuthenticationEntryPointHandler;
import com.tran.pulse.auth.handler.LogoutSuccessDefaultHandler;
import com.tran.pulse.auth.handler.AccessDeniedDefaultHandler;
import com.tran.pulse.auth.properties.AuthProperties;
import com.tran.pulse.auth.properties.CORSProperties;
import com.tran.pulse.auth.service.JwtService;
import com.tran.pulse.auth.service.AuthService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security 安全配置类 配置JWT认证、CORS跨域、安全头部等安全相关功能。 采用基于JWT的无状态认证机制，适用于前后端分离的架构。
 * 主要功能：
 * 1. JWT认证过滤器配置
 * 2. CORS跨域支持
 * 3. 安全头部设置
 * 4. 白名单路径配置
 * 5. 异常处理配置
 * 6. 登出处理配置
 *
 * @author tran
 * @date 2025/6/17 15:22
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableConfigurationProperties({AuthProperties.class, CORSProperties.class})
public class SecurityAuthConfig {

    private final AuthProperties authProperties;
    
    private final CORSProperties corsProperties;

    /**
     * 构造函数注入
     */
    public SecurityAuthConfig(AuthProperties authProperties, CORSProperties corsProperties) {
        this.authProperties = authProperties;
        this.corsProperties = corsProperties;
    }

    /**
     * 密码编码器
     * 使用BCrypt加密算法，强度为10。
     * 即使不使用密码认证，也建议配置以备将来扩展使用。
     *
     * @return BCrypt密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * JWT服务
     *
     * @return JWT服务实例
     */
    @Bean
    public JwtService jwtService() {
        return new JwtService(authProperties);
    }

    /**
     * JWT服务
     *
     * @return JWT服务实例
     */
    @Bean
    public AuthService authService() {
        return new AuthService();
    }


    /**
     * JWT认证过滤器
     *
     * @param jwtService JWT服务
     * @return JWT认证过滤器实例
     */
    @Bean
    public JwtAuthRequestFilter jwtAuthRequestFilter(JwtService jwtService) {
        return new JwtAuthRequestFilter(jwtService, authProperties);
    }

    /**
     * 认证入口点处理器
     * 
     * 当用户未认证访问受保护资源时的处理器
     *
     * @return 认证入口点处理器
     */
    @Bean
    public AuthenticationEntryPointHandler authenticationEntryPointHandler() {
        return new AuthenticationEntryPointHandler();
    }

    /**
     * 访问拒绝处理器
     * 
     * 当用户已认证但权限不足时的处理器
     *
     * @return 访问拒绝处理器
     */
    @Bean
    public AccessDeniedDefaultHandler accessDeniedHandler() {
        return new AccessDeniedDefaultHandler();
    }

    /**
     * 登出成功处理器
     *
     * @return 登出成功处理器
     */
    @Bean
    public LogoutSuccessDefaultHandler logoutSuccessHandler() {
        return new LogoutSuccessDefaultHandler();
    }

    /**
     * CORS配置源
     * 
     * 根据配置文件动态配置CORS规则，支持跨域访问
     *
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的跨域源
        List<String> allowedOrigins = parseCommaSeparatedString(corsProperties.getAllowedOrigins());
        if (allowedOrigins.isEmpty()) {
            // 如果没有配置，则允许所有源（开发环境）
            config.setAllowedOriginPatterns(Collections.singletonList("*"));
        } else {
            config.setAllowedOriginPatterns(allowedOrigins);
        }

        // 允许的HTTP方法
        List<String> allowedMethods = parseCommaSeparatedString(corsProperties.getAllowedMethods());
        if (allowedMethods.isEmpty()) {
            config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        } else {
            config.setAllowedMethods(allowedMethods);
        }

        // 允许的请求头
        List<String> allowedHeaders = parseCommaSeparatedString(corsProperties.getAllowedHeaders());
        if (allowedHeaders.isEmpty()) {
            config.setAllowedHeaders(Arrays.asList("*"));
        } else {
            config.setAllowedHeaders(allowedHeaders);
        }

        // 允许携带凭证（Cookie等）
        config.setAllowCredentials(corsProperties.getAllowCredentials());

        // 预检请求缓存时间
        config.setMaxAge(corsProperties.getMaxAge());

        // 暴露的响应头
        config.setExposedHeaders(Arrays.asList(
                "Authorization", "Cache-Control", "Content-Language",
                "Content-Type", "Expires", "Last-Modified", "Pragma"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 安全过滤器链配置
     * 配置Spring Security的核心安全规则，包括认证、授权、异常处理等
     *
     * @param http                            HTTP安全配置
     * @param jwtAuthRequestFilter            JWT认证过滤器
     * @param authenticationEntryPointHandler 认证入口点处理器
     * @param accessDeniedHandler             访问拒绝处理器
     * @param logoutSuccessHandler            登出成功处理器
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthRequestFilter jwtAuthRequestFilter,
            AuthenticationEntryPointHandler authenticationEntryPointHandler,
            AccessDeniedDefaultHandler accessDeniedHandler,
            LogoutSuccessDefaultHandler logoutSuccessHandler) throws Exception {

        return http
                // CORS配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 禁用CSRF（因为使用JWT无状态认证）
                .csrf(AbstractHttpConfigurer::disable)

                // 安全头部配置
                .headers(headers -> headers
                        // 禁用缓存控制（由应用层控制）
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                        // 允许同源iframe（用于某些管理界面）
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        // 内容类型嗅探保护
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        // XSS保护
                        .xssProtection(xss -> xss.block(true))
                        // Referrer策略
                        .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)

                )

                // 无状态会话管理
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 异常处理配置
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPointHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 请求授权配置
                .authorizeHttpRequests(auth -> {
                    // 获取白名单路径
                    List<String> whiteList = authProperties.getWhiteList();
                    if (whiteList != null && !whiteList.isEmpty()) {
                        auth.antMatchers(whiteList.toArray(new String[0])).permitAll();
                    }

                    // 默认白名单路径
                    auth.antMatchers(
                            // 认证相关接口
                            "/auth/login", "/auth/register", "/auth/refresh",
                            "/user/captcha", "/public/**",
                            // 静态资源
                            "/", "/*.html", "/static/**", "/favicon.ico",
                            // API文档
                            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**",
                            // 监控端点（生产环境建议单独配置）
                            "/actuator/health", "/actuator/info"
                    ).permitAll();

                    // 静态资源（GET方法）
                    auth.antMatchers(HttpMethod.GET,
                            "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.gif",
                            "/**/*.ico", "/**/*.svg", "/**/*.woff", "/**/*.woff2", "/**/*.ttf"
                    ).permitAll();

                    // OPTIONS请求允许（CORS预检）
                    auth.antMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    // 其他所有请求需要认证
                    auth.anyRequest().authenticated();
                })

                // 登出配置
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                )

                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthRequestFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /**
     * 解析逗号分隔的字符串为列表
     *
     * @param str 逗号分隔的字符串
     * @return 字符串列表
     */
    private List<String> parseCommaSeparatedString(String str) {
        if (!StringUtils.hasText(str)) {
            return Collections.emptyList();
        }

        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}