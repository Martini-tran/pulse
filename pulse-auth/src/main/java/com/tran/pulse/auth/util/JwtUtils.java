package com.tran.pulse.auth.util;

import com.tran.pulse.auth.properties.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT（JSON Web Token）工具类
 * 支持双Token机制：Access Token（短期）+ Refresh Token（长期）
 * @author tran
 * @version 2.0.0
 * @since 1.0
 */
public class JwtUtils {

    /**
     * 认证配置属性
     * 包含 JWT 密钥和过期时间等配置信息
     */
    private static AuthProperties props = null;

    /**
     * 签名算法
     * 使用 HS256（HMAC with SHA-256）算法进行签名。
     * HS256 是对称加密算法，使用同一个密钥进行签名和验证。
     */
    private static final SignatureAlgorithm ALG = SignatureAlgorithm.HS256;

    /**
     * 默认Access Token过期时间（2小时）
     */
    private static final int DEFAULT_ACCESS_EXPIRE_SECONDS = 2 * 60 * 60;

    /**
     * 默认Refresh Token过期时间（30天）
     */
    private static final int DEFAULT_REFRESH_EXPIRE_SECONDS = 30 * 24 * 60 * 60;

    /**
     * Token类型常量
     */
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * Token类型声明键
     */
    private static final String CLAIM_TOKEN_TYPE = "token_type";

    /**
     * 私有构造方法，防止实例化
     */
    private JwtUtils() {
        throw new AssertionError("工具类不应该被实例化");
    }

    /**
     * 设置认证配置属性
     *
     * 在使用其他方法前必须先调用此方法注入配置。
     * 通常在 Spring 启动时通过配置类调用。
     *
     * @param props 认证配置属性，不能为 null
     * @throws IllegalArgumentException 如果 props 为 null
     */
    public static void setAuthProperties(AuthProperties props) {
        if (props == null) {
            throw new IllegalArgumentException("AuthProperties 不能为 null");
        }
        JwtUtils.props = props;
    }

    /**
     * 生成Access Token
     * Access Token用于API请求认证，有效期较短（1-2小时）
     *
     * @param subject Token 的主题，通常是用户标识（如用户名、用户ID等）
     * @return 生成的 Access Token 字符串
     * @throws IllegalStateException 如果 AuthProperties 未设置
     * @throws IllegalArgumentException 如果 subject 为 null 或空
     */
    public static String generateAccessToken(String subject) {
        return generateAccessToken(subject, new HashMap<>());
    }

