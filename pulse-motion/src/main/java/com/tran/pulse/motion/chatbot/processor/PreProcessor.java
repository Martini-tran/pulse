package com.tran.pulse.motion.chatbot.processor;

import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/9/19 15:39
 **/
public interface PreProcessor {

    /**
     * 调用的前置操作
     * @param session WebSocket会话
     * @param webSocketMessage WebSocket消息
     * @param message AI聊天消息
     * @return 预处理结果
     */
    PreProcessResult apply(WebSocketSession session, WebSocketMessage<?> webSocketMessage, AIChatMessage message);

    /**
     * 获取预处理器的执行顺序
     * 数值越小，执行优先级越高
     * @return 执行顺序，默认值为100
     */
    default int getOrder() {
        return 100;
    }
}