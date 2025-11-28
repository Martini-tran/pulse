package com.tran.pulse.common.domain.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 饮水记录表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietWater {
    private Long id;              // 主键ID
    private Long userId;          // 用户ID
    private Date recordDate;      // 记录日期
    private Integer intakeMl;     // 饮水量(毫升)
    private Timestamp createdTime;// 创建时间
    private Timestamp updatedTime;// 更新时间
}

