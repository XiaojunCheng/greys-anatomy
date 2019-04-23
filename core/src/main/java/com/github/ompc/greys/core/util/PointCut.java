package com.github.ompc.greys.core.util;

import com.github.ompc.greys.core.util.matcher.Matcher;
import lombok.Getter;

/**
 * 切入点
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/10/24
 */
@Getter
public class PointCut {

    /**
     * 类匹配
     */
    private final Matcher<Class<?>> classMatcher;
    /**
     * 方法匹配
     */
    private final Matcher<GaMethod> gaMethodMatcher;
    /**
     * 匹配是否包含子类
     */
    private final boolean isIncludeSubClass;

    /**
     * 构造切入点
     *
     * @param classMatcher    类匹配
     * @param gaMethodMatcher 方法匹配
     */
    public PointCut(Matcher<Class<?>> classMatcher, Matcher<GaMethod> gaMethodMatcher) {
        this(classMatcher, gaMethodMatcher, true);
    }

    /**
     * 构造切入点
     *
     * @param classMatcher      类匹配
     * @param gaMethodMatcher   方法匹配
     * @param isIncludeSubClass 类匹配是否包含子类
     */
    public PointCut(Matcher<Class<?>> classMatcher, Matcher<GaMethod> gaMethodMatcher, boolean isIncludeSubClass) {
        this.classMatcher = classMatcher;
        this.gaMethodMatcher = gaMethodMatcher;
        this.isIncludeSubClass = isIncludeSubClass;
    }

}
