package com.github.ompc.greys.core.advisor;

import com.github.ompc.greys.core.advisor.asm.AsmMethod;
import com.github.ompc.greys.core.advisor.asm.AsmMethodAdviceAdapter;
import com.github.ompc.greys.core.advisor.listener.AdviceListener;
import com.github.ompc.greys.core.advisor.listener.InvokeTraceable;
import com.github.ompc.greys.core.util.GaCheckUtils;
import com.github.ompc.greys.core.util.GaStringUtils;
import com.github.ompc.greys.core.util.LogUtil;
import com.github.ompc.greys.core.util.affect.EnhancerAffect;
import com.github.ompc.greys.core.util.collection.GaStack;
import com.github.ompc.greys.core.util.collection.ThreadUnsafeFixGaStack;
import com.github.ompc.greys.core.util.collection.ThreadUnsafeGaStack;
import com.github.ompc.greys.core.util.matcher.Matcher;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知编织者<br/>
 * <p/>
 * <h2>线程帧栈与执行帧栈</h2>
 * 编织者在执行通知的时候有两个重要的栈:线程帧栈(threadFrameStack),执行帧栈(frameStack)
 * <p/>
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/5/17
 */
public class AdviceWeaver extends ClassVisitor implements Opcodes {

    private final static Logger logger = LogUtil.getLogger();

    /**
     * 线程帧栈堆栈大小
     */
    private final static int FRAME_STACK_SIZE = 7;
    /**
     * 通知监听器集合
     * adviceId -> listener
     * FIXME: adviceId是用于标识什么信息，用处是什么？
     */
    private final static Map<Integer, AdviceListener> advices = new ConcurrentHashMap<>();
    /**
     * 线程帧封装
     */
    private static final Map<Thread, GaStack<GaStack<Object>>> threadBoundContexts = new ConcurrentHashMap<>();
    /**
     * 防止自己递归调用
     */
    private static final ThreadLocal<Boolean> isSelfCallRef = ThreadLocal.withInitial(() -> false);

    /**
     * 方法开始<br/>
     * 用于编织通知器,外部不会直接调用
     *
     * @param loader     类加载器
     * @param adviceId   通知ID
     * @param className  类名
     * @param methodName 方法名
     * @param methodDesc 方法描述
     * @param target     返回结果
     *                   若为无返回值方法(void),则为null
     * @param args       参数列表
     */
    public static void methodOnBegin(int adviceId, ClassLoader loader, String className, String methodName,
                                     String methodDesc, Object target, Object[] args) {
        //非法adviseId
        if (!advices.containsKey(adviceId)) {
            return;
        }

        //防止嵌套
        if (isSelfCallRef.get()) {
            return;
        }

        try {
            isSelfCallRef.set(true);
            //构建执行帧栈,保护当前的执行现场
            final GaStack<Object> frameStack = new ThreadUnsafeFixGaStack<>(FRAME_STACK_SIZE);
            frameStack.push(loader);
            frameStack.push(className);
            frameStack.push(methodName);
            frameStack.push(methodDesc);
            frameStack.push(target);
            frameStack.push(args);

            final AdviceListener listener = getListener(adviceId);
            frameStack.push(listener);

            //获取通知器并做前置通知
            notifyBefore(listener, loader, className, methodName, methodDesc, target, args);

            //保护当前执行帧栈,压入线程帧栈
            threadFrameStackPush(frameStack);
        } finally {
            isSelfCallRef.set(false);
        }
    }

    /**
     * 方法以返回结束<br/>
     * 用于编织通知器,外部不会直接调用
     *
     * @param returnObject 返回对象
     *                     若目标为静态方法,则为null
     * @param adviceId
     */
    public static void methodOnReturnEnd(Object returnObject, int adviceId) {
        methodOnEnd(adviceId, false, returnObject);
    }

    /**
     * 方法以抛异常结束<br/>
     * 用于编织通知器,外部不会直接调用
     *
     * @param throwable 抛出异常
     * @param adviceId
     */
    public static void methodOnThrowingEnd(Throwable throwable, int adviceId) {
        methodOnEnd(adviceId, true, throwable);
    }

