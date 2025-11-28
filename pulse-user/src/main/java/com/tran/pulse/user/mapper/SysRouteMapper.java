package com.tran.pulse.user.mapper;


import com.tran.pulse.common.domain.entity.SysRole;
import com.tran.pulse.common.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRouteMapper {

    /**
     * 查询指定用户拥有的所有路由信息
     *
     * @param userId 用户ID
     * @return 路由列表
     */
    List<SysRole> selectRoutesByUserId(@Param("userId") Long userId);


    /**
     * 关联用户与角色
     *
     * @param sysUserRole
     * @return
     */
    int insertUserRoleSelective(SysUserRole sysUserRole);
}
