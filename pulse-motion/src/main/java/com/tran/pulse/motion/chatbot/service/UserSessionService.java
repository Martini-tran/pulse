package com.tran.pulse.motion.chatbot.service;

import com.tran.pulse.cache.util.CacheUtil;
import com.tran.pulse.common.constants.CacheConstants;
import com.tran.pulse.motion.chatbot.WebSocketSessionManager;
import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import com.tran.pulse.motion.chatbot.properties.ChatbotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;


/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/8/29 10:23
 **/
@Service
public class UserSessionService {


    @Autowired
    private WebSocketSessionManager webSocketSessionManager;


    @Autowired
    private ChatbotService chatbotService;

    // 添加连接信息
    public void addConnection(String businessCode, Long userId, WebSocketSession session) {
        Long sessionId = chatbotService.getAiConversation(userId, businessCode);
        // 存储WebSocket会话到内存中（注意：集群环境需要考虑会话共享）
        CacheUtil.put(builderKey(userId), sessionId);
        webSocketSessionManager.addUserSession(userId.toString(),session);
    }


    /**
     * 删除用户连接
     * @param userId
     */
    public void removeConnection(Long userId) {
        webSocketSessionManager.removeUserSession(userId.toString());
        CacheUtil.delete(builderKey(userId));
    }

    /**
     * 发送消息
     * @param userId
     * @param message
     * @return
     */
    public boolean sendUserMessage(Long userId, AIChatMessage message) {
        return webSocketSessionManager.sendMessageToUser(userId.toString(), message.toJson());
    }

    /**
     * 获取会话id
     * @param userId
     * @return
     */
    public String getSessionId(Long userId) {
        return CacheUtil.get(builderKey(userId),String.class);
    }

    /**
     * 编译存储Key
     *
     * @param userId
     * @return
     */
    public String builderKey(Long userId) {
        return CacheConstants.SESSION_PREFIX + userId;
    }
}

