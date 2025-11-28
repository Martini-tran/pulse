package com.tran.pulse.motion.chatbot.processor.impl;

import com.chaincraft.ai.client.common.client.AiClient;
import com.chaincraft.ai.client.core.service.AiModelService;
import com.tran.pulse.common.constants.BusinessCode;
import com.tran.pulse.common.constants.Constants;
import com.tran.pulse.common.domain.entity.AiBusiness;
import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import com.tran.pulse.motion.chatbot.processor.PreProcessResult;
import com.tran.pulse.motion.chatbot.processor.PreProcessor;
import com.tran.pulse.motion.chatbot.service.ChatbotService;
import com.tran.pulse.motion.tag.service.TagHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 操作的PreProcessor
 */
@Component
public class OperationPreProcessor implements PreProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OperationPreProcessor.class);


    // 关键字列表（可自行扩展）
    private static final List<String> KEYWORDS = Arrays.asList(
            "今日总结", "查看统计", "帮助"
    );


    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private AiModelService aiModelService;

    @Autowired
    private TagHistoryService tagHistoryService;

    @Override
    public PreProcessResult apply(WebSocketSession session, WebSocketMessage<?> webSocketMessage, AIChatMessage message) {
        if (validateInput(message.getContent())){
            return PreProcessResult.proceed();
        }
        Map<String, Object> attrs = session.getAttributes();
        Long userId = (Long) attrs.get(Constants.USER_ID);
        String userMassage = message.getContent();
        try {
            if ("今日总结".equals(userMassage)) {
                // 获取最近7天的标签变化（体重、消耗等等）
                // 获取近3天的饮食记录

            }

            if ("查看统计".equals(userMassage)) {
                // 获取近7天的体重变化
                // 获取近7天的饮食记录
                // 获取近7天的运动消耗

            }

            if ("帮助".equals(userMassage)) {
                AiBusiness aiBusiness = chatbotService.getAiBusiness(BusinessCode.HELP);
                AIChatMessage chatMessage = AIChatMessage.createChatMessage(aiBusiness.getSystem(), userId.toString());
                chatMessage.addQuickAction("今日总结","今日总结");
                chatMessage.addQuickAction("查看统计","查看统计");
                chatMessage.addQuickAction("记录消耗","记录消耗");
                chatMessage.addQuickAction("记录体重","记录体重");
                chatMessage.addQuickAction("定制标签","定制标签");
//                chatMessage.addQuickAction("构建知识库","构建知识库");
                return PreProcessResult.blockWithReply(chatMessage);
            }

            return PreProcessResult.proceed();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return PreProcessResult.blockWithReply(AIChatMessage.createChatMessage("获取" + userMassage + "失败", userId.toString()));

        }

    }

    /**
     * 判断输入字符串中是否包含任何一个关键字
     * @param input 用户输入
     * @return 包含返回 true，否则 false
     */
    public static boolean containsKeyword(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (String keyword : KEYWORDS) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public boolean validateInput(String content) {
        return !containsKeyword(content);
    }



    @Override
    public int getOrder() {
        return -100;
    }
}