    /**
     * 生成包含额外声明的Access Token
     *
     * @param subject     Token 的主题
     * @param extraClaims 额外的声明信息，可以为空 Map
     * @return 生成的 Access Token 字符串
     * @throws IllegalStateException 如果 AuthProperties 未设置
     */
    public static String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject 不能为空");
        }

        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);

        return generateToken(subject, claims, getAccessTokenExpireMillis());
    }

    /**
     * 生成Refresh Token
     * Refresh Token用于刷新Access Token，有效期较长（30天或更长）
     *
     * @param subject Token 的主题，通常是用户标识
     * @return 生成的 Refresh Token 字符串
     * @throws IllegalStateException 如果 AuthProperties 未设置
     * @throws IllegalArgumentException 如果 subject 为 null 或空
     */
    public static String generateRefreshToken(String subject) {
        return generateRefreshToken(subject, new HashMap<>());
    }

    /**
     * 生成包含额外声明的Refresh Token
     *
     * @param subject     Token 的主题
     * @param extraClaims 额外的声明信息，可以为空 Map
     * @return 生成的 Refresh Token 字符串
     * @throws IllegalStateException 如果 AuthProperties 未设置
     */
    public static String generateRefreshToken(String subject, Map<String, Object> extraClaims) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject 不能为空");
        }

        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);

        return generateToken(subject, claims, getRefreshTokenExpireMillis());
    }

    /**
     * 生成双Token对象
     * 同时生成Access Token和Refresh Token
     *
     * @param subject     Token 的主题
     * @param extraClaims 额外的声明信息，会同时添加到两个token中
     * @return TokenPair 包含Access Token和Refresh Token的对象
     */
    public static TokenPair generateTokenPair(String subject, Map<String, Object> extraClaims) {
        String accessToken = generateAccessToken(subject, extraClaims);
        String refreshToken = generateRefreshToken(subject, extraClaims);

        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * 生成双Token对象（无额外声明）
     *
     * @param subject Token 的主题
     * @return TokenPair 包含Access Token和Refresh Token的对象
     */
    public static TokenPair generateTokenPair(String subject) {
        return generateTokenPair(subject, new HashMap<>());
    }

    /**
     * 使用Refresh Token刷新Access Token
     *
     * @param refreshToken 有效的Refresh Token
     * @return 新的TokenPair，包含新的Access Token和原Refresh Token
     * @throws IllegalArgumentException 如果不是有效的Refresh Token
     * @throws io.jsonwebtoken.JwtException 如果token格式错误或已过期
     */
    public static TokenPair refreshAccessToken(String refreshToken) {
        // 验证是否为有效的Refresh Token
        if (!isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("提供的不是有效的Refresh Token");
        }

        if (isTokenExpired(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token已过期，请重新登录");
        }

        // 从Refresh Token中提取信息
        Claims claims = getAllClaims(refreshToken);
        String subject = claims.getSubject();

        // 移除时间相关和token类型相关的声明
        Map<String, Object> extraClaims = new HashMap<>(claims);
        extraClaims.remove(Claims.SUBJECT);
        extraClaims.remove(Claims.ISSUED_AT);
        extraClaims.remove(Claims.EXPIRATION);
        extraClaims.remove(CLAIM_TOKEN_TYPE);

        // 生成新的Access Token，保持原Refresh Token不变
        String newAccessToken = generateAccessToken(subject, extraClaims);

        return new TokenPair(newAccessToken, refreshToken);
    }

    /**
     * 检查Token是否为Access Token
     *
     * @param token JWT Token字符串
     * @return 如果是Access Token返回true，否则返回false
     */
    public static boolean isAccessToken(String token) {
        try {
            String tokenType = getClaim(token, claims -> (String) claims.get(CLAIM_TOKEN_TYPE));
            return TOKEN_TYPE_ACCESS.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查Token是否为Refresh Token
     *
     * @param token JWT Token字符串
     * @return 如果是Refresh Token返回true，否则返回false
     */
    public static boolean isRefreshToken(String token) {
        try {
            String tokenType = getClaim(token, claims -> (String) claims.get(CLAIM_TOKEN_TYPE));
            return TOKEN_TYPE_REFRESH.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成仅包含 subject（主题）的 JWT Token（兼容性方法）
     * 默认生成Access Token以保持向后兼容
     *
     * @param subject Token 的主题，通常是用户标识
     * @return 生成的 JWT Token 字符串
     * @deprecated 建议使用 generateAccessToken 或 generateRefreshToken
     */
    @Deprecated
    public static String generateToken(String subject) {
        return generateAccessToken(subject);
    }

    /**
     * 生成包含额外声明的 JWT Token（兼容性方法）
     * 默认生成Access Token以保持向后兼容
     *
     * @param subject     Token 的主题
     * @param extraClaims 额外的声明信息
     * @return 生成的 JWT Token 字符串
     * @deprecated 建议使用 generateAccessToken 或 generateRefreshToken
     */
    @Deprecated
    public static String generateToken(String subject, Map<String, Object> extraClaims) {
        return generateAccessToken(subject, extraClaims);
    }

    /**
     * 内部方法：生成JWT Token
     *
     * @param subject      Token的主题
     * @param extraClaims  额外的声明信息
     * @param expireMillis 过期时间（毫秒）
     * @return 生成的JWT Token字符串
     */
    private static String generateToken(String subject, Map<String, Object> extraClaims, long expireMillis) {
        checkPropsInitialized();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMillis);

        // 构建 JWT
        return Jwts.builder()
                .setClaims(extraClaims)    // 设置额外声明（必须在其他设置之前）
                .setSubject(subject)       // 设置主题
                .setIssuedAt(now)         // 设置签发时间
                .setExpiration(expiry)    // 设置过期时间
                .signWith(ALG, getSigningKey()) // 使用密钥和算法进行签名
                .compact();               // 生成最终的 token 字符串
    }

    /**
     * 从 Token 中提取用户名（subject）
     *
     * @param token JWT Token 字符串
     * @return Token 中的 subject（用户名）
     * @throws io.jsonwebtoken.JwtException 如果 token 格式错误或签名验证失败
     * @throws IllegalStateException 如果 AuthProperties 未设置
     */
    public static String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * 从 Token 中提取过期时间
     *
     * @param token JWT Token 字符串
     * @return Token 的过期时间
     * @throws io.jsonwebtoken.JwtException 如果 token 格式错误或签名验证失败
     * @throws IllegalStateException 如果 AuthProperties 未设置
     */
    public static Date getExpirationDateFromToken(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * 检查 Token 是否已过期
     *
     * @param token JWT Token 字符串
     * @return 如果 token 已过期或解析失败返回 true，否则返回 false
     */
    public static boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            // 解析出错时视为失效（可能是 token 格式错误、签名不匹配等）
            return true;
        }
    }

    /**
     * 验证 Token 的有效性
     *
     * @param token 待验证的 JWT Token
     * @return 如果 token 有效返回 true，否则返回 false
     */
    public static boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新 Token（兼容性方法）
     *
     * @param token 原始的 JWT Token
     * @return 新生成的 JWT Token
     * @throws io.jsonwebtoken.JwtException 如果原 token 格式错误或签名验证失败
     * @deprecated 建议使用 refreshAccessToken 方法
     */
    @Deprecated
    public static String refreshToken(String token) {
        Claims claims = getAllClaims(token);
        // 移除旧的时间相关声明
        claims.remove(Claims.ISSUED_AT);
        claims.remove(Claims.EXPIRATION);

        String subject = claims.getSubject();
        claims.remove(Claims.SUBJECT);

        return generateAccessToken(subject, claims);
    }

    /**
     * 从 Token 中提取特定的声明（claim）
     *
     * @param token    JWT Token 字符串
     * @param resolver 声明解析函数，用于从 Claims 中提取特定信息
     * @param <T>      返回值类型
     * @return 提取的声明值
     * @throws io.jsonwebtoken.JwtException 如果 token 格式错误或签名验证失败
     */
    private static <T> T getClaim(String token, Function<Claims, T> resolver) {
        Claims claims = getAllClaims(token);
        return resolver.apply(claims);
    }

    /**
     * 解析 Token 获取所有声明（claims）
     *
     * @param token JWT Token 字符串
     * @return 包含所有声明的 Claims 对象
     * @throws io.jsonwebtoken.ExpiredJwtException 如果 token 已过期
     * @throws io.jsonwebtoken.UnsupportedJwtException 如果 token 格式不支持
     * @throws io.jsonwebtoken.MalformedJwtException 如果 token 格式错误
     * @throws io.jsonwebtoken.SignatureException 如果签名验证失败
     * @throws IllegalArgumentException 如果 token 为 null 或空
     */
    private static Claims getAllClaims(String token) {
        checkPropsInitialized();

        return Jwts.parser()
                .setSigningKey(getSigningKey())  // 设置验证签名的密钥
                .parseClaimsJws(token)          // 解析 token
                .getBody();                     // 获取 claims 主体
    }

    /**
     * 获取签名密钥
     *
     * @return 用于签名和验证的密钥对象
     * @throws IllegalStateException 如果 AuthProperties 未设置或 secret 为空
     */
    private static Key getSigningKey() {
        checkPropsInitialized();

        String secret = props.getSecret();
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret 不能为空");
        }

        byte[] keyBytes;
        try {
            // 尝试 Base64 解码
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ignore) {
            // Base64 解码失败，使用原始字符串的 UTF-8 字节
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        // 创建 HMAC 密钥
        return new SecretKeySpec(keyBytes, ALG.getJcaName());
    }

    /**
     * 获取Access Token过期时间（毫秒）
     *
     * @return Access Token过期时间（毫秒）
     */
    private static long getAccessTokenExpireMillis() {
        int seconds = props.getExpireSeconds();
        if (seconds <= 0) {
            seconds = DEFAULT_ACCESS_EXPIRE_SECONDS;
        }
        return seconds * 1000L;
    }

    /**
     * 获取Refresh Token过期时间（毫秒）
     *
     * @return Refresh Token过期时间（毫秒）
     */
    private static long getRefreshTokenExpireMillis() {
        int seconds = props.getAppExpireSeconds();
        if (seconds <= 0) {
            seconds = DEFAULT_REFRESH_EXPIRE_SECONDS;
        }
        return seconds * 1000L;
    }

    /**
     * 检查 AuthProperties 是否已初始化
     *
     * @throws IllegalStateException 如果 props 为 null
     */
    private static void checkPropsInitialized() {
        if (props == null) {
            throw new IllegalStateException("AuthProperties 未初始化，请先调用 setAuthProperties() 方法");
        }
    }


}