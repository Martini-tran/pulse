package com.tran.pulse.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh Token信息类
 * 用于在缓存中存储与Refresh Token相关的信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public  class RefreshTokenInfo {

    private String loginIp;

    private String username;

}