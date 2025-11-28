package com.tran.pulse.motion.exercise.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 运动记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRecordDTO {

    /**
     * 唯一标识 ID
     */
    private String id;

    /**
     * 日期（包含日期和时间）
     */
    private LocalDateTime date;

    /**
     * 时间字符串（如果需要单独存储时间）
     */
    private String time;

    /**
     * 运动类型（字符串表示）
     */
    private String type;

    /**
     * 运动描述
     */
    private String description;

    /**
     * 运动时长（分钟）
     */
    private int durationMinutes;

    /**
     * 消耗卡路里
     */
    private double caloriesBurned;

    /**
     * 备注（可选）
     */
    private String notes;
}

