package com.tran.pulse.motion.chatbot.scheduled;

import com.tran.pulse.motion.chatbot.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/8/29 10:31
 **/
@Component
public class WebSocketCleanupTask {


    @Autowired
    private WebSocketSessionManager webSocketSessionManager;

    // 每2分钟检查一次非活跃连接
    @Scheduled(fixedRate = 120000)
    public void cleanupInactiveSessions() {
        webSocketSessionManager.cleanupInvalidSessions();
    }
}
