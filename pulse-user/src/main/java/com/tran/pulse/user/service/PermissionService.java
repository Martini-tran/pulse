package com.tran.pulse.user.service;

import com.tran.pulse.common.domain.entity.SysPermission;
import com.tran.pulse.user.mapper.SysPermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务类
 * 提供用户权限相关的服务方法
 */
@Service
public class PermissionService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    /**
     * 获取用户的所有权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<SysPermission> getUserPermissions(Long userId) {
        return permissionMapper.getPermissionsByUserId(userId);
    }

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        List<SysPermission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .anyMatch(permission -> permission.getCode().equals(permissionCode));
    }

    /**
     * 检查用户是否拥有指定资源的操作权限
     *
     * @param userId 用户ID
     * @param resource 资源名称
     * @param action 操作类型
     * @return 是否拥有权限
     */
    public boolean hasResourcePermission(Long userId, String resource, String action) {
        List<SysPermission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .anyMatch(permission -> 
                    permission.getResource().equals(resource) && 
                    permission.getAction().equals(action));
    }

    /**
     * 获取用户的菜单权限
     * 只返回类型为MENU的权限
     *
     * @param userId 用户ID
     * @return 菜单权限列表
     */
    public List<SysPermission> getUserMenuPermissions(Long userId) {
        List<SysPermission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .filter(permission -> "MENU".equals(permission.getResourceType()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的API权限
     * 只返回类型为API的权限
     *
     * @param userId 用户ID
     * @return API权限列表
     */
    public List<SysPermission> getUserApiPermissions(Long userId) {
        List<SysPermission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .filter(permission -> "API".equals(permission.getResourceType()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的按钮权限
     * 只返回类型为BUTTON的权限
     *
     * @param userId 用户ID
     * @return 按钮权限列表
     */
    public List<SysPermission> getUserButtonPermissions(Long userId) {
        List<SysPermission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .filter(permission -> "BUTTON".equals(permission.getResourceType()))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的权限代码列表
     *
     * @param userId 用户ID
     * @return 权限代码列表
     */
    public List<String> getUserPermissionCodes(Long userId) {
        List<SysPermission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .map(SysPermission::getCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的权限，按资源类型分组
     *
     * @param userId 用户ID
     * @return 按资源类型分组的权限Map
     */
    public Map<String, List<SysPermission>> getUserPermissionsByType(Long userId) {
        List<SysPermission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .collect(Collectors.groupingBy(SysPermission::getResourceType));
    }
}
