package com.tran.pulse.motion.chatbot.constants;

/**
 * 消息类型常量类
 */
public final class MessageType {

    private MessageType() {} // 禁止实例化

    /** 普通聊天消息 */
    public static final String CHAT = "chat";

    /** 文本消息（可选，和 CHAT 区分开） */
    public static final String TEXT = "text";

    /** 数据操作响应 */
    public static final String DATA_OPERATION_RESPONSE = "data_operation_response";

    /** 图片分析响应 */
    public static final String IMAGE_ANALYSIS_RESPONSE = "image_analysis_response";

    /** 快捷操作响应 */
    public static final String QUICK_ACTION_RESPONSE = "quick_action_response";

    /** AI 正在输入 */
    public static final String TYPING = "typing";

    /**
     * error 异常消息
     */
    public static final String ERROR = "error";

    /**
     * 心跳包
     */
    public static final String HEARTBEAT = "heartbeat";

    /**
     * 连接
     */
    public static final String CONNECTION = "connection";
}
