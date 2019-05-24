package com.github.ompc.greys.core.advisor.method;

import com.github.ompc.greys.core.util.GaReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 构造函数实现
 *
 * @author vlinux
 */
public class GaConstructorImpl implements GaMethod {

    private final Constructor<?> target;

    public GaConstructorImpl(Constructor<?> target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object obj, Object... args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return target.newInstance(args);
    }

    @Override
    public boolean isAccessible() {
        return target.isAccessible();
    }

    @Override
    public void setAccessible(boolean accessFlag) {
        target.setAccessible(accessFlag);
    }

    @Override
    public String getName() {
        return "<init>";
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return target.getParameterTypes();
    }

    @Override
    public Annotation[] getAnnotations() {
        return target.getAnnotations();
    }

    @Override
    public int getModifiers() {
        return GaReflectUtils.computeModifier(target);
    }

    @Override
    public Class<?> getDeclaringClass() {
        return target.getDeclaringClass();
    }

    @Override
    public Class<?> getReturnType() {
        return target.getDeclaringClass();
    }

    @Override
    public Class<?>[] getExceptionTypes() {
        return target.getExceptionTypes();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return target.getDeclaredAnnotations();
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return target.equals(obj);
    }

    @Override
    public String getDesc() {
        return org.objectweb.asm.Type.getType(target).toString();
    }

}
