package com.tran.pulse.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限校验配置
 *
 * @author tran
 */
@ConfigurationProperties(prefix = "pulse.auth")
public class AuthProperties {


    /**
     * JWT 密钥
     */
    private String secret;

    /**
     * Token 过期时间（秒）
     */
    private int expireSeconds = 1800;

    /**
     * App 第二个 Token 过期时间（秒）
     */
    private int appExpireSeconds = 7776000;

    /**
     * 白名单路径列表
     */
    private List<String> whiteList = new ArrayList<>();

    // getters and setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public List<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }

    public int getAppExpireSeconds() {
        return appExpireSeconds;
    }

    public void setAppExpireSeconds(int appExpireSeconds) {
        this.appExpireSeconds = appExpireSeconds;
    }
}
