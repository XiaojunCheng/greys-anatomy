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
     * 加载ClassLoader<br/>
     * 这里分开静态方法中ClassLoader的获取以及普通方法中ClassLoader的获取
     * 主要是性能上的考虑
     */
    private void loadClassLoader() {
        if (AsmSpyHelper.isStaticMethod(methodAccess)) {

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
            invokeStatic(AsmSpyHelper.ASM_TYPE_CLASS, Method.getMethod("Class forName(String)"));
            invokeVirtual(AsmSpyHelper.ASM_TYPE_CLASS, Method.getMethod("ClassLoader getClassLoader()"));
//                    }

        } else {
            loadThis();
            invokeVirtual(AsmSpyHelper.ASM_TYPE_OBJECT, Method.getMethod("Class getClass()"));
            invokeVirtual(AsmSpyHelper.ASM_TYPE_CLASS, Method.getMethod("ClassLoader getClassLoader()"));
        }
    }

    /**
     * 加载before通知参数数组
     */
    private void loadArrayForBefore() {
        push(7);
        newArray(AsmSpyHelper.ASM_TYPE_OBJECT);

        dup();
        push(0);
        push(adviceId);
        box(AsmSpyHelper.ASM_TYPE_INT);
        arrayStore(AsmSpyHelper.ASM_TYPE_INTEGER);

        dup();
        push(1);
        loadClassLoader();
        arrayStore(AsmSpyHelper.ASM_TYPE_CLASS_LOADER);

        dup();
        push(2);
        push(GaStringUtils.tranClassName(javaClassName));
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);

        dup();
        push(3);
        push(name);
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);

        dup();
        push(4);
        push(desc);
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);

        dup();
        push(5);
        loadThisOrPushNullIfIsStatic();
        arrayStore(AsmSpyHelper.ASM_TYPE_OBJECT);

        dup();
        push(6);
        loadArgArray();
        arrayStore(AsmSpyHelper.ASM_TYPE_OBJECT_ARRAY);
    }

    @Override
    protected void onMethodEnter() {
        codeLockForTracing.lock(() -> {
            final StringBuilder append = new StringBuilder();
            _debug(append, "debug:onMethodEnter()");

            // 加载before方法
            loadAdviceMethod(AsmSpyHelper.KEY_GREYS_ADVICE_BEFORE_METHOD);
            _debug(append, "loadAdviceMethod()");

            // 推入Method.invoke()的第一个参数
            pushNull();

            // 方法参数
            loadArrayForBefore();
            _debug(append, "loadArrayForBefore()");

            // 调用方法
            invokeVirtual(AsmSpyHelper.ASM_TYPE_METHOD, AsmSpyHelper.ASM_METHOD_METHOD_INVOKE);
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
        newArray(AsmSpyHelper.ASM_TYPE_OBJECT);
        dup();
        dup2X1();
        pop2();
        push(0);
        swap();
        arrayStore(AsmSpyHelper.ASM_TYPE_OBJECT);

        dup();
        push(1);
        push(adviceId);
        box(AsmSpyHelper.ASM_TYPE_INT);
        arrayStore(AsmSpyHelper.ASM_TYPE_INTEGER);
    }

    @Override
    protected void onMethodExit(final int opcode) {

        if (!AsmSpyHelper.isThrow(opcode)) {
            codeLockForTracing.lock(new Block() {
                @Override
                public void code() {

                    final StringBuilder append = new StringBuilder();
                    _debug(append, "debug:onMethodExit()");

                    // 加载返回对象
                    loadReturn(opcode);
                    _debug(append, "loadReturn()");

                    // 加载returning方法
                    loadAdviceMethod(AsmSpyHelper.KEY_GREYS_ADVICE_RETURN_METHOD);
                    _debug(append, "loadAdviceMethod()");

                    // 推入Method.invoke()的第一个参数
                    pushNull();

                    // 加载return通知参数数组
                    loadReturnArgs();
                    _debug(append, "loadReturnArgs()");

                    invokeVirtual(AsmSpyHelper.ASM_TYPE_METHOD, AsmSpyHelper.ASM_METHOD_METHOD_INVOKE);
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
        newArray(AsmSpyHelper.ASM_TYPE_OBJECT);
        dup();
        dup2X1();
        pop2();
        push(0);
        swap();
        arrayStore(AsmSpyHelper.ASM_TYPE_THROWABLE);

        dup();
        push(1);
        push(adviceId);
        box(AsmSpyHelper.ASM_TYPE_INT);
        arrayStore(AsmSpyHelper.ASM_TYPE_INTEGER);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {

        mark(endLabel);
        visitTryCatchBlock(beginLabel, endLabel, mark(), AsmSpyHelper.ASM_TYPE_THROWABLE.getInternalName());
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
                loadAdviceMethod(AsmSpyHelper.KEY_GREYS_ADVICE_THROWS_METHOD);
                _debug(append, "loadAdviceMethod()");

                // 推入Method.invoke()的第一个参数
                pushNull();

                // 加载throw通知参数数组
                loadThrowArgs();
                _debug(append, "loadThrowArgs()");

                // 调用方法
                invokeVirtual(AsmSpyHelper.ASM_TYPE_METHOD, AsmSpyHelper.ASM_METHOD_METHOD_INVOKE);
                pop();
                _debug(append, "invokeVirtual()");

            }
        });

        throwException();

        super.visitMaxs(maxStack, maxLocals);
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
        if (AsmSpyHelper.isStaticMethod(methodAccess)) {
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
        newArray(AsmSpyHelper.ASM_TYPE_OBJECT);

        dup();
        push(0);
        push(adviceId);
        box(AsmSpyHelper.ASM_TYPE_INT);
        arrayStore(AsmSpyHelper.ASM_TYPE_INTEGER);

        if (null != currentLineNumber) {
            dup();
            push(1);
            push(currentLineNumber);
            box(AsmSpyHelper.ASM_TYPE_INT);
            arrayStore(AsmSpyHelper.ASM_TYPE_INTEGER);
        }

        dup();
        push(2);
        push(owner);
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);

        dup();
        push(3);
        push(name);
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);

        dup();
        push(4);
        push(desc);
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);
    }

    /**
     * 加载方法调用跟踪通知所需参数数组(for throw)
     */
    private void loadArrayForInvokeThrowTracing(String owner, String name, String desc) {
        push(6);
        newArray(AsmSpyHelper.ASM_TYPE_OBJECT);

        dup();
        push(0);
        push(adviceId);
        box(AsmSpyHelper.ASM_TYPE_INT);
        arrayStore(AsmSpyHelper.ASM_TYPE_INTEGER);

        if (null != currentLineNumber) {
            dup();
            push(1);
            push(currentLineNumber);
            box(AsmSpyHelper.ASM_TYPE_INT);
            arrayStore(AsmSpyHelper.ASM_TYPE_INTEGER);
        }

        dup();
        push(2);
        push(owner);
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);

        dup();
        push(3);
        push(name);
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);

        dup();
        push(4);
        push(desc);
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);

        dup2(); // e,a,e,a
        swap(); // e,a,a,e
        invokeVirtual(AsmSpyHelper.ASM_TYPE_OBJECT, Method.getMethod("Class getClass()"));
        invokeVirtual(AsmSpyHelper.ASM_TYPE_CLASS, Method.getMethod("String getName()"));

        // e,a,a,s
        push(5); // e,a,a,s,4
        swap();  // e,a,a,4,s
        arrayStore(AsmSpyHelper.ASM_TYPE_STRING);

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

        final String label = AsmSpyHelper.getSpyTracingMethodField(tracingType);
        codeLockForTracing.lock(() -> {

            final StringBuilder append = new StringBuilder();
            _debug(append, "debug:" + label + "()");

            if (tracingType == AsmSpyHelper.KEY_GREYS_ADVICE_THROW_INVOKING_METHOD) {
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

            invokeVirtual(AsmSpyHelper.ASM_TYPE_METHOD, AsmSpyHelper.ASM_METHOD_METHOD_INVOKE);
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
        tracing(AsmSpyHelper.KEY_GREYS_ADVICE_BEFORE_INVOKING_METHOD, owner, name, desc);

        final Label beginLabel = new Label();
        final Label endLabel = new Label();
        final Label finallyLabel = new Label();

        // try
        // {

        mark(beginLabel);
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        mark(endLabel);

        // 方法调用后通知
        tracing(AsmSpyHelper.KEY_GREYS_ADVICE_AFTER_INVOKING_METHOD, owner, name, desc);
        goTo(finallyLabel);

        // }
        // catch
        // {

        catchException(beginLabel, endLabel, AsmSpyHelper.ASM_TYPE_THROWABLE);
        tracing(AsmSpyHelper.KEY_GREYS_ADVICE_THROW_INVOKING_METHOD, owner, name, desc);

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

    //=================================== 工具类

    /**
     * 加载通知方法
     *
     * @param keyOfMethod 通知方法KEY
     */
    public void loadAdviceMethod(int keyOfMethod) {
        String spyMethodFieldName = AsmSpyHelper.getSpyMethodField(keyOfMethod);
        getStatic(AsmSpyHelper.ASM_TYPE_SPY, spyMethodFieldName, AsmSpyHelper.ASM_TYPE_METHOD);
    }

}
