package com.github.ompc.greys.core.advisor.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.github.ompc.greys.core.util.GaReflectUtils.computeModifier;

/**
 * Greys封装的方法<br/>
 * 主要用来封装构造函数cinit/init/method
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/5/24
 */
public interface GaMethod {

    /**
     * {@link Method#invoke(Object, Object...)}
     *
     * @param obj
     * @param args
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    Object invoke(Object obj, Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException;

    /**
     * {@link Method#isAccessible()}
     *
     * @return
     */
    boolean isAccessible();

    /**
     * {@link Method#setAccessible(boolean)}
     *
     * @param accessFlag
     */
    void setAccessible(boolean accessFlag);

    /**
     * {@link Method#getName()}
     *
     * @return
     */
    String getName();

    /**
     * {@link Method#getParameterTypes()}
     *
     * @return
     */
    Class<?>[] getParameterTypes();

    /**
     * {@link Method#getAnnotations()}
     *
     * @return
     */
    Annotation[] getAnnotations();

    /**
     * {@link Method#getModifiers()}
     *
     * @return
     */
    int getModifiers();

    /**
     * {@link Method#getDeclaringClass()}
     *
     * @return
     */
    Class<?> getDeclaringClass();

    /**
     * {@link Method#getReturnType()}
     *
     * @return
     */
    Class<?> getReturnType();

    /**
     * {@link Method#getExceptionTypes()}
     *
     * @return
     */
    Class<?>[] getExceptionTypes();

    /**
     * {@link Method#getDeclaredAnnotations()}
     *
     * @return
     */
    Annotation[] getDeclaredAnnotations();

    /**
     * 获取方法描述
     *
     * @return 方法描述
     */
    String getDesc();

}
