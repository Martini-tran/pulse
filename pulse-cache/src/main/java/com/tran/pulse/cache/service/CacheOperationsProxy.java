package com.tran.pulse.cache.service;

import com.tran.pulse.cache.constants.CacheMode;
import com.tran.pulse.cache.properties.PulseCacheProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存代理
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/6/25 10:25
 **/
public class CacheOperationsProxy {

    /**
     * 缓存配置
     */
    private final PulseCacheProperties pulseCacheProperties;


    /**
     * 存储缓存方式
     */
    private final Map<CacheMode, CacheOperations> cacheOperationsMap = new HashMap<>();



    public CacheOperationsProxy(PulseCacheProperties pulseCacheProperties, List<CacheOperations> cacheOperations) {
        this.pulseCacheProperties = pulseCacheProperties;
        for (CacheOperations cacheOperation : cacheOperations) {
            cacheOperationsMap.put(cacheOperation.getCacheMode(),cacheOperation);
        }
    }


    /**
     * 获取默认配置的 CacheOperations
     *
     * @return
     */
    public CacheOperations getCacheOperations(){
        return cacheOperationsMap.get(pulseCacheProperties.getMode());
    }


    /**
     * 获取指定的 CacheOperations
     *
     * @return
     */
    public CacheOperations getCacheOperations(CacheMode cacheMode){
        return cacheOperationsMap.get(cacheMode);
    }

}
