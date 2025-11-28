package com.tran.pulse.motion.config;

import com.tran.pulse.motion.chatbot.interceptor.AuthHandshakeInterceptor;
import com.tran.pulse.motion.chatbot.handler.PulseTextWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/8/29 10:20
 **/
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private PulseTextWebSocketHandler pulseTextWebSocketHandler;

    @Autowired
    private AuthHandshakeInterceptor authHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(pulseTextWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*")
                .addInterceptors(authHandshakeInterceptor);
    }
}
