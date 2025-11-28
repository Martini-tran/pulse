package com.tran.pulse.common.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.Objects;


@Data
public class SysRole {

    // Getter and Setter methods
    /**
     * 角色主键，自增
     */
    private Long id;

    /**
     * 角色标识，如 ROLE_ADMIN
     */
    private String code;

    /**
     * 角色名称，中文描述
     */
    private String name;

    /**
     * 角色详细描述
     */
    private String description;

    /**
     * 状态：1启用，0禁用
     */
    private Boolean status = true;

    /**
     * 排序字段
     */
    private Integer sortOrder = 0;

    /**
     * 是否删除：0未删除，1已删除
     */
    private Boolean isDeleted = false;

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


    public SysRole() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysRole sysRole = (SysRole) o;
        return Objects.equals(id, sysRole.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SysRole{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", sortOrder=" + sortOrder +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
