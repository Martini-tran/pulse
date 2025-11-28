package com.tran.pulse.common.domain.entity;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 标签用户表
 * 对应表：tag_user
 */
@Data
public class TagUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签代码名称
     * 对应字段：tag_code
     * 主键之一
     */
    private String tagCode;

    /**
     * 用户ID
     * 对应字段：user_id
     * 主键之一，自增
     */
    private Long userId;

    /**
     * 标签值
     * 对应字段：tag_value
     */
    private String tagValue;

    /**
     * 创建时间
     * 对应字段：created_time
     * 默认当前时间
     */
    private Timestamp createdTime;

    /**
     * 更新时间
     * 对应字段：updated_time
     * 默认当前时间，更新时自动刷新
     */
    private Timestamp updatedTime;


}
