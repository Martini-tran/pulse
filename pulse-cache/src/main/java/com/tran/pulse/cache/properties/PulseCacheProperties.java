package com.tran.pulse.cache.properties;

import com.tran.pulse.cache.constants.CacheMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存配置
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/6/20 14:11
 **/
@ConfigurationProperties(prefix = "pulse.cache")
public class PulseCacheProperties {


    /**
     * 缓存模式
     */
    private CacheMode mode = CacheMode.GUAVA;


    /**
     * 缓存命名空间
     */
    private String namespace = "pules";

    /**
     * redis缓存配置
     */
    private RedisProperties redis = new RedisProperties();

    /**
     * Guava缓存配置
     */
    private GuavaProperties guava = new GuavaProperties();


    public RedisProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisProperties redis) {
        this.redis = redis;
    }

    public GuavaProperties getGuava() {
        return guava;
    }

    public void setGuava(GuavaProperties guava) {
        this.guava = guava;
    }

    public CacheMode getMode() {
        return mode;
    }

    public void setMode(CacheMode mode) {
        this.mode = mode;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
