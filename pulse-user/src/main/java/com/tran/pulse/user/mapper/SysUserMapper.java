package com.tran.pulse.user.mapper;

import com.tran.pulse.common.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ...
 *
 * @version 1.0.0.0
 * @date 2025/3/27 14:02
 * @Author tran
 **/
@Mapper
public interface SysUserMapper {

    /**
     *  根据用户名称查询用户
     *
     * @param username
     * @return
     */
    SysUser selectByUsername(@Param("username") String username);


    /**
     *  根据email名称查询用户
     *
     * @param email
     * @return
     */
    SysUser selectByUserEmail(@Param("email") String email);


    /**
     *  根据用户名称或者邮箱查询用户
     *
     * @param user
     * @return
     */
    SysUser selectByUsernameOrEmail(@Param("username") String user,@Param("email") String email);


    /**
     *  插入用户
     *
     * @param user
     * @return
     */
    int insertUserSelective(SysUser user);


    /**
     *  更新用户状态用户
     *
     */
    int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);


}
