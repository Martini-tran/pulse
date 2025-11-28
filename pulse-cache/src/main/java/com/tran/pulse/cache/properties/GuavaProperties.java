package com.tran.pulse.cache.properties;

/**
 * guavaProperties
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/6/20 14:20
 **/
public class GuavaProperties {

    /**
     * 默认过期时间（秒）
     */
    private long defaultTtlSeconds = -1;


    /**
     * 缓存最大容量
     */
    private long maximumSize = 2000;

    /**
     * 调度线程池大小
     */
    private int schedulerPoolSize = 5;


    public long getDefaultTtlSeconds() {
        return defaultTtlSeconds;
    }

    public void setDefaultTtlSeconds(long defaultTtlSeconds) {
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public int getSchedulerPoolSize() {
        return schedulerPoolSize;
    }

    public void setSchedulerPoolSize(int schedulerPoolSize) {
        this.schedulerPoolSize = schedulerPoolSize;
    }
}
