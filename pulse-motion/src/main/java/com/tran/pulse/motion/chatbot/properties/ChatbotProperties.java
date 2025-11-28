package com.tran.pulse.motion.chatbot.properties;

import lombok.Data;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/9/18 16:36
 **/
@Data
public class ChatbotProperties {

    /**
     * 会话过期时间 默认10分钟
     */
    private Long sessionExpireSeconds = 600L;




}
