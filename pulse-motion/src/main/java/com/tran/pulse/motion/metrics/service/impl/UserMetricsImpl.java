package com.tran.pulse.motion.metrics.service.impl;

import com.tran.pulse.common.domain.entity.TagUser;
import com.tran.pulse.motion.metrics.service.UserMetrics;
import com.tran.pulse.motion.tag.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserMetricsImpl implements UserMetrics {

    @Autowired
    private TagService tagService;

    @Override
    public Map<String, Object> getUserRecentWeights(Long userId, int hisDay) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<TagUser> userTags = tagService.getUserTags(userId);
        for (TagUser userTag : userTags) {
            resultMap.put(userTag.getTagCode(), userTag.getTagValue());
        }
        return resultMap;
    }
}
