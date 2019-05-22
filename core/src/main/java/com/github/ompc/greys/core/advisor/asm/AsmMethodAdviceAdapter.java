package com.github.ompc.greys.core.advisor.asm;

import com.github.ompc.greys.core.GlobalOptions;
import com.github.ompc.greys.core.advisor.lock.Block;
import com.github.ompc.greys.core.advisor.lock.CodeLock;
import com.github.ompc.greys.core.util.GaStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author chengxiaojun
 * @date 2019-05-22
 */
public class AsmMethodAdviceAdapter extends AdviceAdapter {

    /**
     * -- Label for try...catch block
     */
    private final Label beginLabel = new Label();
    private final Label endLabel = new Label();
    //
    /**
     * -- KEY of advice --
     */
    private final int KEY_GREYS_ADVICE_BEFORE_METHOD = 0;
    private final int KEY_GREYS_ADVICE_RETURN_METHOD = 1;
    private final int KEY_GREYS_ADVICE_THROWS_METHOD = 2;
    private final int KEY_GREYS_ADVICE_BEFORE_INVOKING_METHOD = 3;
    private final int KEY_GREYS_ADVICE_AFTER_INVOKING_METHOD = 4;
    private final int KEY_GREYS_ADVICE_THROW_INVOKING_METHOD = 5;
    /**
     * -- KEY of ASM_TYPE or ASM_METHOD --
     */
    private final Type ASM_TYPE_SPY = Type.getType("Lcom/github/ompc/greys/agent/Spy;");
    private final Type ASM_TYPE_OBJECT = Type.getType(Object.class);
    private final Type ASM_TYPE_OBJECT_ARRAY = Type.getType(Object[].class);
    private final Type ASM_TYPE_CLASS = Type.getType(Class.class);
    private final Type ASM_TYPE_INTEGER = Type.getType(Integer.class);
    private final Type ASM_TYPE_CLASS_LOADER = Type.getType(ClassLoader.class);
    private final Type ASM_TYPE_STRING = Type.getType(String.class);
    private final Type ASM_TYPE_THROWABLE = Type.getType(Throwable.class);
    private final Type ASM_TYPE_INT = Type.getType(int.class);
    private final Type ASM_TYPE_METHOD = Type.getType(java.lang.reflect.Method.class);
    private final Method ASM_METHOD_METHOD_INVOKE = Method.getMethod("Object invoke(Object,Object[])");
    /**
     * 代码锁
     */
    private final CodeLock codeLockForTracing = new TracingAsmCodeLock(this);

    private final String name;
    private final String desc;
    private final String javaClassName;
    private final boolean isTracing;
    private final int adviceId;

