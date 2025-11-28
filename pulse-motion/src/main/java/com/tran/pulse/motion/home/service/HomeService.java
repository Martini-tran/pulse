package com.tran.pulse.motion.home.service;

import com.tran.pulse.motion.home.domain.RecommendationDto;
import com.tran.pulse.motion.home.domain.ProgressDto;

import java.util.List;

/**
 * 获取首页数据
 */
public interface HomeService {


    /**
     * 获取今日建议
     *
     * @return
     */
    List<RecommendationDto> getRecommendation();


    /**
     * 获取今日进度总结
     * @return
     */
    public ProgressDto getProgress();

}