    /**
     * 所有的返回都统一处理
     *
     * @param adviceId
     * @param isThrowing        标记正常返回结束还是抛出异常结束
     * @param returnOrThrowable 正常返回或者抛出异常对象
     */
    private static void methodOnEnd(int adviceId, boolean isThrowing, Object returnOrThrowable) {
        if (!advices.containsKey(adviceId)) {
            return;
        }

        if (isSelfCallRef.get()) {
            return;
        }

        try {
            isSelfCallRef.set(true);
            //弹射线程帧栈,恢复Begin所保护的执行帧栈
            final GaStack<Object> frameStack = threadFrameStackPop();
            // 用于保护reg和before执行并发的情况
            // 如果before没有注入,则不对end做任何处理
            if (null == frameStack) {
                return;
            }

            // 弹射执行帧栈,恢复Begin所保护的现场
            final AdviceListener listener = (AdviceListener) frameStack.pop();
            final Object[] args = (Object[]) frameStack.pop();
            final Object target = frameStack.pop();
            final String methodDesc = (String) frameStack.pop();
            final String methodName = (String) frameStack.pop();
            final String className = (String) frameStack.pop();
            final ClassLoader loader = (ClassLoader) frameStack.pop();

            if (isThrowing) {
                notifyAfterThrowing(listener, loader, className, methodName, methodDesc, target, args, (Throwable) returnOrThrowable);
            } else {
                //返回通知
                notifyAfterReturning(listener, loader, className, methodName, methodDesc, target, args, returnOrThrowable);
            }
        } finally {
            isSelfCallRef.set(false);
        }
    }

    /**
     * 方法内部调用开始
     *
     * @param adviceId   通知ID
     * @param lineNumber 代码行号
     * @param owner      调用类名
     * @param name       调用方法名
     * @param desc       调用方法描述
     */
    public static void methodOnInvokeBeforeTracing(int adviceId, Integer lineNumber, String owner, String name, String desc) {
        if (!advices.containsKey(adviceId)) {
            return;
        }

        final InvokeTraceable listener = (InvokeTraceable) getListener(adviceId);
        if (null != listener) {
            try {
                listener.invokeBeforeTracing(lineNumber, owner, name, desc);
            } catch (Throwable t) {
                logger.warn("advice notifyBefore tracing failed.", t);
            }
        }
    }

    /**
     * 方法内部调用结束(正常返回)
     *
     * @param adviceId   通知ID
     * @param lineNumber 代码行号
     * @param owner      调用类名
     * @param name       调用方法名
     * @param desc       调用方法描述
     */
    public static void methodOnInvokeAfterTracing(int adviceId, Integer lineNumber, String owner, String name, String desc) {
        if (!advices.containsKey(adviceId)) {
            return;
        }
        final InvokeTraceable listener = (InvokeTraceable) getListener(adviceId);
        if (null != listener) {
            try {
                listener.invokeAfterTracing(lineNumber, owner, name, desc);
            } catch (Throwable t) {
                logger.warn("advice after tracing failed.", t);
            }
        }
    }

    /**
     * 方法内部调用结束(异常返回)
     *
     * @param adviceId       通知ID
     * @param lineNumber     代码行号
     * @param owner          调用类名
     * @param name           调用方法名
     * @param desc           调用方法描述
     * @param throwException 抛出的异常
     */
    public static void methodOnInvokeThrowTracing(int adviceId, Integer lineNumber, String owner, String name, String desc, String throwException) {
        if (!advices.containsKey(adviceId)) {
            return;
        }
        final InvokeTraceable listener = (InvokeTraceable) getListener(adviceId);
        if (null != listener) {
            try {
                listener.invokeThrowTracing(lineNumber, owner, name, desc, throwException);
            } catch (Throwable t) {
                logger.warn("advice throw tracing failed.", t);
            }
        }
    }

    private static void notifyBefore(AdviceListener listener,
                                     ClassLoader loader, String className, String methodName, String methodDesc,
                                     Object target, Object[] args) {
        if (null != listener) {
            try {
                listener.before(loader, className, methodName, methodDesc, target, args);
            } catch (Throwable t) {
                logger.warn("advice notify before failed.", t);
            }
        }
    }

    private static void notifyAfterReturning(AdviceListener listener,
                                             ClassLoader loader, String className, String methodName, String methodDesc,
                                             Object target, Object[] args, Object returnObject) {
        if (null != listener) {
            try {
                listener.afterReturning(loader, className, methodName, methodDesc, target, args, returnObject);
            } catch (Throwable t) {
                logger.warn("advice notify returning failed.", t);
            }
        }
    }

