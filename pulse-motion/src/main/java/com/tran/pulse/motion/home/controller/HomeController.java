package com.tran.pulse.motion.home.controller;

import com.tran.pulse.common.domain.model.PulseResult;
import com.tran.pulse.motion.home.domain.ProgressDto;
import com.tran.pulse.motion.home.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页接口定义
 * @author tran
 * @version 1.0.0.0
 * @date 2025/9/24 16:46
 **/
@RestController
@RequestMapping("/app/v1/home/")
public class HomeController {

    @Autowired
    private HomeService homeService;

    /**
     * 获取今日建议
     * @return
     */
    @GetMapping("/recommendations")
    public PulseResult getRecommendation() {
        return PulseResult.success(homeService.getRecommendation());
    }

    /**
     * 获取今日总结
     * @return
     */
    @GetMapping("/progress")
    public PulseResult getProgress() {
        return PulseResult.success(homeService.getProgress());
    }


}
