package com.tran.pulse.motion.chatbot.handler;

import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 后置消息处理器
 */
public interface PostMassageHandler {


    /**
     * 消息处理
     *
     * @param session WebSocketSession
     * @param webSocketMessage WebSocketMessage
     * @param message AIChatMessage
     */
    public AIChatMessage massage(WebSocketSession session, WebSocketMessage<?> webSocketMessage , AIChatMessage message);


    /**
     * 获取预处理器的执行顺序
     * 数值越小，执行优先级越高
     * @return 执行顺序，默认值为100
     */
    default int getOrder() {
        return 100;
    }

}
