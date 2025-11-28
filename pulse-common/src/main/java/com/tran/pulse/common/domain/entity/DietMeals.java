package com.tran.pulse.common.domain.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * 用户餐食记录表（meals）
 */
@Data
public class DietMeals implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID（关联 users 表的 id） */
    private Long userId;

    /** 餐食日期（对应前端 date，仅保存日期部分） */
    private Date mealDate;

    /** 餐食时间（对应前端 time，格式HH:mm） */
    private Time mealTime;

    /** 餐食备注（对应前端 text） */
    private String textNote;

    /** 热量（千卡 kcal） */
    private BigDecimal caloriesKcal;

    /** 蛋白质（克 g） */
    private BigDecimal proteinG;

    /** 碳水化合物（克 g） */
    private BigDecimal carbsG;

    /** 脂肪（克 g） */
    private BigDecimal fatG;

    /** 创建时间 */
    private Timestamp createdTime;

    /** 更新时间 */
    private Timestamp updatedTime;
}

