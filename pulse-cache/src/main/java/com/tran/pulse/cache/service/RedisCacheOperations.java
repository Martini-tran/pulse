package com.tran.pulse.cache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tran.pulse.cache.constants.CacheMode;
import com.tran.pulse.cache.properties.PulseCacheProperties;
import com.tran.pulse.cache.properties.RedisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于 RedisTemplate 的缓存实现，支持JSON序列化存储
 * @author tran
 */
public class RedisCacheOperations implements CacheOperations {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheOperations.class);

    /**
     * Redis 操作模板，使用String类型存储
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 简化的值操作
     */
    private final ValueOperations<String, String> valueOps;

    /**
     * JSON序列化工具
     */
    private final ObjectMapper objectMapper;

    /**
     * 缓存命名空间前缀，确保以冒号结尾
     */
    private final String namespace;

    /**
     * 默认 TTL（秒），<=0 则无默认过期
     */
    private final long defaultTtlSeconds;

    /**
     * 构造函数注入 RedisTemplate 与可配置参数
     *
     * @param redisTemplate     RedisTemplate 实例
     * @param pulseCacheProperties 缓存配置
     * @param objectMapper      JSON序列化工具
     */
    public RedisCacheOperations(
            RedisTemplate<String, String> redisTemplate,
            PulseCacheProperties pulseCacheProperties,
            ObjectMapper objectMapper) {
        String namespace = pulseCacheProperties.getNamespace();
        RedisProperties redisProperties = pulseCacheProperties.getRedis();
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
        this.objectMapper = objectMapper;
        // 处理命名空间末尾冒号
        if (namespace != null && !namespace.isEmpty()) {
            this.namespace = namespace.endsWith(":") ? namespace : namespace + ":";
        } else {
            this.namespace = "";
        }
        this.defaultTtlSeconds = redisProperties.getDefaultTtlSeconds();
    }

    /**
     * 为给定 key 添加命名空间前缀
     *
     * @param key 原始缓存键
     * @return 完整缓存键
     */
    private String prefixedKey(String key) {
        return namespace + key;
    }

    /**
     * 将对象序列化为JSON字符串
     *
     * @param value 要序列化的对象
     * @return JSON字符串
     */
    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("序列化对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("序列化失败", e);
        }
    }

    /**
     * 将JSON字符串反序列化为对象
     *
     * @param json JSON字符串
     * @param clazz 目标类型
     * @return 反序列化后的对象
     */
    private <T> T deserialize(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            if (clazz == String.class) {
                return clazz.cast(json);
            }
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("反序列化JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("反序列化失败", e);
        }
    }

    /**
     * 将JSON字符串反序列化为对象（使用TypeReference）
     *
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @return 反序列化后的对象
     */
    private <T> T deserialize(String json, TypeReference<T> typeReference) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            logger.error("反序列化JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("反序列化失败", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        String json = valueOps.get(prefixedKey(key));
        if (json == null) {
            return null;
        }
        // 注意：这里无法确定具体的类型，返回的是Object
        // 建议使用 get(String key, Class<T> clazz) 方法
        try {
            return (T) objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            logger.error("反序列化JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("反序列化失败", e);
        }
    }

    /**
     * 根据指定类型获取缓存值
     *
     * @param key 缓存键
     * @param clazz 目标类型
     * @return 缓存值
     */
    public <T> T get(String key, Class<T> clazz) {
        String json = valueOps.get(prefixedKey(key));
        return deserialize(json, clazz);
    }

    /**
     * 根据TypeReference获取缓存值（用于泛型类型）
     *
     * @param key 缓存键
     * @param typeReference 类型引用
     * @return 缓存值
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        String json = valueOps.get(prefixedKey(key));
        return deserialize(json, typeReference);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Callable<? extends T> loader) throws Exception {
        String fullKey = prefixedKey(key);
        // 尝试先获取缓存
        String json = valueOps.get(fullKey);
        T value = null;

        if (json != null) {
            // 缓存命中，反序列化
            try {
                value = (T) objectMapper.readValue(json, Object.class);
            } catch (JsonProcessingException e) {
                logger.warn("反序列化缓存值失败，将重新加载: {}", e.getMessage());
                // 反序列化失败，删除错误的缓存
                redisTemplate.delete(fullKey);
            }
        }

        if (value == null) {
            // 缓存未命中或反序列化失败，调用 loader 加载
            value = loader.call();
            if (value != null) {
                // 序列化并写入缓存
                String serializedValue = serialize(value);
                if (serializedValue != null) {
                    if (defaultTtlSeconds > 0) {
                        valueOps.set(fullKey, serializedValue, defaultTtlSeconds, TimeUnit.SECONDS);
                    } else {
                        valueOps.set(fullKey, serializedValue);
                    }
                }
            }
        }
        return value;
    }

    /**
     * 带类型信息的get方法（推荐使用）
     *
     * @param key 缓存键
     * @param clazz 目标类型
     * @param loader 加载器
     * @return 缓存值
     */
    public <T> T get(String key, Class<T> clazz, Callable<? extends T> loader) throws Exception {
        String fullKey = prefixedKey(key);
        // 尝试先获取缓存
        String json = valueOps.get(fullKey);
        T value = null;

        if (json != null) {
            // 缓存命中，反序列化
            try {
                value = deserialize(json, clazz);
            } catch (RuntimeException e) {
                logger.warn("反序列化缓存值失败，将重新加载: {}", e.getMessage());
                // 反序列化失败，删除错误的缓存
                redisTemplate.delete(fullKey);
            }
        }

        if (value == null) {
            // 缓存未命中或反序列化失败，调用 loader 加载
            value = loader.call();
            if (value != null) {
                // 序列化并写入缓存
                String serializedValue = serialize(value);
                if (serializedValue != null) {
                    if (defaultTtlSeconds > 0) {
                        valueOps.set(fullKey, serializedValue, defaultTtlSeconds, TimeUnit.SECONDS);
                    } else {
                        valueOps.set(fullKey, serializedValue);
                    }
                }
            }
        }
        return value;
    }

    /**
     * 带TypeReference的get方法（用于复杂泛型类型）
     *
     * @param key 缓存键
     * @param typeReference 类型引用
     * @param loader 加载器
     * @return 缓存值
     */
    public <T> T get(String key, TypeReference<T> typeReference, Callable<? extends T> loader) throws Exception {
        String fullKey = prefixedKey(key);
        // 尝试先获取缓存
        String json = valueOps.get(fullKey);
        T value = null;

        if (json != null) {
            // 缓存命中，反序列化
            try {
                value = deserialize(json, typeReference);
            } catch (RuntimeException e) {
                logger.warn("反序列化缓存值失败，将重新加载: {}", e.getMessage());
                // 反序列化失败，删除错误的缓存
                redisTemplate.delete(fullKey);
            }
        }

        if (value == null) {
            // 缓存未命中或反序列化失败，调用 loader 加载
            value = loader.call();
            if (value != null) {
                // 序列化并写入缓存
                String serializedValue = serialize(value);
                if (serializedValue != null) {
                    if (defaultTtlSeconds > 0) {
                        valueOps.set(fullKey, serializedValue, defaultTtlSeconds, TimeUnit.SECONDS);
                    } else {
                        valueOps.set(fullKey, serializedValue);
                    }
                }
            }
        }
        return value;
    }

    @Override
    public <T> void put(String key, T value) {
        String fullKey = prefixedKey(key);
        String serializedValue = serialize(value);
        if (serializedValue != null) {
            // 使用默认 TTL 或无过期策略
            if (defaultTtlSeconds > 0) {
                valueOps.set(fullKey, serializedValue, defaultTtlSeconds, TimeUnit.SECONDS);
            } else {
                valueOps.set(fullKey, serializedValue);
            }
        }
    }

    @Override
    public <T> void put(String key, T value, long timeout, TimeUnit unit) {
        String serializedValue = serialize(value);
        if (serializedValue != null) {
            // 指定超时时间写入
            valueOps.set(prefixedKey(key), serializedValue, timeout, unit);
        }
    }

    @Override
    public <T> void put(String key, T value, long ttlSeconds) {
        String serializedValue = serialize(value);
        if (serializedValue != null) {
            // 指定秒级 TTL 写入
            valueOps.set(prefixedKey(key), serializedValue, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean delete(String key) {
        // 删除单个 key，返回删除结果
        return redisTemplate.delete(prefixedKey(key));
    }

    @Override
    public void deleteAll(Collection<? extends String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        // 批量删除，先添加前缀
        Set<String> fullKeys = keys.stream()
                .map(this::prefixedKey)
                .collect(Collectors.toSet());
        redisTemplate.delete(fullKeys);
    }

    @Override
    public void clear() {
        // 删除当前命名空间下所有缓存
        String pattern = namespace + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public boolean expire(String key, long ttlSeconds) {
        // 为指定 key 设置过期时长
        return expire(prefixedKey(key), ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean expire(String key, long ttlSeconds, TimeUnit  timeUnit) {
        // 为指定 key 设置过期时长
        return redisTemplate.expire(prefixedKey(key), ttlSeconds, timeUnit);
    }


    @Override
    public CacheMode getCacheMode() {
        return CacheMode.REDIS;
    }
}