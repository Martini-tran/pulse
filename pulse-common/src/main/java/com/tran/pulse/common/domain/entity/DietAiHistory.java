package com.tran.pulse.common.domain.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * AI生成的记录表
 */
@Data
public class DietAiHistory {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 餐食类型（早餐、午餐、晚餐、其他）
     */
    private String mealType;

    /**
     * 日期（YYYY-MM-DD）
     */
    private LocalDate mealDate;

    /**
     * 饮食内容详情（大文本）
     */
    private String dietDetail;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    // ===== Getter & Setter =====

}

