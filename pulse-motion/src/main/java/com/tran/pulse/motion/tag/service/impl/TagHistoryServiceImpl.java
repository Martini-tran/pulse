package com.tran.pulse.motion.tag.service.impl;

import com.tran.pulse.common.constants.TagCode;
import com.tran.pulse.common.domain.entity.TagHistory;
import com.tran.pulse.motion.tag.mapper.TagHistoryMapper;
import com.tran.pulse.motion.tag.service.TagHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签历史记录 Service 实现类
 * @author tran
 * @version 1.0.0.0
 * @date 2025/8/27 16:11
 */
@Service
public class TagHistoryServiceImpl implements TagHistoryService {

    @Autowired
    private TagHistoryMapper tagHistoryMapper;


    @Override
    @Transactional
    public int insert(TagHistory tagHistory) {
        if (tagHistory == null) {
            return 0;
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        if (tagHistory.getCreatedTime() == null) {
            tagHistory.setCreatedTime(now);
        }
        if (tagHistory.getUpdatedTime() == null) {
            tagHistory.setUpdatedTime(now);
        }

        
        return tagHistoryMapper.insert(tagHistory);
    }

    @Override
    @Transactional
    public int update(TagHistory tagHistory) {
        if (tagHistory == null) {
            return 0;
        }

        // 设置更新时间
        tagHistory.setUpdatedTime(LocalDateTime.now());

        return tagHistoryMapper.update(tagHistory);
    }

    @Override
    public int delete(Long tagHistoryId) {
        return tagHistoryMapper.delete(tagHistoryId);
    }


    @Override
    public List<TagHistory> getHistory(TagHistory tagHistory) {
        return tagHistoryMapper.getTagHistory(tagHistory);
    }

    @Override
    public List<TagHistory> getHistoryWeight(String startTime, String endTime,String userId) {
        return tagHistoryMapper.getHistoryByDateAndCode( startTime,  endTime, TagCode.WEIGHT, userId);
    }


}