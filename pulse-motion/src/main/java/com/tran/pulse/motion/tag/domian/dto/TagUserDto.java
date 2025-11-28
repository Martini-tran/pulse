package com.tran.pulse.motion.tag.domian.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

/**
 * 用户信息传输对象 (DTO)
 * 用于接收前端 Flutter 传过来的用户基本信息
 */
@Data
public class TagUserDto {

    /**
     * 出生日期
     * - 前端使用 Flutter 的 DateTime 类型
     * - 后端接收时使用 LocalDate（只存日期，不含时间）
     * - JSON 格式为 "yyyy-MM-dd"，例如 "1995-06-15"
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * 性别
     * - 由前端传递，例如 "male", "female"
     */
    private String gender;

    /**
     * 身高（单位：厘米）
     * - 使用 Double 包装类，避免 null 时报错
     */
    private Double height;

    /**
     * 当前体重（单位：千克）
     */
    private Double currentWeight;

    /**
     * 目标体重（单位：千克）
     */
    private Double targetWeight;
}
