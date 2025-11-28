package com.tran.pulse.user.mapper;

import com.tran.pulse.common.domain.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/7/3 11:04
 **/
@Mapper
public interface SysPermissionMapper {


    /**
     * 查询指定用户拥有的所有路由信息
     *
     * @param userId 用户ID
     * @return 路由列表
     */
    List<SysPermission> getPermissionsByUserId(@Param("userId") Long userId);

}
