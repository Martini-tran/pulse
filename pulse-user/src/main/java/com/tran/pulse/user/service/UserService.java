package com.tran.pulse.user.service;


import com.tran.pulse.common.constants.Constants;
import com.tran.pulse.common.domain.entity.SysUser;
import com.tran.pulse.user.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private SysUserMapper sysUserMapper;


    /**
     * 根据用户名称查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    public SysUser getUserByUsername(String username) {
       return sysUserMapper.selectByUsername(username);
    }

    /**
     * 根据邮箱查询用户
     *
     * @param email 用户邮箱
     * @return 用户信息
     */
    public SysUser getUserByEmail(String email) {
        return sysUserMapper.selectByUserEmail(email);
    }

    /**
     * 根据用户名或邮箱查询用户
     *
     * @param username 用户名
     * @param email 邮箱
     * @return 用户信息
     */
    public SysUser getUserByUsernameOrEmail(String username, String email) {
        return sysUserMapper.selectByUsernameOrEmail(username, email);
    }

    /**
     * 创建新用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    public int createUser(SysUser user) {
        return sysUserMapper.insertUserSelective(user);
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态码（1000-启用，0000-禁用）
     * @return 影响行数
     */
    public int updateUserStatus(Long userId, String status) {
        return sysUserMapper.updateUserStatus(userId, status);
    }

    /**
     * 启用用户
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    public int enableUser(Long userId) {
        return updateUserStatus(userId, Constants.STATUS_ENABLED);
    }

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    public int disableUser(Long userId) {
        return updateUserStatus(userId, Constants.STATUS_DISABLED);
    }
}
