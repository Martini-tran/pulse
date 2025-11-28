package com.tran.pulse.motion.tag.controller;

import com.tran.pulse.common.domain.model.PulseResult;
import com.tran.pulse.common.exception.PulseException;
import com.tran.pulse.common.util.StringUtils;
import com.tran.pulse.motion.tag.domian.dto.TagUserDto;
import com.tran.pulse.motion.tag.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标签Controller
 */
@RestController
@RequestMapping("/motion/v1/tag")
public class TagController {

    @Autowired
    public TagService tagService;

    /**
     * 批量添加用户标签
     *
     * @param tagUserDto 引导页基础标签
     * @return 是否保存成功
     */
    @PostMapping("/baseUser")
    public PulseResult baseUser(@RequestBody TagUserDto tagUserDto) {
        if (StringUtils.isBlank(tagUserDto.getGender())){
            throw new PulseException("请选择性别");
        }
        if (tagUserDto.getBirthDate() == null){
            throw new PulseException("请选择出生日期");
        }
        if (tagUserDto.getHeight() == null){
            throw new PulseException("请输入身高");
        }
        if (tagUserDto.getTargetWeight() == null){
            throw new PulseException("请输入当前体重");
        }

        if (tagUserDto.getCurrentWeight() == null){
            throw new PulseException("请输入目标体重");
        }
        tagService.addUserTags(tagUserDto);
        return PulseResult.success();
    }


}