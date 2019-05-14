package com.github.ompc.greys.agent;

import java.lang.reflect.Method;

/**
 * 间谍类<br/>
 * 藏匿在各个ClassLoader中
 * Created by oldmanpushcart@gmail.com on 15/8/23.
 */
public class Spy {

    /**
     * -- 各种Advice的钩子引用 --
     */
    public static volatile Method ON_BEFORE_METHOD;
    public static volatile Method ON_RETURN_METHOD;
    public static volatile Method ON_THROWS_METHOD;
    public static volatile Method BEFORE_INVOKING_METHOD;
    public static volatile Method AFTER_INVOKING_METHOD;
    public static volatile Method THROW_INVOKING_METHOD;

    /**
     * 代理重设方法
     */
    public static volatile Method AGENT_RESET_METHOD;

    /**
     * 用于普通的间谍初始化
     *
     * @param onBeforeMethod
     * @param onReturnMethod
     * @param onThrowsMethod
     * @param beforeInvokingMethod
     * @param afterInvokingMethod
     * @param throwInvokingMethod
     */
    public static void init(
            Method onBeforeMethod,
            Method onReturnMethod,
            Method onThrowsMethod,
            Method beforeInvokingMethod,
            Method afterInvokingMethod,
            Method throwInvokingMethod) {
        set(onBeforeMethod, onReturnMethod, onThrowsMethod, beforeInvokingMethod, afterInvokingMethod, throwInvokingMethod, AGENT_RESET_METHOD);
    }

    public static void set(
            Method onBeforeMethod,
            Method onReturnMethod,
            Method onThrowsMethod,
            Method beforeInvokingMethod,
            Method afterInvokingMethod,
            Method throwInvokingMethod,
            Method agentResetMethod) {
        ON_BEFORE_METHOD = onBeforeMethod;
        ON_RETURN_METHOD = onReturnMethod;
        ON_THROWS_METHOD = onThrowsMethod;
        BEFORE_INVOKING_METHOD = beforeInvokingMethod;
        AFTER_INVOKING_METHOD = afterInvokingMethod;
        THROW_INVOKING_METHOD = throwInvokingMethod;
        AGENT_RESET_METHOD = agentResetMethod;
    }

    /**
     * 用于启动线程初始化
     */
    public static void initForAgentLauncher(
            Method onBeforeMethod,
            Method onReturnMethod,
            Method onThrowsMethod,
            Method beforeInvokingMethod,
            Method afterInvokingMethod,
            Method throwInvokingMethod,
            Method agentResetMethod) {
        set(onBeforeMethod, onReturnMethod, onThrowsMethod, beforeInvokingMethod, afterInvokingMethod, throwInvokingMethod, agentResetMethod);
    }

    public static void clean() {
        set(null, null, null, null, null, null, null);
    }

}
