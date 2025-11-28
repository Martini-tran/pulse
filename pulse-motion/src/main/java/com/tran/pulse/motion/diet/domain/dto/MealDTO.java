package com.tran.pulse.motion.diet.domain.dto;


import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 餐食数据传输对象（MealDTO）
 * 用于在前端与后端之间传递餐食相关信息。
 */
@Data
public class MealDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 餐食记录ID
     */
    private String id;

    /**
     * 餐食时间（字符串格式，如 "08:30"）
     */
    private String time;

    /**
     * 餐食备注文字
     */
    private String text;

    /**
     * 餐食日期
     */
    private String date;

    /**
     * 热量（千卡 kcal）
     */
    private double calories;

    /**
     * 蛋白质（克 g）
     */
    private double protein;

    /**
     * 碳水化合物（克 g）
     */
    private double carbs;

    /**
     * 脂肪（克 g）
     */
    private double fat;


    @Override
    public String toString() {
        return "MealDTO{" +
                "id='" + id + '\'' +
                ", time='" + time + '\'' +
                ", text='" + text + '\'' +
                ", date=" + date +
                ", calories=" + calories +
                ", protein=" + protein +
                ", carbs=" + carbs +
                ", fat=" + fat +
                '}';
    }
}

