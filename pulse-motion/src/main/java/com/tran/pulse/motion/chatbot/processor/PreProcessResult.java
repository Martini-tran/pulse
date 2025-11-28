package com.tran.pulse.motion.chatbot.processor;

import com.tran.pulse.motion.chatbot.domain.AIChatMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * 预处理结果封装类
 * 用于封装预处理器处理后的决策结果和相关数据
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/9/19 15:39
 **/
public final class PreProcessResult {


    /** 预处理决策类型 */
    private final Decision decision;

    /** 当决策为BLOCK_WITH_REPLY时，需要立即回复给用户的消息 */
    private final AIChatMessage reply;

    /** 当决策为REDIRECT时，指定重定向的目标消息类型 */
    private final String redirectType;

    /**
     * 其他元数据信息
     */
    private Map<String,Object> meatDate = new HashMap<>();


    /**
     * 私有构造器，防止直接实例化
     *
     * @param d 决策类型
     * @param r 回复消息
     * @param t 重定向类型
     */
    private PreProcessResult(Decision d, AIChatMessage r, String t) {
        this.decision = d; this.reply = r; this.redirectType = t;
    }

    /**
     * 创建继续处理的结果
     * 表示消息通过预处理，可以继续后续流程
     *
     * @return 继续处理的结果
     */
    public static PreProcessResult proceed() {
        return new PreProcessResult(Decision.PROCEED, null, null);
    }

    /**
     * 创建阻止并回复的结果
     * 表示消息被阻止，需要立即回复用户指定内容
     *
     * @param reply 需要回复给用户的消息
     * @return 阻止并回复的结果
     */
    public static PreProcessResult blockWithReply(AIChatMessage reply) {
        return new PreProcessResult(Decision.BLOCK_WITH_REPLY, reply, null);
    }

    /**
     * 创建重定向的结果
     * 表示需要将消息重定向到其他类型的处理器
     *
     * @param newType 重定向的目标消息类型
     * @return 重定向的结果
     */
    public static PreProcessResult redirect(String newType) {
        return new PreProcessResult(Decision.REDIRECT, null, newType);
    }

    /**
     * 获取预处理决策
     *
     * @return 决策类型
     */
    public Decision decision() { return decision; }

    /**
     * 获取回复消息
     * 仅当决策为BLOCK_WITH_REPLY时有效
     *
     * @return 回复消息，可能为null
     */
    public AIChatMessage reply() { return reply; }

    /**
     * 获取重定向类型
     * 仅当决策为REDIRECT时有效
     *
     * @return 重定向的目标类型，可能为null
     */
    public String redirectType() { return redirectType; }


    /**
     * 获取元数据信息
     *
     * @return 元数据
     */
    public Map<String,Object> meatDate() { return meatDate; }


    /**
     * 是否包含某key
     * @param key
     * @return
     */
    public boolean containsMeatDate(String key){
        return meatDate.containsKey(key);
    }

    /**
     * 获取元数据信息
     * @param key
     * @return
     */
    public Object getMeatDate(String key){
        return meatDate.get(key);
    }


    /**
     * 向元数据添加信息
     *
     * @param key
     * @param value
     */
    public void addMeatDate(String key, Object value){
        meatDate.put(key, value);
    }


    /**
     * 向元数据添加信息
     *
     */
    public void addAllMeatDate(Map<String,Object> date){
        if(meatDate != null){
            meatDate.putAll(date);
        }
    }

}