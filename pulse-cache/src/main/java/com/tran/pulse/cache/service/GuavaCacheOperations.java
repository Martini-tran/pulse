package com.tran.pulse.cache.service;

import com.tran.pulse.cache.constants.CacheMode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.tran.pulse.cache.properties.GuavaProperties;
import com.tran.pulse.cache.properties.PulseCacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Guava 缓存实现，缓存参数可通过 Spring Boot 配置文件动态调整
 * 支持类型安全的缓存操作
 * @author tran
 */
public class GuavaCacheOperations implements CacheOperations {

    private static final Logger logger = LoggerFactory.getLogger(GuavaCacheOperations.class);

    /**
     * 缓存命名空间前缀（含末尾冒号）
     */
    private final String namespace;

    /**
     * Guava缓存实例
     */
    private final Cache<String, Object> cache;

    /**
     * 调度器，用于自定义过期时间
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 构造函数注入可配置参数，并初始化缓存和调度器
     *
     * @param pulseCacheProperties 缓存配置
     */
    public GuavaCacheOperations(PulseCacheProperties pulseCacheProperties) {
        String namespace = pulseCacheProperties.getNamespace();
        GuavaProperties guavaProperties = pulseCacheProperties.getGuava();

        // 统一处理 namespace 末尾冒号
        if (namespace != null && !namespace.isEmpty()) {
            this.namespace = namespace.endsWith(":") ? namespace : namespace + ":";
        } else {
            this.namespace = "";
        }

        // 默认过期时间（秒），<=0 则不设置全局过期
        long defaultTtlSeconds = guavaProperties.getDefaultTtlSeconds();
        // 缓存最大容量
        long maximumSize = guavaProperties.getMaximumSize();
        // 调度线程池大小
        int schedulerPoolSize = guavaProperties.getSchedulerPoolSize();

        // 根据配置构建 CacheBuilder
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder()
                .maximumSize(maximumSize);
        if (defaultTtlSeconds > 0) {
            builder.expireAfterWrite(defaultTtlSeconds, TimeUnit.SECONDS);
        }
        this.cache = builder.build();

        this.scheduler = Executors.newScheduledThreadPool(schedulerPoolSize);
    }

    /**
     * 构建带命名空间的完整缓存键
     */
    private String buildKey(String key) {
        return namespace + key;
    }

    @Override
    public CacheMode getCacheMode() {
        return CacheMode.GUAVA;
    }

    /**
     * 获取缓存对象，如果不存在则返回 null
     * 注意：返回类型可能需要强制转换，建议使用类型安全的get方法
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) cache.getIfPresent(buildKey(key));
    }

    /**
     * 根据指定类型获取缓存值（类型安全）
     * 推荐使用此方法替代无类型参数的get方法
     *
     * @param key 缓存键
     * @param clazz 目标类型
     * @return 缓存值，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, Class<T> clazz) {
        Object value = cache.getIfPresent(buildKey(key));
        if (value == null) {
            return null;
        }
        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            logger.warn("缓存值类型不匹配，期望类型: {}, 实际类型: {}", clazz.getName(), value.getClass().getName());
            return null;
        }
    }

    /**
     * 根据TypeReference获取缓存值（用于泛型类型）
     * 适用于List<User>、Map<String, Object>等复杂泛型类型
     *
     * 注意：Guava缓存是内存缓存，此方法主要为了接口一致性
     * 实际类型检查在运行时进行，建议明确知道缓存值类型时使用
     *
     * @param key 缓存键
     * @param typeReference 类型引用
     * @return 缓存值，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, TypeReference<T> typeReference) {
        // 对于Guava缓存，TypeReference主要用于接口一致性
        // 实际的类型检查在运行时进行
        Object value = cache.getIfPresent(buildKey(key));
        if (value == null) {
            return null;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            logger.warn("缓存值类型不匹配，TypeReference: {}", typeReference.getType());
            return null;
        }
    }

    /**
     * 获取缓存；缓存未命中时调用 loader 加载并写入缓存
     * 注意：返回类型可能需要强制转换，建议使用类型安全的get方法
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, Callable<? extends T> loader) throws Exception {
        String realKey = buildKey(key);
        // 先尝试获取
        T existing = (T) cache.getIfPresent(realKey);
        if (existing != null) {
            return existing;
        }
        // 缓存未命中，调用 loader
        T loaded = loader.call();
        if (loaded != null) {
            cache.put(realKey, loaded);
        }
        return loaded;
    }

    /**
     * 带类型信息的get方法（推荐使用）
     * 缓存未命中时用 loader 加载并写入缓存，提供类型安全保障
     *
     * @param key 缓存键
     * @param clazz 目标类型
     * @param loader 加载器，用于缓存未命中时加载数据
     * @return 缓存值
     * @throws Exception 加载过程中的异常
     */
    @Override
    public <T> T get(String key, Class<T> clazz, Callable<? extends T> loader) throws Exception {
        String realKey = buildKey(key);
        // 先尝试获取
        Object existing = cache.getIfPresent(realKey);
        if (existing != null) {
            try {
                return clazz.cast(existing);
            } catch (ClassCastException e) {
                logger.warn("缓存值类型不匹配，将重新加载。期望类型: {}, 实际类型: {}",
                        clazz.getName(), existing.getClass().getName());
                // 类型不匹配，删除错误的缓存
                cache.invalidate(realKey);
            }
        }
        // 缓存未命中或类型不匹配，调用 loader
        T loaded = loader.call();
        if (loaded != null) {
            cache.put(realKey, loaded);
        }
        return loaded;
    }

