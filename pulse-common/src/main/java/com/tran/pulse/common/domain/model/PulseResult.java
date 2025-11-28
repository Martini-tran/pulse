package com.tran.pulse.common.domain.model;

import com.tran.pulse.common.constants.PulseHttpStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Objects;

/**
 * 统一API响应结果类
 *
 *
 * @version 1.0.0.0
 * @date 2025/3/25 15:25
 * @author tran
 */
public class PulseResult extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    /**
     * 成功标识字段名
     */
    public static final String SUCCESS_TAG = "success";

    /**
     * 状态码字段名
     */
    public static final String CODE_TAG = "code";

    /**
     * 返回消息字段名
     */
    public static final String MSG_TAG = "message";

    /**
     * 数据对象字段名
     */
    public static final String DATA_TAG = "data";

    /**
     * 时间戳字段名
     */
    public static final String TIMESTAMP_TAG = "timestamp";

    /**
     * 私有构造函数，防止直接实例化
     */
    private PulseResult() {
        super();
    }

    /**
     * 创建一个新的响应结果
     *
     * @param success 是否成功
     * @param code    状态码
     * @param message 返回消息
     */
    private PulseResult(boolean success, int code, String message) {
        super();
        super.put(SUCCESS_TAG, success);
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, message);
        super.put(TIMESTAMP_TAG, System.currentTimeMillis());
    }

    /**
     * 创建一个新的响应结果
     *
     * @param success 是否成功
     * @param code    状态码
     * @param message 返回消息
     * @param data    数据对象
     */
    private PulseResult(boolean success, int code, String message, Object data) {
        this(success, code, message);
        if (data != null) {
            super.put(DATA_TAG, data);
        }
    }

    // ==================== 静态工厂方法 - 成功响应 ====================

    /**
     * 创建成功响应
     *
     * @return 成功响应结果
     */
    public static PulseResult success() {
        return new PulseResult(true, PulseHttpStatus.OK, "请求成功");
    }

    /**
     * 创建成功响应
     *
     * @param message 成功消息
     * @return 成功响应结果
     */
    public static PulseResult success(String message) {
        return new PulseResult(true, PulseHttpStatus.OK, message);
    }

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static PulseResult success(Object data) {
        return new PulseResult(true, PulseHttpStatus.OK, "请求成功", data);
    }

    /**
     * 创建成功响应
     *
     * @param message 成功消息
     * @param data    响应数据
     * @return 成功响应结果
     */
    public static PulseResult success(String message, Object data) {
        return new PulseResult(true, PulseHttpStatus.OK, message, data);
    }

    /**
     * 创建成功响应
     *
     * @param code    状态码
     * @param message 成功消息
     * @param data    响应数据
     * @return 成功响应结果
     */
    public static PulseResult success(int code, String message, Object data) {
        return new PulseResult(true, code, message, data);
    }

    // ==================== 静态工厂方法 - 失败响应 ====================

    /**
     * 创建失败响应
     *
     * @param message 错误消息
     * @return 失败响应结果
     */
    public static PulseResult fail(String message) {
        return new PulseResult(false, PulseHttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 创建失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 失败响应结果
     */
    public static PulseResult fail(int code, String message) {
        return new PulseResult(false, code, message);
    }

    /**
     * 创建失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    错误数据（如验证错误详情）
     * @return 失败响应结果
     */
    public static PulseResult fail(int code, String message, Object data) {
        return new PulseResult(false, code, message, data);
    }

    // ==================== 便捷的业务方法 ====================

    /**
     * 创建参数错误响应
     *
     * @param message 错误消息
     * @return 参数错误响应
     */
    public static PulseResult paramError(String message) {
        return fail(PulseHttpStatus.BAD_REQUEST, message);
    }

    /**
     * 创建参数错误响应（带验证详情）
     *
     * @param message 错误消息
     * @param errors  验证错误详情
     * @return 参数错误响应
     */
    public static PulseResult paramError(String message, Object errors) {
        return fail(PulseHttpStatus.BAD_REQUEST, message, errors);
    }

    /**
     * 创建认证失败响应
     *
     * @param message 错误消息
     * @return 认证失败响应
     */
    public static PulseResult authFail(String message) {
        return fail(PulseHttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 创建权限不足响应
     *
     * @param message 错误消息
     * @return 权限不足响应
     */
    public static PulseResult accessDenied(String message) {
        return fail(PulseHttpStatus.FORBIDDEN, message);
    }

    /**
     * 创建资源不存在响应
     *
     * @param message 错误消息
     * @return 资源不存在响应
     */
    public static PulseResult notFound(String message) {
        return fail(PulseHttpStatus.NOT_FOUND, message);
    }

    /**
     * 创建操作频繁响应
     *
     * @param message 错误消息
     * @return 操作频繁响应
     */
    public static PulseResult tooManyRequests(String message) {
        return fail(PulseHttpStatus.TOO_MANY_REQUESTS, message);
    }

    // ==================== 类型安全的访问方法 ====================

    /**
     * 获取成功标识
     *
     * @return 是否成功
     */
    @JsonIgnore
    public boolean isSuccess() {
        Object success = super.get(SUCCESS_TAG);
        return success instanceof Boolean ? (Boolean) success : false;
    }

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    @JsonIgnore
    public int getCode() {
        Object code = super.get(CODE_TAG);
        if (code instanceof Integer) {
            return (Integer) code;
        } else if (code instanceof String) {
            try {
                return Integer.parseInt((String) code);
            } catch (NumberFormatException e) {
                return PulseHttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return PulseHttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * 获取消息
     *
     * @return 消息内容
     */
    @JsonIgnore
    public String getMessage() {
        Object message = super.get(MSG_TAG);
        return message != null ? message.toString() : "";
    }

    /**
     * 获取数据
     *
     * @return 数据对象
     */
    @JsonIgnore
    public Object getData() {
        return super.get(DATA_TAG);
    }

    /**
     * 获取指定类型的数据
     *
     * @param clazz 数据类型
     * @param <T>   泛型类型
     * @return 指定类型的数据，如果类型不匹配返回null
     */
    @JsonIgnore
    public <T> T getData(Class<T> clazz) {
        Object data = getData();
        if (data != null && clazz.isInstance(data)) {
            return clazz.cast(data);
        }
        return null;
    }

    /**
     * 获取时间戳
     *
     * @return 时间戳
     */
    @JsonIgnore
    public long getTimestamp() {
        Object timestamp = super.get(TIMESTAMP_TAG);
        return timestamp instanceof Long ? (Long) timestamp : System.currentTimeMillis();
    }

    // ==================== 链式调用方法 ====================

    /**
     * 设置数据（支持链式调用）
     *
     * @param data 数据对象
     * @return 当前实例
     */
    public PulseResult data(Object data) {
        super.put(DATA_TAG, data);
        return this;
    }

    /**
     * 设置消息（支持链式调用）
     *
     * @param message 消息内容
     * @return 当前实例
     */
    public PulseResult message(String message) {
        super.put(MSG_TAG, message);
        return this;
    }

    /**
     * 添加额外字段（支持链式调用）
     *
     * @param key   字段名
     * @param value 字段值
     * @return 当前实例
     */
    @Override
    public PulseResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    // ==================== 重写方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PulseResult that = (PulseResult) o;
        return Objects.equals(this.getCode(), that.getCode()) &&
                Objects.equals(this.getMessage(), that.getMessage()) &&
                Objects.equals(this.isSuccess(), that.isSuccess());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getCode(), getMessage(), isSuccess());
    }

    @Override
    public String toString() {
        return "PulseResult{" +
                "success=" + isSuccess() +
                ", code=" + getCode() +
                ", message='" + getMessage() + '\'' +
                ", data=" + getData() +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}