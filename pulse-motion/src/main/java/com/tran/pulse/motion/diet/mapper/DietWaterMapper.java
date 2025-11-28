package com.tran.pulse.motion.diet.mapper;

import com.tran.pulse.common.domain.entity.DietWater;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper接口：饮水记录数据访问层
 *
 * 提供对 diet_water 表的常用数据库操作方法，包括查询、插入、更新和删除。
 *
 * @author tran
 * @version 1.0.0
 * @since 2025-10-16
 */
public interface DietWaterMapper {

    /**
     * 查询指定用户在时间范围内的饮水记录
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate   结束日期（格式：yyyy-MM-dd）
     * @param userId    用户ID
     * @return 饮水记录列表
     */
    List<DietWater> selectDietWater(@Param("startDate") String startDate,
                                    @Param("endDate") String endDate,
                                    @Param("userId") Long userId);

    /**
     * 插入新的饮水记录
     *
     * @param dietWater 饮水记录实体
     * @return 影响的行数（1 表示成功，0 表示失败）
     */
    int insertDietWater(DietWater dietWater);

    /**
     * 更新饮水记录
     *
     * @param dietWater 饮水记录实体
     * @return 影响的行数（1 表示成功，0 表示失败）
     */
    int updateDietWater(DietWater dietWater);

    /**
     * 根据主键ID删除饮水记录
     *
     * @param dietWaterId 饮水记录ID
     * @return 影响的行数（1 表示成功，0 表示失败）
     */
    int deleteDietWater(@Param("dietWaterId") Long dietWaterId);

    /**
     * 根据主键ID删除饮水记录
     *
     */
    int deleteDietWaterByUserIdAndDate(@Param("userId") Long userId,@Param("recordDate") String recordDate);

    /**
     * 根据用户ID查询饮水记录
     *
     * @param userId 用户ID
     * @return 饮水记录列表
     */
    List<DietWater> selectDietWaterByUserId(@Param("userId") Long userId);
}
