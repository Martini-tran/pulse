package com.tran.pulse.common.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签定义实体类
 */
@Data
public class TagDefinition {

    // Getter and Setter methods
    /**
     * 标签代码名称
     */
    private String tagCode;

    /**
     * 前台显示名称
     */
    private String displayName;

    /**
     * 前台样式配置（选择框选项、hint等）
     */
    private String uiConfig;

    /**
     * 标签单位
     */
    private String unit;

    /**
     * 标签分类ID
     */
    private String categoryCode;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 字段类型：input/select/number/date/boolean等
     */
    private String fieldType;

    /**
     * 是否必填
     */
    private Boolean isRequired;

    /**
     * 显示排序
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

}
