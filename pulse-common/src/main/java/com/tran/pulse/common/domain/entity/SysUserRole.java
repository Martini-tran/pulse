package com.tran.pulse.common.domain.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户角色关联实体类
 * 对应数据库表：sys_user_roles
 * 用于建立用户和角色的多对多关系映射
 *
 * @author system
 * @since 1.0.0
 */
@Data
public class SysUserRole {

    /**
     * 用户ID
     * 关联sys_user表的主键
     * -- GETTER --
     *  获取用户ID
     *
     * -- SETTER --
     *  设置用户ID
     *
     */
    private Long userId;

    /**
     * 角色ID
     * 关联sys_roles表的主键
     * -- GETTER --
     *  获取角色ID
     *
     *
     * -- SETTER --
     *  设置角色ID
     *
     @return 角色ID
      * @param roleId 角色ID

     */
    private Long roleId;

    /**
     * 分配此角色的管理员用户ID
     * 用于记录是谁给用户分配的角色，便于权限追溯
     * -- GETTER --
     *  获取分配人ID
     *
     *
     * -- SETTER --
     *  设置分配人ID
     *
     @return 分配此角色的管理员用户ID
      * @param assignedBy 分配此角色的管理员用户ID

     */
    private Long assignedBy;

    /**
     * 角色过期时间
     * NULL表示永不过期，有值则表示角色在指定时间后失效
     * -- GETTER --
     *  获取角色过期时间
     *
     *
     * -- SETTER --
     *  设置角色过期时间
     *
     @return 角色过期时间，NULL表示永不过期
      * @param expireTime 角色过期时间，NULL表示永不过期

     */
    private Date expireTime;

    /**
     * 是否激活状态
     * 1表示激活，0表示未激活
     * 用于临时禁用用户角色而不删除关联关系
     * -- GETTER --
     *  获取激活状态
     *
     *
     * -- SETTER --
     *  设置激活状态
     *
     @return 激活状态，1表示激活，0表示未激活
      * @param isActive 激活状态，1表示激活，0表示未激活

     */
    private Integer isActive;

    /**
     * 角色分配时间
     * 记录用户角色关系的创建时间
     * -- GETTER --
     *  获取创建时间
     *
     *
     * -- SETTER --
     *  设置创建时间
     *
     @return 角色分配时间
      * @param createTime 角色分配时间

     */
    private Date createTime;

    /**
     * 更新时间
     * 记录用户角色关系的最后修改时间
     * -- GETTER --
     *  获取更新时间
     *
     *
     * -- SETTER --
     *  设置更新时间
     *
     @return 记录更新时间
      * @param updateTime 记录更新时间

     */
    private Date updateTime;

    /**
     * 判断角色是否已过期
     *
     * @return true表示已过期，false表示未过期或永不过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return false; // 永不过期
        }
        return expireTime.before(new Date());
    }

    /**
     * 判断角色是否有效
     * 需要同时满足：激活状态为1且未过期
     *
     * @return true表示有效，false表示无效
     */
    public boolean isValid() {
        return isActive != null && isActive == 1 && !isExpired();
    }

    @Override
    public String toString() {
        return "SysUserRole{" +
                "userId=" + userId +
                ", roleId=" + roleId +
                ", assignedBy=" + assignedBy +
                ", expireTime=" + expireTime +
                ", isActive=" + isActive +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}