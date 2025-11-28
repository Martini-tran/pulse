package com.tran.pulse.motion.tag.mapper;


import com.tran.pulse.motion.tag.domian.dto.TagDto;
import com.tran.pulse.common.domain.entity.TagUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户标签Mapper
 */
@Mapper
public interface TagUserMapper {

    /**
     * 插入多个标签
     * @param userId 用户ID
     * @param tagDtos 标签列表
     * @return 影响的行数
     */
    int addUserTags(@Param("userId") Long userId, @Param("tagDtos") List<TagDto> tagDtos);

    /**
     * 获取用户的所有标签
     * @param userId 用户ID
     * @return 标签列表
     */
    List<TagUser> getUserTags(@Param("userId") Long userId);

    /**
     * 删除用户标签
     * @param userId 用户ID
     * @param tagCode 标签代码
     * @return 影响的行数
     */
    int deleteUserTag(@Param("userId") Long userId, @Param("tagCode") String tagCode);

    /**
     * 删除用户的所有标签
     * @param userId 用户ID
     * @return 影响的行数
     */
    int deleteAllUserTags(@Param("userId") Long userId);

    /**
     * 更新用户标签值
     * @param userId 用户ID
     * @param tagCode 标签代码
     * @param tagValue 新的标签值
     * @return 影响的行数
     */
    int updateUserTagValue(@Param("userId") Long userId, @Param("tagCode") String tagCode, @Param("tagValue") String tagValue);

    /**
     * 插入多个简单标签没有值的标签
     * @param userId 用户ID
     * @param tagDtos 标签列表
     * @return 影响的行数
     */
    int addUserSimpleTags(@Param("userId") Long userId, @Param("tagDtos") List<String> tagDtos);

    /**
     * 根据标签代码查询所有用户标签
     * @param tagCode 标签代码
     * @return 标签列表
     */
    List<TagUser> getUserTagsByTagCode(@Param("tagCode") String tagCode);

    /**
     * 根据用户id和标签code查询
     *
     * @param tagCode
     * @param userId
     * @return
     */
    TagUser getOneByTagCodeAndUserId(@Param("tagCode") String tagCode,
                                     @Param("userId")  Long userId);


}
