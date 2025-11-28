package com.tran.pulse.motion.tag.mapper;

import com.tran.pulse.common.domain.entity.TagDefinition;
import com.tran.pulse.motion.tag.domian.dto.TagCategoryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 标签mapper
 */
@Mapper
public interface TagMapper {


    /**
     * 根据标标签分类查询标签
     *
     * @return TagDefinition
     */
    public List<TagDefinition> getTagsByCategoryCode(String categoryCode);


    /**
     * 获取标签内容
     *
     * @return TagDefinition
     */
    public List<TagCategoryDto> getTagCategoryDtoByCategoryCodes(List<String> categoryCodes);



}
