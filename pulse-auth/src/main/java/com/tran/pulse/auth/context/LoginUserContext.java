package com.tran.pulse.auth.context;

import com.tran.pulse.auth.domain.LoginUser;

/**
 * 线程级别的登录用户上下文。
 *   使用 {@link ThreadLocal} 保障同一线程内任意位置都能快速拿到当前登录人信息。
 *   在请求开始（如 Spring MVC Filter / Interceptor）调用 {@link #set(LoginUser)}，
 *   在请求结束或异常处理后调用 {@link #clear()} 以防止内存泄漏。
 *   若你的业务会使用异步线程（@Async / CompletableFuture）并需要共享登录态，
 *   可以把实现改为 {@link InheritableThreadLocal}，或在异步任务里显式传参。
 *
 */
public final class LoginUserContext {

    /**
     * 线程本地存储，存放当前登录用户。
     */
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
        // 工具类禁止实例化
    }

    /**
     * 设置当前线程的登录用户。
     *
     * @param user 登录用户对象，不可为 null
     */
    public static void set(LoginUser user) {
        if (user == null) {
            throw new IllegalArgumentException("LoginUser cannot be null");
        }
        HOLDER.set(user);
    }

    /**
     * 获取当前线程的登录用户。
     *
     * @return LoginUser 或 null（未登录/未设置时）
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户 ID，若不存在返回 null。
     */
    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 清理 ThreadLocal，防止内存泄漏。（务必在请求完成后调用）
     */
    public static void clear() {
        HOLDER.remove();
    }
}
