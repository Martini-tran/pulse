package com.tran.pulse.cache.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tran.pulse.cache.constants.CacheMode;
import com.tran.pulse.cache.service.CacheOperations;
import com.tran.pulse.cache.service.CacheOperationsProxy;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类
 * 提供静态方法方便调用缓存操作，内部通过 SpringContextUtil 获取 CacheOperationsProxy 实例。
 * 所有方法都使用配置文件中定义的默认缓存模式。
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/7/1 13:40
 */
public class CacheUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private CacheUtil() {
        throw new AssertionError("工具类不应该被实例化");
    }

    /**
     * 获取默认的 CacheOperations 实例
     *
     * @return 默认的 CacheOperations 实例
     */
    private static CacheOperations getCacheOperations() {
        CacheOperationsProxy proxy = SpringContextUtil.getBean(CacheOperationsProxy.class);
        return proxy.getCacheOperations();
    }

    /**
     * 获取指定类型的 CacheOperations 实例
     *
     * @param cacheMode 缓存模式
     * @return 指定类型的 CacheOperations 实例
     */
    private static CacheOperations getCacheOperations(CacheMode cacheMode) {
        CacheOperationsProxy proxy = SpringContextUtil.getBean(CacheOperationsProxy.class);
        return proxy.getCacheOperations(cacheMode);
    }

    /**
     * 获取当前默认的缓存模式
     *
     * @return 缓存模式
     */
    public static CacheMode getCacheMode() {
        return getCacheOperations().getCacheMode();
    }

    // ========== 基本获取方法 ==========

    /**
     * 获取缓存值
     * 
     * 如果缓存不存在，返回 null
     * 注意：返回类型可能需要强制转换，建议使用类型安全的get方法
     * 
     *
     * @param key 缓存键
     * @param <T> 返回值类型
     * @return 缓存值，如果不存在返回 null
     */
    public static <T> T get(String key) {
        return getCacheOperations().get(key);
    }

    /**
     * 根据指定类型获取缓存值（类型安全，推荐）
     * 
     * 提供类型安全保障，避免类型转换异常
     * 
     *
     * @param key   缓存键
     * @param clazz 目标类型
     * @param <T>   返回值类型
     * @return 缓存值，如果不存在返回 null
     */
    public static <T> T get(String key, Class<T> clazz) {
        return getCacheOperations().get(key, clazz);
    }

    /**
     * 根据TypeReference获取缓存值（用于泛型类型）
     * 
     * 适用于List<User>、Map<String, Object>等复杂泛型类型
     * 
     *
     * @param key           缓存键
     * @param typeReference 类型引用
     * @param <T>           返回值类型
     * @return 缓存值，如果不存在返回 null
     */
    public static <T> T get(String key, TypeReference<T> typeReference) {
        return getCacheOperations().get(key, typeReference);
    }

    // ========== 带加载器的获取方法 ==========

    /**
     * 获取缓存值，如果不存在则使用 loader 加载
     * 
     * 当缓存未命中时，会调用 loader 加载数据并写入缓存
     * 注意：返回类型可能需要强制转换，建议使用类型安全的get方法
     * 
     *
     * @param key    缓存键
     * @param loader 数据加载器
     * @param <T>    返回值类型
     * @return 缓存值或新加载的值
     * @throws Exception 如果 loader 执行失败
     */
    public static <T> T get(String key, Callable<? extends T> loader) throws Exception {
        return getCacheOperations().get(key, loader);
    }

    /**
     * 带类型信息的get方法（推荐使用）
     * 
     * 缓存未命中时用 loader 加载并写入缓存，提供类型安全保障
     * 
     *
     * @param key    缓存键
     * @param clazz  目标类型
     * @param loader 数据加载器
     * @param <T>    返回值类型
     * @return 缓存值或新加载的值
     * @throws Exception 如果 loader 执行失败
     */
    public static <T> T get(String key, Class<T> clazz, Callable<? extends T> loader) throws Exception {
        return getCacheOperations().get(key, clazz, loader);
    }

    /**
     * 带TypeReference的get方法（用于复杂泛型类型）
     * 
     * 缓存未命中时用 loader 加载并写入缓存
     * 
     *
     * @param key           缓存键
     * @param typeReference 类型引用
     * @param loader        数据加载器
     * @param <T>           返回值类型
     * @return 缓存值或新加载的值
     * @throws Exception 如果 loader 执行失败
     */
    public static <T> T get(String key, TypeReference<T> typeReference, Callable<? extends T> loader) throws Exception {
        return getCacheOperations().get(key, typeReference, loader);
    }

    // ========== 写入方法 ==========

    /**
     * 写入或更新缓存（使用默认过期时间）
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param <T>   值类型
     */
    public static <T> void put(String key, T value) {
        getCacheOperations().put(key, value);
    }

    /**
     * 写入或更新缓存，指定过期时间
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @param <T>     值类型
     */
    public static <T> void put(String key, T value, long timeout, TimeUnit unit) {
        getCacheOperations().put(key, value, timeout, unit);
    }

    /**
     * 写入或更新缓存，指定过期时间（秒）
     *
     * @param key        缓存键
     * @param value      缓存值
     * @param ttlSeconds 过期时间（秒）
     * @param <T>        值类型
     */
    public static <T> void put(String key, T value, long ttlSeconds) {
        getCacheOperations().put(key, value, ttlSeconds);
    }

    // ========== 删除方法 ==========

    /**
     * 删除单个缓存
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    public static boolean delete(String key) {
        return getCacheOperations().delete(key);
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     */
    public static void deleteAll(Collection<? extends String> keys) {
        getCacheOperations().deleteAll(keys);
    }

    /**
     * 清空当前命名空间下的所有缓存
     * 
     * 注意：此操作会删除所有缓存数据，请谨慎使用
     * 
     */
    public static void clear() {
        getCacheOperations().clear();
    }

    /**
     * 续期（延长）缓存的过期时间
     *
     * @param key        缓存键
     * @param ttlSeconds 新的过期时间（秒）
     * @return 是否续期成功
     */
    public static boolean expire(String key, long ttlSeconds) {
        return getCacheOperations().expire(key, ttlSeconds);
    }

    /**
     * 续期（延长）缓存的过期时间
     *
     * @param key        缓存键
     * @param ttlSeconds 新的过期时间
     * @param timeUnit 单位
     * @return 是否续期成功
     */
    public static boolean expire(String key, long ttlSeconds, TimeUnit  timeUnit) {
        return getCacheOperations().expire(key, ttlSeconds, timeUnit);
    }


    // ========== 使用指定缓存模式的方法 ==========

    /**
     * 使用指定的缓存模式获取缓存
     *
     * @param cacheMode 缓存模式
     * @param key       缓存键
     * @param <T>       返回值类型
     * @return 缓存值，如果不存在返回 null
     */
    public static <T> T get(CacheMode cacheMode, String key) {
        return getCacheOperations(cacheMode).get(key);
    }

    /**
     * 使用指定的缓存模式获取缓存（类型安全）
     *
     * @param cacheMode 缓存模式
     * @param key       缓存键
     * @param clazz     目标类型
     * @param <T>       返回值类型
     * @return 缓存值，如果不存在返回 null
     */
    public static <T> T get(CacheMode cacheMode, String key, Class<T> clazz) {
        return getCacheOperations(cacheMode).get(key, clazz);
    }

    /**
     * 使用指定的缓存模式获取缓存（TypeReference）
     *
     * @param cacheMode     缓存模式
     * @param key           缓存键
     * @param typeReference 类型引用
     * @param <T>           返回值类型
     * @return 缓存值，如果不存在返回 null
     */
    public static <T> T get(CacheMode cacheMode, String key, TypeReference<T> typeReference) {
        return getCacheOperations(cacheMode).get(key, typeReference);
    }

    /**
     * 使用指定的缓存模式获取缓存，如果不存在则使用 loader 加载
     *
     * @param cacheMode 缓存模式
     * @param key       缓存键
     * @param loader    数据加载器
     * @param <T>       返回值类型
     * @return 缓存值或新加载的值
     * @throws Exception 如果 loader 执行失败
     */
    public static <T> T get(CacheMode cacheMode, String key, Callable<? extends T> loader) throws Exception {
        return getCacheOperations(cacheMode).get(key, loader);
    }

    /**
     * 使用指定的缓存模式获取缓存，如果不存在则使用 loader 加载（类型安全）
     *
     * @param cacheMode 缓存模式
     * @param key       缓存键
     * @param clazz     目标类型
     * @param loader    数据加载器
     * @param <T>       返回值类型
     * @return 缓存值或新加载的值
     * @throws Exception 如果 loader 执行失败
     */
    public static <T> T get(CacheMode cacheMode, String key, Class<T> clazz, Callable<? extends T> loader) throws Exception {
        return getCacheOperations(cacheMode).get(key, clazz, loader);
    }

    /**
     * 使用指定的缓存模式获取缓存，如果不存在则使用 loader 加载（TypeReference）
     *
     * @param cacheMode     缓存模式
     * @param key           缓存键
     * @param typeReference 类型引用
     * @param loader        数据加载器
     * @param <T>           返回值类型
     * @return 缓存值或新加载的值
     * @throws Exception 如果 loader 执行失败
     */
    public static <T> T get(CacheMode cacheMode, String key, TypeReference<T> typeReference, Callable<? extends T> loader) throws Exception {
        return getCacheOperations(cacheMode).get(key, typeReference, loader);
    }

    /**
     * 使用指定的缓存模式写入缓存
     *
     * @param cacheMode 缓存模式
     * @param key       缓存键
     * @param value     缓存值
     * @param <T>       值类型
     */
    public static <T> void put(CacheMode cacheMode, String key, T value) {
        getCacheOperations(cacheMode).put(key, value);
    }

    /**
     * 使用指定的缓存模式写入缓存，指定过期时间
     *
     * @param cacheMode  缓存模式
     * @param key        缓存键
     * @param value      缓存值
     * @param ttlSeconds 过期时间（秒）
     * @param <T>        值类型
     */
    public static <T> void put(CacheMode cacheMode, String key, T value, long ttlSeconds) {
        getCacheOperations(cacheMode).put(key, value, ttlSeconds);
    }

    /**
     * 使用指定的缓存模式写入缓存，指定过期时间和时间单位
     *
     * @param cacheMode 缓存模式
     * @param key       缓存键
     * @param value     缓存值
     * @param timeout   过期时间
     * @param unit      时间单位
     * @param <T>       值类型
     */
    public static <T> void put(CacheMode cacheMode, String key, T value, long timeout, TimeUnit unit) {
        getCacheOperations(cacheMode).put(key, value, timeout, unit);
    }

    /**
     * 使用指定的缓存模式删除缓存
     *
     * @param cacheMode 缓存模式
     * @param key       缓存键
     * @return 是否删除成功
     */
    public static boolean delete(CacheMode cacheMode, String key) {
        return getCacheOperations(cacheMode).delete(key);
    }

    /**
     * 使用指定的缓存模式批量删除缓存
     *
     * @param cacheMode 缓存模式
     * @param keys      缓存键集合
     */
    public static void deleteAll(CacheMode cacheMode, Collection<? extends String> keys) {
        getCacheOperations(cacheMode).deleteAll(keys);
    }

    /**
     * 使用指定的缓存模式清空缓存
     *
     * @param cacheMode 缓存模式
     */
    public static void clear(CacheMode cacheMode) {
        getCacheOperations(cacheMode).clear();
    }

    /**
     * 使用指定的缓存模式续期缓存
     *
     * @param cacheMode  缓存模式
     * @param key        缓存键
     * @param ttlSeconds 新的过期时间（秒）
     * @return 是否续期成功
     */
    public static boolean expire(CacheMode cacheMode, String key, long ttlSeconds) {
        return getCacheOperations(cacheMode).expire(key, ttlSeconds);
    }
}