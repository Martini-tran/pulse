package com.tran.pulse.motion.diet.mapper;

import com.tran.pulse.common.domain.entity.DietMeals;
import com.tran.pulse.motion.diet.domain.dto.MealDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/10/15 15:55
 **/
public interface DietMealsMapper {

    /**
     * 查询近期时间内的餐饮记录
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param userId    用户ID
     * @return 餐饮记录列表
     */
    public List<MealDTO> selectDietMeals(@Param("startDate") String startDate, @Param("endDate")String endDate, @Param("userId")Long userId);

    /**
     * 添加饮食
     *
     * @param mealDTO 饮食对象
     * @return 插入的记录数
     */
    public int addDietMeals(DietMeals mealDTO);

    /**
     * 修改饮食
     *
     * @param mealDTO 饮食对象
     * @return 更新的记录数
     */
    public int updateDietMeals(DietMeals mealDTO);

    /**
     * 删除饮食
     *
     * @param mealId 饮食记录ID
     * @return 删除的记录数
     */
    public int deleteDietMeals(@Param("mealId")String mealId);

    /**
     * 根据id查询
     *
     * @param mealId 饮食记录ID
     * @return 饮食对象
     */
    public DietMeals selectDietMealsById(@Param("mealId")String mealId);
}
