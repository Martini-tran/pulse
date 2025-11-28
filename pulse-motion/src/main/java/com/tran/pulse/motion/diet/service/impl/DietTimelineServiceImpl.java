package com.tran.pulse.motion.diet.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.chaincraft.ai.client.common.client.AiClient;
import com.chaincraft.ai.client.common.model.entity.AiModel;
import com.chaincraft.ai.client.common.model.request.AiRequest;
import com.chaincraft.ai.client.common.model.request.Message;
import com.chaincraft.ai.client.common.model.response.AiResponse;
import com.chaincraft.ai.client.common.template.TemplateContext;
import com.chaincraft.ai.client.core.service.AiModelService;
import com.tran.pulse.auth.context.LoginUserContext;
import com.tran.pulse.cache.util.CacheUtil;
import com.tran.pulse.common.constants.BusinessCode;
import com.tran.pulse.common.domain.entity.AiBusiness;
import com.tran.pulse.common.domain.entity.DietMeals;
import com.tran.pulse.common.domain.entity.DietWater;
import com.tran.pulse.common.exception.PulseException;
import com.tran.pulse.common.util.JacksonUtils;
import com.tran.pulse.motion.chatbot.service.ChatbotService;
import com.tran.pulse.motion.diet.domain.dto.DailyNutritionDTO;
import com.tran.pulse.motion.diet.domain.dto.MealDTO;
import com.tran.pulse.motion.diet.domain.dto.WaterIntakeDTO;
import com.tran.pulse.motion.diet.mapper.DietMealsMapper;
import com.tran.pulse.motion.diet.mapper.DietWaterMapper;
import com.tran.pulse.motion.diet.service.DietTimelineService;
import com.tran.pulse.motion.home.domain.ProgressDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 饮食时间线服务实现类
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/10/15 16:19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DietTimelineServiceImpl implements DietTimelineService {

    private final DietMealsMapper dietMealsMapper;

    private final ChatbotService chatbotService;

    private final AiModelService aiModelService;

    private final  AiClient aiClient;

    private final DietWaterMapper dietWaterMapper;

    @Override
    public Map<String, List<MealDTO>> getMealRecords(String startDate, String endDate) {
        Long userId = LoginUserContext.getUserId();
        log.debug("查询用户 {} 的饮食记录，时间范围：{} 至 {}", userId, startDate, endDate);

        List<MealDTO> meals = dietMealsMapper.selectDietMeals(startDate, endDate, userId);

        return meals.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        MealDTO::getDate,
                        Collectors.toList()
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMealRecord(MealDTO mealDTO) {
        validateMealDTO(mealDTO);

        Long userId = LoginUserContext.getUserId();
        DietMeals entity = toEntity(mealDTO, userId);
        DietMeals daily = getDaily(mealDTO.getText());
        entity.setFatG(daily.getFatG());
        entity.setCarbsG(daily.getCarbsG());
        entity.setProteinG(daily.getProteinG());
        entity.setCaloriesKcal(daily.getCaloriesKcal());
        int rows = dietMealsMapper.addDietMeals(entity);
        log.info("用户 {} 添加饮食记录，影响行数：{}", userId, rows);

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMealRecord(MealDTO mealDTO) {
        validateMealDTO(mealDTO);

        if (!StringUtils.hasText(mealDTO.getId())) {
            throw new PulseException("更新记录时ID不能为空");
        }

        DietMeals existingMeal = dietMealsMapper.selectDietMealsById(mealDTO.getId());
        if (existingMeal == null) {
            throw new PulseException("更新的数据不存在");
        }

        Long userId = LoginUserContext.getUserId();
        DietMeals entity = toEntity(mealDTO, userId);
        entity.setCreatedTime(existingMeal.getCreatedTime()); // 保留原创建时间
        DietMeals daily = getDaily(mealDTO.getText());
        entity.setFatG(daily.getFatG());
        entity.setCarbsG(daily.getCarbsG());
        entity.setProteinG(daily.getProteinG());
        entity.setCaloriesKcal(daily.getCaloriesKcal());
        int rows = dietMealsMapper.updateDietMeals(entity);
        log.info("用户 {} 更新饮食记录 {}，影响行数：{}", userId, mealDTO.getId(), rows);

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMealRecord(String mealId) {
        if (!StringUtils.hasText(mealId)) {
            throw new PulseException("删除记录时ID不能为空");
        }

        Long userId = LoginUserContext.getUserId();
        DietMeals existingMeal = dietMealsMapper.selectDietMealsById(mealId);

        if (existingMeal == null) {
            throw new PulseException("删除的数据不存在");
        }

        int rows = dietMealsMapper.deleteDietMeals(mealId);
        log.info("用户 {} 删除饮食记录 {}，影响行数：{}", userId, mealId, rows);

        return rows > 0;
    }

    @Override
    public DailyNutritionDTO getDailyStats(String startDate,String endDate) {
        Long userId = LoginUserContext.getUserId();
        double totalProtein = 0;
        List<DietWater> dietWaters = dietWaterMapper.selectDietWater(startDate, endDate, userId);
        if (!dietWaters.isEmpty()){
            totalProtein = dietWaters.stream()
                    .mapToDouble(DietWater::getIntakeMl)
                    .sum();
        }
        List<MealDTO> meals = dietMealsMapper.selectDietMeals(startDate, endDate, userId);
        return  calculateDailyNutrition(meals, totalProtein);
    }

    /**
     * 将餐食列表聚合计算为每日营养统计
     *
     * @param mealList 餐食列表
     * @param waterIntake 饮水量（毫升）
     * @return 每日营养统计对象
     */
    public static DailyNutritionDTO calculateDailyNutrition(List<MealDTO> mealList, double waterIntake) {
        DailyNutritionDTO dailyNutrition = new DailyNutritionDTO();

        if (mealList == null || mealList.isEmpty()) {
            dailyNutrition.setWaterIntake(waterIntake);
            return dailyNutrition;
        }

        // 使用 Stream API 聚合计算
        double totalCalories = mealList.stream()
                .mapToDouble(MealDTO::getCalories)
                .sum();

        double totalProtein = mealList.stream()
                .mapToDouble(MealDTO::getProtein)
                .sum();

        double totalCarbs = mealList.stream()
                .mapToDouble(MealDTO::getCarbs)
                .sum();

        double totalFat = mealList.stream()
                .mapToDouble(MealDTO::getFat)
                .sum();

        // 设置结果
        dailyNutrition.setTotalCalories(totalCalories);
        dailyNutrition.setTotalProtein(totalProtein);
        dailyNutrition.setTotalCarbs(totalCarbs);
        dailyNutrition.setTotalFat(totalFat);
        dailyNutrition.setWaterIntake(waterIntake);

        return dailyNutrition;
    }

    @Override
    public boolean addWaterRecord(WaterIntakeDTO waterIntakeDTO) {
        Long userId = LoginUserContext.getUserId();
        DietWater dietWater = new DietWater();
        dietWaterMapper.deleteDietWaterByUserIdAndDate(userId,waterIntakeDTO.getDate());
        dietWater.setUserId(userId);
        DateTime date = DateUtil.parse(waterIntakeDTO.getDate());
        dietWater.setRecordDate(date);
        dietWater.setIntakeMl(waterIntakeDTO.getAmount());
        dietWaterMapper.insertDietWater(dietWater);
        return true;
    }

    /**
     * 验证 MealDTO 必填字段
     */
    private void validateMealDTO(MealDTO dto) {
        if (dto == null) {
            throw new PulseException("饮食记录数据不能为空");
        }
        if (!StringUtils.hasText(dto.getDate())) {
            throw new PulseException("日期不能为空");
        }
        if (!StringUtils.hasText(dto.getTime())) {
            throw new PulseException("时间不能为空");
        }
    }

    /**
     * 将 MealDTO 转换为 DietMeals 实体
     *
     * @param dto    MealDTO 对象
     * @param userId 用户ID
     * @return DietMeals 实体
     */
    private DietMeals toEntity(MealDTO dto, Long userId) {
        if (dto == null) {
            throw new IllegalArgumentException("MealDTO 不能为 null");
        }

        DietMeals entity = new DietMeals();

        // 设置主键ID
        if (StringUtils.hasText(dto.getId())) {
            try {
                entity.setId(Long.parseLong(dto.getId()));
            } catch (NumberFormatException e) {
                log.warn("无效的ID格式: {}", dto.getId(), e);
                throw new PulseException("ID格式不正确");
            }
        }

        // 设置用户ID
        entity.setUserId(userId);

        // 日期转换
        if (StringUtils.hasText(dto.getDate())) {
            try {
                entity.setMealDate(Date.valueOf(LocalDate.parse(dto.getDate())));
            } catch (DateTimeParseException e) {
                log.error("日期格式错误: {}", dto.getDate(), e);
                throw new PulseException("日期格式不正确，请使用 yyyy-MM-dd 格式");
            }
        }

        // 时间转换
        if (StringUtils.hasText(dto.getTime())) {
            try {
                entity.setMealTime(Time.valueOf(LocalTime.parse(dto.getTime())));
            } catch (DateTimeParseException e) {
                log.error("时间格式错误: {}", dto.getTime(), e);
                throw new PulseException("时间格式不正确，请使用 HH:mm:ss 格式");
            }
        }

        // 设置文本备注
        entity.setTextNote(dto.getText());

        // 设置营养数据（防止null值）
        entity.setCaloriesKcal( BigDecimal.valueOf(dto.getCalories()));
        entity.setProteinG(BigDecimal.valueOf(dto.getProtein()));
        entity.setCarbsG( BigDecimal.valueOf(dto.getCarbs()));
        entity.setFatG(BigDecimal.valueOf(dto.getFat()));

        // 设置时间戳
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (entity.getId() == null) {
            entity.setCreatedTime(now);
        }
        entity.setUpdatedTime(now);

        return entity;
    }


    private DietMeals getDaily(String message){
        AiBusiness aiBusiness = chatbotService.getAiBusiness(BusinessCode.DAILY_CALORIES_RECOMMENDED);
        AiModel aiModel = aiModelService.getById(aiBusiness.getAiModelId());
        List<Message> messages = Collections.singletonList(Message.user(message));
        AiRequest aiRequest = AiRequest.builder()
                .modelCode(aiModel.getModelCode())
                .systemPrompt(Objects.toString(aiBusiness.getSystem(), null)) // null 安全
                .messages(messages)
                .enableTemplate(true)
                .build();
        try {
            AiResponse resp = aiClient.chat(aiRequest);
            String content = (resp != null) ? resp.getContent() : null;
            content = cleanCodeBlock(content);
            DietMeals dietMeals = JacksonUtils.fromJson(content, DietMeals.class);
            return dietMeals;
        } catch (Exception e) {
            log.error("获取首页信息失败",e);
            throw new PulseException("获取首页信息失败");

        }
    }

    /**
     * 去掉 Markdown 代码块标记（```lang 和 ```），返回中间的纯内容
     *
     * @param input 原始字符串
     * @return 清理后的内容
     */
    public static String cleanCodeBlock(String input) {
        if (input == null) {
            return null;
        }
        // 去掉开头 ```xxx（可能有语言名和换行）
        String cleaned = input.replaceFirst("^```[a-zA-Z0-9]*\\s*", "");
        // 去掉结尾 ```
        cleaned = cleaned.replaceFirst("\\s*```$", "");
        return cleaned;
    }

}
