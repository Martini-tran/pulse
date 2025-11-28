package com.tran.pulse.common.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/7/3 10:26
 **/
@Data
public class SysPermission {

    // Getter and Setter methods
    /**
     * 权限主键，自增
     */
    private Long id;

    /**
     * 权限标识，如 user:read
     */
    private String code;

    /**
     * 权限名称，中文描述
     */
    private String name;

    /**
     * 资源标识，如 user, order
     */
    private String resource;

    /**
     * 操作类型，如 read, write, delete
     */
    private String action;

    /**
     * 资源类型：MENU菜单, BUTTON按钮, API接口
     */
    private String resourceType = "MENU";

    /**
     * 父权限ID，支持权限树结构
     */
    private Long parentId;

    /**
     * 权限路径，如 /user/list
     */
    private String path;

    /**
     * HTTP方法，如 GET, POST
     */
    private String method;

    /**
     * 排序字段
     */
    private Integer sortOrder = 0;

    /**
     * 状态：1启用，0禁用
     */
    private Boolean status = true;

    /**
     * 是否删除：0未删除，1已删除
     */
    private Boolean isDeleted = false;

    /**
     * 权限详细描述
     */
    private String description;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 记录更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


    public SysPermission() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysPermission that = (SysPermission) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SysPermission{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", parentId=" + parentId +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
