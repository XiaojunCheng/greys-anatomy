package com.github.ompc.greys.core.util.matcher;

import com.github.ompc.greys.core.util.GaCheckUtils;

/**
 * 相等比对
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/12/12
 */
public class EqualsMatcher<T> implements Matcher<T> {

    private final T source;

    public EqualsMatcher(T source) {
        this.source = source;
    }

    @Override
    public boolean matching(T target) {
        return GaCheckUtils.isEquals(source, target);
    }

}
