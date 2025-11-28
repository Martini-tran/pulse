package com.tran.pulse.motion.diet.service;

import com.tran.pulse.motion.diet.domain.dto.DailyNutritionDTO;
import com.tran.pulse.motion.diet.domain.dto.MealDTO;
import com.tran.pulse.motion.diet.domain.dto.WaterIntakeDTO;

import java.util.List;
import java.util.Map;

/**
 * 饮食时间线业务接口
 *
 * @author tran
 * @since 2025-10-15
 */
public interface DietTimelineService {

    /**
     * 获取饮食记录列表（按日期区间）
     *
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate   结束日期（yyyy-MM-dd）
     * @return 餐食记录列表
     */
    Map<String,List<MealDTO>> getMealRecords(String startDate, String endDate);

    /**
     * 新增一条餐食记录
     *
     * @param mealDTO 餐食数据
     * @return 是否新增成功
     */
    boolean addMealRecord(MealDTO mealDTO);

    /**
     * 更新一条餐食记录
     *
     * @param mealDTO 餐食数据（需包含记录ID）
     * @return 是否更新成功
     */
    boolean updateMealRecord(MealDTO mealDTO);

    /**
     * 删除一条餐食记录
     *
     * @param mealId 记录ID
     * @return 是否删除成功
     */
    boolean deleteMealRecord(String mealId);

    /**
     * 获取某日营养统计（总热量、蛋白质、碳水、脂肪等）
     *
     * 提示：可返回自定义 DTO（如 DietDailyStatsDTO），
     * 这里先用通用对象承载，便于你后续替换为正式 DTO。
     *
     * @param date 日期（yyyy-MM-dd）
     * @return 当日统计结果对象
     */
    DailyNutritionDTO getDailyStats(String startDate,String endDate);

    /**
     * 新增/记录饮水量
     *
     * @param waterIntakeDTO 饮水记录
     * @return 是否记录成功
     */
    boolean addWaterRecord(WaterIntakeDTO waterIntakeDTO);
}
