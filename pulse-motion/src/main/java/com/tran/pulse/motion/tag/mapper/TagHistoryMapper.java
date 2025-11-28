package com.tran.pulse.motion.tag.mapper;

import com.tran.pulse.common.domain.entity.TagHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签历史记录 Mapper
 * @author tran
 * @version 1.0.0.0
 * @date 2025/8/27 16:11
 **/
@Mapper
public interface TagHistoryMapper {

    /**
     * 根据用户ID和时间范围查询历史记录
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 标签历史记录列表
     */
    List<TagHistory> getHistoryByUserIdAndTimeRange(@Param("userId") Long userId,
                                                   @Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);



    /**
     * 批量插入标签历史记录
     * @param tagHistories 标签历史记录列表
     * @return 影响的行数
     */
    int batchInsert(@Param("tagHistories") List<TagHistory> tagHistories);

    /**
     * 插入单条标签历史记录
     * @param tagHistory 标签历史记录
     * @return 影响的行数
     */
    int insert(TagHistory tagHistory);


    /**
     * 更新单条标签历史记录
     * @param tagHistory 标签历史记录
     * @return 影响的行数
     */
    int update(TagHistory tagHistory);


    /**
     *
     * @return
     */
    List<TagHistory> getTagHistory(TagHistory tagHistory);

    /**
     * 根据Id删除
     * @param id
     * @return
     */
    int delete(@Param("id")Long id);

    /**
     * 获取历史体重信息
     * @param startTime
     * @param endTime
     * @return
     */
    List<TagHistory> getHistoryByDateAndCode(@Param("startTime") String startTime, @Param("endTime")String endTime, @Param("tagCode")String tagCode, @Param("userId")String userId);
}
