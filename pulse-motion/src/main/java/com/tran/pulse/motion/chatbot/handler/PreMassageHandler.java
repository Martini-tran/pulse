package com.tran.pulse.motion.chatbot.handler;

import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 消息处理
 */
public interface PreMassageHandler {


    /**
     * 消息处理
     *
     * @param session WebSocketSession
     * @param webSocketMessage WebSocketMessage
     * @param message AIChatMessage
     */
    public AIChatMessage massage(WebSocketSession session,WebSocketMessage<?> webSocketMessage , AIChatMessage message);


    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    public String getMessageType();





}
