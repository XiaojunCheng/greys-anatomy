package com.github.ompc.greys.core.util;

/**
 * 调用耗时
 *
 * @author vlinux
 * @date 16/6/1
 */
public class InvokeCost {

    private final ThreadLocal<Long> timestampRef = new ThreadLocal<>();

    public long begin() {
        final long timestamp = System.currentTimeMillis();
        timestampRef.set(timestamp);
        return timestamp;
    }

    public long cost() {
        return System.currentTimeMillis() - timestampRef.get();
    }

}
