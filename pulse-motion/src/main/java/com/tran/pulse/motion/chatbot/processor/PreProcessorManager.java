package com.tran.pulse.motion.chatbot.processor;

import com.tran.pulse.motion.chatbot.domain.AIChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预处理器管理器
 * 负责管理和执行所有预处理器的执行流程
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/9/19 15:45
 */
@Component
public class PreProcessorManager {

    /** 所有预处理器实例，按执行顺序排序 */
    private final List<PreProcessor> processors;

    /**
     * 构造器注入所有预处理器实例并按order排序
     *
     * @param processors 所有预处理器实例
     */
    public PreProcessorManager(List<PreProcessor> processors) {
        // 按 getOrder() 方法返回值升序排列，数值越小优先级越高
        this.processors = processors.stream()
                .sorted(Comparator.comparingInt(PreProcessor::getOrder))
                .collect(Collectors.toList());
    }

    /**
     * 执行所有预处理器的处理逻辑
     * 按照优先级顺序依次执行，遇到非PROCEED结果时立即返回
     *
     * @param session WebSocket会话
     * @param webSocketMessage WebSocket消息
     * @param message AI聊天消息
     * @return 预处理结果
     */
    public PreProcessResult process(WebSocketSession session,WebSocketMessage<?> webSocketMessage, AIChatMessage message) {
        Map<String, Object> meatDate = new HashMap<>();
        for (PreProcessor processor : processors) {
            try {
                PreProcessResult result = processor.apply(session, webSocketMessage, message);
                meatDate.putAll(result.meatDate());
                // 如果结果不是继续处理，立即返回结果
                if (result.decision() != Decision.PROCEED) {
                    return result;
                }

            } catch (Exception e) {
                // 处理预处理器执行异常，可以记录日志或返回错误结果
                // 这里可以根据具体需求决定是继续执行下一个处理器还是直接返回错误
                throw new PreProcessException("预处理器执行失败: " + processor.getClass().getSimpleName(), e);
            }
        }

        // 所有预处理器都返回PROCEED，表示可以继续处理
        PreProcessResult proceed = PreProcessResult.proceed();
        proceed.addAllMeatDate(meatDate);
        return proceed;
    }

    /**
     * 获取当前注册的预处理器数量
     *
     * @return 预处理器数量
     */
    public int getProcessorCount() {
        return processors.size();
    }

    /**
     * 预处理异常类
     */
    public static class PreProcessException extends RuntimeException {
        public PreProcessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}