package com.tran.pulse.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

/**
 * Json工具类
 *
 * @author luxianggqian
 * @version 1.0.0.0
 * @date 2025/3/30 11:25
 **/
public class JacksonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 将 JSON 字符串反序列化为指定类型的对象
     *
     * @param json  JSON字符串
     * @param clazz 对象的Class类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败: " + json, e);
        }
    }

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param obj 要序列化的对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败: " + obj, e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象（支持复杂泛型，如 List、Map 等）
     *
     * @param json          JSON字符串
     * @param valueTypeRef  目标类型的引用
     * @param <T>           泛型类型
     * @return 反序列化后的对象
     */
    public static <T> T fromJson(String json, com.fasterxml.jackson.core.type.TypeReference<T> valueTypeRef) {
        try {
            return objectMapper.readValue(json, valueTypeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败: " + json, e);
        }
    }


    /**
     * Same as {@link #(InputStream)} except content read from
     * passed-in {@link String}
     */
    public static JsonNode readTree(String json) throws JsonProcessingException {
        return objectMapper.readTree(json);
    }

    /**
     * 将 JSON 字符串反序列化为 List<T>
     *
     * @param json  JSON数组字符串
     * @param clazz 元素类型
     * @param <T>   泛型
     * @return List<T>
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化为 List 失败: " + json, e);
        }
    }


}
