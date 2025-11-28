package com.tran.pulse.motion.tag.service.impl;

import com.tran.pulse.auth.context.LoginUserContext;
import com.tran.pulse.common.constants.Constants;
import com.tran.pulse.common.constants.TagCode;
import com.tran.pulse.common.domain.entity.TagUser;
import com.tran.pulse.motion.tag.domian.dto.TagDto;
import com.tran.pulse.motion.tag.domian.dto.TagUserDto;
import com.tran.pulse.motion.tag.mapper.TagMapper;
import com.tran.pulse.motion.tag.mapper.TagUserMapper;
import com.tran.pulse.motion.tag.service.TagService;
import com.tran.pulse.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 生成标签内容
 */
@Service
public class TagServiceImpl implements TagService {

    private static final Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);

    @Autowired
    private TagUserMapper tagUserMapper;

    @Autowired
    private UserService userService;


    @Override
    @Transactional
    public void addUserTags(TagUserDto tagUserDto) {
        Long userId = LoginUserContext.getUserId();
        List<TagDto> tgsInfo = new ArrayList<>();
        tgsInfo.add(new TagDto(TagCode.CURRENT_WEIGHT,tagUserDto.getCurrentWeight().toString()));
        tgsInfo.add(new TagDto(TagCode.BIRTH_DATE,tagUserDto.getBirthDate().toString()));
        tgsInfo.add(new TagDto(TagCode.GENDER,tagUserDto.getGender()));
        tgsInfo.add(new TagDto(TagCode.HEIGHT,tagUserDto.getHeight().toString()));
        tgsInfo.add(new TagDto(TagCode.TARGET_WEIGHT,tagUserDto.getTargetWeight().toString()));
        tagUserMapper.addUserTags(userId,tgsInfo);
        userService.updateUserStatus(userId, Constants.STATUS_ENABLED_2000);
        logger.info("用户: {},完成引导页面", userId);
    }

    @Override
    public List<TagUser> getUserTags(Long userId) {
        return tagUserMapper.getUserTags(userId);
    }
}