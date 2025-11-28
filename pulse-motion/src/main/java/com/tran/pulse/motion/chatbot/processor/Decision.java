package com.tran.pulse.motion.chatbot.processor;


/**
 * 预处理决策枚举
 * @author tran
 * @version 1.0.0.0
 * @date 2025/9/19 15:39
 */
public enum Decision {
    /**
     * 继续处理 - 消息通过预处理，可以继续后续流程
     */
    PROCEED,

    /**
     * 阻止并回复 - 消息被阻止，需要立即回复用户
     */
    BLOCK_WITH_REPLY,

    /**
     * 重定向 - 消息类型需要改变，重定向到其他处理器
     */
    REDIRECT
}
