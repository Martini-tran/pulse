package com.tran.pulse.cache.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tran.pulse.cache.constants.CacheMode;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 通用缓存操作接口
 * 支持类型安全的缓存操作和JSON序列化
 */
public interface CacheOperations {

    /**
     * 获取当前实现的类型CacheMode
     */
    CacheMode getCacheMode();

    /**
     * 获取缓存，如果不存在返回 null
     * 注意：返回类型可能需要强制转换，建议使用类型安全的get方法
     */
    <T> T get(String key);

    /**
     * 根据指定类型获取缓存值（类型安全）
     * 推荐使用此方法替代无类型参数的get方法
     *
     * @param key 缓存键
     * @param clazz 目标类型
     * @return 缓存值，如果不存在返回null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 根据TypeReference获取缓存值（用于泛型类型）
     * 适用于List<User>、Map<String, Object>等复杂泛型类型
     *
     * @param key 缓存键
     * @param typeReference 类型引用
     * @return 缓存值，如果不存在返回null
     */
    <T> T get(String key, TypeReference<T> typeReference);

    /**
     * 获取缓存；缓存未命中时用 loader 加载并写入缓存
     * 注意：返回类型可能需要强制转换，建议使用类型安全的get方法
     */
    <T> T get(String key, Callable<? extends T> loader) throws Exception;

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
    <T> T get(String key, Class<T> clazz, Callable<? extends T> loader) throws Exception;

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
    <T> T get(String key, TypeReference<T> typeReference, Callable<? extends T> loader) throws Exception;

    /**
     * 写入或更新缓存，使用默认过期时间
     */
    <T> void put(String key, T value);

    /**
     * 写入或更新缓存，指定过期时间
     */
    <T> void put(String key, T value, long timeout, TimeUnit unit);

    /**
     * 写入或更新缓存，并自定义过期时间（秒）
     */
    <T> void put(String key, T value, long ttlSeconds);

    /**
     * 删除单个缓存，返回是否删除成功
     */
    boolean delete(String key);

    /**
     * 批量删除
     */
    void deleteAll(Collection<? extends String> keys);

    /**
     * 清空当前命名空间下的所有缓存
     */
    void clear();

    /**
     * 续期（延长）Key 的过期时间（秒）
     */
    boolean expire(String key, long ttlSeconds);

    /**
     * 续期（延长）Key 的过期时间
     */
    public boolean expire(String key, long ttlSeconds, TimeUnit  timeUnit);
}