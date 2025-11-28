package com.tran.pulse.motion.tag.domian.dto;

import com.tran.pulse.common.domain.entity.TagDefinition;

import java.util.List;

/**
 * 分类DTO
 */
public class TagCategoryDto {

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 标签多个
     */
    private List<TagDefinition> tagDefinition;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<TagDefinition> getTagDefinition() {
        return tagDefinition;
    }

    public void setTagDefinition(List<TagDefinition> tagDefinition) {
        this.tagDefinition = tagDefinition;
    }
}
