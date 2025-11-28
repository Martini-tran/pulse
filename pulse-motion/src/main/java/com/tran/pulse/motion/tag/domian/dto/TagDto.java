package com.tran.pulse.motion.tag.domian.dto;

import lombok.Data;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/8/11 10:20
 **/
@Data
public class TagDto {

    /**
     * 标签Code
     */
    private String tagCode;

    /**
     * 标签值
     */
    private String tagValue;

    public TagDto() {
    }

    public TagDto(String tagCode, String tagValue) {
        this.tagCode = tagCode;
        this.tagValue = tagValue;
    }
}
