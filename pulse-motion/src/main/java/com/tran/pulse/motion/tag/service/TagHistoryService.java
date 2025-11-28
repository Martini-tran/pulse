package com.tran.pulse.motion.tag.service;

import com.tran.pulse.common.domain.entity.TagHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签历史记录 Service
 * @author tran
 * @version 1.0.0.0
 * @date 2025/8/27 16:11
 */
public interface TagHistoryService {

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
     * 删除标签
     * @param tagHistoryId 标签历史记录Id
     * @return 影响的行数
     */
    int delete(Long tagHistoryId);



    /**
     * 获取历史标签
     * @return 标签历史记录列表
     */
    List<TagHistory> getHistory(TagHistory tagHistory);


    /**
     * 获取历史体重
     * @return 获取历史对话
     */
    List<TagHistory> getHistoryWeight(String startTime, String endTime,String userId);

}