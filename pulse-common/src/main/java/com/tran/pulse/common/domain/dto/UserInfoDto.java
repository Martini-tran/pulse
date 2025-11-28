package com.tran.pulse.common.domain.dto;

import java.util.Set;

/**
 * 用户信息响应DTO
 */
public class UserInfoDto {

    private Long id;
    private String username;
    private String email;
    private String status;
    /**
     * 角色列表（原始角色代码）
     */
    private Set<String> roles;

    /**
     * 权限列表（原始权限代码）
     */
    private Set<String> permissions;

    public UserInfoDto() {}


    public UserInfoDto(Long id, String username, String email, String status,Set<String> roles, Set<String> permissions) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.roles = roles;
        this.permissions = permissions;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
