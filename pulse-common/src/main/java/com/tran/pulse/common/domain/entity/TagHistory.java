package com.tran.pulse.common.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签历史记录表 Bean
 */
@Data
public class TagHistory {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 标签编码
     */
    private String tagCode;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 标签值
     */
    private String tagValue;

    /**
     * 记录时间
     */
    private String recordTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

}

