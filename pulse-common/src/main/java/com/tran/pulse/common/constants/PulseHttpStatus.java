package com.tran.pulse.common.constants;

/**
 * 全局HTTP状态码常量
 * 定义了常用的HTTP状态码，用于统一管理API响应状态。
 * 按照HTTP标准分为五大类：1xx信息响应、2xx成功、3xx重定向、4xx客户端错误、5xx服务器错误。
 * 
 *
 * @version 1.0.0.0
 * @date 2025/3/25 15:44
 * @Author tran
 **/
public class PulseHttpStatus {

    // ==================== 1xx 信息响应 ====================

    /**
     * 继续
     * 服务器已接收到请求头，客户端应继续发送请求体
     */
    public static final int CONTINUE = 100;

    /**
     * 切换协议
     * 服务器正在切换协议
     */
    public static final int SWITCHING_PROTOCOLS = 101;

    // ==================== 2xx 成功响应 ====================

    /**
     * 请求成功
     * 请求已成功，请求所希望的响应头或数据体将随此响应返回
     */
    public static final int OK = 200;

    /**
     * 已创建
     * 请求已经被实现，而且有一个新的资源已经依据请求的需要而建立
     */
    public static final int CREATED = 201;

    /**
     * 已接受
     * 服务器已接受请求，但尚未处理
     */
    public static final int ACCEPTED = 202;

    /**
     * 非权威信息
     * 服务器已成功处理了请求，但返回的实体头部元信息不是在原始服务器上有效的确定集合
     */
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;

    /**
     * 无内容
     * 服务器成功处理了请求，但不需要返回任何实体内容
     */
    public static final int NO_CONTENT = 204;

    /**
     * 重置内容
     * 服务器成功处理了请求，且没有返回任何内容
     */
    public static final int RESET_CONTENT = 205;

    /**
     * 部分内容
     * 服务器已经成功处理了部分GET请求
     */
    public static final int PARTIAL_CONTENT = 206;

    // ==================== 3xx 重定向 ====================

    /**
     * 多种选择
     * 请求的资源有一系列可供选择的回馈信息
     */
    public static final int MULTIPLE_CHOICES = 300;

    /**
     * 永久移动
     * 请求的资源已被永久的移动到新URI
     */
    public static final int MOVED_PERMANENTLY = 301;

    /**
     * 临时移动
     * 请求的资源现在临时从不同的URI响应请求
     */
    public static final int FOUND = 302;

    /**
     * 查看其它地址
     * 对应当前请求的响应可以在另一个URI上被找到
     */
    public static final int SEE_OTHER = 303;

    /**
     * 未修改
     * 如果客户端发送了一个带条件的GET请求且该请求已被允许，而文档的内容并没有改变
     */
    public static final int NOT_MODIFIED = 304;

    /**
     * 使用代理
     * 请求的资源必须通过指定的代理才能被访问
     */
    public static final int USE_PROXY = 305;

    /**
     * 临时重定向
     * 请求的资源在另一个URI
     */
    public static final int TEMPORARY_REDIRECT = 307;

    /**
     * 永久重定向
     * 请求的资源已被永久的移动到新URI
     */
    public static final int PERMANENT_REDIRECT = 308;

    // ==================== 4xx 客户端错误 ====================

    /**
     * 错误请求
     * 服务器不理解请求的语法
     */
    public static final int BAD_REQUEST = 400;

    /**
     * 未授权
     * 请求要求身份验证
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 需要付费
     * 保留，将来使用
     */
    public static final int PAYMENT_REQUIRED = 402;

    /**
     * 禁止访问
     * 服务器理解请求客户端的请求，但是拒绝执行此请求
     */
    public static final int FORBIDDEN = 403;

    /**
     * 未找到
     * 服务器无法根据客户端的请求找到资源
     */
    public static final int NOT_FOUND = 404;

    /**
     * 方法禁用
     * 客户端请求中的方法被禁用
     */
    public static final int METHOD_NOT_ALLOWED = 405;

