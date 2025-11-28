package com.tran.pulse.motion.home.service.impl;

import com.chaincraft.ai.client.common.client.AiClient;
import com.chaincraft.ai.client.common.model.entity.AiModel;
import com.chaincraft.ai.client.common.model.request.AiRequest;
import com.chaincraft.ai.client.common.model.request.Message;
import com.chaincraft.ai.client.common.model.response.AiResponse;
import com.chaincraft.ai.client.common.template.TemplateContext;
import com.chaincraft.ai.client.core.service.AiModelService;
import com.tran.pulse.auth.context.LoginUserContext;
import com.tran.pulse.cache.util.CacheUtil;
import com.tran.pulse.common.constants.BusinessCode;
import com.tran.pulse.common.domain.entity.AiBusiness;
import com.tran.pulse.common.domain.entity.TagHistory;
import com.tran.pulse.common.domain.entity.TagUser;
import com.tran.pulse.common.exception.PulseException;
import com.tran.pulse.common.util.JacksonUtils;
import com.tran.pulse.common.util.StringUtils;
import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import com.tran.pulse.motion.chatbot.service.ChatbotService;
import com.tran.pulse.motion.home.domain.RecommendationDto;
import com.tran.pulse.motion.home.domain.ProgressDto;
import com.tran.pulse.motion.home.service.HomeService;
import com.tran.pulse.motion.tag.domian.dto.TagDto;
import com.tran.pulse.motion.tag.service.TagHistoryService;
import com.tran.pulse.motion.tag.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/9/24 16:51
 **/
@Service
public class HomeServiceImpl implements HomeService {


    private static final Logger logger = LoggerFactory.getLogger(HomeServiceImpl.class);

    public static final String HOME_ADVICE_KEY = "home:advice_key:";
    public static final String HOME_TODAY_SUMMARY_KEY = "home:today_summary:";

    @Autowired
    private TagHistoryService tagHistoryService;
    
    @Autowired
    private AiClient aiClient;

    @Autowired
    private AiModelService aiModelService;

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private TagService tagService;

    @Override
    public List<RecommendationDto> getRecommendation() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new PulseException("请登录后访问");
        }
        if (CacheUtil.get(HOME_ADVICE_KEY + userId) != null){
            String adviceStr = CacheUtil.get(HOME_ADVICE_KEY + userId, String.class);
            List<RecommendationDto> recommendationDtos = JacksonUtils.fromJsonToList(adviceStr, RecommendationDto.class);
            return  recommendationDtos;
        }

        Map<String, Object> userInfo = getUserInfo();
        // 2) 读取业务与模型并校验
        AiBusiness aiBusiness = chatbotService.getAiBusiness(BusinessCode.TODAY_ADVICE);
        AiRequest aiRequest = getAiRequest(aiBusiness, userInfo);
        try {
            AiResponse resp = aiClient.chat(aiRequest);
            String content = (resp != null) ? resp.getContent() : null;
            content = cleanCodeBlock(content);
            List<RecommendationDto> progressDto = JacksonUtils.fromJsonToList(content, RecommendationDto.class);
            CacheUtil.put(HOME_ADVICE_KEY + userId, progressDto,43200);
            return progressDto;
        } catch (Exception e) {
            logger.error("获取首页信息失败",e);
            throw new PulseException("获取首页信息失败");

        }
    }

    @Override
    public ProgressDto getProgress() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new PulseException("请登录后访问");
        }
        if (CacheUtil.get(HOME_TODAY_SUMMARY_KEY + userId) != null){
            ProgressDto summary = CacheUtil.get(HOME_TODAY_SUMMARY_KEY + userId, ProgressDto.class);
            return  summary;
        }

        Map<String, Object> userInfo = getUserInfo();
        // 2) 读取业务与模型并校验
        AiBusiness aiBusiness = chatbotService.getAiBusiness(BusinessCode.TODAY_SUMMARY);
        AiRequest aiRequest = getAiRequest(aiBusiness, userInfo);
        try {
            AiResponse resp = aiClient.chat(aiRequest);
            String content = (resp != null) ? resp.getContent() : null;
            content = cleanCodeBlock(content);
            ProgressDto progressDto = JacksonUtils.fromJson(content, ProgressDto.class);
            CacheUtil.put(HOME_TODAY_SUMMARY_KEY + userId, progressDto,43200);
            return progressDto;
        } catch (Exception e) {
            logger.error("获取首页信息失败",e);
            throw new PulseException("获取首页信息失败");

        }
    }

    private AiRequest getAiRequest(AiBusiness aiBusiness, Map<String, Object> userInfo) {
        AiModel aiModel = aiModelService.getById(aiBusiness.getAiModelId());
        List<Message> messages = Collections.singletonList(Message.user("我的信息如下: 性别：#{gender}；出生日期：#{birth_date}；身高：#{height}；今日体重：#{current_weight}kg；#{delta_7d_kg}；"));
        AiRequest aiRequest = AiRequest.builder()
                .modelCode(aiModel.getModelCode())
                .systemPrompt(Objects.toString(aiBusiness.getSystem(), null)) // null 安全
                .messages(messages)
                .enableTemplate(true)
                .templateContext(TemplateContext.create().addVariables(userInfo))
                .build();
        return aiRequest;
    }


    /**
     * 获取用户信息
     *
     * @return
     */
    public Map<String, Object> getUserInfo() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new PulseException("请登录后访问");
        }
        Map<String, Object> resultMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 今天
        String today = sdf.format(calendar.getTime());
        // 7天前
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String sevenDaysAgo = sdf.format(calendar.getTime());
        List<TagHistory> historyWeight = tagHistoryService.getHistoryWeight(sevenDaysAgo, today, userId.toString());
        String result = historyWeight.stream()
                .map(TagHistory::getTagValue)                    // 取出属性 a
                .collect(Collectors.joining(","));
        List<TagUser> userTags = tagService.getUserTags(userId);
        for (TagUser userTag : userTags) {
            resultMap.put(userTag.getTagCode(), userTag.getTagValue());
        }
        resultMap.put("delta_7d_kg", "");
        if (StringUtils.isNotBlank(result)) {
            resultMap.put("delta_7d_kg", sevenDaysAgo + "-" + today + "体重变换:" + result);
        }
        return resultMap;
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

}
