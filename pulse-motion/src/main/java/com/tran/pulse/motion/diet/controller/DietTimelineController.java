package com.tran.pulse.motion.diet.controller;

import com.tran.pulse.common.domain.model.PulseResult;
import com.tran.pulse.common.exception.PulseException;
import com.tran.pulse.common.util.StringUtils;
import com.tran.pulse.motion.diet.domain.dto.DailyNutritionDTO;
import com.tran.pulse.motion.diet.domain.dto.MealDTO;
import com.tran.pulse.motion.diet.domain.dto.WaterIntakeDTO;
import com.tran.pulse.motion.diet.service.DietTimelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 饮食时间线控制器
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/10/15 15:16
 **/
@RestController
@RequestMapping("/app/v1/diet")
public class DietTimelineController {

    private static final Logger logger = LoggerFactory.getLogger(DietTimelineController.class);


    @Autowired
    private DietTimelineService dietTimelineService;

    /**
     * 获取饮食列表
     *
     * @param startDate 开始时间（yyyy-MM-dd）
     * @param endDate   结束时间（yyyy-MM-dd）
     * @return 饮食列表，按日期分组
     */
    @GetMapping("/meals/list")
    public PulseResult getMealRecords(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            if (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)) {
                throw new PulseException("缺少参数");
            }
            Map<String, List<MealDTO>> mealRecords = dietTimelineService.getMealRecords(startDate, endDate);
            return PulseResult.success(mealRecords);
        } catch (Exception e) {
            logger.error("获取饮食记录失败: startDate: {} , endDate: {}", startDate, endDate, e);
            throw new PulseException("获取饮食记录失败");
        }
    }

    /**
     * 添加饮食记录
     *
     * @param mealDTO 饮食dto
     * @return 操作结果
     */
    @PostMapping("/meals/add")
    public PulseResult addMealRecord(@RequestBody MealDTO mealDTO) {
        try {
            if (mealDTO == null) {
                throw new PulseException("缺少参数");
            }
            if (StringUtils.isEmpty(mealDTO.getText())) {
                throw new PulseException("请填写餐饮内容");
            }
            boolean success = dietTimelineService.addMealRecord(mealDTO);
            if (success) {
                return PulseResult.success("添加餐食记录成功");
            } else {
                return PulseResult.fail("添加餐食记录失败");
            }
        } catch (Exception e) {
            logger.error("添加餐食记录失败: mealDTO: {}", mealDTO, e);
            throw new PulseException("添加餐食记录失败");
        }
    }

    /**
     * 更新饮食记录
     *
     * @param mealDTO 饮食dto
     * @return 操作结果
     */
    @PostMapping("/meals/update")
    public PulseResult updateMealRecord(@RequestBody MealDTO mealDTO) {
        try {
            if (mealDTO == null || StringUtils.isEmpty(mealDTO.getId())) {
                throw new PulseException("缺少参数");
            }
            if (StringUtils.isEmpty(mealDTO.getText())) {
                throw new PulseException("请填写餐饮内容");
            }
            boolean success = dietTimelineService.updateMealRecord(mealDTO);
            if (success) {
                return PulseResult.success("更新餐食记录成功");
            } else {
                return PulseResult.fail("更新餐食记录失败");
            }
        } catch (Exception e) {
            logger.error("更新餐食记录失败: mealDTO: {}", mealDTO, e);
            throw new PulseException("更新餐食记录失败");
        }
    }

    /**
     * 删除餐食记录
     *
     * @param mealId 餐食记录ID
     * @return 操作结果
     */
    @PostMapping("/meals/{mealId}")
    public PulseResult deleteMealRecord(@PathVariable("mealId") String mealId) {
        try {
            if (StringUtils.isEmpty(mealId)) {
                throw new PulseException("缺少参数");
            }
            boolean success = dietTimelineService.deleteMealRecord(mealId);
            if (success) {
                return PulseResult.success("删除餐食记录成功");
            } else {
                return PulseResult.fail("删除餐食记录失败");
            }
        } catch (Exception e) {
            logger.error("删除餐食记录失败: mealId: {}", mealId, e);
            throw new PulseException("删除餐食记录失败");
        }
    }

    /**
     * 获取每日营养统计
     *
     * @param startDate 开始时间（yyyy-MM-dd）
     * @param endDate   结束时间（yyyy-MM-dd）
     * @return 统计结果
     */
    @GetMapping("/stats/daily")
    public PulseResult getDailyStats(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            if (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)) {
                throw new PulseException("缺少参数");
            }
            DailyNutritionDTO dailyStats = dietTimelineService.getDailyStats(startDate,endDate);
            return PulseResult.success(dailyStats);
        } catch (Exception e) {
            logger.error("获取每日营养统计失败: startDate: {} , endDate: {}", startDate, endDate, e);
            throw new PulseException("获取每日营养统计失败");
        }
    }

    /**
     * 新增或记录饮水量
     * 前端调用示例：
     * POST /app/v1/diet/water
     * body: { "date": "2025-10-15", "amount": 250 }
     *
     * @param waterIntakeDTO 饮水记录
     * @return 操作结果
     */
    @PostMapping("/water")
    public PulseResult addWaterRecord(@RequestBody WaterIntakeDTO waterIntakeDTO) {
        try {
            if (waterIntakeDTO == null) {
                throw new PulseException("缺少参数");
            }
            if (StringUtils.isEmpty(waterIntakeDTO.getDate())) {
                throw new PulseException("日期不能为空");
            }
            if (waterIntakeDTO.getAmount() <= 0) {
                throw new PulseException("饮水量必须大于0");
            }
            boolean success = dietTimelineService.addWaterRecord(waterIntakeDTO);
            if (success) {
                return PulseResult.success("记录饮水量成功");
            } else {
                return PulseResult.fail("记录饮水量失败");
            }
        } catch (Exception e) {
            logger.error("记录饮水量失败: waterIntakeDTO: {}", waterIntakeDTO, e);
            throw new PulseException("记录饮水量失败");
        }
    }
}
