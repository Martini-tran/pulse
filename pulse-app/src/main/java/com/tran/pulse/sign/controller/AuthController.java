package com.tran.pulse.sign.controller;

import com.tran.pulse.auth.context.LoginUserContext;
import com.tran.pulse.auth.domain.LoginUser;
import com.tran.pulse.auth.service.JwtService;
import com.tran.pulse.auth.util.IPUtils;
import com.tran.pulse.auth.util.TokenPair;
import com.tran.pulse.common.domain.dto.UserInfoDto;
import com.tran.pulse.common.domain.dto.LoginRequestDto;
import com.tran.pulse.common.domain.dto.UserRegisterDto;
import com.tran.pulse.common.domain.entity.SysUser;
import com.tran.pulse.common.domain.model.PulseResult;
import com.tran.pulse.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户对外接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);


    public static final String USER_INFO = "user_info";


    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;


    /**
     * 用户登录
     * 公开接口，验证用户凭证并返回JWT token
     *
     * @param loginDto      登录请求数据
     * @param bindingResult 参数验证结果
     * @param request       HTTP请求对象
     * @return 登录结果，包含JWT token
     */
    @PostMapping("/login")
    public PulseResult login(@Valid @RequestBody LoginRequestDto loginDto,
                             BindingResult bindingResult,
                             HttpServletRequest request) {

        // 参数验证
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return PulseResult.paramError("参数验证失败：" + errorMsg);
        }

        String clientIp = IPUtils.getClientIpAddress(request);
        try {
            // 验证用户凭证
            SysUser user = authService.validateUser(loginDto.getUsername(), loginDto.getPassword());
            if (user == null) {
                log.warn("用户登录失败 - 用户名：{}，客户端：{}，原因：用户名或密码错误",
                        loginDto.getUsername(), clientIp);
                return PulseResult.authFail("用户名或密码错误");
            }
            Map<String, Object> result = genToken(user);
            // 记录登录成功日志
            String loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.info("用户登录成功 - 用户：{}[{}]，时间：{}，客户端：{}",
                    user.getUsername(), user.getId(), loginTime, clientIp);
            return PulseResult.success(result);

        } catch (IllegalStateException e) {
            log.warn("用户登录失败 - 用户名：{}，客户端：{}，原因：{}",
                    loginDto.getUsername(), clientIp, e.getMessage());
            return PulseResult.authFail(e.getMessage());
        } catch (Exception e) {
            log.error("用户登录异常 - 用户名：{}，客户端：{}", loginDto.getUsername(), clientIp, e);
            return PulseResult.fail("登录服务异常，请稍后重试");
        }
    }


    /**
     * 用户注册
     * <p>
     * 公开接口，不需要认证。支持用户自主注册。
     *
     * @param registerDto   注册请求数据
     * @param bindingResult 参数验证结果
     * @return 注册结果
     */
    @PostMapping("/register")
    public PulseResult register(@Valid @RequestBody UserRegisterDto registerDto,
                                BindingResult bindingResult) {

        // 参数验证
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return PulseResult.paramError("参数验证失败：" + errorMsg);
        }

        try {
            // 额外的业务验证
            if (!authService.isValidPassword(registerDto.getPassword())) {
                return PulseResult.paramError("密码强度不足，请使用至少6位包含字母和数字的密码");
            }

            if (StringUtils.hasText(registerDto.getEmail()) &&
                    !authService.isValidEmail(registerDto.getEmail())) {
                return PulseResult.paramError("邮箱格式不正确");
            }

            // 执行注册
            boolean success = authService.registerUser(
                    registerDto.getUsername(),
                    registerDto.getPassword(),
                    registerDto.getEmail()
            );

            if (success) {
                log.info("用户注册成功 - 用户名：{}", registerDto.getUsername());
                // 直接登录返回
                SysUser user = authService.validateUser(registerDto.getUsername(), registerDto.getPassword());
                Map<String, Object> result = genToken(user);
                return PulseResult.success(result);
            } else {
                return PulseResult.fail("注册失败，请稍后重试");
            }

        } catch (IllegalArgumentException e) {
            log.warn("用户注册失败 - 用户名：{}，原因：{}", registerDto.getUsername(), e.getMessage());
            return PulseResult.paramError(e.getMessage());
        } catch (Exception e) {
            log.error("用户注册异常 - 用户名：{}", registerDto.getUsername(), e);
            return PulseResult.fail("系统异常，请稍后重试");
        }
    }

    /**
     * 用户登录
     * 公开接口，验证用户凭证并返回JWT token
     *
     * @param request HTTP请求对象
     * @return 登录结果，包含JWT token
     */
    @PostMapping("/refresh")
    public PulseResult refreshToken(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Map<String, Object> result = null;
        try {
            String refreshToken = params.get(JwtService.REFRESH_TOKEN_HEADER);
            String clientIp = IPUtils.getClientIpAddress(request);
            TokenPair tokenPair = jwtService.refreshAccessToken(refreshToken, clientIp);
            result = new HashMap<>();
            result.put(JwtService.ACCESS_TOKEN, tokenPair.getAccessToken());
            result.put(JwtService.REFRESH_TOKEN_HEADER, tokenPair.getRefreshToken());
            result.put(JwtService.EXPIRE_SECONDS, jwtService.getExpireSeconds());

        } catch (Exception e) {
            log.error("token 刷新失败 - {} ", params, e);
            return PulseResult.fail("系统异常，请稍后重试");
        }
        return PulseResult.success(result);
    }

    /**
     * 生成并设置token
     *
     * @param user
     */
    private Map<String, Object> genToken(SysUser user) {
        // 创建LoginUser对象
        LoginUser loginUser = authService.getLoginUser(user);
        // 1. App登录 - 生成双Token
        TokenPair tokenPair = jwtService.generateTokenPair(loginUser);
        // 构建响应数据
        Map<String, Object> result = new HashMap<>();
        result.put(JwtService.ACCESS_TOKEN, tokenPair.getAccessToken());
        result.put(JwtService.REFRESH_TOKEN_HEADER, tokenPair.getRefreshToken());
        result.put(JwtService.EXPIRE_SECONDS, jwtService.getExpireSeconds());
        UserInfoDto userInfo = new UserInfoDto(loginUser.getUserId(), loginUser.getUsername(), loginUser.getEmail(), loginUser.getStatus(), loginUser.getRoles(), loginUser.getPermissions());
        result.put(USER_INFO, userInfo);
        return result;
    }


    /**
     * 获取当前用户信息
     * <p>
     * 需要认证。返回当前登录用户的基本信息。
     *
     * @return 用户信息
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public PulseResult getCurrentUserProfile() {

        try {
            LoginUser loginUser = LoginUserContext.get();
            if (loginUser == null) {
                return PulseResult.authFail("用户信息不存在，请重新登录");
            }

            UserInfoDto userInfo = new UserInfoDto(loginUser.getUserId(), loginUser.getUsername(), loginUser.getEmail(), loginUser.getStatus(), loginUser.getRoles(), loginUser.getPermissions());
            return PulseResult.success("获取用户信息成功", userInfo);

        } catch (Exception e) {
            log.error("获取当前用户信息异常", e);
            return PulseResult.fail("获取用户信息失败");
        }
    }

}
