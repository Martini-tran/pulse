package com.tran.pulse.motion.diet.domain.dto;


import lombok.Data;

/**
 * 每日营养统计数据传输对象（DailyNutritionDTO）
 *
 * 用于在前端与后端之间传递每日总热量、营养素及饮水量数据。
 */
@Data
public class DailyNutritionDTO  {


    /**
     * 总热量（千卡 kcal）
     */
    private double totalCalories = 0;

    /**
     * 总蛋白质（克 g）
     */
    private double totalProtein = 0;

    /**
     * 总碳水化合物（克 g）
     */
    private double totalCarbs = 0;

    /**
     * 总脂肪（克 g）
     */
    private double totalFat = 0;

    /**
     * 饮水量（毫升 ml）
     */
    private double waterIntake = 0;

}
