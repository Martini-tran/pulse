package com.tran.pulse.auth.service;

import com.tran.pulse.auth.domain.LoginUser;
import com.tran.pulse.auth.domain.RefreshTokenInfo;
import com.tran.pulse.auth.properties.AuthProperties;
import com.tran.pulse.auth.util.JwtUtils;
import com.tran.pulse.auth.util.TokenPair;
import com.tran.pulse.cache.util.CacheUtil;
import com.tran.pulse.common.exception.PulseException;
import com.tran.pulse.common.util.StringIdGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * JWT 服务层实现
 * 支持双Token机制：Access Token（短期）+ Refresh Token（长期）
 *
 * @author tran
 * @version 2.0.0
 * @date 2025/6/30 17:19
 * @since 1.0
 */
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /**
     * Access Token缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "auth:session:";

    /**
     * Refresh Token缓存键前缀，用于存储刷新token的会话信息
     */
    private static final String CACHE_APP_KEY_PREFIX = "auth:app:session:";


    /**
     * 请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";


    /**
     * access token
     */
    public static final String ACCESS_TOKEN = "access_token";

    /**
     * Refresh Token 请求头名称
     */
    public static final String REFRESH_TOKEN_HEADER = "refresh_token";

    /**
     * Refresh Token 请求头名称
     */
    public static final String EXPIRE_SECONDS = "expire_seconds";

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 认证配置属性
     */
    private final AuthProperties authProperties;

    @Autowired
    private AuthService authService;


    /**
     * 构造函数
     * 初始化 JWT 服务，并设置 JwtUtil 的配置属性
     *
     * @param authProperties 认证配置属性，不能为 null
     */
    public JwtService(AuthProperties authProperties) {
        if (authProperties == null) {
            throw new IllegalArgumentException("AuthProperties 不能为 null");
        }
        this.authProperties = authProperties;
        JwtUtils.setAuthProperties(authProperties);
        log.info("JWT 服务初始化完成，Access Token过期时间：{} 秒，Refresh Token过期时间：{} 秒",
                authProperties.getExpireSeconds(), authProperties.getAppExpireSeconds());
    }

    /**
     * 生成单个Access Token（兼容性方法）
     * 为Web端或不需要长期登录的客户端生成Access Token
     *
     * @param loginUser 登录用户信息，不能为 null
     * @return 生成的 JWT token 字符串
     * @throws PulseException           如果生成 token 失败
     * @throws IllegalArgumentException 如果 loginUser 为 null
     */
    public String generateToken(LoginUser loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("登录用户信息不能为 null");
        }

        try {
            // 生成唯一的会话 ID
            String sessionId = StringIdGenerator.next();

            // 生成 Access Token
            String token = JwtUtils.generateAccessToken(sessionId);

            // 将用户信息存储到缓存
            String cacheKey = buildCacheKey(sessionId);
            CacheUtil.put(cacheKey, loginUser, authProperties.getExpireSeconds());

            log.debug("为用户 [{}] 生成单个 Access Token，会话 ID：{}", loginUser.getUsername(), sessionId);
            return token;
        } catch (Exception e) {
            log.error("生成 token 失败，用户：{}", loginUser.getUsername(), e);
            throw new PulseException("登录失败，系统异常");
        }
    }

    /**
     * 生成双Token对（推荐用于移动端App）
     * 为需要长期登录的客户端生成Access Token和Refresh Token
     *
     * @param loginUser 登录用户信息，不能为 null
     * @return TokenPair 包含Access Token和Refresh Token
     * @throws PulseException           如果生成 token 失败
     * @throws IllegalArgumentException 如果 loginUser 为 null
     */
    public TokenPair generateTokenPair(LoginUser loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("登录用户信息不能为 null");
        }

        try {
            // 生成唯一的会话 ID
            String sessionId = StringIdGenerator.next();

            // 生成双Token
            TokenPair tokenPair = JwtUtils.generateTokenPair(sessionId);

            // 将用户信息存储到Access Token缓存（短期）
            String cacheKey = buildCacheKey(sessionId);
            CacheUtil.put(cacheKey, loginUser, authProperties.getExpireSeconds());

            // 将登录IP等关键信息存储到Refresh Token缓存（长期）
            // 这里存储登录IP是为了安全验证，防止Refresh Token被盗用
            String cacheAppKey = buildCacheAppKey(sessionId);
            RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(loginUser.getLoginIp(), loginUser.getUsername());
            CacheUtil.put(cacheAppKey, refreshTokenInfo, authProperties.getAppExpireSeconds());

            log.debug("为App用户 [{}] 生成双Token，会话 ID：{}", loginUser.getUsername(), sessionId);
            return tokenPair;
        } catch (Exception e) {
            log.error("生成双Token失败，用户：{}", loginUser.getUsername(), e);
            throw new PulseException("登录失败，系统异常");
        }
    }

    /**
     * 使用Refresh Token刷新Access Token
     * 这是双Token机制的核心方法，用于获取新的Access Token
     *
     * @param refreshToken Refresh Token
     * @param clientIp     客户端IP地址，用于安全验证
     * @return 新的TokenPair（包含新的Access Token和原Refresh Token）
     * @throws PulseException 如果刷新失败
     */
    public TokenPair refreshAccessToken(String refreshToken, String clientIp) {
        try {
            // 验证是否为有效的Refresh Token
            if (!JwtUtils.isRefreshToken(refreshToken)) {
                throw new PulseException("登录已过期，请重新登录");
            }

            if (JwtUtils.isTokenExpired(refreshToken)) {
                throw new PulseException("登录已过期，请重新登录");
            }

            // 获取会话ID
            String sessionId = JwtUtils.getUsernameFromToken(refreshToken);
            String cacheAppKey = buildCacheAppKey(sessionId);

            // 检查Refresh Token缓存信息
            RefreshTokenInfo refreshTokenInfo = CacheUtil.get(cacheAppKey, RefreshTokenInfo.class);
            if (refreshTokenInfo == null) {
                throw new PulseException("登录已过期，请重新登录");
            }

            // 安全验证：检查IP地址（可选，根据安全需求决定是否启用）
            if (clientIp != null && !clientIp.equals(refreshTokenInfo.getLoginIp())) {
                log.warn("Refresh Token IP地址不匹配，原IP：{}，当前IP：{}", refreshTokenInfo.getLoginIp(), clientIp);
                // 根据安全策略决定是否抛出异常
                // throw new PulseException("安全验证失败，请重新登录");
            }

            // 使用JwtUtils刷新Token
            TokenPair newTokenPair = JwtUtils.refreshAccessToken(refreshToken);

            // 重新获取用户信息（可能需要从数据库查询最新信息）
            LoginUser loginUser = getUserByUsername(refreshTokenInfo.getUsername());
            if (loginUser == null) {
                throw new PulseException("用户信息不存在，请重新登录");
            }

            // 更新Access Token缓存
            String cacheKey = buildCacheKey(sessionId);
            CacheUtil.put(cacheKey, loginUser, authProperties.getExpireSeconds());

            // 延长Refresh Token缓存时间
            CacheUtil.expire(cacheAppKey, authProperties.getAppExpireSeconds());

            log.debug("用户 [{}] 成功刷新Access Token，会话ID：{}", refreshTokenInfo.getUsername(), sessionId);
            return newTokenPair;

        } catch (PulseException e) {
            throw e;
        } catch (Exception e) {
            log.error("刷新Access Token失败", e);
            throw new PulseException("刷新Token失败，请重新登录");
        }
    }

    /**
     * 刷新 Token（兼容性方法）
     * 用于刷新单个Access Token，保持向后兼容
     *
     * @param token 原始 Access Token
     * @return 新的 Access Token
     * @throws PulseException 如果刷新失败
     */
    public String refreshToken(String token) {
        try {
            // 验证原 token 是否有效
            if (!validateToken(token)) {
                throw new PulseException("无效的 token");
            }

            // 获取会话 ID
            String sessionId = JwtUtils.getUsernameFromToken(token);
            String cacheKey = buildCacheKey(sessionId);

            // 检查用户信息是否还在缓存中
            LoginUser loginUser = CacheUtil.get(cacheKey, LoginUser.class);
            if (loginUser == null) {
                throw new PulseException("会话已失效，请重新登录");
            }

            // 延长缓存过期时间
            boolean renewed = CacheUtil.expire(cacheKey, authProperties.getExpireSeconds());
            if (!renewed) {
                log.warn("延长会话 [{}] 缓存时间失败", sessionId);
            }

            // 生成新的 token
            String newToken = JwtUtils.generateAccessToken(sessionId);
            log.debug("刷新 token 成功，会话 ID：{}", sessionId);

            return newToken;
        } catch (PulseException e) {
            throw e;
        } catch (Exception e) {
            log.error("刷新 token 失败", e);
            throw new PulseException("刷新 token 失败");
        }
    }

    /**
     * 获取登录用户信息
     * 从 token 中解析出会话 ID，然后从缓存中获取对应的用户信息
     *
     * @param token JWT token（Access Token或Refresh Token）
     * @return 登录用户信息，如果 token 无效或用户信息不存在则返回 null
     */
    public LoginUser getLoginUser(String token) {
        try {
            String sessionId = JwtUtils.getUsernameFromToken(token);

            // 优先从Access Token缓存获取完整用户信息
            String cacheKey = buildCacheKey(sessionId);
            LoginUser loginUser = CacheUtil.get(cacheKey, LoginUser.class);

            if (loginUser == null) {
                log.debug("Access Token缓存中未找到会话 ID [{}] 对应的用户信息", sessionId);

                // 如果是Refresh Token，尝试从Refresh Token缓存获取基本信息
                if (JwtUtils.isRefreshToken(token)) {
                    String cacheAppKey = buildCacheAppKey(sessionId);
                    RefreshTokenInfo refreshTokenInfo = CacheUtil.get(cacheAppKey, RefreshTokenInfo.class);
                    if (refreshTokenInfo != null) {
                        // 从数据库重新获取用户信息
                        loginUser = getUserByUsername(refreshTokenInfo.getUsername());
                    }
                }
            }

            return loginUser;
        } catch (Exception e) {
            log.error("获取用户信息失败，token：{}", token, e);
            return null;
        }
    }

    /**
     * 检查 Token 是否已过期
     * 仅检查 token 本身的过期时间，不检查缓存中的用户信息
     *
     * @param token JWT token
     * @return 如果 token 已过期返回 true，否则返回 false
     */
    public boolean isTokenExpired(String token) {
        return JwtUtils.isTokenExpired(token);
    }

    /**
     * 验证 Token 有效性
     * 完整的验证流程包括验证token格式、签名、过期时间和缓存信息
     *
     * @param token JWT token
     * @return 如果 token 完全有效返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        try {
            // 1. 基础验证（格式、签名、过期时间）
            if (!JwtUtils.validateToken(token)) {
                return false;
            }

            String sessionId = JwtUtils.getUsernameFromToken(token);

            // 2. 根据Token类型验证对应的缓存
            if (JwtUtils.isAccessToken(token)) {
                // Access Token验证：检查用户信息缓存
                String cacheKey = buildCacheKey(sessionId);
                LoginUser loginUser = CacheUtil.get(cacheKey, LoginUser.class);
                return loginUser != null;
            } else if (JwtUtils.isRefreshToken(token)) {
                // Refresh Token验证：检查刷新令牌缓存
                String cacheAppKey = buildCacheAppKey(sessionId);
                RefreshTokenInfo refreshTokenInfo = CacheUtil.get(cacheAppKey, RefreshTokenInfo.class);
                return refreshTokenInfo != null;
            }

            return false;
        } catch (Exception e) {
            log.error("Token 验证失败", e);
            return false;
        }
    }

    /**
     * 登出（注销）
     * 同时删除Access Token和Refresh Token相关的缓存信息
     *
     * @param token JWT token（Access Token或Refresh Token都可以）
     * @return 如果成功登出返回 true，否则返回 false
     */
    public boolean logout(String token) {
        try {
            String sessionId = JwtUtils.getUsernameFromToken(token);

            // 删除Access Token缓存
            String cacheKey = buildCacheKey(sessionId);
            boolean accessDeleted = CacheUtil.delete(cacheKey);

            // 删除Refresh Token缓存
            String cacheAppKey = buildCacheAppKey(sessionId);
            boolean refreshDeleted = CacheUtil.delete(cacheAppKey);

            boolean success = accessDeleted || refreshDeleted;
            if (success) {
                log.info("用户登出成功，会话 ID：{}", sessionId);
            } else {
                log.warn("用户登出失败，会话可能已过期，会话 ID：{}", sessionId);
            }

            return success;
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return false;
        }
    }

    /**
     * 从请求头中提取 Access Token
     * 支持标准的 Bearer token 格式：Authorization: Bearer {token}
     *
     * @param authHeader Authorization 请求头的值
     * @return 提取的 token，如果格式不正确返回 null
     */
    public String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 从请求头中提取 Refresh Token
     *
     * @param refreshHeader Refresh-Token 请求头的值
     * @return 提取的 refresh token，如果为空返回 null
     */
    public String extractRefreshToken(String refreshHeader) {
        return refreshHeader != null && !refreshHeader.trim().isEmpty() ? refreshHeader.trim() : null;
    }

    /**
     * 构建Access Token缓存键
     *
     * @param sessionId 会话 ID
     * @return 完整的缓存键
     */
    private String buildCacheKey(String sessionId) {
        return CACHE_KEY_PREFIX + sessionId;
    }

    /**
     * 构建Refresh Token缓存键
     *
     * @param sessionId 会话 ID
     * @return 完整的缓存键
     */
    private String buildCacheAppKey(String sessionId) {
        return CACHE_APP_KEY_PREFIX + sessionId;
    }

    /**
     * 获取 Token 剩余有效时间（秒）
     *
     * @param token JWT token
     * @return 剩余有效时间（秒），如果 token 无效或已过期返回 0
     */
    public long getTokenRemainingTime(String token) {
        try {
            java.util.Date expiration = JwtUtils.getExpirationDateFromToken(token);
            long remainingMillis = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingMillis / 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 验证并获取登录用户
     * 组合了验证和获取用户信息的操作，简化使用
     *
     * @param token JWT token
     * @return 登录用户信息的 Optional 包装，如果验证失败返回空的 Optional
     */
    public Optional<LoginUser> validateAndGetUser(String token) {
        if (validateToken(token)) {
            return Optional.ofNullable(getLoginUser(token));
        }
        return Optional.empty();
    }

    /**
     * 检查Token类型
     *
     * @param token JWT token
     * @return Token类型：access, refresh, 或 unknown
     */
    public String getTokenType(String token) {
        try {
            if (JwtUtils.isAccessToken(token)) {
                return "access";
            } else if (JwtUtils.isRefreshToken(token)) {
                return "refresh";
            }
            return "unknown";
        } catch (Exception e) {
            return "invalid";
        }
    }

    /**
     * 根据用户名获取用户信息
     * 这是一个抽象方法，需要具体实现类提供用户查询逻辑
     * 或者通过依赖注入用户服务来实现
     *
     * @param username 用户名
     * @return 用户信息，如果不存在返回null
     */
    private LoginUser getUserByUsername(String username) {
        return authService.getLoginUser(username);
    }

    /**
     * 获取token超时时间
     * @return
     */
    public int getExpireSeconds() {
        return authProperties.getExpireSeconds();
    }


}