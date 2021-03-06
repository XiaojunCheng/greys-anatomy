package com.github.ompc.greys.core;

import java.util.Collection;

/**
 * 类据源
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/12/13
 */
public interface ClassDataSource {

    /**
     * 获取所有可被感知的Class
     *
     * @return Class集合
     */
    Collection<Class<?>> allLoadedClasses();

}