    /**
     * 带TypeReference的get方法（用于复杂泛型类型）
     * 缓存未命中时用 loader 加载并写入缓存
     *
     * @param key 缓存键
     * @param typeReference 类型引用
     * @param loader 加载器，用于缓存未命中时加载数据
     * @return 缓存值
     * @throws Exception 加载过程中的异常
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, TypeReference<T> typeReference, Callable<? extends T> loader) throws Exception {
        String realKey = buildKey(key);
        // 先尝试获取
        Object existing = cache.getIfPresent(realKey);
        if (existing != null) {
            try {
                return (T) existing;
            } catch (ClassCastException e) {
                logger.warn("缓存值类型不匹配，将重新加载。TypeReference: {}", typeReference.getType());
                // 类型不匹配，删除错误的缓存
                cache.invalidate(realKey);
            }
        }
        // 缓存未命中或类型不匹配，调用 loader
        T loaded = loader.call();
        if (loaded != null) {
            cache.put(realKey, loaded);
        }
        return loaded;
    }

    /**
     * 写入或更新缓存，使用默认过期时间或无过期
     */
    @Override
    public <T> void put(String key, T value) {
        cache.put(buildKey(key), value);
    }

    /**
     * 写入或更新缓存，并在指定时间后过期
     */
    @Override
    public <T> void put(String key, T value, long timeout, TimeUnit unit) {
        String realKey = buildKey(key);
        cache.put(realKey, value);
        scheduler.schedule(() -> cache.invalidate(realKey), timeout, unit);
    }

    /**
     * 写入或更新缓存，并自定义过期时间（秒）
     */
    @Override
    public <T> void put(String key, T value, long ttlSeconds) {
        put(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * 删除指定缓存，返回删除前是否存在
     */
    @Override
    public boolean delete(String key) {
        String realKey = buildKey(key);
        boolean existed = cache.getIfPresent(realKey) != null;
        cache.invalidate(realKey);
        return existed;
    }

    /**
     * 批量删除缓存
     */
    @Override
    public void deleteAll(Collection<? extends String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        cache.invalidateAll(
                keys.stream()
                        .map(this::buildKey)
                        .collect(Collectors.toList())
        );
    }

    /**
     * 清空当前命名空间下所有缓存
     */
    @Override
    public void clear() {
        cache.invalidateAll();
    }

    /**
     * 延长指定 key 的过期时间（秒），通过调度任务实现
     */
    @Override
    public boolean expire(String key, long ttlSeconds) {
        return expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean expire(String key, long ttlSeconds, TimeUnit timeUnit) {
        String realKey = buildKey(key);
        Object value = cache.getIfPresent(realKey);
        if (value == null) {
            return false;
        }
        scheduler.schedule(() -> cache.invalidate(realKey), ttlSeconds, timeUnit);
        return true;
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}