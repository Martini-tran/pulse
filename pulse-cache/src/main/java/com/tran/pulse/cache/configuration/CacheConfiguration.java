package com.tran.pulse.cache.configuration;

import com.tran.pulse.cache.properties.PulseCacheProperties;
import com.tran.pulse.cache.service.CacheOperations;
import com.tran.pulse.cache.service.CacheOperationsProxy;
import com.tran.pulse.cache.service.GuavaCacheOperations;
import com.tran.pulse.cache.util.SpringContextUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 缓存配置
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/6/25 10:07
 **/
public class CacheConfiguration {



    @Bean
    public PulseCacheProperties pulseCacheProperties() {
        return new PulseCacheProperties();
    }


    @Bean
    public CacheOperations guavaCacheOperations(PulseCacheProperties pulseCacheProperties) {
        return new GuavaCacheOperations(pulseCacheProperties);
    }

    @Bean
    public CacheOperationsProxy cacheOperationsProxy(PulseCacheProperties pulseCacheProperties, List<CacheOperations> cacheOperations) {
        return new CacheOperationsProxy(pulseCacheProperties, cacheOperations);
    }

    @Bean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }



}
