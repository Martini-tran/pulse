package com.tran.pulse.motion.chatbot.handler;

import com.tran.pulse.common.constants.BusinessCode;
import com.tran.pulse.common.constants.Constants;
import com.tran.pulse.common.util.JacksonUtils;
import com.tran.pulse.common.util.StringUtils;
import com.tran.pulse.motion.chatbot.constants.MessageType;
import com.tran.pulse.motion.chatbot.processor.PreProcessResult;
import com.tran.pulse.motion.chatbot.processor.PreProcessorManager;
import com.tran.pulse.motion.chatbot.service.UserSessionService;
import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/8/29 10:27
 **/
@Component
public class PulseTextWebSocketHandler extends TextWebSocketHandler {

    private static  final Logger logger = LoggerFactory.getLogger(PulseTextWebSocketHandler.class);

    private final UserSessionService userSessionService;


    private final Map<String, PreMassageHandler> massageHandlerMap = new HashMap<>();


    @Autowired
    private PreProcessorManager preProcessorManager;


    private final List<PostMassageHandler> postMassageHandlers;


    public PulseTextWebSocketHandler(UserSessionService userSessionService, List<PreMassageHandler> preMassageHandlers, List<PostMassageHandler> postMassageHandlers) {
        this.userSessionService = userSessionService;
        for (PreMassageHandler preMassageHandler : preMassageHandlers) {
            massageHandlerMap.put(preMassageHandler.getMessageType(), preMassageHandler);
        }
        this.postMassageHandlers = postMassageHandlers.stream()
                .sorted(Comparator.comparingInt(PostMassageHandler::getOrder))
                .collect(Collectors.toList());
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 获取用户ID
        Long userId = (Long) session.getAttributes().get(Constants.USER_ID);
        String businessCode = (String) session.getAttributes().get(BusinessCode.HEADER);
        // 存储连接信息
        userSessionService.addConnection(businessCode,userId, session);
        logger.info("用户连接建立: userId: {}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get(Constants.USER_ID);
        try {
            if (message.getPayload().startsWith("CONNECT")){
                return;
            }
            // 解析消息
            AIChatMessage aiChatMessage = JacksonUtils.fromJson(message.getPayload(), AIChatMessage.class);
            String messageType = aiChatMessage.getType();
            if (MessageType.CONNECTION.equals(messageType)){
                userSessionService.sendUserMessage(userId,AIChatMessage.createConnectionMessage());
                return;
            }
            if (!massageHandlerMap.containsKey(messageType)) {
                userSessionService.sendUserMessage(userId,AIChatMessage.createUndefinedMessageTypeError());
                return;
            }
            if (StringUtils.isBlank(aiChatMessage.getContent())){
                return;
            }
            AIChatMessage typingMessage = AIChatMessage.createTypingMessage(session.getId(),true);
            userSessionService.sendUserMessage(userId,typingMessage);
            // 前置操作如检测是否用户记录等等
            PreProcessResult process = preProcessorManager.process(session, message, aiChatMessage);
            switch (process.decision()) {
                case PROCEED:
                    PreMassageHandler preMassageHandler = massageHandlerMap.get(messageType);
                    AIChatMessage massage = preMassageHandler.massage(session, message, aiChatMessage);
                    for (PostMassageHandler postMassageHandler : postMassageHandlers) {
                        massage = postMassageHandler.massage(session, message, massage);
                    }
                    userSessionService.sendUserMessage(userId,massage);
                    break;
                case BLOCK_WITH_REPLY:
                    userSessionService.sendUserMessage(userId,process.reply());
                    break;
                default:
                    userSessionService.sendUserMessage(userId,AIChatMessage.createDefaultErrorMessage());
            }

        } catch (Exception e) {
            userSessionService.sendUserMessage(userId,AIChatMessage.createDefaultErrorMessage());
            logger.error("<UNK>", e);
        }finally {
            AIChatMessage typingMessage = AIChatMessage.createTypingMessage(session.getId(),false);
            userSessionService.sendUserMessage(userId,typingMessage);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get(Constants.USER_ID);

        logger.info("用户连接关闭: userId: {}", userId);
    }

    @Override
    public void handleTransportError(WebSocketSession session,  Throwable exception) throws Exception {
        logger.error("WebSocket传输错误", exception);
    }

}

