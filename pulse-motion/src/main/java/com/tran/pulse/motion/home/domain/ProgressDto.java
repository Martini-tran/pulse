package com.tran.pulse.motion.home.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDto {

    /**
     * 今日状态
     */
    private  String status;

    /**
     * ai总结信息
     */
    private  String message;

    /**
     *健康目标
     */
    private String healthGoal;

}
