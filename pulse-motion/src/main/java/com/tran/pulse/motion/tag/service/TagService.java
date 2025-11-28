package com.tran.pulse.motion.tag.service;

import com.tran.pulse.common.domain.entity.TagUser;
import com.tran.pulse.motion.tag.domian.dto.TagUserDto;

import java.util.List;

/**
 * 标签service
 */
public interface TagService {

    /**
     * 添加用户基本信息
     */
    void addUserTags(TagUserDto tagUserDto);


    /**
     * 获取用户tags
     *
     * @param userId
     * @return
     */
    List<TagUser> getUserTags(Long userId);
}