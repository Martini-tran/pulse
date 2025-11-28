package com.tran.pulse.auth.service;

import com.tran.pulse.auth.domain.LoginUser;
import com.tran.pulse.common.constants.Constants;
import com.tran.pulse.common.domain.entity.SysPermission;
import com.tran.pulse.common.domain.entity.SysRole;
import com.tran.pulse.common.domain.entity.SysUser;
import com.tran.pulse.common.domain.entity.SysUserRole;
import com.tran.pulse.user.mapper.SysPermissionMapper;
import com.tran.pulse.user.mapper.SysRouteMapper;
import com.tran.pulse.user.mapper.SysUserMapper;
import com.tran.pulse.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务层
 * 提供用户相关的业务逻辑处理，包括用户注册、查询、权限管理等功能。
 *
 * @author tran
 * @date 2025/7/02
 */
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private SysRouteMapper sysRouteMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息，不包含密码
     */
    public SysUser getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }

        SysUser user = userService.getUserByUsername(username);
        if (user != null) {
            // 清除密码信息，确保安全
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 用户注册
     *
     * @param password 密码（明文）
     * @param email    邮箱
     * @return 注册结果
     */
    public boolean registerUser(String username, String password, String email) {
        // 参数验证
        if (!StringUtils.hasText(username) || !StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("邮箱和密码不能为空");
        }

        // 检查用户名是否已存在
        SysUser existingUser = userService.getUserByUsername(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查用户名是否已存在
        SysUser existingEmail = userService.getUserByEmail(email);
        if (existingEmail != null) {
            throw new IllegalArgumentException("邮箱已存在");
        }


        // 创建新用户
        SysUser newUser = new SysUser();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // 加密密码
        newUser.setEmail(email);
        newUser.setStatus(Constants.STATUS_ENABLED); // 默认启用
        newUser.setCreateTime(new Date());
        newUser.setUpdateTime(new Date());

        int result = userService.createUser(newUser);

        // 给用户添加默认角色权限
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(newUser.getId());
        sysUserRole.setRoleId(Constants.ORDINARY_USER);
        sysRouteMapper.insertUserRoleSelective(sysUserRole);

        return result > 0;
    }

    /**
     * 验证用户登录
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return 验证成功的用户信息，失败返回null
     */
    public SysUser validateUser(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return null;
        }

        // 查询用户（包含密码）
        SysUser user = userService.getUserByUsernameOrEmail(username, username);
        if (user == null) {
            return null;
        }

        // 检查用户状态
        if (Constants.STATUS_DISABLED.equals(user.getStatus())) {
            throw new IllegalStateException("用户账户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        // 清除密码信息
        user.setPassword(null);
        return user;
    }

    /**
     * 获取LoginUser
     *
     * @param username 用户名称
     * @return LoginUser
     */
    public LoginUser getLoginUser(String username) {
        SysUser user = getUserByUsername(username);
        return getLoginUser(user);
    }


    /**
     * 更新用户状态
     *
     * @param userId
     * @param status
     * @return
     */
    public boolean updateUserStatus(Long userId, String status) {
        return userService.updateUserStatus(userId, status) > 0;
    }

    /**
     * 获取LoginUser
     *
     * @param user 用户
     * @return LoginUser
     */
    public LoginUser getLoginUser(SysUser user) {
        LoginUser loginUser = LoginUser.fromSysUser(user);
        List<SysRole> userRoutes = getUserRoutes(loginUser.getUserId());
        List<SysPermission> userPermissions = getUserPermission(loginUser.getUserId());
        Set<String> routes = userRoutes.stream()
                .map(SysRole::getCode)
                .collect(Collectors.toSet());
        Set<String> permissions = userPermissions.stream()
                .map(SysPermission::getCode)
                .collect(Collectors.toSet());
        loginUser.setRoles(routes);
        loginUser.setPermissions(permissions);
        return loginUser;
    }


    /**
     * 查询用户拥有的路由权限
     *
     * @param userId 用户ID
     * @return 用户的路由/角色列表
     */
    public List<SysRole> getUserRoutes(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return sysRouteMapper.selectRoutesByUserId(userId);
    }

    /**
     * 查询用户拥有的路由权限
     *
     * @param userId 用户ID
     * @return 用户的路由/角色列表
     */
    public List<SysPermission> getUserPermission(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return sysPermissionMapper.getPermissionsByUserId(userId);
    }


    /**
     * 检查用户是否存在
     *
     * @param username 用户名
     * @return 存在返回true，否则返回false
     */
    public boolean isUserExists(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }

        SysUser user = userService.getUserByUsername(username);
        return user != null;
    }

    /**
     * 检查用户是否存在
     *
     * @param email 用户名
     * @return 存在返回true，否则返回false
     */
    public boolean isUserEmailExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        SysUser user = userService.getUserByEmail(email);
        return user != null;
    }


    /**
     * 验证邮箱格式
     *
     * @param email 邮箱地址
     * @return 格式正确返回true
     */
    public boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 强度足够返回true
     */
    public boolean isValidPassword(String password) {
        if (!StringUtils.hasText(password)) {
            return false;
        }

        // 密码长度至少6位，包含字母和数字
        if (password.length() < 6) {
            return false;
        }

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasLetter && hasDigit;
    }
}