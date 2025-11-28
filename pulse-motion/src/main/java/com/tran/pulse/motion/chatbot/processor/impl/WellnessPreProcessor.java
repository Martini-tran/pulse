package com.tran.pulse.motion.chatbot.processor.impl;

import com.chaincraft.ai.client.common.client.AiClient;
import com.chaincraft.ai.client.common.model.entity.AiModel;
import com.chaincraft.ai.client.common.model.request.AiRequest;
import com.chaincraft.ai.client.common.model.request.Message;
import com.chaincraft.ai.client.common.model.response.AiResponse;
import com.chaincraft.ai.client.core.service.AiModelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.tran.pulse.common.constants.BusinessCode;
import com.tran.pulse.common.constants.Constants;
import com.tran.pulse.common.constants.TagCode;
import com.tran.pulse.common.domain.entity.AiBusiness;
import com.tran.pulse.common.domain.entity.TagHistory;
import com.tran.pulse.common.util.JacksonUtils;
import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import com.tran.pulse.motion.chatbot.processor.PreProcessResult;
import com.tran.pulse.motion.chatbot.processor.PreProcessor;
import com.tran.pulse.motion.chatbot.service.ChatbotService;
import com.tran.pulse.motion.chatbot.service.UserSessionService;
import com.tran.pulse.motion.tag.service.TagHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ai消息判断
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/9/19 15:53
 **/
@Component
public class WellnessPreProcessor implements PreProcessor {


    private static final Logger logger = LoggerFactory.getLogger(WellnessPreProcessor.class);

    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");


    // 关键字列表（可自行扩展）
    private static final List<String> KEYWORDS = Arrays.asList(
            "记录", "体重", "消耗"
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
        try {
            AiBusiness aiBusiness = chatbotService.getAiBusiness(BusinessCode.BODY_CHANGE_BIZ_KEY);
            AiModel aiModel = aiModelService.getById(aiBusiness.getAiModelId());
            // 4) 组装请求（使用不可变列表，命名 messages）
            List<Message> messages = Collections.singletonList(Message.user(message.getContent() + "今天日期为: " + LocalDate.now()));
            AiRequest aiRequest = AiRequest.builder()
                    .modelCode(aiModel.getModelCode())
                    .systemPrompt(Objects.toString(aiBusiness.getSystem(), null)) // null 安全
                    .messages(messages)
                    .build();
            AiResponse resp = aiClient.chat(aiRequest);
            String content = resp.getContent();
            content = cleanCodeBlock(content);
            JsonNode root = JacksonUtils.readTree(content);
            String type = root.get("type").asText();
            switch (type) {
                case "weight":
                    return saveWeight(userId, root,message.getContent());
                case "weight_v":
                    return PreProcessResult.blockWithReply(AIChatMessage.createChatMessage("⚠ 请输入体重，例如：今天体重65kg 或 昨天体重130斤", userId.toString()));
                case "consume":
                    return saveConsume(userId,root,message.getContent());
                case "consume_v":
                    return PreProcessResult.blockWithReply(AIChatMessage.createChatMessage("⚠ 请输入消耗热量，例如：今天消耗300kcal 或 跑步消耗200卡路里", userId.toString()));
                default:
                    return PreProcessResult.proceed();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return PreProcessResult.blockWithReply(AIChatMessage.createChatMessage("记录信息失败", userId.toString()));
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

    /**
     * 校验是否包含数字
     *
     * @param input
     * @return
     */
    public static boolean hasDigit(String input) {
        return input != null && DIGIT_PATTERN.matcher(input).matches();
    }


    /**
     * 记录消耗
     * @param userId
     * @param root
     * @return
     */
    private PreProcessResult saveConsume(Long userId, JsonNode root,String content) {
        if (!hasDigit(content)){
            return PreProcessResult.blockWithReply(AIChatMessage.createChatMessage("⚠ 请输入消耗热量，例如：“今天消耗300kcal” 或 “跑步消耗200卡路里”", userId.toString()));
        }
        JsonNode weightArray = root.get("consume");
        if (weightArray.isArray() && !weightArray.isEmpty()) {
            for (JsonNode w : weightArray) {

                String date = w.get("date").asText();
                String value = w.get("value").asText();
                TagHistory tagHistory = new TagHistory();
                tagHistory.setTagCode(TagCode.CONSUME);
                tagHistory.setUserId(userId);
                tagHistory.setRecordTime(date);
                List<TagHistory> histories = tagHistoryService.getHistory(tagHistory);
                for (TagHistory history : histories) {
                    tagHistoryService.delete(history.getId());
                }
                tagHistory.setTagValue(value);
                tagHistoryService.insert(tagHistory);
            }
        }
        String message = root.get("message").asText();
        return PreProcessResult.blockWithReply(AIChatMessage.createDataOperationResponse(true,message, userId.toString(),null));
    }


    /**
     * 记录饮食
     *
     * @param userId
     * @param root
     * @return
     */
    private PreProcessResult saveWeight(Long userId, JsonNode root, String content) {
        if (!hasDigit(content)){
            return PreProcessResult.blockWithReply(AIChatMessage.createChatMessage("⚠ 请输入体重，例如：“今天体重65kg” 或 “昨天体重130斤", userId.toString()));
        }
        JsonNode weightArray = root.get("weight");
        if (weightArray.isArray() && !weightArray.isEmpty()) {
            for (JsonNode w : weightArray) {

                String date = w.get("date").asText();
                String value = w.get("value").asText();
                TagHistory tagHistory = new TagHistory();
                tagHistory.setTagCode(TagCode.WEIGHT);
                tagHistory.setRecordTime(date);
                tagHistory.setUserId(userId);
                List<TagHistory> histories = tagHistoryService.getHistory(tagHistory);
                for (TagHistory history : histories) {
                    tagHistoryService.delete(history.getId());
                }
                tagHistory.setTagValue(value);
                tagHistoryService.insert(tagHistory);
            }
        }
        String message = root.get("message").asText();
        return PreProcessResult.blockWithReply(AIChatMessage.createDataOperationResponse(true,message, userId.toString(),null));

    }


    /**
     * 去掉 Markdown 代码块标记（```lang 和 ```），返回中间的纯内容
     *
     * @param input 原始字符串
     * @return 清理后的内容
     */
    public static String cleanCodeBlock(String input) {
        if (input == null) {
            return null;
        }
        // 去掉开头 ```xxx（可能有语言名和换行）
        String cleaned = input.replaceFirst("^```[a-zA-Z0-9]*\\s*", "");
        // 去掉结尾 ```
        cleaned = cleaned.replaceFirst("\\s*```$", "");
        return cleaned;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