    /**
     * 不接受
     * 服务器无法使用请求的内容特性响应请求的网页
     */
    public static final int NOT_ACCEPTABLE = 406;

    /**
     * 需要代理授权
     * 此状态代码与401类似，但指定请求者应当使用代理进行授权
     */
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

    /**
     * 请求超时
     * 服务器等候请求时发生超时
     */
    public static final int REQUEST_TIMEOUT = 408;

    /**
     * 冲突
     * 服务器在完成请求时发生冲突
     */
    public static final int CONFLICT = 409;

    /**
     * 已删除
     * 客户端请求的资源已经不存在
     */
    public static final int GONE = 410;

    /**
     * 需要有效长度
     * 服务器不接受不含有效内容长度标头字段的请求
     */
    public static final int LENGTH_REQUIRED = 411;

    /**
     * 未满足前提条件
     * 服务器未满足请求者在请求中设置的其中一个前提条件
     */
    public static final int PRECONDITION_FAILED = 412;

    /**
     * 请求实体过大
     * 服务器无法处理请求，因为请求实体过大
     */
    public static final int PAYLOAD_TOO_LARGE = 413;

    /**
     * 请求的URI过长
     * 请求的URI过长，服务器无法处理
     */
    public static final int URI_TOO_LONG = 414;

    /**
     * 不支持的媒体类型
     * 请求的格式不受请求页面的支持
     */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * 请求范围不符合要求
     * 如果页面无法提供请求的范围，则服务器会返回此状态代码
     */
    public static final int RANGE_NOT_SATISFIABLE = 416;

    /**
     * 未满足期望值
     * 服务器未满足"期望"请求标头字段的要求
     */
    public static final int EXPECTATION_FAILED = 417;

    /**
     * 我是茶壶
     * 愚人节玩笑，服务器拒绝冲泡咖啡，因为它是个茶壶
     */
    public static final int I_AM_A_TEAPOT = 418;

    /**
     * 请求参数错误
     * 请求格式正确，但是由于含有语义错误，无法响应
     */
    public static final int UNPROCESSABLE_ENTITY = 422;

    /**
     * 资源被锁定
     * 当前资源被锁定
     */
    public static final int LOCKED = 423;

    /**
     * 依赖失败
     * 由于之前的某个请求发生的错误，导致当前请求失败
     */
    public static final int FAILED_DEPENDENCY = 424;

    /**
     * 需要升级
     * 客户端应当切换到TLS/1.0
     */
    public static final int UPGRADE_REQUIRED = 426;

    /**
     * 需要前提条件
     * 原服务器要求该请求满足一定条件
     */
    public static final int PRECONDITION_REQUIRED = 428;

    /**
     * 请求过多
     * 用户在给定的时间内发送了太多的请求
     */
    public static final int TOO_MANY_REQUESTS = 429;

    /**
     * 请求头字段太大
     * 服务器不愿处理请求，因为一个或多个头字段过大
     */
    public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

    /**
     * 因法律原因不可用
     * 由于法律原因，服务器无法提供请求的资源
     */
    public static final int UNAVAILABLE_FOR_LEGAL_REASONS = 451;

    // ==================== 5xx 服务器错误 ====================

    /**
     * 服务器内部错误
     * 服务器遇到错误，无法完成请求
     */
    public static final int INTERNAL_SERVER_ERROR = 500;

    /**
     * 尚未实施
     * 服务器不具备完成请求的功能
     */
    public static final int NOT_IMPLEMENTED = 501;

    /**
     * 错误网关
     * 服务器作为网关或代理，从上游服务器收到无效响应
     */
    public static final int BAD_GATEWAY = 502;

    /**
     * 服务不可用
     * 服务器目前无法使用（由于超载或停机维护）
     */
    public static final int SERVICE_UNAVAILABLE = 503;

