package com.tran.pulse.motion.chatbot.service;

import com.chaincraft.ai.client.common.model.entity.AiConversation;
import com.chaincraft.ai.client.common.model.entity.AiModel;
import com.chaincraft.ai.client.core.mapper.AiModelMapper;
import com.chaincraft.ai.client.core.service.AiModelService;
import com.chaincraft.ai.client.core.service.ConversationService;
import com.tran.pulse.common.domain.entity.AiBusiness;
import com.tran.pulse.motion.chatbot.mapper.AiBusinessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author tran
 * @version 1.0.0.0
 * @date 2025/9/18 15:46
 **/
@Service
public class ChatbotService {


    @Autowired
    private AiBusinessMapper aiBusinessMapper;


    @Autowired
    private ConversationService conversationService;



    /**
     * 获取聊天ID
     * @param userid
     * @param businessKey
     * @return
     */
   public Long getAiConversation(Long userid, String businessKey) {
       AiBusiness aiBusiness = aiBusinessMapper.selectById(businessKey);
       AiConversation conversation = conversationService.createConversation(businessKey, aiBusiness.getSystem(), userid);
       return conversation.getId();
   }



    /**
     * 获取业务信息
     * @param businessKey
     * @return
     */
   public AiBusiness getAiBusiness(String businessKey) {
       return aiBusinessMapper.selectById(businessKey);
   }


}
