package com.tran.pulse.motion.home.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐信息实体类
 */
@Data                   // 自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor      // 生成无参构造函数
@AllArgsConstructor     // 生成全参构造函数
public class RecommendationDto {

    /**
     * 标题
     */
    private String title;

    /**
     * 子标题
     */
    private String subtitle;

    /**
     * 颜色
     */
    private String color;


}

