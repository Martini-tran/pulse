package com.tran.pulse.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tran.pulse.common.domain.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 登录用户信息类
 * 实现Spring Security的UserDetails接口，用于JWT认证和权限控制。
 * 包含用户的基本信息、权限列表、账户状态等信息。
 * 设计要点：
 *   实现UserDetails接口，与Spring Security无缝集成
 *   支持JSON序列化，可以存储在缓存中
 *   包含完整的用户信息和权限信息
 *   提供灵活的权限和角色管理
 *
 * @author tran
 * @date 2025/7/02
 */
public class LoginUser implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 密码（加密后）
     * 注意：在JWT场景下通常不需要密码，使用@JsonIgnore避免序列化
     */
    @JsonIgnore
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户状态（如：1000-启用，0000-禁用）
     */
    private String status;

    /**
     * 权限列表
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 角色列表（原始角色代码）
     */
    private Set<String> roles;

    /**
     * 权限列表（原始权限代码）
     */
    private Set<String> permissions;

    /**
     * 账户是否未过期
     */
    private boolean accountNonExpired = true;

    /**
     * 账户是否未锁定
     */
    private boolean accountNonLocked = true;

    /**
     * 凭证是否未过期
     */
    private boolean credentialsNonExpired = true;

    /**
     * 账户是否启用
     */
    private boolean enabled = true;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 扩展属性（用于存储额外信息）
     */
    private Map<String, Object> attributes;


    /**
     * 默认构造函数
     */
    public LoginUser() {
        this.authorities = new ArrayList<>();
        this.roles = new HashSet<>();
        this.permissions = new HashSet<>();
        this.attributes = new HashMap<>();
    }

    /**
     * 基础构造函数
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    public LoginUser(Long userId, String username) {
        this();
        this.userId = userId;
        this.username = username;
    }

    /**
     * 完整构造函数
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param password    密码
     * @param authorities 权限列表
     */
    public LoginUser(Long userId, String username, String password,
                     Collection<? extends GrantedAuthority> authorities) {
        this(userId, username);
        this.password = password;
        this.authorities = authorities != null ? authorities : new ArrayList<>();

        // 从权限中提取角色和权限
        extractRolesAndPermissions();
    }

    /**
     * 从SysUser创建LoginUser的静态工厂方法
     *
     * @param sysUser 系统用户
     * @return LoginUser实例
     */
    public static LoginUser fromSysUser(SysUser sysUser) {
        if (sysUser == null) {
            return null;
        }

        LoginUser loginUser = new LoginUser(sysUser.getId(), sysUser.getUsername());
        loginUser.setEmail(sysUser.getEmail());
        loginUser.setPassword(sysUser.getPassword());

        // 根据用户状态设置账户状态
        boolean isEnabled = !("0000".equals(sysUser.getStatus()));
        loginUser.setEnabled(isEnabled);
        loginUser.setAccountNonLocked(isEnabled);
        loginUser.setStatus(sysUser.getStatus());
        return loginUser;
    }

    /**
     * 从权限列表中提取角色和权限
     */
    private void extractRolesAndPermissions() {
        if (authorities == null) {
            return;
        }

        Set<String> roleSet = new HashSet<>();
        Set<String> permissionSet = new HashSet<>();

        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            if (auth.startsWith("ROLE_")) {
                roleSet.add(auth);
            } else {
                permissionSet.add(auth);
            }
        }

        this.roles = roleSet;
        this.permissions = permissionSet;
    }

    // ==================== UserDetails接口实现 ====================

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    // ==================== 便捷方法 ====================

    /**
     * 是否拥有指定角色
     *
     * @param role 角色代码（如：ROLE_ADMIN）
     * @return 是否拥有该角色
     */
    public boolean hasRole(String role) {
        if (roles == null) {
            return false;
        }

        // 支持带ROLE_前缀和不带前缀的查询
        String roleToCheck = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return roles.contains(roleToCheck);
    }

    /**
     * 是否拥有指定权限
     *
     * @param permission 权限代码
     * @return 是否拥有该权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 是否拥有任意一个指定角色
     *
     * @param roles 角色列表
     * @return 是否拥有任意一个角色
     */
    public boolean hasAnyRole(String... roles) {
        if (roles == null) {
            return false;
        }

        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否拥有任意一个指定权限
     *
     * @param permissions 权限列表
     * @return 是否拥有任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        if (permissions == null) {
            return false;
        }

        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是管理员
     *
     * @return 是否是管理员
     */
    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }

    /**
     * 获取角色名称列表（去掉ROLE_前缀）
     *
     * @return 角色名称列表
     */
    public List<String> getRoleNames() {
        if (roles == null) {
            return new ArrayList<>();
        }

        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .collect(Collectors.toList());
    }

    /**
     * 添加角色
     *
     * @param role 角色代码
     */
    public void addRole(String role) {
        if (roles == null) {
            roles = new HashSet<>();
        }

        String roleToAdd = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        roles.add(roleToAdd);
        refreshAuthorities();
    }

    /**
     * 添加权限
     *
     * @param permission 权限代码
     */
    public void addPermission(String permission) {
        if (permissions == null) {
            permissions = new HashSet<>();
        }

        permissions.add(permission);
        refreshAuthorities();
    }

    /**
     * 刷新权限列表（当角色或权限发生变化时调用）
     */
    private void refreshAuthorities() {
        List<GrantedAuthority> authList = new ArrayList<>();

        if (roles != null) {
            authList.addAll(roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        if (permissions != null) {
            authList.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        this.authorities = authList;
    }

    /**
     * 设置权限列表
     *
     * @param authorities 权限列表
     */
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities != null ? authorities : new ArrayList<>();
        extractRolesAndPermissions();
    }

    /**
     * 获取扩展属性
     *
     * @param key 属性key
     * @return 属性值
     */
    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    /**
     * 设置扩展属性
     *
     * @param key   属性key
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }

    /**
     * 更新最后访问时间
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }

    // ==================== Getter and Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
        refreshAuthorities();
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
        refreshAuthorities();
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }


    // ==================== Object方法重写 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginUser loginUser = (LoginUser) o;
        return Objects.equals(userId, loginUser.userId) &&
                Objects.equals(username, loginUser.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    @Override
    public String toString() {
        return "LoginUser{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                ", roles=" + getRoleNames() +
                ", permissions=" + permissions +
                '}';
    }
}