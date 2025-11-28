package com.tran.pulse.motion.chatbot.handler.massage.post;

import com.chaincraft.ai.client.common.client.AiClient;
import com.chaincraft.ai.client.common.model.entity.AiModel;
import com.chaincraft.ai.client.common.model.request.AiRequest;
import com.chaincraft.ai.client.common.model.request.Message;
import com.chaincraft.ai.client.common.model.response.AiResponse;
import com.chaincraft.ai.client.core.service.AiModelService;
import com.tran.pulse.common.constants.BusinessCode;
import com.tran.pulse.common.domain.entity.AiBusiness;
import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import com.tran.pulse.motion.chatbot.handler.PostMassageHandler;
import com.tran.pulse.motion.chatbot.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 按钮处理
 */
@Component
public class HintPostMassageHandler implements PostMassageHandler {

    private static final Logger logger = LoggerFactory.getLogger(HintPostMassageHandler.class);

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private AiModelService aiModelService;

    @Override
    public AIChatMessage massage(WebSocketSession session, WebSocketMessage<?> webSocketMessage, AIChatMessage message) {
        try {
//            AiBusiness aiBusiness = chatbotService.getAiBusiness(BusinessCode.DIET_CHANGE_BIZ_KEY);
//            AiModel aiModel = aiModelService.getById(aiBusiness.getAiModelId());
//            // 4) 组装请求（使用不可变列表，命名 messages）
//            List<Message> messages = Collections.singletonList(Message.user(message.getContent()));
//            AiRequest aiRequest = AiRequest.builder()
//                    .modelCode(aiModel.getModelCode())
//                    .systemPrompt(Objects.toString(aiBusiness.getSystem(), null)) // null 安全
//                    .messages(messages)
//                    .build();
//            AiResponse resp = aiClient.chat(aiRequest);
//            String content = resp.getContent();
//            if ("true".equals(content)) {
//                return message.addQuickAction("记录饮食","记录饮食");
//            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return message;
    }



}
