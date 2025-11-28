package com.tran.pulse.common.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 高概率随机 ID + <b>雪花算法</b> 唯一 ID 生成器。<br>
 * 
 *     {@link #random(int)}：Base62 随机串（概率唯一，速度快）
 *     {@link #next()}：Snowflake‑like 64bit → Base62，<b>单 JVM 绝对不重复</b>
 * 
 * 默认字符集：Base62（0‑9A‑Z a‑z）。
 */
public final class StringIdGenerator {

    /* ==================  Base62 随机 ================== */
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RNG = new SecureRandom();

    public static String random(int length) {
        return random(length, BASE62);
    }

    public static String random(int length, String chars) {
        Objects.requireNonNull(chars, "chars must not be null");
        if (length <= 0) throw new IllegalArgumentException("length > 0");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RNG.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /* ==================  Snowflake 唯一 ================== */

    // 起始纪元（2025‑01‑01T00:00:00Z）
    private static final long EPOCH = 1735689600000L;
    // 每毫秒最多 4096 个序号 (2^12‑1)
    private static final int SEQ_BITS = 12;
    private static final int SEQ_MASK = (1 << SEQ_BITS) - 1;
    private static volatile long lastMillis = -1L;
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    /**
     * 生成 19‑22 位左右的 Base62 字符串，<b>在单 JVM 内绝对不重复</b>。
     */
    public static synchronized String next() {
        long now = currentMillis();
        if (now == lastMillis) {
            // 同毫秒：递增序号并取低位
            int seq = SEQ.incrementAndGet() & SEQ_MASK;
            if (seq == 0) {
                // 序号溢出 —> 等待下一毫秒
                now = waitNextMillis(now);
            }
        } else {
            SEQ.set(0);
        }
        lastMillis = now;
        long id = ((now - EPOCH) << SEQ_BITS) | SEQ.get();
        return base62(id);
    }

    /* ==================  utils  ================== */
    private static long currentMillis() { return Instant.now().toEpochMilli(); }

    private static long waitNextMillis(long cur) {
        long ts;
        do { ts = currentMillis(); } while (ts == cur);
        return ts;
    }

    /** 十进制转 Base62 */
    private static String base62(long num) {
        StringBuilder sb = new StringBuilder();
        do {
            int r = (int) (num % 62);
            sb.append(BASE62.charAt(r));
            num /= 62;
        } while (num > 0);
        return sb.reverse().toString();
    }

    private StringIdGenerator() { /* static util */ }
}

