package  com.tran.pulse.motion.chatbot;

import com.tran.pulse.cache.util.CacheUtil;
import com.tran.pulse.common.constants.CacheConstants;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

/**
 * WebSocket会话管理器
 * 用于管理用户WebSocket连接，确保一个用户只有一个活跃连接
 */
@Component
public class WebSocketSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionManager.class);

    // 用户ID -> WebSocket会话映射
    private final ConcurrentMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // WebSocket会话ID -> 用户ID映射（反向映射，便于清理）
    private final ConcurrentMap<String, String> sessionUsers = new ConcurrentHashMap<>();

    /**
     * 添加用户会话
     * 如果用户已有连接，会先关闭旧连接再建立新连接
     *
     * @param userId 用户ID
     * @param session WebSocket会话
     */
    public void addUserSession(String userId, WebSocketSession session) {
        if (userId == null || session == null) {
            logger.warn("尝试添加无效的用户会话：userId={}, session={}", userId, session);
            return;
        }

        // 建立新连接
        userSessions.put(userId, session);
        sessionUsers.put(session.getId(), userId);

        logger.info("用户 {} 建立WebSocket连接，会话ID: {}", userId, session.getId());
    }

    /**
     * 移除用户会话
     *
     * @param userId 用户ID
     */
    public void removeUserSession(String userId) {
        if (userId == null) {
            return;
        }

        WebSocketSession session = userSessions.remove(userId);
        CacheUtil.delete(builderKey(userId));
        if (session != null) {
            sessionUsers.remove(session.getId());
            logger.info("移除用户 {} 的WebSocket会话", userId);
        }
    }

    /**
     * 根据会话ID移除会话
     *
     * @param sessionId 会话ID
     */
    public void removeSessionById(String sessionId) {
        if (sessionId == null) {
            return;
        }

        String userId = sessionUsers.remove(sessionId);
        CacheUtil.delete(builderKey(userId));
        if (userId != null) {
            userSessions.remove(userId);
            logger.info("移除会话 {} 对应的用户 {} 连接", sessionId, userId);
        }
    }

    /**
     * 获取用户的WebSocket会话
     *
     * @param userId 用户ID
     * @return WebSocket会话，如果不存在或已关闭则返回null
     */
    public WebSocketSession getUserSession(String userId) {
        if (userId == null) {
            return null;
        }

        WebSocketSession session = userSessions.get(userId);
        // 检查会话是否仍然有效
        if (session != null && !session.isOpen()) {
            // 会话已关闭，清理映射
            removeUserSession(userId);
            return null;
        }

        return session;
    }

    /**
     * 根据会话ID获取用户ID
     *
     * @param sessionId 会话ID
     * @return 用户ID
     */
    public String getUserBySessionId(String sessionId) {
        return sessionUsers.get(sessionId);
    }

    /**
     * 向指定用户发送消息
     *
     * @param userId 用户ID
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessageToUser(String userId, String message) {
        WebSocketSession session = getUserSession(userId);
        if (session == null) {
            logger.warn("用户 {} 没有活跃的WebSocket连接", userId);
            return false;
        }

        try {
            session.sendMessage(new TextMessage(message));
            logger.debug("向用户 {} 发送消息: {}", userId, message);
            return true;
        } catch (IOException e) {
            logger.error("向用户 {} 发送消息失败", userId, e);
            // 发送失败，移除无效会话
            removeUserSession(userId);
            return false;
        }
    }

    /**
     * 向所有在线用户发送消息（广播）
     *
     * @param message 消息内容
     * @return 成功发送的用户数量
     */
    public int broadcastMessage(String message) {
        int successCount = 0;
        Set<String> failedUsers = new HashSet<>();

        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            String userId = entry.getKey();
            WebSocketSession session = entry.getValue();

            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                    successCount++;
                } catch (IOException e) {
                    logger.error("向用户 {} 广播消息失败", userId, e);
                    failedUsers.add(userId);
                }
            } else {
                failedUsers.add(userId);
            }
        }

        // 清理失败的会话
        failedUsers.forEach(this::removeUserSession);

        logger.info("广播消息完成，成功发送给 {} 个用户", successCount);
        return successCount;
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(String userId) {
        return getUserSession(userId) != null;
    }

    /**
     * 获取当前在线用户数量
     *
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        // 清理无效会话
        cleanupInvalidSessions();
        return userSessions.size();
    }

    /**
     * 获取所有在线用户ID
     *
     * @return 在线用户ID集合
     */
    public Set<String> getOnlineUsers() {
        // 清理无效会话
        cleanupInvalidSessions();
        return new HashSet<>(userSessions.keySet());
    }

    /**
     * 强制断开用户连接
     *
     * @param userId 用户ID
     * @param reason 断开原因
     */
    public void disconnectUser(String userId, String reason) {
        WebSocketSession session = getUserSession(userId);
        if (session != null) {
            closeSession(session, CloseStatus.NORMAL.withReason(reason));
            removeUserSession(userId);
            logger.info("强制断开用户 {} 的连接，原因: {}", userId, reason);
        }
    }

    /**
     * 清理所有连接
     */
    public void closeAllSessions() {
        logger.info("开始关闭所有WebSocket连接，当前连接数: {}", userSessions.size());

        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            String userId = entry.getKey();
            WebSocketSession session = entry.getValue();

            try {
                if (session.isOpen()) {
                    session.close(CloseStatus.GOING_AWAY.withReason("服务器关闭"));
                }
                logger.debug("关闭用户 {} 的连接", userId);
            } catch (IOException e) {
                logger.error("关闭用户 {} 连接时发生错误", userId, e);
            }
        }

        userSessions.clear();
        sessionUsers.clear();
        logger.info("所有WebSocket连接已关闭");
    }

    /**
     * 清理无效的会话
     */
    public void cleanupInvalidSessions() {
        Set<String> invalidUsers = new HashSet<>();

        for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
            if (!entry.getValue().isOpen()) {
                invalidUsers.add(entry.getKey());
            }
        }

        invalidUsers.forEach(this::removeUserSession);

        if (!invalidUsers.isEmpty()) {
            logger.debug("清理了 {} 个无效会话", invalidUsers.size());
        }
    }

    /**
     * 安全关闭WebSocket会话
     *
     * @param session WebSocket会话
     * @param status 关闭状态
     */
    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException e) {
            logger.error("关闭WebSocket会话时发生错误: {}", e.getMessage());
        }
    }


    /**
     * 编译存储Key
     *
     * @param userId
     * @return
     */
    public String builderKey(String userId) {
        return CacheConstants.SESSION_PREFIX + userId;
    }

}