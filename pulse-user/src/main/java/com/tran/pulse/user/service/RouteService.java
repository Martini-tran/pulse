package com.tran.pulse.user.service;

import com.tran.pulse.common.domain.entity.SysRole;
import com.tran.pulse.common.domain.entity.SysUserRole;
import com.tran.pulse.user.mapper.SysRouteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 路由服务类
 * 注意：虽然命名为RouteService，但实际处理的是角色和用户-角色关系
 * 这是因为在当前系统设计中，路由权限是通过角色来控制的
 */
@Service
public class RouteService {

    @Autowired
    private SysRouteMapper sysRouteMapper;

    /**
     * 获取用户的所有路由（实际上是角色）
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<SysRole> getUserRoutes(Long userId) {
        return sysRouteMapper.selectRoutesByUserId(userId);
    }

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @param assignedBy 分配人ID
     * @return 影响行数
     */
    public int assignRoleToUser(Long userId, Long roleId, Long assignedBy) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setAssignedBy(assignedBy);
        userRole.setIsActive(1);
        userRole.setCreateTime(new Date());
        userRole.setUpdateTime(new Date());
        
        return sysRouteMapper.insertUserRoleSelective(userRole);
    }

    /**
     * 为用户分配角色（带过期时间）
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @param assignedBy 分配人ID
     * @param expireTime 过期时间
     * @return 影响行数
     */
    public int assignRoleToUserWithExpiry(Long userId, Long roleId, Long assignedBy, Date expireTime) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setAssignedBy(assignedBy);
        userRole.setExpireTime(expireTime);
        userRole.setIsActive(1);
        userRole.setCreateTime(new Date());
        userRole.setUpdateTime(new Date());
        
        return sysRouteMapper.insertUserRoleSelective(userRole);
    }

    /**
     * 为用户分配临时角色
     * 创建一个有效期为指定天数的角色分配
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @param assignedBy 分配人ID
     * @param days 有效天数
     * @return 影响行数
     */
    public int assignTemporaryRoleToUser(Long userId, Long roleId, Long assignedBy, int days) {
        Date expireTime = new Date();
        // 添加指定天数
        expireTime.setTime(expireTime.getTime() + (long) days * 24 * 60 * 60 * 1000);
        
        return assignRoleToUserWithExpiry(userId, roleId, assignedBy, expireTime);
    }
}
