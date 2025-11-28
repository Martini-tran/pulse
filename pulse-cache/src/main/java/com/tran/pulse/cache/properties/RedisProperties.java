package com.tran.pulse.cache.properties;

/**
 * redis 配置
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/6/20 14:19
 **/
public class RedisProperties {

    /**
     * host
     */
    private String host;

    /**
     * port
     */
    private int port;

    /**
     * password
     */
    private String password;

    /**
     * database
     */
    private int database;

    /**
     * time
     */
    private int timeoutSeconds = 30;


    /**
     * 默认过期时间
     */
    private long defaultTtlSeconds = -1;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }


    public long getDefaultTtlSeconds() {
        return defaultTtlSeconds;
    }

    public void setDefaultTtlSeconds(long defaultTtlSeconds) {
        this.defaultTtlSeconds = defaultTtlSeconds;
    }
}
