package com.tran.pulse.motion.tag.scheduler;

import com.tran.pulse.common.constants.TagCode;
import com.tran.pulse.common.domain.entity.TagHistory;
import com.tran.pulse.common.domain.entity.TagUser;
import com.tran.pulse.motion.tag.mapper.TagHistoryMapper;
import com.tran.pulse.motion.tag.mapper.TagUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 体重历史记录定时任务
 * 每天凌晨1点执行，将当前体重记录保存到历史表中
 */
@Component
public class WeightHistoryScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(WeightHistoryScheduler.class);
    

    @Autowired
    private TagUserMapper tagUserMapper;

    @Autowired
    private TagHistoryMapper tagHistoryMapper;

    /**
     * 每天凌晨1点执行
     * 将tag_user表中tag_code为current_weight的记录保存到tag_history表中
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void recordWeightHistory() {
        try {
            logger.info("开始执行体重历史记录定时任务");
            
            // 获取当前体重记录
            List<TagUser> currentWeightList = tagUserMapper.getUserTagsByTagCode(TagCode.CURRENT_WEIGHT);
            
            if (currentWeightList == null || currentWeightList.isEmpty()) {
                logger.info("没有找到current_weight标签记录，跳过本次执行");
                return;
            }
            
            // 获取前一天日期作为记录时间
            LocalDateTime recordTime = LocalDateTime.now().minusDays(1);
            LocalDateTime currentTime = LocalDateTime.now();
            
            List<TagHistory> tagHistoryList = new ArrayList<>();
            for (TagUser tagUser : currentWeightList) {
                TagHistory tagHistory = new TagHistory();
                tagHistory.setTagCode(TagCode.WEIGHT);
                tagHistory.setUserId(tagUser.getUserId());
                tagHistory.setTagValue(tagUser.getTagValue());
                tagHistory.setRecordTime(recordTime.toString());
                tagHistory.setCreatedTime(currentTime);
                tagHistory.setUpdatedTime(currentTime);
                
                tagHistoryList.add(tagHistory);
            }
            
            // 批量插入历史记录
            int insertCount = tagHistoryMapper.batchInsert(tagHistoryList);
            logger.info("体重历史记录定时任务执行完成，共插入{}条记录", insertCount);
            
        } catch (Exception e) {
            logger.error("执行体重历史记录定时任务时发生异常", e);
        }
    }
}