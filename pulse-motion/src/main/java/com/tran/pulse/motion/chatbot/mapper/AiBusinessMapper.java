package com.tran.pulse.motion.chatbot.mapper;


import com.tran.pulse.common.domain.entity.AiBusiness;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Mapper 接口
 * 对应表：ai_business
 */
@Mapper
public interface AiBusinessMapper {

    /**
     * 根据主键查询
     * @param businessId 业务主键ID
     * @return AiBusiness
     */
    AiBusiness selectById(String businessId);

    /**
     * 查询全部记录
     * @return List<AiBusiness>
     */
    List<AiBusiness> selectAll();

    /**
     * 新增记录
     * @param entity AiBusiness 实体
     * @return 影响行数
     */
    int insert(AiBusiness entity);

    /**
     * 根据主键更新
     * @param entity AiBusiness 实体
     * @return 影响行数
     */
    int updateById(AiBusiness entity);

    /**
     * 根据主键删除
     * @param businessId 业务主键ID
     * @return 影响行数
     */
    int deleteById(String businessId);
}

