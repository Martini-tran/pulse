package com.tran.pulse.common.domain.entity;

import lombok.Data;

/**
 * 业务与模型配置关联实体类
 * 对应表：ai_business
 */
@Data
public class AiBusiness {

    /**
     * 业务主键ID
     * 对应字段：business_id
     */
    private String businessId;

    /**
     * 模型配置ID（允许为空）
     * 对应字段：ai_model_id
     */
    private Long aiModelId;

    /**
     * 调用AI的system定义
     * 对应字段：system
     */
    private String system;
}