    public AsmMethodAdviceAdapter(MethodVisitor mv,
                                  int access, final String name, final String desc,
                                  final String signature, final String[] exceptions,
                                  final String javaClassName, boolean isTracing, int adviceId) {
        super(ASM5, new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions), access, name, desc);
        this.name = name;
        this.desc = desc;
        this.javaClassName = javaClassName;
        this.isTracing = isTracing;
        this.adviceId = adviceId;
    }

    private void _debug(final StringBuilder append, final String msg) {
        if (!GlobalOptions.isDebugForAsm) {
            return;
        }

        // println msg
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        if (StringUtils.isBlank(append.toString())) {
            visitLdcInsn(append.append(msg).toString());
        } else {
            visitLdcInsn(append.append(" >> ").append(msg).toString());
        }

        visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    /**
     * 加载通知方法
     *
     * @param keyOfMethod 通知方法KEY
     */
    private void loadAdviceMethod(int keyOfMethod) {

        switch (keyOfMethod) {

            case KEY_GREYS_ADVICE_BEFORE_METHOD: {
                getStatic(ASM_TYPE_SPY, "ON_BEFORE_METHOD", ASM_TYPE_METHOD);
                break;
            }

            case KEY_GREYS_ADVICE_RETURN_METHOD: {
                getStatic(ASM_TYPE_SPY, "ON_RETURN_METHOD", ASM_TYPE_METHOD);
                break;
            }

            case KEY_GREYS_ADVICE_THROWS_METHOD: {
                getStatic(ASM_TYPE_SPY, "ON_THROWS_METHOD", ASM_TYPE_METHOD);
                break;
            }

            case KEY_GREYS_ADVICE_BEFORE_INVOKING_METHOD: {
                getStatic(ASM_TYPE_SPY, "BEFORE_INVOKING_METHOD", ASM_TYPE_METHOD);
                break;
            }

            case KEY_GREYS_ADVICE_AFTER_INVOKING_METHOD: {
                getStatic(ASM_TYPE_SPY, "AFTER_INVOKING_METHOD", ASM_TYPE_METHOD);
                break;
            }

            case KEY_GREYS_ADVICE_THROW_INVOKING_METHOD: {
                getStatic(ASM_TYPE_SPY, "THROW_INVOKING_METHOD", ASM_TYPE_METHOD);
                break;
            }

            default: {
                throw new IllegalArgumentException("illegal keyOfMethod=" + keyOfMethod);
            }

        }

    }

    /**
     * 加载ClassLoader<br/>
     * 这里分开静态方法中ClassLoader的获取以及普通方法中ClassLoader的获取
     * 主要是性能上的考虑
     */
    private void loadClassLoader() {

        if (this.isStaticMethod()) {

//                    // fast enhance
//                    if (GlobalOptions.isEnableFastEnhance) {
//                        visitLdcInsn(Type.getType(String.format("L%s;", internalClassName)));
//                        visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
//                    }

            // normal enhance
//                    else {

            // 这里不得不用性能极差的Class.forName()来完成类的获取,因为有可能当前这个静态方法在执行的时候
            // 当前类并没有完成实例化,会引起JVM对class文件的合法性校验失败
            // 未来我可能会在这一块考虑性能优化,但对于当前而言,功能远远重要于性能,也就不打算折腾这么复杂了
            visitLdcInsn(javaClassName);
            invokeStatic(ASM_TYPE_CLASS, Method.getMethod("Class forName(String)"));
            invokeVirtual(ASM_TYPE_CLASS, Method.getMethod("ClassLoader getClassLoader()"));
//                    }

        } else {
            loadThis();
            invokeVirtual(ASM_TYPE_OBJECT, Method.getMethod("Class getClass()"));
            invokeVirtual(ASM_TYPE_CLASS, Method.getMethod("ClassLoader getClassLoader()"));
        }

    }

    /**
     * 加载before通知参数数组
     */
    private void loadArrayForBefore() {
        push(7);
        newArray(ASM_TYPE_OBJECT);

        dup();
        push(0);
        push(adviceId);
        box(ASM_TYPE_INT);
        arrayStore(ASM_TYPE_INTEGER);

        dup();
        push(1);
        loadClassLoader();
        arrayStore(ASM_TYPE_CLASS_LOADER);

        dup();
        push(2);
        push(GaStringUtils.tranClassName(javaClassName));
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(3);
        push(name);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(4);
        push(desc);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(5);
        loadThisOrPushNullIfIsStatic();
        arrayStore(ASM_TYPE_OBJECT);

        dup();
        push(6);
        loadArgArray();
        arrayStore(ASM_TYPE_OBJECT_ARRAY);
    }

    @Override
    protected void onMethodEnter() {
        codeLockForTracing.lock(() -> {
            final StringBuilder append = new StringBuilder();
            _debug(append, "debug:onMethodEnter()");

            // 加载before方法
            loadAdviceMethod(KEY_GREYS_ADVICE_BEFORE_METHOD);
            _debug(append, "loadAdviceMethod()");

            // 推入Method.invoke()的第一个参数
            pushNull();

            // 方法参数
            loadArrayForBefore();
            _debug(append, "loadArrayForBefore()");

            // 调用方法
            invokeVirtual(ASM_TYPE_METHOD, ASM_METHOD_METHOD_INVOKE);
            pop();
            _debug(append, "invokeVirtual()");
        });

        mark(beginLabel);

    }

    /**
     * 加载return通知参数数组
     */
    private void loadReturnArgs() {
        dup2X1();
        pop2();
        push(2);
        newArray(ASM_TYPE_OBJECT);
        dup();
        dup2X1();
        pop2();
        push(0);
        swap();
        arrayStore(ASM_TYPE_OBJECT);

        dup();
        push(1);
        push(adviceId);
        box(ASM_TYPE_INT);
        arrayStore(ASM_TYPE_INTEGER);
    }

    @Override
    protected void onMethodExit(final int opcode) {

        if (!isThrow(opcode)) {
            codeLockForTracing.lock(new Block() {
                @Override
                public void code() {

                    final StringBuilder append = new StringBuilder();
                    _debug(append, "debug:onMethodExit()");

                    // 加载返回对象
                    loadReturn(opcode);
                    _debug(append, "loadReturn()");

                    // 加载returning方法
                    loadAdviceMethod(KEY_GREYS_ADVICE_RETURN_METHOD);
                    _debug(append, "loadAdviceMethod()");

                    // 推入Method.invoke()的第一个参数
                    pushNull();

                    // 加载return通知参数数组
                    loadReturnArgs();
                    _debug(append, "loadReturnArgs()");

                    invokeVirtual(ASM_TYPE_METHOD, ASM_METHOD_METHOD_INVOKE);
                    pop();
                    _debug(append, "invokeVirtual()");

                }
            });
        }

    }

    /**
     * 创建throwing通知参数本地变量
     */
    private void loadThrowArgs() {
        dup2X1();
        pop2();
        push(2);
        newArray(ASM_TYPE_OBJECT);
        dup();
        dup2X1();
        pop2();
        push(0);
        swap();
        arrayStore(ASM_TYPE_THROWABLE);

        dup();
        push(1);
        push(adviceId);
        box(ASM_TYPE_INT);
        arrayStore(ASM_TYPE_INTEGER);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {

        mark(endLabel);
        visitTryCatchBlock(beginLabel, endLabel, mark(), ASM_TYPE_THROWABLE.getInternalName());
        // catchException(beginLabel, endLabel, ASM_TYPE_THROWABLE);

        codeLockForTracing.lock(new Block() {
            @Override
            public void code() {

                final StringBuilder append = new StringBuilder();
                _debug(append, "debug:catchException()");

                // 加载异常
                loadThrow();
                _debug(append, "loadAdviceMethod()");

                // 加载throwing方法
                loadAdviceMethod(KEY_GREYS_ADVICE_THROWS_METHOD);
                _debug(append, "loadAdviceMethod()");

                // 推入Method.invoke()的第一个参数
                pushNull();

                // 加载throw通知参数数组
                loadThrowArgs();
                _debug(append, "loadThrowArgs()");

                // 调用方法
                invokeVirtual(ASM_TYPE_METHOD, ASM_METHOD_METHOD_INVOKE);
                pop();
                _debug(append, "invokeVirtual()");

            }
        });

        throwException();

        super.visitMaxs(maxStack, maxLocals);
    }

    /**
     * 是否静态方法
     *
     * @return true:静态方法 / false:非静态方法
     */
    private boolean isStaticMethod() {
        return (methodAccess & ACC_STATIC) != 0;
    }

    /**
     * 是否抛出异常返回(通过字节码判断)
     *
     * @param opcode 操作码
     * @return true:以抛异常形式返回 / false:非抛异常形式返回(return)
     */
    private boolean isThrow(int opcode) {
        return opcode == ATHROW;
    }

    /**
     * 将NULL推入堆栈
     */
    private void pushNull() {
        push((Type) null);
    }

    /**
     * 加载this/null
     */
    private void loadThisOrPushNullIfIsStatic() {
        if (isStaticMethod()) {
            pushNull();
        } else {
            loadThis();
        }
    }

    /**
     * 加载返回值
     *
     * @param opcode 操作吗
     */
    private void loadReturn(int opcode) {
        switch (opcode) {

            case RETURN: {
                pushNull();
                break;
            }

            case ARETURN: {
                dup();
                break;
            }

            case LRETURN:
            case DRETURN: {
                dup2();
                box(Type.getReturnType(methodDesc));
                break;
            }

            default: {
                dup();
                box(Type.getReturnType(methodDesc));
                break;
            }

        }
    }

    /**
     * 加载异常
     */
    private void loadThrow() {
        dup();
    }

    /**
     * 加载方法调用跟踪通知所需参数数组(for notifyBefore/after)
     */
    private void loadArrayForInvokeBeforeOrAfterTracing(String owner, String name, String desc) {
        push(5);
        newArray(ASM_TYPE_OBJECT);

        dup();
        push(0);
        push(adviceId);
        box(ASM_TYPE_INT);
        arrayStore(ASM_TYPE_INTEGER);

        if (null != currentLineNumber) {
            dup();
            push(1);
            push(currentLineNumber);
            box(ASM_TYPE_INT);
            arrayStore(ASM_TYPE_INTEGER);
        }

        dup();
        push(2);
        push(owner);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(3);
        push(name);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(4);
        push(desc);
        arrayStore(ASM_TYPE_STRING);
    }

    /**
     * 加载方法调用跟踪通知所需参数数组(for throw)
     */
    private void loadArrayForInvokeThrowTracing(String owner, String name, String desc) {
        push(6);
        newArray(ASM_TYPE_OBJECT);

        dup();
        push(0);
        push(adviceId);
        box(ASM_TYPE_INT);
        arrayStore(ASM_TYPE_INTEGER);

        if (null != currentLineNumber) {
            dup();
            push(1);
            push(currentLineNumber);
            box(ASM_TYPE_INT);
            arrayStore(ASM_TYPE_INTEGER);
        }

        dup();
        push(2);
        push(owner);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(3);
        push(name);
        arrayStore(ASM_TYPE_STRING);

        dup();
        push(4);
        push(desc);
        arrayStore(ASM_TYPE_STRING);

        dup2(); // e,a,e,a
        swap(); // e,a,a,e
        invokeVirtual(ASM_TYPE_OBJECT, Method.getMethod("Class getClass()"));
        invokeVirtual(ASM_TYPE_CLASS, Method.getMethod("String getName()"));

        // e,a,a,s
        push(5); // e,a,a,s,4
        swap();  // e,a,a,4,s
        arrayStore(ASM_TYPE_STRING);

        // e,a
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        codeLockForTracing.code(opcode);
    }

    /**
     * 跟踪代码
     */
    private void tracing(final int tracingType, final String owner, final String name, final String desc) {

        final String label;
        switch (tracingType) {
            case KEY_GREYS_ADVICE_BEFORE_INVOKING_METHOD: {
                label = "beforeInvoking";
                break;
            }
            case KEY_GREYS_ADVICE_AFTER_INVOKING_METHOD: {
                label = "afterInvoking";
                break;
            }
            case KEY_GREYS_ADVICE_THROW_INVOKING_METHOD: {
                label = "throwInvoking";
                break;
            }
            default: {
                throw new IllegalStateException("illegal tracing type: " + tracingType);
            }
        }

        codeLockForTracing.lock(() -> {

            final StringBuilder append = new StringBuilder();
            _debug(append, "debug:" + label + "()");

            if (tracingType == KEY_GREYS_ADVICE_THROW_INVOKING_METHOD) {
                loadArrayForInvokeThrowTracing(owner, name, desc);
            } else {
                loadArrayForInvokeBeforeOrAfterTracing(owner, name, desc);
            }
            _debug(append, "loadArrayForInvokeTracing()");

            loadAdviceMethod(tracingType);
            swap();
            _debug(append, "loadAdviceMethod()");

            pushNull();
            swap();

            invokeVirtual(ASM_TYPE_METHOD, ASM_METHOD_METHOD_INVOKE);
            pop();
            _debug(append, "invokeVirtual()");

        });

    }

    private Integer currentLineNumber;

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        currentLineNumber = line;
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {

        if (!isTracing || codeLockForTracing.isLock()) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }

        // 方法调用前通知
        tracing(KEY_GREYS_ADVICE_BEFORE_INVOKING_METHOD, owner, name, desc);

        final Label beginLabel = new Label();
        final Label endLabel = new Label();
        final Label finallyLabel = new Label();

        // try
        // {

        mark(beginLabel);
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        mark(endLabel);

        // 方法调用后通知
        tracing(KEY_GREYS_ADVICE_AFTER_INVOKING_METHOD, owner, name, desc);
        goTo(finallyLabel);

        // }
        // catch
        // {

        catchException(beginLabel, endLabel, ASM_TYPE_THROWABLE);
        tracing(KEY_GREYS_ADVICE_THROW_INVOKING_METHOD, owner, name, desc);

        throwException();

        // }
        // finally
        // {
        mark(finallyLabel);
        // }
    }

    /**
     * 用于try-catch的重排序,目的是让tracing的try...catch能在exceptions tables排在前边
     */
    private final Collection<AsmTryCatchBlock> asmTryCatchBlocks = new ArrayList<>();

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        asmTryCatchBlocks.add(new AsmTryCatchBlock(start, end, handler, type));
    }

    @Override
    public void visitEnd() {
        for (AsmTryCatchBlock tcb : asmTryCatchBlocks) {
            super.visitTryCatchBlock(tcb.getStart(), tcb.getEnd(), tcb.getHandler(), tcb.getType());
        }
        super.visitEnd();
    }
}
