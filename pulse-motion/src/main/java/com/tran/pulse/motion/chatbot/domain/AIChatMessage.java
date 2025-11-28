package com.tran.pulse.motion.chatbot.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tran.pulse.common.util.JacksonUtils;
import com.tran.pulse.motion.chatbot.constants.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI聊天消息实体类
 *
 * <p>用于表示前端和后端之间传递的聊天消息对象，支持多种消息类型和扩展数据</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>支持多种消息类型（文本、图片、系统消息等）</li>
 *   <li>提供消息元数据和附加数据支持</li>
 *   <li>支持快捷操作配置</li>
 *   <li>提供JSON序列化/反序列化</li>
 * </ul>
 *
 * @author Tran Pulse Team
 * @since 1.0.0
 */
@Data
@Builder
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIChatMessage {

    /** 消息类型，必填 */
    @NotBlank(message = "消息类型不能为空")
    private String type;

    /** 消息唯一标识 */
    private String messageId;

    /** 消息文本内容 */
    private String content;

    /** 附加数据，用于存放结构化信息 */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    /** 消息生成时间戳（毫秒） */
    @Builder.Default
    private long timestamp = System.currentTimeMillis();

    /** 发送消息的用户ID */
    private String userId;

    /** 会话ID，用于区分不同会话 */
    private String sessionId;

    /** 元数据信息 */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    // ================== 构造方法 ==================

    /** 默认构造方法 */
    public AIChatMessage() {
        this.data = new HashMap<>();
        this.metadata = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    /** 全参构造方法 */
    public AIChatMessage(String type, String messageId, String content,
                         Map<String, Object> data, long timestamp,
                         String userId, String sessionId, Map<String, Object> metadata) {
        this.type = type;
        this.messageId = messageId;
        this.content = content;
        this.data = data != null ? data : new HashMap<>();
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.userId = userId;
        this.sessionId = sessionId;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // ================== JSON序列化方法 ==================

    /**
     * 将对象转为JSON字符串
     *
     * @return JSON字符串，转换失败返回空字符串
     */
    public String toJson() {
        try {
            return JacksonUtils.toJson(this);
        } catch (Exception e) {
            log.error("消息对象序列化失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 从JSON字符串解析为AIChatMessage对象
     *
     * @param json JSON字符串
     * @return AIChatMessage对象
     * @throws JsonProcessingException JSON解析异常
     */
    public static AIChatMessage fromJson(String json) throws JsonProcessingException {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON字符串不能为空");
        }

        AIChatMessage message = JacksonUtils.fromJson(json, AIChatMessage.class);

        // 补充默认值
        if (message.timestamp <= 0) {
            message.timestamp = System.currentTimeMillis();
        }
        if (message.data == null) {
            message.data = new HashMap<>();
        }
        if (message.metadata == null) {
            message.metadata = new HashMap<>();
        }

        return message;
    }

    // ================== 静态工厂方法 ==================

    /**
     * 创建聊天消息
     *
     * @param content 消息内容
     * @param userId 用户ID
     * @return AIChatMessage实例
     */
    public static AIChatMessage createChatMessage(@NotNull String content, String userId) {
        return AIChatMessage.builder()
                .type(MessageType.TEXT)
                .content(content)
                .userId(userId)
                .build();
    }

    /**
     * 创建文本消息
     *
     * @param content 消息内容
     * @return AIChatMessage实例
     */
    public static AIChatMessage createTextMessage(@NotNull String content) {
        return AIChatMessage.builder()
                .type(MessageType.TEXT)
                .content(content)
                .build();
    }

    /**
     * 创建数据操作响应消息
     *
     * @param success 操作是否成功
     * @param message 响应消息
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return AIChatMessage实例
     */
    public static AIChatMessage createDataOperationResponse(boolean success, String message,
                                                            String userId, String sessionId) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", message);

        return AIChatMessage.builder()
                .type(MessageType.DATA_OPERATION_RESPONSE)
                .content(message)
                .data(responseData)
                .userId(userId)
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建数据操作响应消息（仅数据）
     *
     * @param data 响应数据
     * @param sessionId 会话ID
     * @return AIChatMessage实例
     */
    public static AIChatMessage createDataOperationResponse(Map<String, Object> data, String sessionId) {
        return AIChatMessage.builder()
                .type(MessageType.DATA_OPERATION_RESPONSE)
                .data(data != null ? data : new HashMap<>())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建图片分析响应消息
     *
     * @param data 分析结果数据
     * @param sessionId 会话ID
     * @return AIChatMessage实例
     */
    public static AIChatMessage createImageAnalysisResponse(Map<String, Object> data, String sessionId) {
        return AIChatMessage.builder()
                .type(MessageType.IMAGE_ANALYSIS_RESPONSE)
                .data(data != null ? data : new HashMap<>())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建快捷操作响应消息
     *
     * @param data 操作数据
     * @param sessionId 会话ID
     * @return AIChatMessage实例
     */
    public static AIChatMessage createQuickActionResponse(Map<String, Object> data, String sessionId) {
        return AIChatMessage.builder()
                .type(MessageType.QUICK_ACTION_RESPONSE)
                .data(data != null ? data : new HashMap<>())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建正在输入消息
     *
     * @param sessionId 会话ID
     * @return AIChatMessage实例
     */
    public static AIChatMessage createTypingMessage(String sessionId,boolean isTyping) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("isTyping", isTyping);

        return AIChatMessage.builder()
                .type(MessageType.TYPING)
                .sessionId(sessionId)
                .data(responseData)
                .build();
    }

    /**
     * 创建心跳消息
     *
     * @param sessionId 会话ID
     * @return AIChatMessage实例
     */
    public static AIChatMessage createHeartbeatMessage(String sessionId) {
        return AIChatMessage.builder()
                .type(MessageType.HEARTBEAT)
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建错误消息
     *
     * @param errorMessage 错误信息
     * @param sessionId 会话ID
     * @return AIChatMessage实例
     */
    public static AIChatMessage createErrorMessage(String errorMessage, String sessionId) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("error", errorMessage != null ? errorMessage : "未知错误");

        return AIChatMessage.builder()
                .type(MessageType.ERROR)
                .data(errorData)
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建通用错误消息
     *
     * @return AIChatMessage实例
     */
    public static AIChatMessage createDefaultErrorMessage() {
        return createErrorMessage("消息处理失败", null);
    }

    /**
     * 创建连接成功消息
     *
     * @return AIChatMessage实例
     */
    public static AIChatMessage createConnectionMessage() {
        Map<String, Object> connectionData = new HashMap<>();
        connectionData.put("connection", "连接建立成功");
        connectionData.put("timestamp", Instant.now().toString());

        return AIChatMessage.builder()
                .type(MessageType.CONNECTION)
                .content("连接建立成功")
                .data(connectionData)
                .build();
    }

    /**
     * 创建未定义消息类型错误消息
     *
     * @return AIChatMessage实例
     */
    public static AIChatMessage createUndefinedMessageTypeError() {
        return createErrorMessage("未定义的消息类型", null);
    }

    // ================== 业务方法 ==================

    /**
     * 添加快捷操作
     *
     * @param label 操作标签
     * @param payload 操作载荷
     * @return 当前消息实例，支持链式调用
     */
    public AIChatMessage addQuickAction(@NotNull String label, @NotNull String payload) {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("快捷操作标签不能为空");
        }
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("快捷操作载荷不能为空");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> quickActions = (List<Map<String, String>>)
                this.metadata.computeIfAbsent("quickActions", k -> new ArrayList<>());

        Map<String, String> quickAction = new HashMap<>();
        quickAction.put("label", label.trim());
        quickAction.put("payload", payload.trim());
        quickActions.add(quickAction);

        return this;
    }

    /**
     * 批量添加快捷操作
     *
     * @param actions 快捷操作列表
     * @return 当前消息实例，支持链式调用
     */
    public AIChatMessage addQuickActions(List<Map<String, String>> actions) {
        if (actions != null && !actions.isEmpty()) {
            for (Map<String, String> action : actions) {
                String label = action.get("label");
                String payload = action.get("payload");
                if (label != null && payload != null) {
                    addQuickAction(label, payload);
                }
            }
        }
        return this;
    }

    /**
     * 添加元数据
     *
     * @param key 键
     * @param value 值
     * @return 当前消息实例，支持链式调用
     */
    public AIChatMessage addMetadata(String key, Object value) {
        if (key != null && !key.trim().isEmpty()) {
            this.metadata.put(key.trim(), value);
        }
        return this;
    }

    /**
     * 添加数据
     *
     * @param key 键
     * @param value 值
     * @return 当前消息实例，支持链式调用
     */
    public AIChatMessage addData(String key, Object value) {
        if (key != null && !key.trim().isEmpty()) {
            this.data.put(key.trim(), value);
        }
        return this;
    }

    /**
     * 检查消息是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return type != null && !type.trim().isEmpty();
    }

    // ================== Object方法重写 ==================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AIChatMessage that = (AIChatMessage) o;
        return timestamp == that.timestamp &&
                Objects.equals(type, that.type) &&
                Objects.equals(messageId, that.messageId) &&
                Objects.equals(content, that.content) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, messageId, content, timestamp, userId, sessionId);
    }

    @Override
    public String toString() {
        return String.format("AIChatMessage{type='%s', messageId='%s', userId='%s', sessionId='%s', timestamp=%d}",
                type, messageId, userId, sessionId, timestamp);
    }
}