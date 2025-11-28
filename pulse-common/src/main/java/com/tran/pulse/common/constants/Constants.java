package com.tran.pulse.common.constants;


import java.util.Arrays;
import java.util.List;

/**
 * 公共常量定义
 * @date 2025-03-22
 * @author tran
 */
public class Constants {

    /**
     * 用户状态常量
     */
    // 注册完成
    public static final String STATUS_ENABLED = "1000";

    // 用户完成注册
    public static final String STATUS_ENABLED_2000 = "2000";
    public static final String STATUS_DISABLED = "0000"; // 禁用


    /**
     * 用户角色信息
     */
    /** 超级管理员 */
    public static final Long SUPER_ADMIN = 1L;

    /** 管理员 */
    public static final Long ADMIN = 2L;

    /** 用户管理员 */
    public static final Long USER_MANAGER = 3L;

    /** 普通用户 */
    public static final Long ORDINARY_USER =4L;

    /**
     * YES OR NO
     *
     */
    public static final Integer YES = 1;
    public static final Integer NO = 0;

    /**
     * ai
     */
    public static final List<String> SUPPORTED_PROVIDERS = Arrays.asList(
            Constants.AI_PROVIDER_OPENAI, Constants.AI_PROVIDER_DEEPSEEK,Constants.AI_PROVIDER_ANTHROPIC,
            Constants.AI_PROVIDER_GOOGLE,Constants.AI_PROVIDER_AZURE
    );
    public static final String AI_PROVIDER_OPENAI = "openai";
    public static final String AI_PROVIDER_DEEPSEEK = "deepseek";
    public static final String AI_PROVIDER_ANTHROPIC = "anthropic";
    public static final String AI_PROVIDER_GOOGLE = "google";
    public static final String AI_PROVIDER_AZURE = "azure";

    public static final String AI_MESSAGE_SYSTEM = "system";



    /**
     * 跳过认证
     */
    public static final String SKIP_JWT_CHECK = "SKIP_JWT_CHECK";

    /**
     * 用户ID
     */
    public static final String USER_ID = "userId";

}

