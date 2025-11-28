package com.tran.pulse.motion.chatbot.handler.massage.pre;

import com.chaincraft.ai.client.common.client.AiClient;
import com.chaincraft.ai.client.common.model.entity.AiModel;
import com.chaincraft.ai.client.common.model.request.AiRequest;
import com.chaincraft.ai.client.common.model.request.Message;
import com.chaincraft.ai.client.common.model.response.AiResponse;
import com.chaincraft.ai.client.core.service.AiModelService;
import com.tran.pulse.common.constants.BusinessCode;
import com.tran.pulse.common.constants.Constants;
import com.tran.pulse.common.domain.entity.AiBusiness;
import com.tran.pulse.motion.chatbot.constants.MessageType;
import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import com.tran.pulse.motion.chatbot.handler.PreMassageHandler;
import com.tran.pulse.motion.chatbot.service.ChatbotService;
import com.tran.pulse.motion.chatbot.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author tran
 * @version 1.0.0.1
 * @date 2025/09/19
 */
@Component
public class AiTextMessageHandler implements PreMassageHandler {

    private static final Logger log = LoggerFactory.getLogger(AiTextMessageHandler.class);

    private final UserSessionService userSessionService;
    private final ChatbotService chatbotService;
    private final AiClient aiClient;
    private final AiModelService aiModelService;

    public AiTextMessageHandler(UserSessionService userSessionService,
                                ChatbotService chatbotService,
                                AiClient aiClient,
                                AiModelService aiModelService) {
        this.userSessionService = userSessionService;
        this.chatbotService = chatbotService;
        this.aiClient = aiClient;
        this.aiModelService = aiModelService;
    }

    @Override
    public AIChatMessage massage(WebSocketSession session, WebSocketMessage<?> webSocketMessage, AIChatMessage message) {
        // 1) 读取并校验会话属性
        Map<String, Object> attrs = session.getAttributes();
        Object uidObj = attrs.get(Constants.USER_ID);
        Long userId = (Long) uidObj;

        Object bizHeader = attrs.get(BusinessCode.HEADER);

        String businessCode = (String) bizHeader;

        // 2) 读取业务与模型并校验
        AiBusiness aiBusiness = chatbotService.getAiBusiness(businessCode);
        AiModel aiModel = aiModelService.getById(aiBusiness.getAiModelId());
        // 3) 获取对话 sessionId
        String sessionId = userSessionService.getSessionId(userId);
        // 4) 组装请求（使用不可变列表，命名 messages）
        List<Message> messages = Collections.singletonList(Message.user(message.getContent()));
        AiRequest aiRequest = AiRequest.builder()
                .modelCode(aiModel.getModelCode())
                .conversationId(sessionId)
                .systemPrompt(Objects.toString(aiBusiness.getSystem(), null)) // null 安全
                .messages(messages)
                .build();

        // 5) 调用模型并兜底异常
        try {
            AiResponse resp = aiClient.chat(aiRequest);
            String content = (resp != null) ? resp.getContent() : null;
            return AIChatMessage.createTextMessage(content);
        } catch (Exception e) {
            log.error("AI 聊天调用失败，request={}，err={}", safeBrief(aiRequest), e.toString(), e);
            return AIChatMessage.createDefaultErrorMessage();
        }
    }

    @Override
    public String getMessageType() {
        return MessageType.TEXT;
    }



    /** 避免在错误日志里打过多内容，做一个简短版 */
    private String safeBrief(AiRequest req) {
        if (req == null) return "null";
        return String.format("model=%s, conv=%s, sys.len=%d, msgs=%d",
                req.getModelCode(),
                req.getConversationId(),
                req.getSystemPrompt() == null ? 0 : req.getSystemPrompt().length(),
                req.getMessages() == null ? 0 : req.getMessages().size());
    }
}
