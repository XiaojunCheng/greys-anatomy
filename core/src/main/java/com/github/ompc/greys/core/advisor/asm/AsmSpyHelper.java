package com.github.ompc.greys.core.advisor.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chengxiaojun
 * @date 2019-05-22
 */
public final class AsmSpyHelper implements Opcodes {

    /**
     * -- KEY of advice --
     */
    public static final int KEY_GREYS_ADVICE_BEFORE_METHOD = 0;
    public static final int KEY_GREYS_ADVICE_RETURN_METHOD = 1;
    public static final int KEY_GREYS_ADVICE_THROWS_METHOD = 2;
    public static final int KEY_GREYS_ADVICE_BEFORE_INVOKING_METHOD = 3;
    public static final int KEY_GREYS_ADVICE_AFTER_INVOKING_METHOD = 4;
    public static final int KEY_GREYS_ADVICE_THROW_INVOKING_METHOD = 5;
    /**
     * -- KEY of ASM_TYPE or ASM_METHOD --
     */
    public static final Type ASM_TYPE_SPY = Type.getType("Lcom/github/ompc/greys/agent/Spy;");
    public static final Type ASM_TYPE_OBJECT = Type.getType(Object.class);
    public static final Type ASM_TYPE_OBJECT_ARRAY = Type.getType(Object[].class);
    public static final Type ASM_TYPE_CLASS = Type.getType(Class.class);
    public static final Type ASM_TYPE_INTEGER = Type.getType(Integer.class);
    public static final Type ASM_TYPE_CLASS_LOADER = Type.getType(ClassLoader.class);
    public static final Type ASM_TYPE_STRING = Type.getType(String.class);
    public static final Type ASM_TYPE_THROWABLE = Type.getType(Throwable.class);
    public static final Type ASM_TYPE_INT = Type.getType(int.class);
    public static final Type ASM_TYPE_METHOD = Type.getType(java.lang.reflect.Method.class);
    public static final Method ASM_METHOD_METHOD_INVOKE = Method.getMethod("Object invoke(Object,Object[])");

    /**
     * index -> spy method field name
     */
    private static final Map<Integer, String> apyMethod2FieldNameMap = new HashMap<>(8);

    {
        apyMethod2FieldNameMap.put(KEY_GREYS_ADVICE_BEFORE_METHOD, "ON_BEFORE_METHOD");
        apyMethod2FieldNameMap.put(KEY_GREYS_ADVICE_RETURN_METHOD, "ON_RETURN_METHOD");
        apyMethod2FieldNameMap.put(KEY_GREYS_ADVICE_THROWS_METHOD, "ON_THROWS_METHOD");
        apyMethod2FieldNameMap.put(KEY_GREYS_ADVICE_BEFORE_INVOKING_METHOD, "BEFORE_INVOKING_METHOD");
        apyMethod2FieldNameMap.put(KEY_GREYS_ADVICE_AFTER_INVOKING_METHOD, "BEFORE_INVOKING_METHOD");
        apyMethod2FieldNameMap.put(KEY_GREYS_ADVICE_THROW_INVOKING_METHOD, "AFTER_INVOKING_METHOD");
    }

    /**
     * index -> spy tracing method field name
     */
    private static final Map<Integer, String> apyTracingMethod2FieldNameMap = new HashMap<>(8);

    {
        apyTracingMethod2FieldNameMap.put(KEY_GREYS_ADVICE_BEFORE_INVOKING_METHOD, "beforeInvoking");
        apyTracingMethod2FieldNameMap.put(KEY_GREYS_ADVICE_AFTER_INVOKING_METHOD, "afterInvoking");
        apyTracingMethod2FieldNameMap.put(KEY_GREYS_ADVICE_THROW_INVOKING_METHOD, "throwInvoking");
    }

    public static String getSpyMethodField(int keyOfMethod) {
        if (!apyMethod2FieldNameMap.containsKey(keyOfMethod)) {
            throw new IllegalArgumentException("illegal keyOfMethod=" + keyOfMethod);
        }
        return apyMethod2FieldNameMap.get(keyOfMethod);
    }

    public static String getSpyTracingMethodField(int keyOfTracingMethod) {
        if (!apyTracingMethod2FieldNameMap.containsKey(keyOfTracingMethod)) {
            throw new IllegalArgumentException("illegal keyOfTracingMethod=" + keyOfTracingMethod);
        }
        return apyTracingMethod2FieldNameMap.get(keyOfTracingMethod);
    }

    /**
     * 是否静态方法
     *
     * @return true:静态方法 / false:非静态方法
     */
    public static boolean isStaticMethod(int methodAccess) {
        return (methodAccess & ACC_STATIC) != 0;
    }

    /**
     * 是否抛出异常返回(通过字节码判断)
     *
     * @param opcode 操作码
     * @return true:以抛异常形式返回 / false:非抛异常形式返回(return)
     */
    public static boolean isThrow(int opcode) {
        return opcode == ATHROW;
    }

}