    private static void notifyAfterThrowing(AdviceListener listener,
                                            ClassLoader loader, String className, String methodName, String methodDesc,
                                            Object target, Object[] args, Throwable throwable) {
        if (null != listener) {
            try {
                listener.afterThrowing(loader, className, methodName, methodDesc, target, args, throwable);
            } catch (Throwable t) {
                logger.warn("advice notify throwing failed.", t);
            }
        }
    }

    private final int adviceId;
    private final boolean isTracing;
    private final String javaClassName;
    private final Matcher<AsmMethod> asmMethodMatcher;
    private final EnhancerAffect affect;

    /**
     * 构建通知编织器
     *
     * @param adviceId          通知ID
     * @param isTracing         可跟踪方法调用
     * @param internalClassName 类名称(透传)，例如 java/lang/String
     * @param asmMethodMatcher  asm方法匹配
     *                          只有匹配上的方法才会被织入通知器
     * @param affect            影响计数
     * @param cv                ClassVisitor for ASM
     */
    public AdviceWeaver(
            final int adviceId,
            final boolean isTracing,
            final String internalClassName,
            final Matcher<AsmMethod> asmMethodMatcher,
            final EnhancerAffect affect,
            final ClassVisitor cv) {
        super(ASM5, cv);
        this.adviceId = adviceId;
        this.isTracing = isTracing;
        this.javaClassName = GaStringUtils.tranClassName(internalClassName);
        this.asmMethodMatcher = asmMethodMatcher;
        this.affect = affect;
    }

    /**
     * 是否需要忽略
     */
    private boolean isIgnore(MethodVisitor mv, int access, String name, String desc) {
        return null == mv
                || isAbstract(access)
                //为什么需要忽略类构造方法
                || GaCheckUtils.isEquals(name, "<clinit>")
                || !asmMethodMatcher.matching(new AsmMethod(name, desc));
    }

    /**
     * 是否抽象属性
     */
    private static boolean isAbstract(int access) {
        return (ACC_ABSTRACT & access) == ACC_ABSTRACT;
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final String[] exceptions) {

        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        //如果需要忽略则跳过
        if (isIgnore(mv, access, name, desc)) {
            return mv;
        }

        //编织方法计数
        affect.mCnt(1);
        return new AsmMethodAdviceAdapter(mv, access, name, desc, signature, exceptions, javaClassName, isTracing, adviceId);
    }

    //=================================== 功能管理函数

    /**
     * 线程帧栈压栈<br/>
     * 将当前执行帧栈压入线程栈
     */
    private static void threadFrameStackPush(GaStack<Object> frameStack) {
        final Thread thread = Thread.currentThread();
        GaStack<GaStack<Object>> threadFrameStack = threadBoundContexts.get(thread);
        if (null == threadFrameStack) {
            threadBoundContexts.put(thread, threadFrameStack = new ThreadUnsafeGaStack<>());
        }
        threadFrameStack.push(frameStack);
    }

    private static GaStack<Object> threadFrameStackPop() {
        final GaStack<GaStack<Object>> stackGaStack = threadBoundContexts.get(Thread.currentThread());
        //用于保护reg和before并发导致before/end乱序的场景
        if (null == stackGaStack || stackGaStack.isEmpty()) {
            return null;
        }
        return stackGaStack.pop();
    }

    private static AdviceListener getListener(int adviceId) {
        return advices.get(adviceId);
    }

    /**
     * 注册监听器
     *
     * @param adviceId 通知ID
     * @param listener 通知监听器
     */
    public static void reg(int adviceId, AdviceListener listener) {

        // 触发监听器创建
        listener.create();

        // 注册监听器
        advices.put(adviceId, listener);

        logger.info("reg adviceId={};listener={}", adviceId, listener);
    }

    /**
     * 注销监听器
     *
     * @param adviceId 通知ID
     */
    public static void unReg(int adviceId) {

        // 注销监听器
        final AdviceListener listener = advices.remove(adviceId);

        // 触发监听器销毁
        if (null != listener) {
            listener.destroy();
        }

        logger.info("unReg adviceId={};listener={}", adviceId, listener);
    }

}
