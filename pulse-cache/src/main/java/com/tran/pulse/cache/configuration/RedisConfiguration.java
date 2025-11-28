package com.tran.pulse.cache.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tran.pulse.cache.properties.PulseCacheProperties;
import com.tran.pulse.cache.properties.RedisProperties;
import com.tran.pulse.cache.service.CacheOperations;
import com.tran.pulse.cache.service.RedisCacheOperations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis缓存配置类
 * @author tran
 * @version 1.0.0.0
 * @date 2025/6/24 15:48
 **/
@ConditionalOnProperty(name = "pulse.cache.mode", havingValue = "REDIS")
public class RedisConfiguration {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(PulseCacheProperties cacheProperties) {
        RedisProperties cacheRedisProperties = cacheProperties.getRedis();
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(cacheRedisProperties.getTimeoutSeconds()))
                .shutdownTimeout(Duration.ZERO)
                .build();

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(
                cacheRedisProperties.getHost(),
                cacheRedisProperties.getPort()
        );
        serverConfig.setPassword(RedisPassword.of(cacheRedisProperties.getPassword()));
        serverConfig.setDatabase(cacheRedisProperties.getDatabase());

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    /**
     * 配置RedisTemplate，使用String类型存储
     * Key和Value都使用StringRedisSerializer
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用String序列化器，因为我们现在把所有值都转为JSON字符串
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 设置Key的序列化器
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 设置Value的序列化器 - 使用String序列化器
        template.setValueSerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        // 设置默认序列化器
        template.setDefaultSerializer(stringSerializer);
        template.setEnableDefaultSerializer(false);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置ObjectMapper用于JSON序列化
     * 专门用于缓存操作的JSON转换
     */
    @Bean("cacheObjectMapper")
    public ObjectMapper cacheObjectMapper() {
        return JsonMapper.builder()
                // 忽略未知属性
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 添加 Java 8 时间模块支持
                .addModule(new JavaTimeModule())
                // 序列化时忽略 null
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }


    /**
     * 创建RedisCacheOperations Bean
     */
    @Bean
    public CacheOperations redisCacheOperations(
            RedisTemplate<String, String> redisTemplate,
            PulseCacheProperties cacheProperties,
            ObjectMapper objectMapper) {
        return new RedisCacheOperations(redisTemplate, cacheProperties, objectMapper);
    }
}