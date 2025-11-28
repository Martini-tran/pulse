package com.tran.pulse.motion.metrics.service;

import java.util.List;
import java.util.Map;

/**
 * 用户指标数据
 */
public interface UserMetrics {


    /**
     * 近期体重数据
     *
     * @return
     */
    Map<String,Object> getUserRecentWeights(Long userId, int hisDay);


    /**
     * 获取用户基本信息
     * @param userId
     * @return
     */
    Map<String,Object> getUserInfo(Long userId);



}
