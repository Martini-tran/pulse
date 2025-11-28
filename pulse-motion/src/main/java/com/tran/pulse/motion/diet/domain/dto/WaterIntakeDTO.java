package com.tran.pulse.motion.diet.domain.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 饮水记录请求体 DTO
 */
@Data
public class WaterIntakeDTO implements Serializable {
    /**
     * 饮水日期（格式：yyyy-MM-dd）
     */
    private String date;

    /**
     * 饮水量（单位：ml）
     */
    private Integer amount;
}