    /**
     * 网关超时
     * 服务器作为网关或代理，但是没有及时从上游服务器收到请求
     */
    public static final int GATEWAY_TIMEOUT = 504;

    /**
     * HTTP版本不受支持
     * 服务器不支持请求中所用的HTTP协议版本
     */
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

    /**
     * 变体协商
     * 服务器存在内部配置错误
     */
    public static final int VARIANT_ALSO_NEGOTIATES = 506;

    /**
     * 存储空间不足
     * 服务器无法存储完成请求所必须的内容
     */
    public static final int INSUFFICIENT_STORAGE = 507;

    /**
     * 检测到循环
     * 服务器在处理请求时陷入死循环
     */
    public static final int LOOP_DETECTED = 508;

    /**
     * 不扩展
     * 获取资源所需要的策略并没有被满足
     */
    public static final int NOT_EXTENDED = 510;

    /**
     * 需要网络认证
     * 客户端需要进行身份验证才能获得网络访问权限
     */
    public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;

    // ==================== 业务状态码 ====================

    /**
     * 业务操作成功
     */
    public static final int SUCCESS = 200;

    /**
     * 业务操作失败
     */
    public static final int FAILURE = 500;

    /**
     * 参数验证失败
     */
    public static final int PARAM_ERROR = 400;

    /**
     * 认证失败
     */
    public static final int AUTH_FAILED = 401;

    /**
     * 权限不足
     */
    public static final int ACCESS_DENIED = 403;

    /**
     * 资源不存在
     */
    public static final int RESOURCE_NOT_FOUND = 404;

    /**
     * 操作频繁
     */
    public static final int RATE_LIMITED = 429;

    /**
     * 系统异常
     */
    public static final int SYSTEM_ERROR = 500;


    // ==================== pulse自定义状态吗 ====================

    /**
     * 用户未配置模板
     */
    public static final int PULSE_MOTION_TEMPLATE_NONE = 1000;



    // ==================== 工具方法 ====================

    /**
     * 判断是否为成功状态码
     *
     * @param statusCode 状态码
     * @return 是否为成功状态码
     */
    public static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * 判断是否为重定向状态码
     *
     * @param statusCode 状态码
     * @return 是否为重定向状态码
     */
    public static boolean isRedirection(int statusCode) {
        return statusCode >= 300 && statusCode < 400;
    }

    /**
     * 判断是否为客户端错误状态码
     *
     * @param statusCode 状态码
     * @return 是否为客户端错误状态码
     */
    public static boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * 判断是否为服务器错误状态码
     *
     * @param statusCode 状态码
     * @return 是否为服务器错误状态码
     */
    public static boolean isServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }

    /**
     * 判断是否为错误状态码
     *
     * @param statusCode 状态码
     * @return 是否为错误状态码
     */
    public static boolean isError(int statusCode) {
        return statusCode >= 400;
    }

    /**
     * 获取状态码描述
     *
     * @param statusCode 状态码
     * @return 状态码描述
     */
    public static String getReasonPhrase(int statusCode) {
        switch (statusCode) {
            case OK: return "OK";
            case CREATED: return "Created";
            case NO_CONTENT: return "No Content";
            case BAD_REQUEST: return "Bad Request";
            case UNAUTHORIZED: return "Unauthorized";
            case FORBIDDEN: return "Forbidden";
            case NOT_FOUND: return "Not Found";
            case METHOD_NOT_ALLOWED: return "Method Not Allowed";
            case CONFLICT: return "Conflict";
            case TOO_MANY_REQUESTS: return "Too Many Requests";
            case INTERNAL_SERVER_ERROR: return "Internal Server Error";
            case NOT_IMPLEMENTED: return "Not Implemented";
            case BAD_GATEWAY: return "Bad Gateway";
            case SERVICE_UNAVAILABLE: return "Service Unavailable";
            case GATEWAY_TIMEOUT: return "Gateway Timeout";
            default: return "Unknown Status";
        }
    }
}